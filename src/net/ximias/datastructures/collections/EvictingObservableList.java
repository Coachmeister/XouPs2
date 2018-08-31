package net.ximias.datastructures.collections;

import javafx.collections.ModifiableObservableListBase;

import java.util.LinkedList;

public class EvictingObservableList<E> extends ModifiableObservableListBase<E> {
	private final LinkedList<E> list = new LinkedList<>();
	private final int maxSize;
	
	public EvictingObservableList(int size) {
		this.maxSize = size;
	}
	
	@Override
	public E get(int index) {
		return list.get(index);
	}
	
	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	protected void doAdd(int index, E element) {
		list.add(index, element);
		if (size()>maxSize) removeFirst();
	}
	
	@Override
	protected E doSet(int index, E element) {
		return list.set(index, element);
	}
	
	@Override
	protected E doRemove(int index) {
		return list.remove(index);
	}
	
	public void addFirst(E e){
		if (size()+1>maxSize) removeLast();
		doAdd(0, e);
	}
	
	public void removeFirst(){
		doRemove(0);
	}
	
	public void addLast(E e){
		if (size()+1>maxSize) removeFirst();
		doAdd(size(),e);
	}
	
	public E getLast(){
		return get(size()-1);
	}
	
	public E getFirst(){
		return get(0);
	}
	
	public void removeLast(){
		doRemove(size()-1);
	}
}
