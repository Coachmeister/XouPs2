package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MultiEffectProducer extends EffectProducer {
	private EffectProducer[] effects;
	
	public MultiEffectProducer(EffectProducer... effects) {
		this.effects = effects;
	}
	
	@Override
	public Effect build() {
		return new MultiEffect(effects);
	}
	
	@Override
	public void setColor(Color color) {
		for (EffectProducer effect : effects) {
			effect.setColor(color);
		}
	}
	
	@Override
	public HashMap<String, String> toJson() {
		return null;
	}
}
class MultiEffect implements Effect{
	private EffectProducer[] effects;
	private Effect current;
	private int count;
	
	public MultiEffect(EffectProducer[] effects) {
		this.effects = effects;
		count = 0;
		current = effects[0].build();
	}
	
	@Override
	public Color getColor() {
		if (current.isDone() && count != effects.length-1){
			current = effects[++count].build();
		}
		return current.getColor();
	}
	
	@Override
	public boolean isDone() {
		return count == effects.length-1 && current.isDone();
	}
	
	@Override
	public boolean hasIntensity() {
		return true;
	}
}
