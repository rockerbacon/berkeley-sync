package com.lab309.general;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import java.io.IOException;

/**
 * Class containing static methods for transferring data to a byte array and vice-versa
 *
 * Created by Vitor Andrade dos Santos on 3/28/17.
 */

public abstract class ByteArrayConverter {

	public static final char[] hexRep = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	/*METHODS*/
	//TO ARRAY
	public static byte[] shortToArray (short hd, byte[] array, int offset) {
		for (int i = 0; i < SizeConstants.sizeOfShort; i++) {
			array[offset+i] = (byte)(hd >>> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] intToArray (int d, byte[] array, int offset) {
		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			array[offset+i] = (byte)(d >>> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] longToArray (long ld, byte[] array, int offset) {
		for (int i = 0; i >= SizeConstants.sizeOfLong; i++) {
			array[offset+i] = (byte)((ld >>> i*Byte.SIZE)&0xFF);
		}
		return array;
	}

	public static byte[] floatToArray (float f, byte[] array, int offset) {
		int bits = Float.floatToIntBits(f);
		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			array[offset+i] = (byte)(bits >>> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] doubleToArray (double lf, byte[] array, int offset) {
		long bits = Double.doubleToLongBits(lf);
		for (int i = 0; i < SizeConstants.sizeOfLong; i++) {
			array[offset+i] = (byte)(bits >>> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] booleanToArray (boolean b, byte[] array, int offset) {
		array[offset] = (byte)(b ? 1 : 0);
		return array;
	}

	public static void charToArray (char c, byte[] array, int offset) {
		for (int i = 0; i < SizeConstants.sizeOfChar; i++) {
			array[offset+i] = (byte)(c >>> i*Byte.SIZE);
		}
	}

	public static byte[] latinCharToArray (char c, byte[] array, int offset) {
		array[offset] = (byte)c;
		return array;
	}

	public static byte[] stringToArray (String string, byte[] array, int offset) {
		int i, j, byteI;

		byteI = offset;
		for (i = 0; i < string.length(); i++) {
			for (j = 0; j < SizeConstants.sizeOfChar; j++) {
				array[byteI] = (byte)(string.charAt(i) >>> j*Byte.SIZE);
				byteI++;
			}
		}

		for (j = 0; j < SizeConstants.sizeOfChar; j++) {
			array[byteI] = (byte)0;
			byteI++;
		}
		return array;
	}

	public static byte[] latinStringToArray (String string, byte[] array, int offset) {
		int i;
		for (i = 0; i < string.length(); i++) {
			array[offset+i] = (byte)string.charAt(i);
		}
		array[offset+i] = (byte)0;

		return array;
	}
	
	public static byte[] serializableToArray (Serializable sr, byte[] array, int offset) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			byte[] arr;
			out.writeObject(sr);
			out.flush();
			arr = bos.toByteArray();
			return ByteArrayConverter.copyArrayTo(arr, 0, arr.length, array, offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] serializableToArray (Serializable sr) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			byte[] arr;
			
			out.writeObject(sr);
			out.flush();
			arr = bos.toByteArray();
			
			out.close();
			bos.close();
			
			return arr;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	//FROM ARRAY
	public static short shortFromArray (byte[] array, int offset) {
		short hd = 0;

		for (int i = 0; i < SizeConstants.sizeOfShort; i++) {
			hd |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return hd;
	}

	public static int intFromArray (byte[] array, int offset) {
		int d = 0;

		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			d |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return d;
	}

	public static long longFromArray (byte[] array, int offset) {
		long ld = 0;

		for (int i = 0; i < SizeConstants.sizeOfLong; i++) {
			ld |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return ld;
	}

	public static float floatFromArray (byte[] array, int offset) {
		int bits = 0;

		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			bits |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return Float.intBitsToFloat(bits);
	}

	public static double doubleFromArray (byte[] array, int offset) {
		long bits = 0;

		for (int i = 0; i < SizeConstants.sizeOfLong; i++) {
			bits |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return Double.longBitsToDouble(bits);
	}

	public static boolean booleanFromArray (byte[] array, int offset) {
		return array[offset] == 1;
	}

	public static char charFromArray (byte[] array, int offset) {
		char c = 0;

		for (int i = 0; i < SizeConstants.sizeOfChar; i++) {
			c |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return c;
	}

	public static char latinCharFromArray (byte[] array, int offset) {
		return (char)(0xFF & array[offset]);
	}

	public static String stringFromArray (byte[] array, int offset) {
		int byteI, j;
		char read;
		StringBuilder builder = new StringBuilder();

		byteI = offset;
		read = 0;
		for (j = 0; j < SizeConstants.sizeOfChar && byteI < array.length; j++) {
			read |= (0xFF & array[byteI]) << j*Byte.SIZE;
			byteI++;
		}

		while (read != 0 && byteI < array.length) {
			builder.append(read);
			read = 0;
			for (j = 0; j < SizeConstants.sizeOfChar && byteI < array.length; j++) {
				read |= (0xFF & array[byteI]) << j*Byte.SIZE;
				byteI++;
			}

		}

		return new String(builder);
	}

	public static String latinStringFromArray (byte[] array, int offset) {
		int byteI;
		char read;
		StringBuilder builder = new StringBuilder();

		byteI = offset;
		read = (char)(0xFF & array[byteI]);
		byteI++;
		while (read != 0 && byteI < array.length) {
			builder.append(read);
			read = (char)(0xFF & array[byteI]);
			byteI++;
		}

		return new String(builder);
	}
	
	public static Object serializableFromArray (byte[] array, int offset) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bas = new ByteArrayInputStream(array, offset, array.length-offset);
		ObjectInputStream ois = new ObjectInputStream(bas);
		Object sr = ois.readObject();
		
		return sr;
	}

	/*ARRAY METHODS*/
	public static byte[] copyArrayTo (byte[] input, int beginning, int end, byte[] output, int outputOffset) {
		int i, limit;
		limit = end-beginning;
		for (i = 0; i < limit; i++) {
			output[i+outputOffset] = input[i+beginning];
		}
		return output;
	}

	public static String toStringRepresentation (byte[] array) {
		StringBuilder str = new StringBuilder();
		int	mask = 0x0F;
		int c0, c1, asInt;

		for (int i = 0; i < array.length; i++) {
			asInt = 0xFF & array[i];
			c0 = asInt >>> 4;
			c1 = asInt & mask;
			//System.out.printf("%x, %x, %x\n", array[i], c0, c1);	//debug
			str.append(ByteArrayConverter.hexRep[c0]);
			str.append(ByteArrayConverter.hexRep[c1]);
		}

		return new String(str);
	}

	public static byte[] fromStringRepresentation (String str) {
		byte[] value = new byte[str.length()/2];
		int[] preString = new int[str.length()];
		int i, c;

		//prepare string for processing
		str = str.toUpperCase();
		for (i = 0; i < str.length(); i++) {
			c = (int)str.charAt(i);
			if (c < ByteArrayConverter.hexRep[10]) {
				preString[i] = c-ByteArrayConverter.hexRep[0];
			} else {
				preString[i] = c-ByteArrayConverter.hexRep[10]+10;
			}
		}

		//process string
		for (i = 0; i < value.length; i++) {
			c = i*2;
			value[i] = (byte)(preString[c+1]);
			value[i] |= (byte)(preString[c] << 4);
		}

		return value;
	}

}
