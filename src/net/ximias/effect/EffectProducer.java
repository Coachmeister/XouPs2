package net.ximias.effect;

import javafx.scene.paint.Color;
import net.ximias.fileParser.JsonSerializable;
import net.ximias.peripheral.PeripheralEffectProducer;
import net.ximias.peripheral.keyboard.KeyEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class EffectProducer extends JsonSerializable{
	private ArrayList<PeripheralEffectProducer> peripheralEffects = new ArrayList<>(6);
	private String name;
	private Logger logger = Logger.getLogger(getClass().getName());
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
			logger.warning("Attached effect: "+peripheralEffect.getClass().getName()+" In effect "+getName());
			if (superclass.isAssignableFrom(peripheralEffect.getClass())){
				logger.warning("peripheral effect did match: "+superclass.getName());
				return superclass.cast(peripheralEffect);
			}
			logger.warning("Did not match query of: "+superclass.getName());
		}
		return null;
	}
	
	/**
	 * Used to obtain a list of all peripheralEffects assignable from a superclass.
	 * Will return an empty list, if none are found.
	 * @param superclass the class assignable from peripharalEffect. Fx. {@link KeyEffect}
	 * @param <T> The type if the class.
	 * @return All peripheralEffectProducers subclassed to the provided argument, cast as the argument type.
	 */
	public <T extends PeripheralEffectProducer> List<T> getAllPeripheralEffectProducersBySuperclass(Class<T> superclass){
		return peripheralEffects.stream().filter(it->superclass.isAssignableFrom(it.getClass())).map(superclass::cast).collect(Collectors.toList());
	}
}
