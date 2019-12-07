package com.raptor.sdu.type;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIterator<T> implements Iterator<T> {
	@SuppressWarnings("rawtypes")
	public static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();
	
	@SuppressWarnings("unchecked")
	public static <T> EmptyIterator<T> emptyIterator() {
		return EMPTY_ITERATOR;
	}
	
	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		throw new NoSuchElementException();
	}

}
