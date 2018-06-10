package com.lab309.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.lab309.general.ByteArrayConverter;

import javax.crypto.Cipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class RC4Cipher implements com.lab309.security.Cipher {
	/*ATRIBUTES*/
	private transient Cipher encryptor;
	private transient Cipher decryptor;
	private byte[] key;
	
	/*CONSTRUCTORS*/
	public RC4Cipher (byte[] key) {
		try {
			this.encryptor = Cipher.getInstance("RC4");
			this.decryptor = Cipher.getInstance("RC4");
			this.setKey(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public RC4Cipher (int publicKeySize) {
		try {
			SecureRandom s;

			this.encryptor = Cipher.getInstance("RC4");
			this.decryptor = Cipher.getInstance("RC4");
			this.key = new byte[publicKeySize];

			s = new SecureRandom();
			s.nextBytes(this.key);
			this.setKey(this.key);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*GETTERS*/
	@Override
	public byte[] getKey () {
		return ByteArrayConverter.copyArrayTo(this.key, 0, this.key.length, new byte[this.key.length], 0);
	}
	
	/*SETTERS*/
	@Override
	public void setKey (byte[] key) throws InvalidKeyException {
		if (this.key != key) {
			this.key = ByteArrayConverter.copyArrayTo(key, 0, key.length, new byte[key.length], 0);
		}
		this.encryptor.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(this.key, "RC4"));
		this.decryptor.init(Cipher.DECRYPT_MODE, new SecretKeySpec(this.key, "RC4"));
	}
	
	/*METHODS*/
	@Override
	public byte[] encrypt(byte[] data) throws IllegalBlockSizeException {
		try {
			if (data == null) return null;
			return this.encryptor.doFinal(data);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] encrypt(byte[] data, int offset, int length) throws IllegalBlockSizeException {
		try {
			if (data == null) return null;
			return this.encryptor.doFinal(data, offset, length);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
		if (data == null) return null;
		return this.decryptor.doFinal(data);
	}

	@Override
	public byte[] decrypt(byte[] data, int offset, int length) throws IllegalBlockSizeException, BadPaddingException {
		if (data == null) return null;
		return this.decryptor.doFinal(data, offset, length);
	}

	private void writeObject (ObjectOutputStream output) throws IOException {
		output.defaultWriteObject();
	}

	private void readObject (ObjectInputStream input) throws IOException, ClassNotFoundException {
		input.defaultReadObject();
		try {
			this.encryptor = Cipher.getInstance("RC4");
			this.decryptor = Cipher.getInstance("RC4");
			this.setKey(this.key);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
	}
}
