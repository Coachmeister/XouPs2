package net.ximias.peripheral;


import javafx.scene.paint.Color;

public interface Keyboard {
	int getRows();
	int getColumns();
	void setAndExemptKeyByName(String name, Color color);
	
	void enable();
	
	void disable();
}
