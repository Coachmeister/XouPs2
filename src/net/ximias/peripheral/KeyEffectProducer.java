package net.ximias.peripheral;

import javafx.scene.paint.Color;
import net.ximias.effect.PeripheralEffectProducer;

public interface KeyEffectProducer extends PeripheralEffectProducer {
	KeyEffect build();
	
	void setColor(Color color);
}
