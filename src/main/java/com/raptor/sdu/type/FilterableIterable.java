package com.raptor.sdu.type;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public interface FilterableIterable<T> extends Iterable<T> {
	
	default FilterableIterable<T> filter(Predicate<? super T> filter) {
		final Iterator<T> iter = StreamSupport.stream(this.spliterator(), false)
				.filter(filter)
				.iterator();
		return () -> iter;
	}
	
	default <E extends T> FilterableIterable<E> filter(Class<E> type) {
		final Iterator<E> iter = StreamSupport.stream(this.spliterator(), false)
				.filter(type::isInstance)
				.map(type::cast)
				.iterator();
		return () -> iter;
	}
}