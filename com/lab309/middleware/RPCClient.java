package com.lab309.middleware;

import com.lab309.general.ByteBuffer;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;

import java.net.InetAddress;
import java.io.Serializable;
import java.io.IOException;

public class RPCClient {

	private class AnswerSlot {
		private Object o_content;
		private boolean b_available;
		private Object lk;
		
		public AnswerSlot() {
			this.o_content = null;
			this.b_available = true;
			this.lk = new Object();
		}
		
		public Object retrieveContent () {
			Object retrieved = null;
			try {
				if (this.o_content == null) {
					synchronized(this.lk) {
						this.lk.wait();
					}
				}
				retrieved = this.o_content;
				this.o_content = null;
				this.b_available = true;
			} catch (InterruptedException e) {
				System.err.println("Content retrieval interrupted");
			}
			
			return retrieved;
		}
		
		public void store (ByteBuffer bb_content) throws IOException, ClassNotFoundException {
			if (this.o_content == null) {
				synchronized(this.lk) {
					this.o_content = bb_content.retrieveSerializable();
					this.lk.notify();
				}
			}
		}
		
		public boolean allocate () {
			if (this.b_available) {
				this.b_available = false;
				return true;
			}
			return false;
		}
		
	}

	private AnswerSlot [] ans_array;
	private UDPClient udpc;
	private int size_args;
	private int size_return;
	
	private class RetrieveReturnPacket implements Runnable {
		private int i_cmdr;
		private UDPServer udps;
		
		public RetrieveReturnPacket (int i_cmdr) {
			try {
				this.i_cmdr = i_cmdr;
				this.udps = new UDPServer(RPCClient.this.size_return, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public int getPort () {
			return this.udps.getPort();
		}
		
		@Override
		public void run () {
			try {
			
				UDPDatagram dtg = this.udps.receive();
				//System.out.println("Client received an answer on index "+this.i_cmdr);	//debug
				RPCClient.this.ans_array[this.i_cmdr].store(dtg.getBuffer());
				this.udps.close();
				
			} catch (IOException|ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	};
	
	public RPCClient (int size_queue, int size_return, int size_args) {
		this.ans_array = new AnswerSlot[size_queue];
		for (int i = 0; i < ans_array.length; i++) {
			this.ans_array[i] = new AnswerSlot();
		}
		this.size_args = size_args;
		this.size_return = size_return;
	}
	
	public void connect (int port, InetAddress address) throws IOException {
		this.udpc = new UDPClient(port, address, null);
		
		//TODO test connection
	}
	
	
	public void close () {
		this.udpc.close();
	}
	
	public Object retrieveReturn (int i_queueIndex) {
		//blocks waiting for the return of the remote procedure
		return ans_array[i_queueIndex].retrieveContent();
	}
	
	private int allocateCmdr () throws QueueOverflowException {
		int i = 0;
		while (i < this.ans_array.length && !this.ans_array[i].allocate()) {
			i++;
		}
		if (i == this.ans_array.length) {
			throw new QueueOverflowException();
		}
		
		return i;
	}
	
	//calls procedure without blocking, returns queue index used for retrieving the result later
	public int asyncCall (String s_procedureName, Serializable... args) throws QueueOverflowException, IOException {
		int i_cmdr = this.allocateCmdr();
		UDPDatagram dtg_call = new UDPDatagram(size_args);
		RetrieveReturnPacket run_udps = new RetrieveReturnPacket(i_cmdr);
		
		dtg_call.getBuffer().pushInt(run_udps.getPort());
		dtg_call.getBuffer().pushLatinString(s_procedureName);
		dtg_call.getBuffer().pushInt(args.length);
		for (Serializable sr : args) {
			dtg_call.getBuffer().pushSerializable(sr);
		}
		
		//System.out.println("Client sent a request");	//debug
		this.udpc.send(dtg_call);
		
		new Thread(run_udps).start();
		
		return i_cmdr;
	}

	//calls procedure and blocks, waiting for the result
	public Object call (String s_procedureName, Serializable... args) throws QueueOverflowException, IOException {
		return this.retrieveReturn(this.asyncCall(s_procedureName, args));
	}

}
