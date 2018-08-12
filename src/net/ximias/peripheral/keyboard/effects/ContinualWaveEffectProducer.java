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
	
	@Override
	public KeyEffect build() {
		return null;
	}
	
	@Override
	public void setColor(Color color) {
	
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
