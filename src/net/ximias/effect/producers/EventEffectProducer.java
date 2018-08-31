package net.ximias.effect.producers;


import javafx.scene.paint.Color;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectProducer;
import net.ximias.effect.FixedEffect;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import net.ximias.logging.Logger;


/**
 * Modifiable color effect.
 * Needs work. The idea is to use it for background lighting.
 * Should be changeable and removable on event.
 * Should only return an effect on build, when no effect is showing with the same name.
 * Weak references!
 */
public class EventEffectProducer extends EffectProducer{
	private static final HashMap<String, WeakReference<EventColorEffect>> eventEffects = new HashMap<>(12);
	@SuppressWarnings("FieldCanBeLocal")
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final String name;
	private Color color;
	
	public EventEffectProducer(Color color, String name){
		this.name = name;
		this.color = color;
		if (color.getOpacity()==0) {
			logger.effects().severe("This effect producer is fully transparent!");
			//throw new Error("Event effects are backgrounds and shouldn't be fully transparent");
		}
	}
	
	public EventEffectProducer(JSONObject data) {
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
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public Effect build() {
		
		WeakReference<EventColorEffect> wec = eventEffects.get(name);
		EventColorEffect ec = null;
		if (wec != null) {
			ec = wec.get();
		}
		
		if (ec == null) {
			ec = new EventColorEffect(color, name,this);
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

class EventColorEffect implements FixedEffect {
	private boolean done = false;
	private Color color;
	private final String name;
	private final EffectProducer parent;
	EventColorEffect(Color color_javafx, String name, EffectProducer parent) {
		color = color_javafx;
		this.parent = parent;
		this.name = name;
	}
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	@Override
	public boolean hasIntensity() {
		return false;
	}
	
	public void setColor(Color color){
		this.color = color;
	}
	
	public void setDone() {
		this.done= true;
	}
	
	@Override
	public String getName() {
		return "Constant color. Name: "+name;
	}
	
	@Override
	public EffectProducer getProducer() {
		return parent;
	}
	
	@Override
	public long getRemainingTime() {
		return Long.MAX_VALUE;
	}
}
