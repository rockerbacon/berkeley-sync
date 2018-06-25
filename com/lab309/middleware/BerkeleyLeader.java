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
		public long timeStamp;
		public InetAddress address;
		public long estimatedRtt;
		
		public Slave (long timeStamp, InetAddress address, long estimatedRtt) {
			this.timeStamp = timeStamp;
			this.address = address;
			this.estimatedRtt = estimatedRtt;
		}
	}
	
	public BerkeleyLeader (int port, long answerTimeLimit, Clock clock) throws IOException {
		this.broadcastIp = NetInfo.broadcastIp();
		this.answerTimeLimit = answerTimeLimit;
		this.clock = clock;
	}
	
	public void sync () {
		ArrayList<Slave> slaves = new ArrayList<Slave>();
		long startTime = clock.getTimeMillis();
		long avgTime;
		long availableAnswerTime;
		long beginTime, endTime;
		UDPServer s = null;
		UDPDatagram dtg;
		UDPClient c = null;
		
		try {
			//broadcast time request
			dtg = new UDPDatagram(SizeConstants.sizeOfByte);
			dtg.getBuffer().pushByte(BerkeleyLeader.syncClockRequest);
			for (InetAddress addr : this.broadcastIp) {
				c = new UDPClient(this.port, addr, null);
				c.send(dtg);
				c.close();
			}
		
			//receive time requests within time limit
			s = new UDPServer(SizeConstants.sizeOfLong, null);
			s.bind(BerkeleyLeader.this.port, null);
			availableAnswerTime = this.answerTimeLimit;
			beginTime = System.currentTimeMillis();
			while ( (dtg = s.receiveOnTime((int)availableAnswerTime)) != null && availableAnswerTime > 0 ) {
				long estimatedRtt = (clock.getTimeMillis()-startTime)/2;
				slaves.add(new Slave(dtg.getBuffer().retrieveLong(), dtg.getSender(), estimatedRtt));
				//calculate remaining answer time
				endTime = System.currentTimeMillis();
				availableAnswerTime -= endTime-beginTime;
				beginTime = endTime;
			}
			s.close();
		
			//calculate average time
			avgTime = 0;
			for (Slave slave : slaves) {
				avgTime += slave.timeStamp;
			}
			avgTime /= slaves.size();
		
			//send offsets to slaves
			dtg = new UDPDatagram(SizeConstants.sizeOfLong);
			for (Slave slave : slaves) {
				c = new UDPClient(this.port, slave.address, null);
				dtg.getBuffer().pushLong(slave.timeStamp+slave.estimatedRtt+this.answerTimeLimit-avgTime);
				c.send(dtg);
				c.close();
				dtg.getBuffer().rewind();
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (s != null && !s.isClosed()) s.close();
			if (c != null && !c.isClosed()) c.close();
		}
		
	}

}

