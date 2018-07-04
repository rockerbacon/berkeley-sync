package com.lab309.middleware;

import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;
import com.lab309.network.NetInfo;

import com.lab309.general.SizeConstants;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.LinkedList;

import java.io.IOException;

public class BerkeleyLeader {

	/*package private*/ static final byte syncClockRequest = (byte)0x1F;

	private int port;
	private LinkedList<InetAddress> broadcastIp;
	private long answerTimeLimit;
	private Clock clock;
	
	private class Slave {
		public int port;
		public long timeStamp;
		public InetAddress address;
		public long estimatedRtt;
		public long delayToSend;
		
		public Slave (int port, long timeStamp, InetAddress address, long estimatedRtt, long delayToSend) {
			this.port = port;
			this.timeStamp = timeStamp;
			this.address = address;
			this.estimatedRtt = estimatedRtt;
			this.delayToSend = delayToSend;
		}
	}
	
	public BerkeleyLeader (int port, long answerTimeLimit, Clock clock) throws IOException {
		this.broadcastIp = NetInfo.broadcastIp();
		this.answerTimeLimit = answerTimeLimit;
		this.clock = clock;
		this.port = port;
	}
	
	public void sync () {
		ArrayList<Slave> slaves = new ArrayList<Slave>();
		long startTime = System.currentTimeMillis();
		long avgTime;
		long availableAnswerTime;
		long beginTime, endTime;
		UDPServer s = null;
		UDPDatagram dtg;
		UDPClient c = null;
		
		try {
			s = new UDPServer(SizeConstants.sizeOfInt+SizeConstants.sizeOfLong, null, true);
			
			//broadcast time request
			dtg = new UDPDatagram(SizeConstants.sizeOfByte+SizeConstants.sizeOfInt);
			dtg.getBuffer().put(BerkeleyLeader.syncClockRequest);
			dtg.getBuffer().putInt(s.getPort());
			for (InetAddress addr : this.broadcastIp) {
				c = new UDPClient(this.port, addr, null);
				c.send(dtg);
				c.close();
			}
		
			
			//receive time requests within time limit
			availableAnswerTime = this.answerTimeLimit;
			beginTime = System.currentTimeMillis();
			while ( availableAnswerTime > 0 && (dtg = s.receiveOnTime((int)availableAnswerTime)) != null ) {
				long estimatedRtt = (System.currentTimeMillis()-startTime)/2;
				int port = dtg.getBuffer().getInt();
				long timestamp = dtg.getBuffer().getLong();
				slaves.add(new Slave(port, timestamp, dtg.getSender(), estimatedRtt, availableAnswerTime));
				
				//System.out.println("Received timestamp "+timestamp+" estimated rtt "+estimatedRtt);	//debug
				
				//calculate remaining answer time
				endTime = System.currentTimeMillis();
				availableAnswerTime -= endTime-beginTime;
				beginTime = endTime;
			}
			s.close();
		
			if (slaves.size() > 0) {
				//calculate average time
				avgTime = 0;
				for (Slave slave : slaves) {
					avgTime += slave.timeStamp+slave.estimatedRtt+slave.delayToSend;
				}
				avgTime /= slaves.size();
				
				//System.out.println("Avg time "+avgTime);	//debug
		
				//send offsets to slaves
				dtg = new UDPDatagram(SizeConstants.sizeOfLong);
				for (Slave slave : slaves) {
					c = new UDPClient(slave.port, slave.address, null);
					//System.out.println("Offset for "+slave.timeStamp+" through " +slave.port+" = "+(avgTime-slave.timeStamp-slave.estimatedRtt));	//debug
					dtg.getBuffer().putLong(avgTime-slave.timeStamp/*-slave.estimatedRtt*/);
					c.send(dtg);
					c.close();
					dtg.getBuffer().rewind();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (s != null && !s.isClosed()) s.close();
			if (c != null && !c.isClosed()) c.close();
		}
		
	}

}

