package net.ximias.peripheral.Hue.Hue;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Callback;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Entertainment;
import com.philips.lighting.hue.sdk.wrapper.entertainment.StartCallback;
import javafx.beans.property.SimpleBooleanProperty;
import net.ximias.peripheral.Hue.Hue.hueEffects.HueEffect;
import net.ximias.peripheral.Hue.Hue.hueEffects.GlobalConstantEffect;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectAddListener;
import net.ximias.effect.views.EffectContainer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class HueGameState implements EffectAddListener {
	private final Entertainment entertainment;
	private HueEffectWrapper effectWrapper = new HueEffectWrapper();
	private SimpleBooleanProperty running = new SimpleBooleanProperty(false);
	private Logger logger = Logger.getLogger(getClass().getName());
	private Timer cancellationTimer = new Timer(true);
	private EffectContainer effectContainer;
	
	public HueGameState(Entertainment entertainment, EffectContainer effectContainer) {
		this.entertainment = entertainment;
		this.effectContainer = effectContainer;
		startEntertainmentSystem();
	}
	
	private void startEntertainmentSystem() {
		entertainment.start(new StartCallback() {
			@Override
			public void handleCallback(StartStatus startStatus) {
				if (startStatus != StartStatus.Success) return;
				entertainmentStarted();
			}
		});
	}
	
	public void endGameState(){
		entertainment.stop(new Callback() {
			@Override
			public void handleCallback() {
				entertainmentStopped();
			}
		});
	}
	
	private void entertainmentStarted() {
		logger.warning("Entertainment game state started");
		running.set(true);
		effectContainer.addEffectAddListener(this);
	}
	
	private void entertainmentStopped() {
		logger.warning("Entertainment game state stopped");
		running.set(false);
		effectContainer.removeEffectListener(this);
	}
	
	@Override
	public void onEffectAdded(Effect effect) {
		logger.info("Effect received:"+effect.getName());
		if (!running.get()) return;
		
		HueEffect attached = effect.getProducer().getPeripheralEffectProducerBySuperclass(HueEffect.class);
		if (attached == null) {
			attached = effectWrapper.getAsHueEffect(effect);
		}
		if (attached instanceof GlobalConstantEffect){
			attached.adjustOpacity(1);
		}else{
			attached.adjustOpacity(effectContainer.getEffectIntensity());
		}
		com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect entertainmentEffect = attached.getEffect();
		synchronized (entertainment){
			entertainment.lockMixer();
			entertainment.addEffect(entertainmentEffect);
			entertainment.unlockMixer();
		}
		entertainmentEffect.enable();
		if (attached.getDuration()>0){
			cancellationTimer.schedule(cancellationTask(entertainmentEffect),attached.getDuration());
		}
	}
	
	private TimerTask cancellationTask(com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect entertainmentEffect) {
		return new TimerTask() {
			@Override
			public void run() {
				entertainmentEffect.disable();
				entertainmentEffect.finish();
			}
		};
	}
	
	public boolean isRunning() {
		return running.get();
	}
	
	public SimpleBooleanProperty runningProperty() {
		return running;
	}
}
