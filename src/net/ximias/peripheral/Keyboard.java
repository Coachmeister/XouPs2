package net.ximias.peripheral;


public interface Keyboard {
	int getRows();
	int getColumns();
	void setAndExemptKeyByName(String name);
}
