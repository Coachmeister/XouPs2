package net.ximias.peripheral.keyboard;

import javafx.scene.paint.Color;
import net.ximias.peripheral.PeripheralEffectProducer;

public interface KeyEffectProducer extends PeripheralEffectProducer {
	KeyEffect build();
	
	void setColor(Color color);
}
