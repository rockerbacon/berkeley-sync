package com.lab309.general;

/**
 * Class containing size constants
 *
 * Created by Vitor Andrade dos Santos on 3/28/17.
 */

public abstract class SizeConstants {
	public static final int sizeOfByte = 1;
	public static final int sizeOfBoolean = SizeConstants.sizeOfByte;
	public static final int sizeOfChar = Character.SIZE/Byte.SIZE;
	public static final int sizeOfLatinChar = SizeConstants.sizeOfByte;
	public static final int sizeOfInt = Integer.SIZE/Byte.SIZE;
	public static final int sizeOfShort = Short.SIZE/Byte.SIZE;
	public static final int sizeOfLong = Long.SIZE/Byte.SIZE;
	public static final int sizeOfFloat = Float.SIZE/Byte.SIZE;
	public static final int sizeOfDouble = Double.SIZE/Byte.SIZE;

	public static int sizeOfString (String string) {
		return string.length()*SizeConstants.sizeOfChar+SizeConstants.sizeOfChar;
	}

	public static int sizeOfLatinString (String string) {
		return string.length()*SizeConstants.sizeOfLatinChar+SizeConstants.sizeOfLatinChar;
	}
}
