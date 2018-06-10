package com.lab309.network;

import com.lab309.general.ByteBuffer;

import java.net.InetAddress;

public class UDPDatagram {

	/*ATTRIBUTES*/
	private ByteBuffer buffer;
	private InetAddress sender;
	private int sentPort;

	/*CONSTRUCTORS*/
	UDPDatagram (ByteBuffer buffer, InetAddress sender, int sentPort) {
		this.buffer = buffer;
		this.sender = sender;
		this.sentPort = sentPort;
	}
	public UDPDatagram (ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public UDPDatagram (int bufferSize) {
		this.buffer = new ByteBuffer(bufferSize);
	}

	/*GETTERS*/
	public InetAddress getSender() {
		return this.sender;
	}

	public int getSentPort() {
		return this.sentPort;
	}
	
	public ByteBuffer getBuffer() {
		return this.buffer;
	}
	
}
