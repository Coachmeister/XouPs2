package net.ximias.effect.producers;

import javafx.scene.paint.Color;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectProducer;
import net.ximias.effect.FixedEffect;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A timed color effect. Should probably be renamed. See EventColorEffect doc.
 */
public class TimedEffectProducer extends EffectProducer {
	private final long duration;
	private Color color;
	
	public TimedEffectProducer(Color color_javafx, long duration_milliseconds){
		duration = duration_milliseconds;
		color = color_javafx;
	}
	
	public TimedEffectProducer(JSONObject data) {
		this(Color.valueOf(data.getString("color")),data.getLong("duration"));
	}
	
	@Override
	public Effect build() {
		return new TimedColorEffect(duration, color, this);
	}
	
	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(4);
		h.put("duration", String.valueOf(duration));
		h.put("color", color.toString());
		return h;
	}
	
	public int getDuration() {
		return (int) duration;
	}
}
class TimedColorEffect implements FixedEffect{
	private final long startTime;
	private final long duration;
	protected final Color color;
	private final EffectProducer parent;
	
	TimedColorEffect(long duration_milliseconds, Color color_javafx, EffectProducer parent){
		this.parent = parent;
		startTime = System.currentTimeMillis();
		duration = duration_milliseconds;
		color = color_javafx;
	}
	
	public boolean isDone(){
		return System.currentTimeMillis()-startTime > duration;
	}
	
	@Override
	public boolean hasIntensity() {
		return true;
	}
	
	@Override
	public EffectProducer getProducer() {
		return parent;
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public long getRemainingTime() {
		return System.currentTimeMillis()-startTime;
	}
}
