package com.raptor.sdu.type;

import static com.raptor.sdu.type.EmptyIterator.emptyIterator;

import java.util.Iterator;

class NestedIterator<T> implements Iterator<T> {
	private final Iterator<? extends Iterator<T>> iterators;
	private Iterator<T> iter;
	
	NestedIterator(Iterator<? extends Iterator<T>> iterators) {
		this.iterators = iterators;
		this.iter = iterators.hasNext()? iterators.next() : emptyIterator();
	}
	
	@Override
	public boolean hasNext() {
		if(iter.hasNext())
			return true;
		while(iterators.hasNext() && !iter.hasNext())
			iter = iterators.next();
		return iter.hasNext();
	}

	@Override
	public T next() {
		if(iter.hasNext())
			return iter.next();
		
		do {
			iter = iterators.next();
		} while(!iter.hasNext());
		
		return iter.next();
	}
	
}