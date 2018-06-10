package com.lab309.network;

import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Vitor Andrade dos Santos on 7/1/17.
 */

public class TCPClient extends TCPConnection {
	/*CONSTRUCTORS*/
	public TCPClient () {}
	public TCPClient (int port, InetAddress address) throws IOException {
		this.connect(port, address);
	}

	/*METHODS*/
	public void connect (int port, InetAddress address) throws IOException {
		this.setConnection(new Socket(address, port));
	}
}
