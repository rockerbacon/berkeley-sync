package com.lab309.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Hasher implements Hasher {
	/*ATRIBUTES*/
	private MessageDigest hasher;
	
	/*CONSTRUCTORS*/
	public SHA256Hasher () {
		try {
			this.hasher = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/*METHODS*/
	@Override
	public byte[] hash (byte[] data) {
		this.hasher.update(data);
		return this.hasher.digest();
	}	
}
