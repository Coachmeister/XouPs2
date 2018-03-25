package net.ximias.effect;

import javafx.scene.paint.Color;

public interface Effect {
	Color getColor();
	boolean isDone();
	boolean hasIntensity();
	default String getName(){
		return getClass().getSimpleName();
	}
}
