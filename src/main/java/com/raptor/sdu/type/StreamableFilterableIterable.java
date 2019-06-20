package com.raptor.sdu.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface StreamableFilterableIterable<T> extends StreamableIterable<T>, FilterableIterable<T> {
	
	default StreamableFilterableIterable<T> filter(Predicate<? super T> filter) {
		return () -> StreamSupport.stream(this.spliterator(), false)
				.filter(filter)
				.iterator();
	}
	
	default <E extends T> StreamableFilterableIterable<E> filter(Class<E> type) {
		return () -> StreamSupport.stream(this.spliterator(), false)
				.filter(type::isInstance)
				.map(type::cast)
				.iterator();
	}
	
	static <T> StreamableFilterableIterable<T> wrap(Iterable<T> iter) {
		return iter instanceof StreamableFilterableIterable? (StreamableFilterableIterable<T>)iter : iter::iterator;
	}
	
	static <T> StreamableFilterableIterable<T> wrap(Collection<T> c) {
		return new StreamableFilterableIterable<T>() {

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