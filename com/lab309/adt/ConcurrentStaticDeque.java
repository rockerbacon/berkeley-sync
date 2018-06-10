package com.lab309.adt;

/**
 * Classe deque com tamanho fixo para operacoes em paralelo
 *
 * Created by Vitor Andrade dos Santos on 3/8/17.
 */

public class ConcurrentStaticDeque<T> {
	/*ATTRIBUTES*/
	private Object [] data;
	private int beginning;
	private int end;
	private int length;
	
	private Object beginningLock = new Object();
	private Object endLock = new Object();
	private Object lengthLock = new Object();

	/*CONSTRUCTORS*/
	public ConcurrentStaticDeque (int size) {
		this.data = new Object[size];
		this.beginning = size/2;
		this.end = this.beginning+1;
		this.length = 0;
	}

	/*GETTERS*/
	public int length () {
		return this.length;	
	}
	
	public int size () {
		return this.data.length;
	}

	/*METHODS*/
	public boolean full () {
		return this.length == this.data.length;	
	}
	
	public boolean empty () {
		return this.length == 0;
	}

	@SuppressWarnings("unchecked")
	public T dataAt (int index) throws IndexOutOfBoundsException {
		if (index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		int realIndex = this.beginning+index;
		if (realIndex > this.data.length) {
			realIndex -= this.data.length;
		}
		return (T)this.data[realIndex];
	}

	public void prepend (T data) throws IndexOutOfBoundsException {
		synchronized (this.lengthLock) {
			if (this.length == this.data.length) {
				throw new IndexOutOfBoundsException("Deque is full");
			}	
			this.length++;
		}
		
		synchronized (this.beginningLock) {
			this.data[beginning] = data;
			if (this.beginning-- == -1) {
				this.beginning = this.data.length - 1;
			}
		}
	}

	public void append (T data) throws IndexOutOfBoundsException {
		synchronized (this.lengthLock) {
			if (this.length == this.data.length) {
				throw new IndexOutOfBoundsException("Deque is full");
			}
			this.length++;
		}
		
		synchronized (this.endLock) {
			this.data[end] = data;
			this.end = (this.end+1) % this.data.length;
		}
	}

	@SuppressWarnings("unchecked")
	public T popFirst () {
		synchronized (this.lengthLock) {
			if (this.length == 0) {
				return null;
			}
			this.length--;
		}
		
		synchronized (this.beginningLock) {
			this.beginning = (this.beginning+1) % this.data.length;

			return (T)this.data[this.beginning];
		}
	}

	@SuppressWarnings("unchecked")
	public T popLast () {
		synchronized (this.lengthLock) {
			if (this.length == 0) {
				return null;
			}
			this.length--;
		}	

		synchronized (this.endLock) {
			if (this.end-- == -1) {
				this.end = this.data.length-1;
			}

			return (T)this.data[this.end];
		}
	}

	@SuppressWarnings("unchecked")
	public T peekFirst () {
		int index = (this.beginning+1) % this.data.length;
		
		return (T)this.data[index];
	}

	@SuppressWarnings("unchecked")
	public T peekLast () {
		int index = this.end-1;
		if (index == -1) {
			index = this.data.length-1;
		}
		return (T)this.data[index];
	}

}
