package net.ximias.peripheral.keyboard.effects;

import javafx.scene.paint.Color;
import net.ximias.peripheral.keyboard.KeyEffect;
import net.ximias.peripheral.keyboard.KeyEffectProducer;


public class MultiKeyEffectProducer implements KeyEffectProducer {
	private final KeyEffectProducer[] effectProducers;
	
	public MultiKeyEffectProducer(KeyEffectProducer... effectProducers) {
		this.effectProducers = effectProducers;
	}
	
	@Override
	public KeyEffect build() {
		return new MultiKeyEffect(effectProducers);
	}
	
	@Override
	public void setColor(Color color) {
	
	}
}

class MultiKeyEffect implements KeyEffect {
	private final KeyEffectProducer[] effects;
	private KeyEffect current;
	private int count;
	
	MultiKeyEffect(KeyEffectProducer[] effects) {
		this.effects = effects;
		this.current = effects[0].build();
		this.count = 0;
	}
	
	@Override
	public Color[][] getKeyColors(int width, int height) {
		if (current.isDone() && count != effects.length - 1) {
			current = effects[++count].build();
		}
		return current.getKeyColors(width, height);
	}
	
	@Override
	public boolean isDone() {
		return count == effects.length - 1 && current.isDone();
	}
}
