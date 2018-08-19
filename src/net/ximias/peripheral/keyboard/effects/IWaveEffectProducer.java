package net.ximias.peripheral.keyboard.effects;

import net.ximias.peripheral.keyboard.KeyEffect;
import net.ximias.peripheral.keyboard.KeyEffectProducer;

public interface IWaveEffectProducer extends KeyEffectProducer {
	@Override
	WaveEffect build();
}
