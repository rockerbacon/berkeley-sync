package com.lab309.middleware;

import com.lab309.network.UDPServer;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPDatagram;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteBuffer;

import java.io.IOException;

public class BerkeleySlave {

	private long maxInactivityInterval;
	private long lastLeaderActivity;
	private boolean syncing;
	int port;
	Clock clock;
	UDPServer s;
	UDPServer s2;
	
	public BerkeleySlave (int port, long maxInactivityInterval, Clock clock) {
		this.maxInactivityInterval = maxInactivityInterval;
		this.lastLeaderActivity = System.currentTimeMillis();
		this.syncing = false;
		this.port = port;
		this.clock = clock;
	}
	
	public void startSyncing () {
		this.syncing = true;
		new Thread ( new Runnable () {	@Override public void run () {		
			UDPClient c = null;
			try {
				BerkeleySlave.this.s = new UDPServer(SizeConstants.sizeOfByte+SizeConstants.sizeOfLong, null, false);
				BerkeleySlave.this.s.bind(port, null);
				
				BerkeleySlave.this.s2 = new UDPServer(SizeConstants.sizeOfLong, null, true);
				
				ByteBuffer requestMsg = new ByteBuffer(SizeConstants.sizeOfByte);
				requestMsg.pushByte(BerkeleyLeader.syncClockRequest);
				UDPDatagram dtg = new UDPDatagram(SizeConstants.sizeOfInt+SizeConstants.sizeOfLong);
				dtg.getBuffer().pushInt(BerkeleySlave.this.s2.getPort());
			
				while (BerkeleySlave.this.syncing) {
			
					//send time upon request
					UDPDatagram request = BerkeleySlave.this.s.receiveExpected(requestMsg.getByteArray()); //blocks until request message is received
					BerkeleySlave.this.lastLeaderActivity = System.currentTimeMillis();	//update leader activity
					
					System.out.println("Received sync request at "+BerkeleySlave.this.clock.getTimeMillis());	//debug
					
					c = new UDPClient(BerkeleySlave.this.port, request.getSender(), null);
					dtg.getBuffer().pushLong(BerkeleySlave.this.clock.getTimeMillis());
					c.send(dtg);
				
					dtg.getBuffer().rewind(SizeConstants.sizeOfLong);
					c.close();
				
					//wait for answer and adjusts clock
					request = BerkeleySlave.this.s.receiveExpected(requestMsg.getByteArray());
					BerkeleySlave.this.lastLeaderActivity = System.currentTimeMillis();	//update leader activity
					long offset = request.getBuffer().retrieveLong();
					BerkeleySlave.this.clock.adjustTime(offset);
					
					System.out.println("Adjusted clock by "+offset+" now at "+BerkeleySlave.this.clock.getTimeMillis());	//debug
				
				}
				s.close();
			} catch (IOException e) {
				boolean printTrace = true;
				if (BerkeleySlave.this.s != null) {
					if (BerkeleySlave.this.s.isClosed()) {
						System.err.println("Socket closed");
						printTrace = false;
					} else {
						BerkeleySlave.this.s.close();
					}
				}
				if (c != null && !c.isClosed()) c.close();
				if (printTrace) e.printStackTrace();
			}
		}}).start();
	}
	
	public void stopSyncing () {
		this.syncing = false;
		this.s.close();
	}
	
	public void monitorLeaderActivity() {
		//TODO implement method
	}

}
