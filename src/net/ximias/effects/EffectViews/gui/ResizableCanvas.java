package net.ximias.effects.EffectViews.gui;


import javafx.scene.canvas.Canvas;

public class ResizableCanvas extends Canvas {
	
	@Override
	public double prefWidth(double height) {
		return 0;
	}
	
	@Override
	public double prefHeight(double width) {
		return 0;
	}
}
