package net.ximias.peripheral.keyboard.hardware;

import net.ximias.effect.Effect;
import net.ximias.effect.EffectAddListener;
import net.ximias.effect.views.EffectContainer;
import net.ximias.peripheral.keyboard.KeyEffectProducer;
import net.ximias.peripheral.keyboard.Keyboard;

import java.util.List;
import net.ximias.logging.Logger;


public abstract class AbstractKeyboard implements EffectAddListener, Keyboard {
	private final Logger logger = Logger.getLogger(getClass().getName());
	protected AbstractKeyboard(EffectContainer globalEffectContainer) {
		globalEffectContainer.addEffectAddListener(this);
	}
	
	@Override
	public void onEffectAdded(Effect effect) {
		List<? extends KeyEffectProducer> attached = effect.getProducer().getAllPeripheralEffectProducersBySuperclass(getPeripheralEffectType());
		logger.effects().fine("Looking for attached effects in: "+effect.getClass().getSimpleName());
		if (attached == null||!isMultiKey()) {
			logger.effects().fine("no attached effects found in: "+effect.getName());
			return;
		}
		logger.effects().info("Adding peripheral effect(s) found in: "+effect.getName());
		attached.forEach(it->getEffectContainer().addEffect(it.build()));
	}
	
	protected Class<? extends KeyEffectProducer> getPeripheralEffectType(){
		return KeyEffectProducer.class;
	}
}
