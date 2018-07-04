package com.lab309.network;

import java.io.IOException;

import com.lab309.security.Cipher;

import com.lab309.general.ByteArrayConverter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.crypto.IllegalBlockSizeException;

/**
 * Class for sending UDP packets
 *
 * Created by Vitor Andrade dos Santos on 3/27/17.
 */
public class UDPClient implements Serializable {

		/*ATTRIBUTES*/
		private int boundPort;
		private InetAddress boundAddress;
		private transient DatagramSocket sender;
		private Cipher cipher;


		/*CONSTRUCTOR*/
		public UDPClient (int port, InetAddress address, Cipher cipher) throws IOException {
			this.boundPort = port;
			this.boundAddress = address;
			this.sender = new DatagramSocket();
			this.cipher = cipher;
		}

		/*GETTERS*/
		public InetAddress getAddress () {
			return this.boundAddress;
		}
		public int getPort () {
			return this.boundPort;
		}
		public boolean isClosed() { return this.sender.isClosed(); }
		
		/*SETTERS*/
		public void setCipher (Cipher cipher) {
			this.cipher = cipher;
		}

		/*METHODS*/
		public void send (UDPDatagram datagram) throws IOException {
			byte[] message;
			//System.out.println ("Message before encryption:");	//debug
			//System.out.println (ByteArrayConverter.toStringRepresentation(datagram.getBuffer().getByteArray()));	//debug
			if (this.cipher != null) {

				try {
					message = this.cipher.encrypt(datagram.getBuffer().array(), 0, datagram.getBuffer().position());
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
					return;
				}
				//System.out.println ("Message after encryption:");	//debug
				//System.out.println (ByteArrayConverter.toStringRepresentation(message));	//debug

			} else {
				message = datagram.getBuffer().array();
			}

			this.sender.send( new DatagramPacket(message, message.length, this.boundAddress, this.boundPort) );
			//System.out.println();	//debug
		}

		public void close () {
			this.sender.close();
		}

		private void writeObject (ObjectOutputStream output) throws IOException {
			output.defaultWriteObject();
		}

		private void readObject (ObjectInputStream input) throws IOException, ClassNotFoundException {
			input.defaultReadObject();
			this.sender = new DatagramSocket();
		}
}
