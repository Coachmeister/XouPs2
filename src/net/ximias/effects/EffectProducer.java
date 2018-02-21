package net.ximias.effects;

import net.ximias.fileParser.JsonSerializable;

import java.util.HashMap;

public abstract class EffectProducer extends JsonSerializable{
	private String name;
	
	public abstract Effect build();
	
	public String getName() {
		return name;
	}
}
