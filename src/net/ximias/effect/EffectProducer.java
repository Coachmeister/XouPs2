package net.ximias.effect;

import javafx.scene.paint.Color;
import net.ximias.fileParser.JsonSerializable;

import java.util.ArrayList;

public abstract class EffectProducer extends JsonSerializable{
	private ArrayList<PeripheralEffectProducer> peripheralEffects = new ArrayList<>(6);
	private String name;
	
	public abstract Effect build();
	
	public String getName() {
		return name;
	}
	
	public abstract void setColor(Color color);
	
	public void attachPeripheralEffect(PeripheralEffectProducer effectProducer){
		peripheralEffects.add(effectProducer);
	}
	
	/**
	 * Used to obtain the first peripheralEffect assignable from a specific class.
	 * Will return {@code null} if none is found.
	 * @param superclass the class assignable to a peripheralEffect.
	 * @param <T> final type of the class.
	 * @return the first occurence of a peripheralEffectProducer assignable from the given class.
	 */
	public <T extends PeripheralEffectProducer> T getPeripheralEffectProducerBySuperclass(Class<T> superclass){
		for (PeripheralEffectProducer peripheralEffect : peripheralEffects) {
			if (peripheralEffect.getClass().isAssignableFrom(superclass)){
				return superclass.cast(peripheralEffect);
			}
		}
		return null;
	}
}
