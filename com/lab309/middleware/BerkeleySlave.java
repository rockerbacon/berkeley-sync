package com.lab309.middleware;

import com.lab309.network.UDPServer;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPDatagram;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteBuffer;

import java.io.IOException;
import java.net.SocketException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BerkeleySlave {

	private boolean syncing;
	private int port;
	private Clock clock;
	private UDPServer requestServer, syncServer;
	private BullyElector elector;
	
	public BerkeleySlave (int syncPort, Clock clock, BullyElector elector) {
		this.syncing = false;
		this.port = syncPort;
		this.clock = clock;
		this.elector = elector;
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
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			try {
				ByteBuffer requestMsg = new ByteBuffer(SizeConstants.sizeOfByte);
				requestMsg.pushByte(BerkeleyLeader.syncClockRequest);
				
				UDPDatagram dtg = new UDPDatagram(SizeConstants.sizeOfInt+SizeConstants.sizeOfLong);
				dtg.getBuffer().putInt(BerkeleySlave.this.syncServer.getPort());
			
				while (BerkeleySlave.this.syncing) {
			
					//send time upon request
					UDPDatagram request = BerkeleySlave.this.requestServer.receiveExpected(requestMsg.getByteArray()); //blocks until request message is received
					int answerPort = request.getBuffer().getInt();
					BerkeleySlave.this.elector.updateActivity();
					
					//System.out.println("Recebeu pedido de sincronizacao em "+BerkeleySlave.this.clock.getTimeMillis());	//debug
					
					c = new UDPClient(answerPort, request.getSender(), null);
					dtg.getBuffer().putLong(BerkeleySlave.this.clock.getTimeMillis());
					c.send(dtg);
				
					dtg.getBuffer().position(dtg.getBuffer().position()-SizeConstants.sizeOfLong);
					c.close();
				
					//wait for answer and adjusts clock
					request = BerkeleySlave.this.syncServer.receive();
					long offset = request.getBuffer().getLong();
					//System.out.println("Received offset "+offset+" through "+BerkeleySlave.this.syncServer.getPort());	//debug
					
					BerkeleySlave.this.clock.adjustTime(offset);
					BerkeleySlave.this.elector.updateActivity();
					
					System.out.println("Ajustou relogio em "+offset+" tempo real: "+ sdf.format(new Date(System.currentTimeMillis())) + " relogio: "+BerkeleySlave.this.clock.getTimeMillis());	//debug
					
				
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

}
