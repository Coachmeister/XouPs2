package net.ximias.effect.producers;

import javafx.scene.paint.Color;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectProducer;
import net.ximias.fileParser.Initializer;
import org.json.JSONArray;
import org.json.JSONObject;

public class MultiEffectProducer extends EffectProducer {
	private final EffectProducer[] effects;
	
	public MultiEffectProducer(String name, EffectProducer... effects) {
		this.name = name;
		this.effects = effects;
	}
	
	public MultiEffectProducer(JSONObject json) {
		this(json.getString("name"), getEffectsFromNames(json));
	}
	
	private static EffectProducer[] getEffectsFromNames(JSONObject json) {
		JSONArray jsonEffects = json.getJSONArray("effects");
		EffectProducer[] producers = new EffectProducer[jsonEffects.length()];
		for (int i = 0; i < jsonEffects.length(); i++) {
			producers[i] = Initializer.effectProducerFromNameWhileInit(jsonEffects.getString(i));
		}
		return producers;
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("name", name);
		ret.put("type", "Multi");
		JSONArray JSONEffects = new JSONArray();
		for (EffectProducer effect : effects) {
			JSONEffects.put(effect.getName());
		}
		ret.put("effects", JSONEffects);
		return ret;
	}
	
	public EffectProducer[] getContainedEffects() {
		return effects;
	}
	
	@Override
	public Effect build() {
		return new MultiEffect(effects, this);
	}
	
	@Override
	public void setColor(Color color) {
		for (EffectProducer effect : effects) {
			effect.setColor(color);
		}
	}
}

class MultiEffect implements Effect {
	private final EffectProducer[] effects;
	private Effect current;
	private int count;
	private final EffectProducer parent;
	
	public MultiEffect(EffectProducer[] effects, EffectProducer parent) {
		this.effects = effects;
		this.parent = parent;
		count = 0;
		current = effects[0].build();
	}
	
	@Override
	public Color getColor() {
		if (current.isDone() && count != effects.length - 1) {
			current = effects[++count].build();
		}
		return current.getColor();
	}
	
	@Override
	public boolean isDone() {
		return count == effects.length - 1 && current.isDone();
	}
	
	@Override
	public boolean hasIntensity() {
		return true;
	}
	
	@Override
	public EffectProducer getProducer() {
		return parent;
	}
}
