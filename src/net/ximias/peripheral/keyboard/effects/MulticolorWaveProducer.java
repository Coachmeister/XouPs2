package net.ximias.peripheral.keyboard.effects;

import javafx.scene.paint.Color;

import java.util.Arrays;

public class MulticolorWaveProducer implements IWaveEffectProducer {
	private final Color[] colors;
	private final int duration;
	private final WaveEffectDirection direction;
	
	public MulticolorWaveProducer(Color[] colors, int duration, WaveEffectDirection direction) {
		this.colors = colors;
		this.duration = duration;
		this.direction = direction;
	}
	
	@Override
	public WaveEffect build() {
		return new MulticoloredWaveEffect(duration, direction, colors);
	}
	
	@Override
	public void setColor(Color color) {
		colors[0] = color;
	}
	
	@SuppressWarnings("MismatchedReadAndWriteOfArray")
	class MulticoloredWaveEffect extends WaveEffect {
		private final Color[] colors;
		
		MulticoloredWaveEffect(int duration, WaveEffectDirection direction, Color[] colors) {
			super(colors[0], duration, colors.length, direction);
			this.colors = colors;
		}
		
		@Override
		protected Color[][] getHorizontalColors(int width, int height) {
			Color[][] result = new Color[width][height];
			Color[] empty = new Color[height];
			Arrays.fill(empty, Color.TRANSPARENT);
			Arrays.fill(result, empty);
			
			int start = getStartVerticalLocation(width);
			int end = Math.min(start + getEffectWidth(), width);
			start = Math.max(start, 0);
			
			fillColors(result, start, end);
			return result;
		}
		
		void fillColors(Color[][] destination, int startLocation, int endLocation) {
			for (int i = startLocation; i < endLocation; i++) {
				Color[] column = new Color[destination[i].length];
				Arrays.fill(column, colors[i - startLocation]);
				destination[i] = column;
			}
		}
		
		void fillColors(Color[] destination, int startLocation, int endLocation) {
			if (endLocation - startLocation >= 0) System.arraycopy(colors, 0, destination, startLocation, endLocation - startLocation);
		}
		
		@Override
		protected Color[][] getVerticalColors(int width, int height) {
			Color[][] result = new Color[width][height];
			Color[] column = new Color[height];
			Arrays.fill(column, Color.TRANSPARENT);
			
			int startLocation = getStartHorizontalLocation(height);
			int endLocation = startLocation + getEffectWidth();
			endLocation = Math.min(endLocation, height);
			startLocation = Math.max(startLocation, 0);
			fillColors(column, startLocation, endLocation);
			
			Arrays.fill(result, column);
			return result;
		}
		
		@Override
		protected Color[][] getCircularColors(int width, int height) {
			Color[][] result = new Color[width][height];
			double outerRadius = getStartRadius(width, height);
			double innerRadius = outerRadius - getEffectWidth();
			
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					double distanceFromCenter = dist(width / 2.0, height / 2.0, x + 0.5, y + 0.5);
					if (distanceFromCenter <= outerRadius && distanceFromCenter >= innerRadius) {
						result[x][y] = colors[(int) Math.min(Math.floor(outerRadius - distanceFromCenter), colors.length - 1)];
					} else {
						result[x][y] = Color.TRANSPARENT;
					}
				}
			}
			
			return result;
		}
	}
}
/*
Exception in thread "Logitech animation timer" java.lang.ArrayIndexOutOfBoundsException: 2
	at net.ximias.peripheral.keyboard.effects.MulticolorWaveProducer$MulticoloredWaveEffect.getCircularColors(MulticolorWaveProducer.java:89)
	at net.ximias.peripheral.keyboard.effects.WaveEffect.getKeyColors(WaveEffect.java:58)
	at net.ximias.peripheral.keyboard.effects.MultiKeyEffect.getKeyColors(MultiKeyEffectProducer.java:41)
	at net.ximias.peripheral.keyboard.KeyboardEffectContainer.getColorsFromAndClearEffects(KeyboardEffectContainer.java:67)
	at net.ximias.peripheral.keyboard.KeyboardEffectContainer.getPerKeyColor(KeyboardEffectContainer.java:52)
	at net.ximias.peripheral.keyboard.hardware.logitech.Logitech.getFormattedColorArray(Logitech.java:224)
	at net.ximias.peripheral.keyboard.hardware.logitech.Logitech.drawFrame(Logitech.java:174)
	at net.ximias.peripheral.keyboard.hardware.logitech.Logitech.access$200(Logitech.java:20)
	at net.ximias.peripheral.keyboard.hardware.logitech.Logitech$2.run(Logitech.java:253)
	at java.util.TimerThread.mainLoop(Timer.java:555)
	at java.util.TimerThread.run(Timer.java:505)
 */
