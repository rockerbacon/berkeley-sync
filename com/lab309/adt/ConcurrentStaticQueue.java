package com.lab309.adt;

public class ConcurrentStaticQueue<T> extends ConcurrentStaticDeque<T> {
	/*CONSTRUCTORS*/
	public ConcurrentStaticQueue (int size) { super(size); }
	
	/*METHODS*/
	public void push (T data) throws IndexOutOfBoundsException {
		super.append(data);
	}
	
	public T pop () throws IndexOutOfBoundsException {
		return super.popFirst();
	}
	
	@Override
	public void prepend (T data) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	@Override
	public T popLast () throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
}
