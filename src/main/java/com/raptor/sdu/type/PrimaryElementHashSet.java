package com.raptor.sdu.type;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class PrimaryElementHashSet<T> extends AbstractSet<T> {
	private final HashSet<T> set;
	private T primary = null;

	public PrimaryElementHashSet() {
		set = new HashSet<>();
	}
	
	public PrimaryElementHashSet(int initialCapacity) {
		set = new HashSet<>(initialCapacity);
	}
	
	public PrimaryElementHashSet(Collection<? extends T> c) {
		set = new HashSet<>(c);
		if(!c.isEmpty()) {
			primary = c.iterator().next();
		}
	}
	
	@Override
	public boolean add(T t) {
		Objects.requireNonNull(t);
		if(primary == null) {
			primary = t;
		}
		return set.add(t);
	}
	
	public void setPrimary(T t) {
		Objects.requireNonNull(t);
		set.add(t);
		primary = t;
	}
	
	public Optional<T> getPrimary() {
		return Optional.ofNullable(primary);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			boolean sentPrimary = false, lastWasPrimary = false;
			Iterator<T> setIterator = set.iterator();
			T next;
			
			{
				if(primary == null) {
					if(setIterator.hasNext()) {
						next = setIterator.next();
					}
				} else {
					next = primary;
				}
			}

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public T next() {
				if(next == null)
					throw new NoSuchElementException();
				T result = next;
				if(result == primary) {
					sentPrimary = lastWasPrimary = true;
				} else {
					lastWasPrimary = false;
				}
				if(setIterator.hasNext()) {
					next = setIterator.next();
					if(next == primary) {
						if(setIterator.hasNext()) {
							next = setIterator.next();
						} else {
							next = null;
						}
					}
				} else {
					next = null;
				}
				return result;
			}

			@Override
			public void remove() {
				if(sentPrimary) {
					if(lastWasPrimary) {
						if(set.isEmpty()) {
							primary = null;
						} else {
							set.remove(primary);
							primary = set.iterator().next(); 
						}
					} else {
						setIterator.remove();
					}
				} else {
					throw new NoSuchElementException();
				}
			}

		};
	}

	@Override
	public int size() {
		return set.size();
	}

}
