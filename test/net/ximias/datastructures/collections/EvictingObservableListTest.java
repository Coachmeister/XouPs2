package net.ximias.datastructures.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvictingObservableListTest {
	private final int MAX_SIZE = 20;
	private EvictingObservableList<Integer> list = new EvictingObservableList<>(MAX_SIZE);
	private int next = 0;
	
	@BeforeEach
	void setUp() {
		list = new EvictingObservableList<>(MAX_SIZE);
	}
	
	@Test
	void testListEnforcesMaxSize() {
		for (int i = 0; i < MAX_SIZE+2; i++) {
			list.addFirst(i);
		}
		assertEquals(MAX_SIZE, list.size(),"List should not grow past max size.");
		
		for (int i = 0; i > -MAX_SIZE-1; i--) {
			list.addLast(i);
		}
		assertEquals(MAX_SIZE, list.size(), "List should not grow past max sixe");
		
		list.addLast(1);
		list.addFirst(1);
		assertEquals(-MAX_SIZE, list.getLast().intValue(), "List should evict first when adding last.");
		list.addFirst(-1);
		list.addLast(-1);
		assertEquals(1,list.getFirst().intValue(), "List should evict last when adding first.");
	}
	
	@Test
	void addFirst() {
		int first = next++;
		int second = next++;
		list.addFirst(first);
		assertEquals(first, list.getFirst().intValue());
		list.addFirst(second);
		assertEquals(second, list.getFirst().intValue());
		assertEquals(first, list.get(1).intValue());
	}
	
	@Test
	void removeFirst() {
		int first = next++;
		int second = next++;
		list.addFirst(first);
		list.removeFirst();
		assertTrue(list.isEmpty(),"list should be empty after first remove");
		
		list.addLast(first);
		list.removeFirst();
		assertTrue(list.isEmpty(), "List should be empty after remove");
		
		list.addLast(first);
		list.addLast(second);
		list.removeFirst();
		assertEquals(second, list.getFirst().intValue(), "First element should be removed.");
	}
	
	@Test
	void addLast() {
		int first = next++;
		int second = next++;
		list.addLast(first);
		assertEquals(first, list.getLast().intValue(),"Get last should return only added element");
		list.addLast(second);
		assertEquals(second, list.getLast().intValue());
		assertEquals(first, list.get(list.size()-2).intValue());
	}
	
	@Test
	void removeLast() {
		int first = next++;
		int second = next++;
		list.addFirst(first);
		list.removeLast();
		assertTrue(list.isEmpty(),"list should be empty after first remove");
		
		list.addLast(first);
		list.removeLast();
		assertTrue(list.isEmpty(), "List should be empty after remove");
		
		list.addLast(first);
		list.addLast(second);
		list.removeLast();
		assertEquals(first, list.getFirst().intValue(), "First element should be removed.");
	}
}