package net.ximias.effect;

import javafx.scene.effect.Blend;
import javafx.scene.paint.Color;

public interface Effect {
	Color getColor();
	boolean isDone();
	boolean hasIntensity();
	default String getName(){
		return getClass().getSimpleName();
	}
	
	static Color blend(double effectIntensity, Effect ... effects){
		double r, g, b, a;
		r = g = b = a = 0;
		for (Effect effect : effects) {
			r += effect.getColor().getRed() * effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			g += effect.getColor().getGreen() * effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			b += effect.getColor().getBlue() * effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			a += effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
		}
		return Color.color(Math.min(r / a, 1.0), Math.min(g / a, 1.0), Math.min(b / a, 1.0));
	}
}
