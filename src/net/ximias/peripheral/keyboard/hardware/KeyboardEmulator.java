package net.ximias.peripheral.keyboard.hardware;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import net.ximias.effect.Renderer;
import net.ximias.peripheral.keyboard.KeyboardEffectContainer;

/**
 * Used to emulate a w*h sized hardware on screen.
 * Mustly used for debugging and preview purposes.
 */
public class KeyboardEmulator implements Renderer {
	
	private static final int SPACING = 5;
	private Canvas canvas;
	private KeyboardEffectContainer effectContainer;
	private boolean isEnabled = false;
	
	public KeyboardEmulator(Canvas canvas, KeyboardEffectContainer container) {
		this.canvas = canvas;
		this.effectContainer = container;
	}
	
	AnimationTimer animationTimer = new AnimationTimer() {
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
				ctx.fillRect(SPACING/2+column*(width+SPACING), SPACING/2+row*(height+SPACING),width, height);
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
		isEnabled = true;
		animationTimer.start();
	}
	
	/**
	 * Stops the emulator.
	 */
	public void stop(){
		isEnabled = false;
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
	
	public KeyboardEffectContainer getEffectContainer() {
		return effectContainer;
	}
}
