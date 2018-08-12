package net.ximias.peripheral.keyboard;

import javafx.scene.paint.Color;

public interface KeyEffect {
	Color[][] getKeyColors(int width, int height);
	boolean isDone();
	
}
