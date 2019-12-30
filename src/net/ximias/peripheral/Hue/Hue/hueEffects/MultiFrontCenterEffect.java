package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Area;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import net.ximias.logging.Logger;


public class MultiFrontCenterEffect implements AreaEffectProducer {
	private int duration;
	private double opacityMultiplyer = 1;
	private SequenceAnimation r;
	private SequenceAnimation g;
	private SequenceAnimation b;
	private SequenceAnimation a;
	private final AreaEffectProducer[] effects;
	@SuppressWarnings("FieldCanBeLocal")
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public MultiFrontCenterEffect(AreaEffectProducer[] effects) {
		this.effects = effects;
		logger.effects().fine("Initializing MultiFrontEffect with size of: " + effects.length);
		
		calculateValues(effects);
		for (AreaEffectProducer effect : effects) {
			duration += effect.getDuration();
		}
	}
	
	private void calculateValues(AreaEffectProducer[] effects) {
		r = new SequenceAnimation();
		g = new SequenceAnimation();
		b = new SequenceAnimation();
		a = new SequenceAnimation();
		for (int i = 0; i < effects.length; i++) {
			effects[i].setOpacity(opacityMultiplyer);
			AreaEffect effect = (AreaEffect) effects[i].getEffect();
			r.append(effect.getRedAnimation(), "r" + i);
			g.append(effect.getGreenAnimation(), "g" + i);
			b.append(effect.getBlueAnimation(), "b" + i);
			a.append(effect.getOpacityAnimation(), "a" + i);
		}
	}
	
	@Override
	public Effect getEffect() {
		AreaEffect effect = new AreaEffect();
		
		effect.setColorAnimation(r, g, b);
		effect.setOpacityAnimation(a);
		effect.setArea(Area.Predefine.Front);
		return effect;
	}
	
	@Override
	public void setOpacity(double opacity) {
		opacityMultiplyer = opacity;
		calculateValues(effects);
	}
	
	@Override
	public int getDuration() {
		return duration;
	}
}
