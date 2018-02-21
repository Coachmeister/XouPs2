package net.ximias.psEvent.condition;

import java.util.function.BiPredicate;

public enum Condition {
	EQUALS((a,b)->a.equals(b)),
	NOT_EQUALS((a,b)->!a.equals(b)),
	GREATER((a,b)->Integer.valueOf(a)>Integer.valueOf(b)),
	LESS((a,b)->Integer.valueOf(a)<Integer.valueOf(b)),
	GREATER_OR_EQUALS((a,b)->Integer.valueOf(a)>=Integer.valueOf(b)),
	LESS_OR_EQUALS((a,b)->Integer.valueOf(a)<=Integer.valueOf(b));
	private final BiPredicate<String, String> predicate;
	Condition(BiPredicate<String, String> predicate){
		this.predicate = predicate;
	}
	
	boolean eval(String a, String b){
		return predicate.test(a,b);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < Condition.values().length; i++) {
			Condition condition = Condition.values()[i];
			System.out.println(condition.toString());
		}
	}
}