package com.lab309.middleware;

import java.util.Random;
import java.util.LinkedList;

import com.lab309.network.UDPServer;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPDatagram;
import com.lab309.network.NetInfo;

import com.lab309.general.SizeConstants;

import java.net.InetAddress;

import java.io.IOException;
import java.net.SocketException;

public class BullyElector {

	private static final byte[] electionMsg = {(byte)0x8A};

	private int port;
	private long maxInactivityInterval;
	private long answerTimeLimit;
	private Clock clock;
	private long lastLeaderActivity;
	private boolean monitoringLeader;
	private int id;
	private UDPServer answerServer;
	private Object resultLock;
	private boolean result;
	
	public BullyElector (int port, long maxInactivityInterval, long answerTimeLimit, Clock clock) {
		this.port = port;
		this.maxInactivityInterval = maxInactivityInterval;
		this.answerTimeLimit = answerTimeLimit;
		this.clock = clock;
		this.lastLeaderActivity = 0;
		this.monitoringLeader = false;
		this.id = new Random().nextInt();
		this.answerServer = null;
		this.resultLock = new Object();
		this.result = false;
	}
	
	public void updateActivity () {
		this.lastLeaderActivity = this.clock.getTimeMillis();
	}
	
	//blocks until there's an election, after which it returns true if this elector became the new leader
	public boolean becomesLeaderUponElection () {
		this.monitoringLeader = true;
		try {
			this.answerServer = new UDPServer(SizeConstants.sizeOfByte+SizeConstants.sizeOfInt, null, false);
			this.answerServer.bind(this.port, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//start routine to check for election requests
		new Thread (new Runnable () { @Override public void run () {
			UDPDatagram dtg, candidature;
			InetAddress thisAddr;
			LinkedList<InetAddress> broadcast;
			long availableTime, beginTime, endTime;
			int id, thisId;
			boolean receivedCandidature;
			try {
				thisAddr = NetInfo.thisMachineIpv4();
				broadcast = NetInfo.broadcastIp();
				BullyElector.this.answerServer = new UDPServer(SizeConstants.sizeOfByte+SizeConstants.sizeOfInt, null, false);
				BullyElector.this.answerServer.bind(BullyElector.this.port, null);
				
				candidature = new UDPDatagram(BullyElector.electionMsg.length+SizeConstants.sizeOfInt);
				candidature.getBuffer().pushByteArray(BullyElector.electionMsg);
				candidature.getBuffer().pushInt(BullyElector.this.id);

				//waits for first election candidature to begin algorithm
				dtg = BullyElector.this.answerServer.receiveExpected(BullyElector.electionMsg);
				if (!dtg.getSender().equals(thisAddr))
					id = dtg.getBuffer().retrieveInt();
				else
					id = 0;
				
				//execute while someone is still sending candidatures
				do {
					receivedCandidature = false;
					availableTime = BullyElector.this.answerTimeLimit;
					beginTime = System.currentTimeMillis();
					//receive all candidates and stop once a better than this one was found or the time has run out
					while (	id < BullyElector.this.id &&
							availableTime > 0 &&
							(dtg = BullyElector.this.answerServer.receiveExpectedOnTime(BullyElector.electionMsg, (int)availableTime, 1)) != null ) {
						
						//ignores its own msg
						if (!dtg.getSender().equals(thisAddr)) {
							receivedCandidature = true;
							id = dtg.getBuffer().retrieveInt();
							endTime = System.currentTimeMillis();
							availableTime -= endTime-beginTime;
							beginTime = endTime;
						}
			
					}
					
					thisId = BullyElector.this.id;	//used to preserve value of id used in last election iteration
					//special case when two processes have the same id
					if (id == BullyElector.this.id && !dtg.getSender().equals(thisAddr)) {
						BullyElector.this.id = new Random().nextInt();
						candidature.getBuffer().rewind(SizeConstants.sizeOfInt);
						candidature.getBuffer().pushInt(BullyElector.this.id);
					}
					
					//broadcast candidature in case no process has shown a better case
					if (id <= thisId) {
						for (InetAddress addr : broadcast) {
							UDPClient c = new UDPClient(BullyElector.this.port, addr, null);
							c.send(candidature);
							c.close();
						}
					} else {
						BullyElector.this.result = false;	//if there's an id bigger than this one, it won't be the leader
						BullyElector.this.resultLock.notifyAll();
						return;
					}
					
				} while (receivedCandidature);
				
				BullyElector.this.result = true;	//if no one challenges the candidature at some point this process becomes the leader
				BullyElector.this.resultLock.notifyAll();
				
			} catch (IOException e) {
				if (!(e instanceof SocketException)) {
					e.printStackTrace();
				}
			} finally {
				if (BullyElector.this.answerServer != null) BullyElector.this.answerServer.close();
			}
		}}).start();
		
		//monitor leader activity
		new Thread (new Runnable () { @Override public void run () {
			try {
				while (BullyElector.this.monitoringLeader) {
					if (BullyElector.this.clock.getTimeMillis()-BullyElector.this.lastLeaderActivity > BullyElector.this.maxInactivityInterval) {
						//broadcast candidature to start election after leader goes inactive
						LinkedList<InetAddress> broadcast = NetInfo.broadcastIp();
						UDPDatagram dtg = new UDPDatagram(BullyElector.electionMsg.length+SizeConstants.sizeOfInt);
						dtg.getBuffer().pushByteArray(BullyElector.electionMsg);
						dtg.getBuffer().pushInt(BullyElector.this.id);
						for (InetAddress addr : broadcast) {
							UDPClient c = new UDPClient(BullyElector.this.port, addr, null);
							c.send(dtg);
							c.close();
						}
					}
					Thread.sleep(BullyElector.this.maxInactivityInterval);
				}
			} catch (InterruptedException e) {
				System.err.println("Leader monitoring interrupted");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}}).start();
		
		try {
			this.resultLock.wait();
		} catch (InterruptedException e) {
			System.err.println("Election aborted");
		}
		this.monitoringLeader = false;
		return this.result;
	}
	
	public void stopAllActivity() {
		this.monitoringLeader = false;
		this.resultLock.notifyAll();
		if (this.answerServer != null) this.answerServer.close();
	}
	
	private void electLeader () {
		long availableAnswerTime, beginTime, endTime;
		try {
			LinkedList<InetAddress> broadcast = NetInfo.broadcastIp();
			UDPServer answerServer = new UDPServer(SizeConstants.sizeOfInt, null, true);
			UDPDatagram dtg = new UDPDatagram(SizeConstants.sizeOfByte+2*SizeConstants.sizeOfInt);
			dtg.getBuffer().pushByteArray(BullyElector.electionMsg);
			dtg.getBuffer().pushInt(this.id);
			dtg.getBuffer().pushInt(answerServer.getPort());
			UDPDatagram answer;
			
			//broadcast election announcement
			for (InetAddress addr : broadcast) {
				UDPClient client = new UDPClient(this.port, addr, null);
				client.send(dtg);
				client.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
