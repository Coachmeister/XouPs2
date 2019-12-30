package net.ximias.peripheral.keyboard.effects;

import javafx.scene.paint.Color;
import net.ximias.peripheral.keyboard.KeyEffect;
import net.ximias.peripheral.keyboard.KeyEffectProducer;

import java.util.Arrays;

public class DelayProducer implements KeyEffectProducer {
	
	private final long duration;
	
	public DelayProducer(long duration) {
		this.duration = duration;
	}
	
	@Override
	public KeyEffect build() {
		return new Delay(duration);
	}
	
	@Override
	public void setColor(Color color) {
	}
}

@SuppressWarnings("MismatchedReadAndWriteOfArray")
class Delay implements KeyEffect {
	private final long startTime = System.currentTimeMillis();
	private final long delay;
	private Color[][] colors;
	
	Delay(long delay) {
		this.delay = delay;
	}
	
	@Override
	public Color[][] getKeyColors(int width, int height) {
		if (colors == null) {
			colors = new Color[width][height];
			Color[] empty = new Color[height];
			Arrays.fill(empty, Color.TRANSPARENT);
			Arrays.fill(colors, empty);
		}
		return colors;
	}
	
	@Override
	public boolean isDone() {
		return System.currentTimeMillis() >= startTime + delay;
	}
}