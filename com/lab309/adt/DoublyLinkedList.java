package com.lab309.adt;

/**
 * Criei pois ListIterator do java possui uso extremamente limitado, reduzindo operaçoes constantes da lista encadeada a operacoes lineares em muitos casos
 * Classe nao eh compativel com loop for each. Em substituicao de um for each fazer 'for(DoubleLinkedList<T>.Iterator i = list.iterator(); !i.end(); i.next())'
 * Atencoes especiais:
 * 		Apos uso da funcao remove(Iterator) o iterador passado nao sera movido e se faz necessario chamar as funcoes next() ou previous() antes de qualquer outra operacao utilizando o mesmo iterador
 *
 * Created by Vitor Andrade dos Santos on 12/28/16.
 */

public class DoublyLinkedList<T> {

	private class Node {
		/*ATTRIBUTES*/
		public T data;
		public Node previous;
		public Node next;

		/*CONSTRUCTORS*/
		public Node (Node previous, T data, Node next) {
			this.data = data;
			this.previous = previous;
			this.next = next;
		}

	}

	/**
	 * Classe para iterar a lista
	 */
	public class Iterator {
		/*ATTRIBUTES*/
		private Node node;

		/*CONSTRUCTORS*/
		public Iterator (DoublyLinkedList<T> list, int index) {
			this.node = list.getNode(index);
		}

		public Iterator (DoublyLinkedList<T> list) {
			this.node = list.first;
		}

		private Iterator (Node node) {
			this.node = node;
		}

		public Iterator (Iterator iterator) {
			this.node = iterator.node;
		}

		/*METHODS*/
		public void setPos(Iterator iterator) {
			this.node = iterator.node;
		}
		public void set (T data) {
			this.node.data = data;
		}
		public T get () {
			return this.node.data;
		}

		public T peekNext () {
			return this.node.next.data;
		}
		public T peekPrevious () {
			return this.node.previous.data;
		}

		public void next () {
			this.node = this.node.next;
		}
		public void previous () {
			this.node = this.node.previous;
		}

		public boolean end () {
			return this.node == null;
		}

	}

	/*ATTRIBUTES*/
	private Node first;
	private Node last;
	private int length;

	/*CONSTRUCTORS*/
	public DoublyLinkedList (DoublyLinkedList<T> list) {
		this.length = 0;
		this.add(list, 0);
	}
	public DoublyLinkedList () {
		this.first = null;
		this.last = null;
		this.length = 0;
	}

	/*SETTERS*/
	public T set (int index, T data) throws IndexOutOfBoundsException {
		if (index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		Node node = this.getNode(index);
		T temp = node.data;
		node.data = data;
		return temp;
	}

	public void set (T original, T data) {
		Node node = this.getNode(original);
		if (node != null) {
			node.data = data;
		}
	}

	/*GETTERS*/
	public int length () {
		return this.length;
	}

	private Node getNode (int index) {
		Node node;
		if (index < this.length/2) {
			for (node = this.first; index > 0; index--) {
				node = node.next;
			}
		} else {
			for (node = this.last; index > 0; index--) {
				node = node.previous;
			}
		}
		return node;
	}

	private Node getNode (T data) {
		for (Iterator i = new Iterator(this.first); !i.end(); i.next()) {
			if (i.get().equals(data)) {
				return i.node;
			}
		}
		return null;
	}

	public T get (int index) throws IndexOutOfBoundsException {
		if (index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		return this.getNode(index).data;
	}

	/*METHODS*/
	public boolean contains(T data) {
		return this.getNode(data) != null;
	}

	//construir iteradores a partir da lista
	public Iterator iterator () {
		return new Iterator(this.first);
	}
	public Iterator iterator (int index) throws IndexOutOfBoundsException {
		if (index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		return new Iterator(this.getNode(index));
	}
	public Iterator iterator (T data) {
		Node node = this.getNode(data);
		if (node == null) {
			return null;
		}
		return new Iterator(node);
	}

	public void addAfter (T data, Iterator iterator) {
		Node temp = iterator.node.next;
		iterator.node.next = new Node(iterator.node, data, temp);
		if (temp != null) {
			temp.previous = iterator.node.next;
		}
	}

	public void addBefore (T data, Iterator iterator) {
		Node temp = iterator.node.previous;
		iterator.node.previous = new Node(temp, data, iterator.node);
		if (temp != null) {
			temp.next = iterator.node.previous;
		}
	}

	public void add (T data, int index) throws IndexOutOfBoundsException {
		if (index > this.length) {
			throw new IndexOutOfBoundsException();
		}
		
		if (this.length == 0) {

			this.first = new Node(null, data, null);
			this.last = this.first;

		} else if (index == 0) {

			Node temp = this.first;
			this.first = new Node(null, data, temp);

		} else if (index == this.length) {

			this.last.next = new Node (this.last, data, null);
			this.last = this.last.next;

		} else {

			Node addBefore = this.getNode(index);
			Node add = new Node(addBefore.previous, data, addBefore);
			addBefore.previous.next = add;
			addBefore.previous = add;

		}
		
		this.length++;
	}
	public void append (T data) {
		this.add(data, this.length);
	}
	public void prepend (T data) {
		this.add(data, 0);
	}

	public void add (DoublyLinkedList<T> list, int index) throws IndexOutOfBoundsException {
		if (index > this.length) {
			throw new IndexOutOfBoundsException();
		}
		if (list.length == 0) {
			return;
		}

		Node previouslyAdded;

		if (this.length == 0) {

			this.first = new Node(null, list.first.data, null);
			previouslyAdded = this.first;
			for (Iterator i = new Iterator(this.first); !i.end(); i.next()) {
				previouslyAdded.next = new Node(previouslyAdded, i.get(), null);
				previouslyAdded = previouslyAdded.next;
			}
			this.last = previouslyAdded;

		} else if (index == 0) {

			previouslyAdded = this.first;
			for (Iterator i = new Iterator(list.last); !i.end(); i.previous()) {
				previouslyAdded.previous = new Node(null, i.get(), previouslyAdded);
				previouslyAdded = previouslyAdded.previous;
			}
			this.first = previouslyAdded;

		} else if (index == this.length) {

			previouslyAdded = this.last;
			for (Iterator i = new Iterator(list.first); !i.end(); i.next()) {
				previouslyAdded.next = new Node(previouslyAdded, i.get(), null);
				previouslyAdded = previouslyAdded.next;
			}
			this.last = previouslyAdded;

		} else {
			Node after;

			previouslyAdded = this.getNode(index-1);
			after = previouslyAdded.next;
			for (Iterator i = new Iterator(list.first); !i.end(); i.next()) {
				previouslyAdded.next = new Node(previouslyAdded, i.get(), after);
				previouslyAdded = previouslyAdded.next;
			}
			after.previous = previouslyAdded;

		}

		this.length += list.length;
	}
	public void append(DoublyLinkedList<T> list) {
		this.add(list, this.length);
	}
	public void prepend(DoublyLinkedList<T> list) {
		this.add(list, 0);
	}

	private void remove (Node node) {
		node.previous.next = node.next;
		node.next.previous = node.previous;
	}
	public T remove (int index) throws IndexOutOfBoundsException {
		if (index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		Node removed = this.getNode(index);
		this.remove(removed);
		return removed.data;
	}
	public void remove (T data) {
		Node removed = this.getNode(data);
		if (removed != null) {
			this.remove(removed);
		}
	}
	public void remove (Iterator iterator) {
		this.remove(iterator.node);
	}

	@SuppressWarnings("unchecked")
	public T[] toArray () {
		Object [] data = new Object[this.length];
		Node cursor = this.first;
		for (int i = 0; i < this.length; i++) {
			data[i] = cursor.data;
			cursor = cursor.next;
		}
		return (T[])data;
	}

}
