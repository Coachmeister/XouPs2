package net.ximias.persistence;

import javafx.scene.paint.Color;

import java.io.Serializable;

public class PersistColor implements Serializable {
	private double r,g,b,a;
	
	public PersistColor(double r, double g, double b, double a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	public static PersistColor getPersistColor(Color color){
		return new PersistColor(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
	}
	public Color toColor(){
		return new Color(r,g,b,a);
	}
}
