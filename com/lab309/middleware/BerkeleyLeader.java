package com.lab309.middleware;

import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;
import com.lab309.network.NetInfo;

import com.lab309.general.SizeConstants;

import java.net.InetAddress;

import java.util.ArrayList;

import java.io.IOException;

public class BerkeleyLeader {

	int port;
	LinkedList<InetAddress> broadcastIp;
	Clock clock;
	
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
	
	public BerkeleyLeader (int port, Clock clock) {
		this.broadcastIp = NetInfo.broadcastIp();
		this.clock = clock;
	}
	
	public void sync () {
		ArrayList<Slave> slaves = new ArrayList<Slave>();
		long startTime = clock.getTime();
		long avgTime;
		UDPServer s;
		
		//broadcast time request
		try {
			for (InetAddress addr : this.broadcastIp) {
				UDPClient c = new UDPClient(this.port, addr, null);
				c.send(/*TODO define request message*/);
				c.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//receive time requests
		s = new UDPServer(BerkeleyLeader.this.port, SizeConstants.sizeOfLong, null);
		while (/*TODO define stop condition*/) {
			UDPDatagram dtg = s.receive();
			int estimatedRtt = (clock.getTime()-startTime)/2;
			slaves.add(new Slave(dtg.retrieveLong(), dtg.getSender(), estimatedRtt));
		}
		
		//calculate average time
		avgTime = 0;
		for (Slave slave : slaves) {
			avgTime += slave.timeStamp;
		}
		avgTime /= slaves.size();
		
		//send offsets to slaves
		for (Slave slave : slaves) {
			UDPClient c = new UDPClient(this.port, slave.address, null);
			UDPDatagram dtg = new UDPDatagram(SizeConstants.sizeOfLong);
			dtg.pushLong(slave.timeStamp+slave.estimatedRtt-avgTime);
			c.send(dtg);
		}
		
	}

}
