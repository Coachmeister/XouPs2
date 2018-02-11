package net.ximias.effects.impl;

import javafx.scene.paint.Color;

public class TimedFadingColorAnimation extends TimedColorAnimation {
	public TimedFadingColorAnimation(Color startColor, long duration_milliseconds) {
		super(startColor, Color.TRANSPARENT, duration_milliseconds);
	}
}
