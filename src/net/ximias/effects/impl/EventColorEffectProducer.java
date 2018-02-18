package net.ximias.effects.impl;


import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;
import net.ximias.fileParser.JsonSerializable;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;

/**
 * Modifiable color effect.
 * Needs work. The idea is to use it for background lighting.
 * Should be changeable and removable on event.
 * Should only return an effect on build, when no effect is showing with the same name.
 * Weak references!
 */
public class EventColorEffectProducer extends JsonSerializable implements EffectProducer{
	private static final HashMap<String, WeakReference<EventColorEffect>> eventEffects = new HashMap<>(12);
	
	private String name;
	private Color color;
	
	public EventColorEffectProducer(Color color, String name){
		this.name = name;
		this.color = color;
	}
	
	public EventColorEffectProducer(JSONObject data) {
		this(Color.valueOf(data.getString("color")), data.getString("name"));
	}
	
	public void clearEffect(){
		if (eventEffects.get(name) != null) {
			EventColorEffect ec = eventEffects.get(name).get();
			if (ec != null) {
				ec.setDone();
			}
		}
	}
	
	@Override
	public Effect build() {
		
		WeakReference<EventColorEffect> wec = eventEffects.get(name);
		EventColorEffect ec = null;
		if (wec != null) {
			ec = wec.get();
		}
		
		if (ec == null) {
			ec = new EventColorEffect(color);
			eventEffects.put(name,new WeakReference<>(ec));
		}else{
			ec.setColor(color);
		}
		
		return ec;
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>();
		h.put("color", color.toString());
		h.put("name", name);
		return h;
	}
}

class EventColorEffect implements Effect {
	private boolean done = false;
	private Color color;

	EventColorEffect(Color color_javafx) {
		color = color_javafx;
	}
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	public void setColor(Color color){
		this.color = color;
	}
	
	public void setDone() {
		this.done= true;
	}
}
