package com.lab309.adt;

/**
 * Fila de dimensao fixa
 * Criado pois java nao possui uma classe de fila implementada
 *
 * Created by Vitor Andrade dos Santos on 3/8/17.
 */

public class StaticQueue<T> extends StaticDeque<T> {

	/*CONSTRUCTORS*/
	public StaticQueue (int size) {
		super(size);
	}

	/*METHODS*/
	public void push (T data) throws IndexOutOfBoundsException {
		super.append(data);
	}

	public T pop () {
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