package com.lab309.middleware;

import com.lab309.network.UDPServer;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPDatagram;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteBuffer;

import java.io.IOException;

public class BerkeleySlave {

	private long maxInactivityInterval;
	private boolean syncing;
	int port;
	Clock clock;
	UDPServer s;
	
	public BerkeleySlave (int port, long maxInactivityInterval, Clock clock) {
		this.maxInactivityInterval = maxInactivityInterval;
		this.syncing = false;
		this.port = port;
		this.clock = clock;
	}
	
	public void startSyncing () {
		this.syncing = true;
		new Thread ( new Runnable () {	@Override public void run () {		
			UDPClient c = null;
			try {
				BerkeleySlave.this.s = new UDPServer(port, SizeConstants.sizeOfLong, null);
				ByteBuffer requestMsg = new ByteBuffer(SizeConstants.sizeOfByte);
				requestMsg.pushByte(BerkeleyLeader.syncClockRequest);
				UDPDatagram dtg = new UDPDatagram(SizeConstants.sizeOfLong);
			
				while (BerkeleySlave.this.syncing) {
			
					//send time upon request
					UDPDatagram request = BerkeleySlave.this.s.receiveExpected(requestMsg.getByteArray()); //blocks until request message is received
					c = new UDPClient(BerkeleySlave.this.port, request.getSender(), null);
				
					dtg.getBuffer().pushLong(BerkeleySlave.this.clock.getTimeMillis());
					c.send(dtg);
				
					dtg.getBuffer().rewind();
					c.close();
				
					//wait for answer and adjusts clock
					request = BerkeleySlave.this.s.receive();
					BerkeleySlave.this.clock.adjustTime(request.getBuffer().retrieveLong());
				
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
			}
		}}).start();
	}
	
	public void stopSyncing () {
		this.syncing = false;
		this.s.close();
	}

}
