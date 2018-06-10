package com.lab309.general;

import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import java.io.IOException;

public class ByteBuffer {
	private byte[] buffer;
	private int offset;
	
	/*CONSTRUCTORS*/
	public ByteBuffer (byte[] buffer, int offset) {
		this.buffer = buffer;
		this.offset = offset;
	}
	
	public ByteBuffer (byte[] buffer) {
		this.buffer = buffer;
		this.offset = 0;
	}	
	
	public ByteBuffer (int size) {
		this(new byte[size]);
	}
	
	/*GETTERS*/
	public int getCapacity () {
		return this.buffer.length;
	}
	
	public byte[] getByteArray() {
		return this.buffer;
	}

	public int getOffset () {
		return this.offset;
	}
	
	public int available () {
		return this.buffer.length-this.offset;
	}
	
	/*METHODS*/
	/*PUSH DATA*/
	public void pushByte (byte b) {
		this.buffer[this.offset] = b;
		this.offset++;
	}

	public void pushShort (short hd) {
		ByteArrayConverter.shortToArray(hd, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfShort;
	}

	public void pushInt (int d) {
		ByteArrayConverter.intToArray(d, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfInt;
	}

	public void pushLong (long ld) {
		ByteArrayConverter.longToArray(ld, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLong;
	}

	public void pushFloat (float f) {
		ByteArrayConverter.floatToArray(f, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfFloat;
	}

	public void pushDouble (double lf) {
		ByteArrayConverter.doubleToArray(lf, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfDouble;
	}

	public void pushBoolean (boolean b) {
		ByteArrayConverter.booleanToArray(b, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfBoolean;
	}

	public void pushChar (char c) {
		ByteArrayConverter.charToArray(c, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfChar;
	}

	public void pushLatinChar (char c) {
		ByteArrayConverter.latinCharToArray(c, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinChar;
	}

	public void pushString (String string) {
		ByteArrayConverter.stringToArray(string, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfString(string);
	}

	public void pushLatinString (String string) {
		ByteArrayConverter.latinStringToArray(string, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinString(string);
	}

	public void pushByteArray (byte[] b, int beginning, int end) {
		ByteArrayConverter.copyArrayTo(b, beginning, end, this.buffer, this.offset);
		this.offset += (end - beginning > 0) ? end - beginning : 0; 
	}

	public void pushByteArray (byte[] b) {
		this.pushByteArray(b, 0, b.length);
	}
	
	public void pushSerializable (Serializable sr) {
		byte[] bar_sr = ByteArrayConverter.serializableToArray(sr);
		this.pushByteArray(bar_sr, 0, bar_sr.length);
	}

	/*RETRIEVE DATA*/
	public byte retrieveByte () {
		byte b = this.buffer[this.offset];
		this.offset++;
		return b;
	}

	public short retrieveShort () {
		short hd = ByteArrayConverter.shortFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfShort;
		return hd;
	}

	public int retrieveInt () {
		int d = ByteArrayConverter.intFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfInt;
		return d;
	}

	public long retrieveLong () {
		long ld = ByteArrayConverter.longFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLong;
		return ld;
	}

	public float retrieveFloat () {
		float f = ByteArrayConverter.floatFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfFloat;
		return f;
	}

	public double retrieveDouble () {
		double lf = ByteArrayConverter.doubleFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfDouble;
		return lf;
	}

	public boolean retrieveBoolean () {
		boolean b = ByteArrayConverter.booleanFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfBoolean;
		return b;
	}

	public char retrieveChar () {
		char c = ByteArrayConverter.charFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfChar;
		return c;
	}

	public char retrieveLatinChar () {
		char c = ByteArrayConverter.latinCharFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinChar;
		return c;
	}

	public String retrieveString () {
		String string = ByteArrayConverter.stringFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfString(string);
		return string;
	}

	public String retrieveLatinString () {
		String string = ByteArrayConverter.latinStringFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinString(string);
		return string;
	}

	public byte[] retrieveByteArray (int size, byte[] output, int outputOffset) {
		ByteArrayConverter.copyArrayTo(this.buffer, this.offset, this.offset+size, output, outputOffset);
		this.offset += (size > 0) ? size : 0;
		return output;
	}
	
	public Object retrieveSerializable () throws IOException, ClassNotFoundException {
		ByteArrayInputStream bas = new ByteArrayInputStream(this.buffer, this.offset, this.buffer.length-this.offset);
		ObjectInputStream ois = new ObjectInputStream(bas);
		Object read = ois.readObject();
		
		this.offset = this.buffer.length-bas.available();
		
		return read;
	}
	
	/*DELETE DATA*/
	public void rewind (int size) {
		this.offset -= size;
		if (this.offset < 0) {
			this.offset = 0;
		}
	}
	
	public void rewind () {
		this.offset = 0;
	}
	
	public void skip (int size) {
		this.offset += size;
	}
}
