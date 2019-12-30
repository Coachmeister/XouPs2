package net.ximias.peripheral.keyboard;

import javafx.scene.paint.Color;

public interface GlobalKeyEffect extends KeyEffect {
	Color getGlobalColor();
	
	
	@Override
	default Color[][] getKeyColors(int width, int height) {
		return new Color[][]{{getGlobalColor()}};
	}
}
