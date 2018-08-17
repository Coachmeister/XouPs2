package net.ximias.logging;

public enum Category {
	GENERAL("General"),
	APPLICATION("Application"),
	EFFECTS("Effects"),
	NETWORK("Network");
	
	private String name;
	Category(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
