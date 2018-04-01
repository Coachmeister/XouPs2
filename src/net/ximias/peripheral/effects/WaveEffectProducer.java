package net.ximias.peripheral.effects;

import javafx.scene.paint.Color;
import net.ximias.peripheral.KeyEffect;
import net.ximias.peripheral.KeyEffectProducer;

import java.util.Arrays;

public class WaveEffectProducer implements KeyEffectProducer {
	private Color color;
	private long duration;
	private int effectWidth;
	private WaveEffectDirection direction;
	
	public WaveEffectProducer(Color color, long duration, int effectWidth, WaveEffectDirection direction) {
		this.color = color;
		this.duration = duration;
		this.effectWidth = effectWidth;
		this.direction = direction;
	}
	
	@Override
	public KeyEffect build() {
		return new WaveEffect(color,duration,effectWidth, direction);
	}
	
	@Override
	public void setColor(Color color) {
		this.color = color;
	}
}

class WaveEffect implements KeyEffect{
	private Color color;
	private long duration;
	private int effectWidth;
	private long startTime;
	private WaveEffectDirection direction;
	
	WaveEffect(Color color, long duration, int effectWidth, WaveEffectDirection direction) {
		this.color = color;
		this.duration = duration;
		this.effectWidth = effectWidth;
		this.direction = direction;
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public boolean isDone() {
		return System.currentTimeMillis() >= startTime+duration;
	}
	@Override
	public Color[][] getKeyColors(int width, int height) {
		switch (direction) {
			case LEFT_TO_RIGHT:
			case RIGHT_TO_LEFT:
				return getHorizontalColors(width, height);
			case UP_TO_DOWN:
			case DOWN_TO_UP:
				return getVerticalColors(width, height);
			default:
				return getCircularColors(width, height);
		}
	}
	
	private Color[][] getHorizontalColors(int width, int height){
		Color[][] result = new Color[width][height];
		Color[] empty = new Color[height];
		Arrays.fill(empty, Color.TRANSPARENT);
		Color[] colored = new Color[height];
		Arrays.fill(colored, color);
		Arrays.fill(result, empty);
		
		int start = getStartVerticalLocation(width);
		int end = Math.min(start + effectWidth, width);
		start = Math.max(start, 0);
		
		Arrays.fill(result, start, end, colored);
		return result;
	}
	
	private Color[][] getVerticalColors(int width, int height) {
		Color[][] result = new Color[width][height];
		Color[] column = new Color[height];
		Arrays.fill(column,Color.TRANSPARENT);
		
		int startLocation = getStartHorizontalLocation(height);
		int endLocation = startLocation+effectWidth;
		endLocation = Math.min(endLocation, height);
		startLocation = Math.max(startLocation, 0);
		Arrays.fill(column, startLocation, endLocation, color);
		
		Arrays.fill(result, column);
		return result;
	}
	
	private Color[][] getCircularColors(int width, int height) {
		Color[][] result = new Color[width][height];
		double outerRadius = getStartRadius(width, height);
		double innerRadius = outerRadius - effectWidth;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double distanceFromCenter = dist(width / 2.0, height / 2.0, x+0.5, y+0.5);
				if (distanceFromCenter <= outerRadius && distanceFromCenter >= innerRadius){
					result[x][y] = color;
				}else{
					result[x][y] = Color.TRANSPARENT;
				}
			}
		}
		
		return result;
	}
	
	private int getStartVerticalLocation(int width){
		return (int) Math.floor(mapProgress(getProgress(), -effectWidth,width));
	}
	
	private int getStartHorizontalLocation(int height){
		return (int) Math.floor(mapProgress(getProgress(), -effectWidth, height));
	}
	
	private double getStartRadius(int width, int height){
		return mapProgress(getProgress(), 0, (int) (Math.ceil(dist(0,0, width / 2, height / 2)) + effectWidth + 1));
	}
	
	
	private double getProgress() {
		double progress;
		if (direction == WaveEffectDirection.LEFT_TO_RIGHT || direction == WaveEffectDirection.UP_TO_DOWN ||direction == WaveEffectDirection.CENTER_OUT){
			progress = Math.min(((double)getCurrentDuration()) / (duration), 1);
		}else{
			progress = Math.min( ((double) duration-getCurrentDuration())/duration, 1);

		}
		return progress;
	}
	
	private long getCurrentDuration() {
		return System.currentTimeMillis()-startTime;
	}
	
	private double mapProgress(double progress, int start, int end){
		double delta = end-start;
		return (progress * delta + start);
	}
	
	private double dist(double x1, double y1, double x2, double y2){
		return (Math.sqrt(sqr(x2 - x1) + sqr(y2 - y1)));
	}
	
	private double sqr(double a){
		return a*a;
	}
}
