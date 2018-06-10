package com.lab309.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Vitor Andrade dos Santos on 7/1/17.
 */

public class TCPServer extends TCPConnection {
	/*ATTRIBUTES*/
	private ServerSocket connectionListener;
	private Socket connection;

	/*CONSTRUCTORS*/
	public TCPServer (int port) throws IOException {
		this.connectionListener = new ServerSocket(port);
	}

	/*METHODS*/
	public void connect() throws IOException {
		this.connection = this.connectionListener.accept();
		this.setConnection(this.connection);
	}
	
	public void close() {
		try {
			this.connectionListener.close();
			super.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
