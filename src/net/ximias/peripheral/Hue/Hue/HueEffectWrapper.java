package net.ximias.peripheral.Hue.Hue;


import javafx.scene.paint.Color;
import net.ximias.peripheral.Hue.Hue.hueEffects.HueEffect;
import net.ximias.peripheral.Hue.Hue.hueEffects.AreaEffectProducer;
import net.ximias.peripheral.Hue.Hue.hueEffects.FrontCenterEffect;
import net.ximias.peripheral.Hue.Hue.hueEffects.GlobalConstantEffect;
import net.ximias.peripheral.Hue.Hue.hueEffects.MultiFrontCenterEffect;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectProducer;
import net.ximias.effect.producers.BlendingEffectProducer;
import net.ximias.effect.producers.EventEffectProducer;
import net.ximias.effect.producers.MultiEffectProducer;
import net.ximias.effect.producers.TimedEffectProducer;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Wrapper for displaying the standard effects on the hue lights.
 */
public class HueEffectWrapper {
	Logger logger = Logger.getLogger(getClass().getName());
	
	public HueEffect getAsHueEffect(Effect effectToWrap){
		EffectProducer producer = effectToWrap.getProducer();
		
		if (producer instanceof BlendingEffectProducer){
			BlendingEffectProducer bProducer = ((BlendingEffectProducer) producer);
			return new FrontCenterEffect(bProducer.getStartColor(), bProducer.getEndColor(), bProducer.getDuration());
		}if (producer instanceof EventEffectProducer){
			return new GlobalConstantEffect(effectToWrap.getColor(), effectToWrap.getName());
		}if (producer instanceof TimedEffectProducer){
			return new FrontCenterEffect(effectToWrap.getColor(), effectToWrap.getColor(), ((TimedEffectProducer)producer).getDuration());
		}if (producer instanceof MultiEffectProducer){
			return handleMultiEffect((MultiEffectProducer) producer);
		}
		logger.severe("Effect not recognized: "+effectToWrap.getProducer().getClass().getSimpleName());
		return new FrontCenterEffect(Color.TRANSPARENT, Color.TRANSPARENT, 0);
	}
	
	private HueEffect handleMultiEffect(MultiEffectProducer producer) {
		logger.fine("Wrapping multi effect");
		MultiEffectProducer mEffect = producer;
		ArrayList<AreaEffectProducer> effectProducers = new ArrayList<>(mEffect.getEffects().length);
		
		for (EffectProducer effectProducer : mEffect.getEffects()) {
			effectProducers.add((AreaEffectProducer) getAsHueEffect(effectProducer.build()));
		}
		logger.fine("MultiEffect size: "+effectProducers.size());
		return new MultiFrontCenterEffect(effectProducers.toArray(new AreaEffectProducer[0]));
	}
	
}
