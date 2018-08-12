package net.ximias.peripheral.keyboard.effects;

import javafx.scene.paint.Color;
import net.ximias.peripheral.keyboard.KeyEffect;
import net.ximias.peripheral.keyboard.KeyEffectProducer;

public class ContinualWaveEffectProducer implements KeyEffectProducer {
	private Color color;
	private long duration;
	private int iterations;
	private int effectWidth;
	private WaveEffectDirection direction;
	
	public ContinualWaveEffectProducer(Color color, long duration, int iterations, int effectWidth, WaveEffectDirection direction) {
		this.color = color;
		this.duration = duration;
		this.iterations = iterations;
		this.effectWidth = effectWidth;
		this.direction = direction;
	}
	
	@Override
	public KeyEffect build() {
		return new ContinualWaveEffect(color, duration, iterations, effectWidth, direction);
	}
	
	@Override
	public void setColor(Color color) {
		this.color = color;
	}
}
class ContinualWaveEffect extends WaveEffect{
	private int iterations;
	private final long startTime = System.currentTimeMillis();
	private final long duration;
	ContinualWaveEffect(Color color, long duration, int iterations ,int effectWidth, WaveEffectDirection direction) {
		super(color, duration, effectWidth, direction);
		this.duration = duration;
		this.iterations = iterations;
	}
	
	@Override
	public boolean isDone() {
		return System.currentTimeMillis() >= startTime+duration*iterations;
	}
}
