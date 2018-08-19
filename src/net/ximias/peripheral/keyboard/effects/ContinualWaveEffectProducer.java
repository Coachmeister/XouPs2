package net.ximias.peripheral.keyboard.effects;

import javafx.scene.paint.Color;
import net.ximias.peripheral.keyboard.KeyEffect;
import net.ximias.peripheral.keyboard.KeyEffectProducer;

public class ContinualWaveEffectProducer implements KeyEffectProducer {
	private final IWaveEffectProducer producer;
	private final int iterations;
	
	public ContinualWaveEffectProducer(IWaveEffectProducer producer, int iterations) {
		this.producer = producer;
		this.iterations = iterations;
	}
	
	@Override
	public KeyEffect build() {
		return new ContinualWaveEffect(iterations, producer.build());
	}
	
	@Override
	public void setColor(Color color) {
	}
}
class ContinualWaveEffect implements KeyEffect {
	private final WaveEffect effect;
	private final long startTime = System.currentTimeMillis();
	private final int iterations;
	private final long duration;
	
	ContinualWaveEffect(int iterations, WaveEffect effect) {
		this.effect = effect;
		this.iterations = iterations;
		this.duration = effect.getDuration();
	}
	
	@Override
	public Color[][] getKeyColors(int width, int height) {
		return effect.getKeyColors(width, height);
	}
	
	@Override
	public boolean isDone() {
		return System.currentTimeMillis() >= startTime+duration*iterations;
	}
}
