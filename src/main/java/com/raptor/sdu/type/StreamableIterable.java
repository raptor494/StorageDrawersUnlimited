package com.raptor.sdu.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.Validate;

public interface StreamableIterable<T> extends Iterable<T> {
	
	Stream<T> stream();
	
	default Stream<T> parallelStream() {
		return stream().parallel();
	}
	
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}
	
	static <T> StreamableIterable<T> wrap(final Iterable<T> iterable) {
		Validate.notNull(iterable);
		if(iterable instanceof StreamableIterable) {
			return (StreamableIterable<T>)iterable;
		}
		else if(iterable instanceof Collection) {
			return wrap((Collection<T>)iterable);
		}
		else {
			return new StreamableIterable<T>() {
				
				@Override
				public Stream<T> stream() {
					return StreamSupport.stream(iterable.spliterator(), false);
				}
				
				@Override
				public Stream<T> parallelStream() {
					return StreamSupport.stream(iterable.spliterator(), true);
				}
				
				@Override
				public Iterator<T> iterator() {
					return iterable.iterator();
				}
				
				@Override
				public int hashCode() {
					return iterable.hashCode();
				}
				
				@Override
				public String toString() {
					return "StreamableIterable.wrap(" + iterable + ")";
				}
				
			};
		}
	}
	
	static <T> StreamableIterable<T> wrap(final Collection<T> collection) {
		return new StreamableIterable<T>() {

			@Override
			public Stream<T> stream() {
				return collection.stream();
			}
			
			@Override
			public Stream<T> parallelStream() {
				return collection.parallelStream();
			}
			
			@Override
			public Iterator<T> iterator() {
				return collection.iterator();
			}
			
			@Override
			public int hashCode() {
				return collection.hashCode();
			}
			
			@Override
			public String toString() {
				return "StreamableIterable.wrap(" + collection.toString() + ")";
			}
			
		};
	}
}