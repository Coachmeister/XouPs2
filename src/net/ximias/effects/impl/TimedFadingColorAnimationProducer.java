package net.ximias.effects.impl;

import javafx.scene.paint.Color;

public class TimedFadingColorAnimationProducer extends TimedColorAnimationProducer {
	TimedFadingColorAnimationProducer(Color startColor, long duration_milliseconds) {
		super(startColor, Color.TRANSPARENT, duration_milliseconds);
	}
}
