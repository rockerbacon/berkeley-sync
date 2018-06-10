package com.lab309.security;

import java.io.Serializable;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public interface Cipher extends Serializable {
	public byte[] encrypt (byte[] data) throws IllegalBlockSizeException;
	public byte[] encrypt (byte[] data, int offset, int length) throws IllegalBlockSizeException;
	public byte[] decrypt (byte[] data) throws IllegalBlockSizeException, BadPaddingException;
	public byte[] decrypt (byte[] data, int offset, int length) throws IllegalBlockSizeException, BadPaddingException;
	public byte[] getKey ();
	public void setKey (byte[] key) throws InvalidKeyException;
}
