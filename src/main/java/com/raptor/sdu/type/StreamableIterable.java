package com.raptor.sdu.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface StreamableIterable<T> extends Iterable<T> {
	
	Stream<T> stream();
	
	default Stream<T> parallelStream() {
		return stream().parallel();
	}
	
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}
	
	static <T> StreamableIterable<T> wrap(Iterable<T> iterable) {
		return iterable instanceof StreamableIterable? (StreamableIterable<T>)iterable : () -> StreamSupport.stream(iterable.spliterator(), false);
	}
	
	static <T> StreamableIterable<T> wrap(Collection<T> c) {
		return new StreamableIterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return c.iterator();
			}
			
			@Override
			public Stream<T> stream() {
				return c.stream();
			}
			
			@Override
			public Stream<T> parallelStream() {
				return c.parallelStream();
			}
			
		};
	}
}