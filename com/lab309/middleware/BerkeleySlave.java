package com.lab309.middleware;

import com.lab309.network.UDPServer;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPDatagram;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteBuffer;

import java.io.IOException;
import java.net.SocketException;

public class BerkeleySlave {

	private long maxInactivityInterval;
	private long lastLeaderActivity;
	private boolean syncing;
	private int port;
	private Clock clock;
	private UDPServer requestServer, syncServer;
	private boolean monitoringLeader;
	
	public BerkeleySlave (int port, long maxInactivityInterval, Clock clock) {
		this.maxInactivityInterval = maxInactivityInterval;
		this.lastLeaderActivity = 0;
		this.lastLeaderActivity = System.currentTimeMillis();
		this.syncing = false;
		this.port = port;
		this.clock = clock;
	}
	
	public void startSyncing () {
		this.syncing = true;
		try {
			this.requestServer = new UDPServer(SizeConstants.sizeOfByte+SizeConstants.sizeOfInt, null, false);
			this.requestServer.bind(port, null);	
			this.syncServer = new UDPServer(SizeConstants.sizeOfLong, null, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread ( new Runnable () {	@Override public void run () {		
			UDPClient c = null;
			try {
				ByteBuffer requestMsg = new ByteBuffer(SizeConstants.sizeOfByte);
				requestMsg.pushByte(BerkeleyLeader.syncClockRequest);
				
				UDPDatagram dtg = new UDPDatagram(SizeConstants.sizeOfInt+SizeConstants.sizeOfLong);
				dtg.getBuffer().pushInt(BerkeleySlave.this.syncServer.getPort());
			
				while (BerkeleySlave.this.syncing) {
			
					//send time upon request
					UDPDatagram request = BerkeleySlave.this.requestServer.receiveExpected(requestMsg.getByteArray()); //blocks until request message is received
					int answerPort = request.getBuffer().retrieveInt();
					BerkeleySlave.this.lastLeaderActivity = BerkeleySlave.this.clock.getTimeMillis();	//update leader activity
					
					System.out.println("Received sync request at "+BerkeleySlave.this.clock.getTimeMillis());	//debug
					
					c = new UDPClient(answerPort, request.getSender(), null);
					dtg.getBuffer().pushLong(BerkeleySlave.this.clock.getTimeMillis());
					c.send(dtg);
				
					dtg.getBuffer().rewind(SizeConstants.sizeOfLong);
					c.close();
				
					//wait for answer and adjusts clock
					request = BerkeleySlave.this.syncServer.receive();
					long offset = request.getBuffer().retrieveLong();
					BerkeleySlave.this.lastLeaderActivity = BerkeleySlave.this.clock.getTimeMillis();	//update leader activity
					
					BerkeleySlave.this.clock.adjustTime(offset);
					
					System.out.println("Adjusted clock by "+offset+" now at "+BerkeleySlave.this.clock.getTimeMillis());	//debug
					
				
				}
			} catch (IOException e) {
				if (c != null) c.close();
				
				//a socket exception is thrown if the stopSyncing function is called, so no need to trace as a bug
				//(not in a different catch since it extends IOException)
				if (!(e instanceof SocketException)) {
					e.printStackTrace();
				}
				
			} finally {
				BerkeleySlave.this.stopSyncing();	//handles closing all the sockets
			}
		}}).start();
	}
	
	public void stopSyncing () {
		this.syncing = false;
		if (this.requestServer != null) this.requestServer.close();
		if (this.syncServer != null) this.syncServer.close();
	}
	
	private void electBully () {
		//TODO implement Bully election
	}
	
	public void startMonitoringLeader() {
		this.monitoringLeader = true;
		new Thread ( new Runnable () { @Override public void run () {
			try {
				while (BerkeleySlave.this.monitoringLeader) {
					if (BerkeleySlave.this.clock.getTimeMillis()-BerkeleySlave.this.lastLeaderActivity > BerkeleySlave.this.maxInactivityInterval) {
						BerkeleySlave.this.electBully();
					}
					Thread.sleep(BerkeleySlave.this.maxInactivityInterval);
				}
			} catch (InterruptedException e) {
				System.err.println("Leader monitoring interrupted");
			}
		}}).start();
	}
	
	public void stopMonitoringLeader() {
		this.monitoringLeader = false;
	}

}
