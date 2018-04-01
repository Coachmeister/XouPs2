package net.ximias.peripheral;

import javafx.scene.paint.Color;

public interface KeyEffectProducer {
	KeyEffect build();
	
	void setColor(Color color);
}
