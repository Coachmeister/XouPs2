package net.ximias.peripheral.keyboard.hardware;

import net.ximias.effect.Effect;
import net.ximias.effect.EffectAddListener;
import net.ximias.effect.views.EffectContainer;
import net.ximias.peripheral.keyboard.KeyEffectProducer;
import net.ximias.peripheral.keyboard.Keyboard;

import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractKeyboard implements EffectAddListener, Keyboard {
	private Logger logger = Logger.getLogger(getClass().getName());
	public AbstractKeyboard(EffectContainer globalEffectContainer) {
		globalEffectContainer.addEffectAddListener(this);
	}
	
	@Override
	public void onEffectAdded(Effect effect) {
		List<? extends KeyEffectProducer> attached = effect.getProducer().getAllPeripheralEffectProducersBySuperclass(getPeripheralEffectType());
		logger.warning("Looking for attached effects in: "+effect.getClass().getSimpleName());
		if (attached == null||!isMultiKey()) {
			logger.warning("no effects found");
			return;
		}
		logger.warning("Attached effect(s) found!");
		attached.forEach(it->getEffectContainer().addEffect(it.build()));
	}
	
	protected Class<? extends KeyEffectProducer> getPeripheralEffectType(){
		return KeyEffectProducer.class;
	}
}
