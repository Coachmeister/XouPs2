package net.ximias.effect.producers;

import javafx.scene.paint.Color;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectProducer;

import java.util.HashMap;

public class MultiEffectProducer extends EffectProducer {
	private final EffectProducer[] effects;
	
	public MultiEffectProducer(EffectProducer... effects) {
		this.effects = effects;
	}
	
	@Override
	public Effect build() {
		return new MultiEffect(effects, this);
	}
	
	@Override
	public void setColor(Color color) {
		for (EffectProducer effect : effects) {
			effect.setColor(color);
		}
	}
	
	public EffectProducer[] getEffects() {
		return effects;
	}
	
	@Override
	public HashMap<String, String> toJson() {
		return null;
	}
}
class MultiEffect implements Effect{
	private final EffectProducer[] effects;
	private Effect current;
	private int count;
	private final EffectProducer parent;
	
	public MultiEffect(EffectProducer[] effects, EffectProducer parent) {
		this.effects = effects;
		this.parent = parent;
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
	
	@Override
	public EffectProducer getProducer() {
		return parent;
	}
}
