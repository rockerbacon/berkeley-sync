package com.lab309.middleware;

import java.util.LinkedList;
import java.util.Iterator;

import com.lab309.adt.ProductionQueue;
import com.lab309.general.ByteBuffer;
import com.lab309.network.UDPDatagram;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;

import java.io.Serializable;
import java.io.IOException;

import javax.crypto.IllegalBlockSizeException;

public abstract class RPCServer {

	private ProductionQueue<UDPDatagram> que_cmd;
	private boolean broutineExecute;
	private UDPServer udps;
	private int size_args;
	private int size_return;

	public RPCServer (int size_queue, int size_return, int size_args) {
		this.que_cmd = new ProductionQueue<UDPDatagram>(size_queue);
		this.broutineExecute = false;
		this.udps = null;
		this.size_args = size_args;
		this.size_return = size_return;
	}
	
	protected int getSizeArgs () {
		return this.size_args;
	}
	
	protected int getSizeReturn () {
		return this.size_return;
	}
	
	protected abstract Serializable processCmd (String s_procedure, Object args[]);
		
	public void startRoutineExecute () {
		broutineExecute = true;
		new Thread ( new Runnable () { @Override public void run () {
		
			try {
		
				while (RPCServer.this.broutineExecute) {
				
					UDPDatagram dtg_received = RPCServer.this.que_cmd.pop();	//blocks until there's something to pop
					ByteBuffer bb_received = dtg_received.getBuffer();
					int i_port = bb_received.retrieveInt();
					String s_proc = bb_received.retrieveLatinString();
					int c_args = bb_received.retrieveInt();
					Serializable result;
					UDPDatagram dtg_send;
					UDPClient uc;
					Object[] arr_args;
		
					//retrieve data					
					arr_args = new Object[c_args];
					for (int i = 0; i < c_args; i++) {
						arr_args[i] = bb_received.retrieveSerializable();
					}
					
					//execute procedure
					result = RPCServer.this.processCmd(s_proc, arr_args);
	
					if (result == null) {
						//TODO inform of function not declared error
					}
	
					//pack result
					dtg_send = new UDPDatagram(RPCServer.this.size_return);
					dtg_send.getBuffer().pushSerializable(result);
					
					//post result
					uc = new UDPClient(i_port, dtg_received.getSender(), null);
					uc.send(dtg_send);
					uc.close();
				
				}
			
			} catch (IOException|ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Executing commands queue stopped");
			}
		
		}}).start();
	}
	
	public void stopRoutineExecute () {
		broutineExecute = false;
		this.que_cmd.interrupt();
	}
	
	public void startRoutineReceiveCmd (int i_port) {
		try {
			this.udps = new UDPServer (i_port, this.size_args, null); 
			new Thread ( new Runnable () { @Override public void run () {
				UDPDatagram dtg;
				try {
					while (RPCServer.this.udps != null) {
						dtg = RPCServer.this.udps.receive();
						//System.out.println("Server received a request");	//debug
						RPCServer.this.que_cmd.push(dtg);
						//System.out.println("Server published a request");	//debug
					}
				} catch (IOException e) {
					System.err.println("Socket closed");
				} catch (InterruptedException e) {
					System.err.println("Receiving commands queue stoped");
				}
			}}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stopRoutineReceiveCmd () {
		this.udps.close();
		this.udps = null;
	}
	
}
