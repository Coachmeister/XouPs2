package net.ximias.peripheral.keyboard.effects;

import javafx.scene.paint.Color;
import net.ximias.peripheral.keyboard.KeyEffect;
import net.ximias.peripheral.keyboard.KeyEffectProducer;

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
	
	class MulticoloredWaveEffect extends WaveEffect{
		private final Color[] colors;
		MulticoloredWaveEffect(int duration, WaveEffectDirection direction, Color[] colors) {
			super(colors[0], duration, colors.length, direction);
			this.colors = colors;
		}
		
		@Override
		protected Color[][] getHorizontalColors(int width, int height){
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
				Arrays.fill(column, colors[i-startLocation]);
				destination[i] = column;
			}
		}
		
		void fillColors(Color[] destination, int startLocation, int endLocation){
			for (int i = startLocation; i < endLocation; i++) {
				destination[i] = colors[i-startLocation];
			}
		}
		
		@Override
		protected Color[][] getVerticalColors(int width, int height) {
			Color[][] result = new Color[width][height];
			Color[] column = new Color[height];
			Arrays.fill(column,Color.TRANSPARENT);
			
			int startLocation = getStartHorizontalLocation(height);
			int endLocation = startLocation+getEffectWidth();
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
					double distanceFromCenter = dist(width / 2.0, height / 2.0, x+0.5, y+0.5);
					if (distanceFromCenter <= outerRadius && distanceFromCenter >= innerRadius){
						result[x][y] = colors[(int) Math.floor(outerRadius-distanceFromCenter)];
					}else{
						result[x][y] = Color.TRANSPARENT;
					}
				}
			}
			
			return result;
		}
	}
}
