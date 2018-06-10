package com.lab309.adt;

/**
 * Classe deque com tamanho fixo
 *
 * Created by Vitor Andrade dos Santos on 3/8/17.
 */

public class StaticDeque<T> {
	/*ATTRIBUTES*/
	private Object [] data;
	private int beginning;
	private int end;
	private int length;

	/*CONSTRUCTORS*/
	public StaticDeque (int size) {
		this.data = new Object[size];
		this.beginning = size/2;
		this.end = size/2;
		this.length = 0;
	}

	/*GETTERS*/
	public int length () {
		return this.length;
	}

	/*METHODS*/
	public boolean full () {
		return this.length == this.data.length;
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
		if (this.length == this.data.length) {
			throw new IndexOutOfBoundsException("Deque is full");
		}
		this.data[beginning] = data;
		this.length++;
		if (this.beginning-- == -1) {
			this.beginning = this.data.length - 1;
		}
	}

	public void append (T data) throws IndexOutOfBoundsException {
		if (this.length == this.data.length) {
			throw new IndexOutOfBoundsException("Deque is full");
		}
		this.data[end] = data;
		this.length++;
		if (this.end++ == this.data.length) {
			this.end = 0;
		}
	}

	@SuppressWarnings("unchecked")
	public T popFirst () {
		if (this.beginning++ == this.data.length) {
			this.beginning = 0;
		}
		this.length--;

		return (T)this.data[this.beginning];
	}

	@SuppressWarnings("unchecked")
	public T popLast () {
		if (this.end-- == -1) {
			this.end = this.data.length-1;
		}
		this.length--;

		return (T)this.data[this.end];
	}

	@SuppressWarnings("unchecked")
	public T peekFirst () {
		int index = this.beginning+1;
		if (index == this.data.length) {
			index = 0;
		}
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