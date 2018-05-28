package net.ximias.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Area;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;

public class MultiFrontCenterEffect implements AreaEffectProducer {
	private int duration;
	private double opacityMultiplyer =1;
	private SequenceAnimation r;
	private SequenceAnimation g;
	private SequenceAnimation b;
	private SequenceAnimation a;
	private AreaEffectProducer[] effects;
	
	public MultiFrontCenterEffect(AreaEffectProducer[] effects) {
		this.effects = effects;
		r = new SequenceAnimation();
		g = new SequenceAnimation();
		b = new SequenceAnimation();
		a = new SequenceAnimation();
		
		calculateValues(effects);
		for (AreaEffectProducer effect : effects) {
			duration += effect.getDuration();
		}
	}
	
	private void calculateValues(AreaEffectProducer[] effects) {
		for (int i = 0; i < effects.length; i++) {
			effects[i].adjustOpacity(opacityMultiplyer);
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
		effect.setArea(Area.Predefine.Front);
		effect.setColorAnimation(r, g, b);
		effect.setOpacityAnimation(a);
		return effect;
	}
	
	@Override
	public void adjustOpacity(double opacity) {
		opacityMultiplyer = opacity;
		calculateValues(effects);
	}
	
	@Override
	public int getDuration() {
		return duration;
	}
}
