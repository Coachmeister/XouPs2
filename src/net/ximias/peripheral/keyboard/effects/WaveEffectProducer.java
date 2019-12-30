package net.ximias.peripheral.keyboard.effects;

import javafx.scene.paint.Color;

public class WaveEffectProducer implements IWaveEffectProducer {
	private Color color;
	private final int duration;
	private final int effectWidth;
	private final WaveEffectDirection direction;
	
	public WaveEffectProducer(Color color, int duration_ms, int effectWidth, WaveEffectDirection direction) {
		this.color = color;
		this.duration = duration_ms;
		this.effectWidth = effectWidth;
		this.direction = direction;
	}
	
	@Override
	public WaveEffect build() {
		return new WaveEffect(color, duration, effectWidth, direction);
	}
	
	@Override
	public void setColor(Color color) {
		this.color = color;
	}
}

