package net.ximias.peripheral.keyboard.hardware;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import net.ximias.effect.Renderer;
import net.ximias.effect.views.EffectContainer;
import net.ximias.peripheral.keyboard.KeyboardEffectContainer;

import java.util.Map;

/**
 * Used to emulate a w*h sized hardware on screen.
 * Mustly used for debugging and preview purposes.
 */
public class KeyboardEmulator extends AbstractKeyboard implements Renderer {
	
	private static final int SPACING = 5;
	private final Canvas canvas;
	private final KeyboardEffectContainer effectContainer;
	private final int rows;
	private final int cols;
	
	public KeyboardEmulator(Canvas canvas, EffectContainer container, int rows, int columns) {
		super(container);
		this.canvas = canvas;
		this.rows = rows;
		this.cols = columns;
		this.effectContainer = new KeyboardEffectContainer(container, rows, cols);
	}
	
	private final AnimationTimer animationTimer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			animateFrame();
		}
	};
	
	/**
	 * Called every frame of animation.
	 */
	private void animateFrame() {
		GraphicsContext ctx = canvas.getGraphicsContext2D();
		ctx.setFill(Color.BLACK);
		ctx.fillRect(0,0,canvas.getWidth(), canvas.getHeight());
		Color[][] perKey = effectContainer.getPerKeyColor();
		int width, height;
		width = getWidthOfSquare(perKey.length);
		height = getHeightOfSquare(perKey[0].length);
		
		for (int row = 0; row < perKey[0].length; row++) {
			for (int column = 0; column < perKey.length; column++) {
				Color color = perKey[column][row];
				ctx.setFill(color);
				ctx.fillRect(SPACING/2.0+column*(width+SPACING), SPACING/2.0+row*(height+SPACING),width, height);
			}
		}
	}
	
	/**
	 * Called when emulator is stopped.
	 * Used for visual purposes.
	 */
	private void insertStoppedFrame(){
		GraphicsContext ctx = canvas.getGraphicsContext2D();
		ctx.applyEffect(new ColorAdjust(0,-1,-0.5,0));
	}
	
	/**
	 * Starts the emulator.
	 */
	private void start(){
		animationTimer.start();
	}
	
	/**
	 * Stops the emulator.
	 */
	public void stop(){
		animationTimer.stop();
		insertStoppedFrame();
	}
	
	/**
	 * Utility function for calculating the with of a single key.
	 * @param numWidth the number of keys to fit in the width of the canvas.
	 * @return the size in pixels of the with of the key.
	 */
	private int getWidthOfSquare(int numWidth){
		return (int) Math.floor(canvas.getWidth()/numWidth - SPACING);
	}
	
	/**
	 * Utility function for calculating the height of a single key.
	 * @param numHeight the number of keys to fit in the height of the canvas.
	 * @return the size in pixels of the height of the key.
	 */
	private int getHeightOfSquare(int numHeight){
		return (int) Math.floor(canvas.getHeight()/numHeight - SPACING);
	}
	
	@Override
	public void resumeRendering() {
		start();
	}
	
	@Override
	public int getRows() {
		return rows;
	}
	
	@Override
	public int getColumns() {
		return cols;
	}
	
	@Override
	public void setAndExemptColors(Map<String, Color> keyColorMap) { }
	
	@Override
	public void enable() { start(); }
	
	@Override
	public void disable() { stop(); }
	
	public KeyboardEffectContainer getEffectContainer() {
		return effectContainer;
	}
	
	@Override
	public void resetExemptions() { }
	
	@Override
	public void setMultiKey(boolean enableMultiKey) { }
	
	@Override
	public boolean isMultiKey() {
		return true;
	}
}
