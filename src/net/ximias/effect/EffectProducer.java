package net.ximias.effect;

import javafx.scene.paint.Color;
import net.ximias.fileParser.JsonSerializable;

public abstract class EffectProducer extends JsonSerializable{
	private String name;
	
	public abstract Effect build();
	
	public String getName() {
		return name;
	}
	
	public abstract void setColor(Color color);
}
