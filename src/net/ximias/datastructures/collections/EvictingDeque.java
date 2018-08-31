package net.ximias.datastructures.collections;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class EvictingDeque<E> extends AbstractSequentialList<E> implements Deque<E> {
	private final int maxSize;
	
	private final LinkedBlockingDeque<E> deque;
	
	public EvictingDeque(int maxSize) {
		this.maxSize = maxSize;
		deque = new LinkedBlockingDeque<>(maxSize+1);
	}
	
	@Override
	public void addFirst(E e) {
		deque.addFirst(e);
		if (shouldRemove()) deque.removeFirst();
	}
	
	@Override
	public void addLast(E e) {
		deque.addLast(e);
		if (shouldRemove()) removeFirst();
	}
	
	@Override
	public boolean add(E e) {
		addLast(e);
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c) {
			add(e);
		}
		return true;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		Iterator<E> iter = iterator();
		boolean b = false;
		int index = 0;
		while (iter.hasNext()){
			index++;
			E val = iter.next();
			if (c.contains(val)){
				b=true;
				iter.remove();
			}
		}
		return b;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		Iterator<E> iter = iterator();
		boolean b = false;
		int index = 0;
		while (iter.hasNext()){
			index++;
			E val = iter.next();
			if (!c.contains(val)){
				b=true;
				iter.remove();
			}
		}
		return b;
		
	}
	
	@Override
	public void clear() {
		deque.clear();
	}
	
	@Override
	public E removeFirst() {
		errIfEmpty();
		return deque.removeFirst();
	}
	
	@Override
	public E removeLast() {
		errIfEmpty();
		return deque.removeLast();
	}
	
	@Override
	public E pollFirst() {
		errIfEmpty();
		return deque.pollFirst();
	}
	
	@Override
	public E pollLast() {
		errIfEmpty();
		return deque.pollLast();
	}
	
	@Override
	public E peekFirst() {
		return deque.peekFirst();
	}
	
	@Override
	public E peekLast() {
		return deque.peekLast();
	}
	
	@Override
	public boolean removeFirstOccurrence(Object o) {
		Iterator<E> iter = iterator();
		while (iter.hasNext()){
			if (iter.next().equals(o)){
				iter.remove();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean removeLastOccurrence(Object o) {
		Iterator<E> iter = descendingIterator();
		while (iter.hasNext()){
			if (iter.next().equals(o)){
				iter.remove();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean offer(E e) {
		return offerLast(e);
	}
	
	private void errIfEmpty() {
		if (isEmpty()) throw new NoSuchElementException("Deque is empty");
	}
	
	@Override
	public E getLast() {
		errIfEmpty();
		return deque.getLast();
	}
	
	@Override
	public E getFirst() {
		errIfEmpty();
		return deque.getFirst();
	}
	
	@Override
	public boolean offerLast(E e) {
		deque.addLast(e);
		return true;
	}
	
	@Override
	public boolean offerFirst(E e) {
		addFirst(e);
		return true;
	}
	
	private boolean shouldRemove() {
		return deque.size()> maxSize;
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return deque.containsAll(c);
	}
	
	@Override
	public boolean remove(Object o) {
		errIfEmpty();
		return removeFirstOccurrence(o);
	}
	
	@Override
	public E remove() {
		return removeFirst();
	}
	
	@Override
	public E poll() {
		return pollFirst();
	}
	
	@Override
	public E element() {
		return getFirst();
	}
	
	@Override
	public E peek() {
		return peekFirst();
	}
	
	@Override
	public void push(E e) {
		addFirst(e);
	}
	
	@Override
	public E pop() {
		return removeFirst();
	}
	
	@Override
	public int size() {
		return deque.size();
	}
	
	@Override
	public boolean isEmpty() {
		return deque.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return deque.contains(o);
	}
	
	@Override
	public Iterator<E> iterator() {
		return new Iter(deque.iterator());
	}
	
	@Override
	public Iterator<E> descendingIterator() {
		return new Iter(deque.descendingIterator());
	}
	
	@Override
	public ListIterator<E> listIterator(int index) {
		return new Liter(0);
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	class Iter implements Iterator<E>{
		final Iterator<E> chitor;
		int index = 0;
		E current;
		
		public Iter(Iterator<E> chitor) {
			this.chitor = chitor;
		}
		
		@Override
		public boolean hasNext() {
			return chitor.hasNext();
		}
		
		@Override
		public E next() {
			index++;
			current = chitor.next();
			return current;
		}
		
		@Override
		public void remove() {
			chitor.remove();
		}
	}
	
	class Liter implements ListIterator<E> {
		
		final LinkedList<E> lst = new LinkedList<>();
		final ListIterator<E> chiter;
		
		public Liter(int index) {
			chiter = lst.listIterator(index);
		}
		
		@Override
		public boolean hasNext() {
			return chiter.hasNext();
		}
		
		@Override
		public E next() {
			return chiter.next();
		}
		
		@Override
		public boolean hasPrevious() {
			return chiter.hasPrevious();
		}
		
		@Override
		public E previous() {
			return chiter.previous();
		}
		
		@Override
		public int nextIndex() {
			return chiter.nextIndex();
		}
		
		@Override
		public int previousIndex() {
			return chiter.previousIndex();
		}
		
		@Override
		public void remove() {
			if (!chiter.hasNext()) {
				System.out.println("Deque remove");
				removeLast();
				chiter.remove();
				return;
			}
			throw new UnsupportedOperationException("No random access");
		}
		
		@Override
		public void set(E e) {
			if (!chiter.hasNext()) {
				System.out.println("Deque set");
				removeLast();
				addLast(e);
				chiter.set(e);
				return;
			}
			throw new UnsupportedOperationException("No random access");
		}
		
		@Override
		public void add(E e) {
			/*if (!chiter.hasNext()) {
				System.out.println("Deque add");
				addLast(e);
				lst.clear();
				lst.addAll(deque);
				return;
			}*/
			throw new UnsupportedOperationException("No random access");
		}
	}
}
