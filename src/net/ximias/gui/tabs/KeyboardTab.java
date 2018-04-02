package net.ximias.gui.tabs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import net.ximias.gui.MainController;
import net.ximias.gui.ResizableCanvas;
import net.ximias.gui.tabs.keyboard.KeyboardEmulator;
import net.ximias.gui.tabs.keyboard.logitech.Logitech;
import net.ximias.peripheral.KeyEffect;
import net.ximias.peripheral.KeyboardEffectContainer;
import net.ximias.peripheral.effects.WaveEffectDirection;
import net.ximias.peripheral.effects.WaveEffectProducer;


public class KeyboardTab {
	private MainController mainController;
	
	@FXML
	private AnchorPane keyboardTab;
	
	@FXML
	private AnchorPane emmulatorRoot;
	
	@FXML
	private CheckBox emulatorEnableBox;
	
	@FXML
	private AnchorPane logitechRoot;
	
	@FXML
	private CheckBox logiEnable;
	
	@FXML
	private CheckBox logitechPerKey;
	
	@FXML
	private AnchorPane razerRoot;
	
	@FXML
	private AnchorPane steelseriesRoot;
	
	@FXML
	private AnchorPane corsairRoot;
	
	@FXML
	private CheckBox reverseEffectDir;
	
	private Canvas emulationCanvas = new ResizableCanvas();
	private KeyboardEmulator emulator;
	
	private Logitech logitech;
	
	@FXML
	void addHorizontalWave(ActionEvent event) {
		addEffect(reverseEffectDir.isSelected()? WaveEffectDirection.RIGHT_TO_LEFT : WaveEffectDirection.LEFT_TO_RIGHT);
	}
	
	@FXML
	void addVerticalWave(ActionEvent event){
		addEffect(reverseEffectDir.isSelected() ? WaveEffectDirection.DOWN_TO_UP : WaveEffectDirection.UP_TO_DOWN);
	}
	
	@FXML
	void addRipple(){
		addEffect(reverseEffectDir.isSelected() ? WaveEffectDirection.OUT_CENTER : WaveEffectDirection.CENTER_OUT);
	}
	
	private void addEffect(WaveEffectDirection direction){
		Color rnd = new Color(Math.random() > 0.5 ? 0.0 : 1.0, Math.random() > 0.5 ? 0.0 : 1.0, Math.random() > 0.5 ? 0.0 : 1.0, 1);
		KeyEffect effect = new WaveEffectProducer(rnd, 10_000L, 4, direction).build();
		if (logitech != null) {
			if (logitech.getEffectContainer() != null) {
				logitech.getEffectContainer().addEffect(effect);
			}
		}
		if (emulator != null) {
			emulator.getEffectContainer().addEffect(effect);
		}
	}
	
	@FXML
	void emulatorEnable(ActionEvent event) {
		if (emulatorEnableBox.isSelected()) {
			emulator.resumeRendering();
		} else {
			emulator.stop();
		}
	}
	
	@FXML
	void logitechEnable(ActionEvent event) {
		if (logitech == null) {
			logitech = new Logitech(mainController.getEffectContainer(), logitechPerKey.isSelected());
		}
		if (logiEnable.isSelected()) {
			logitech.enable();
		} else {
			logitech.disable();
		}
	}
	
	@FXML
	void logiPerKeyToggle(ActionEvent event) {
		if (logitech != null) {
			logitech.setMultiKey(logitechPerKey.isSelected());
		}
	}
	
	
	public void injectMainController(MainController controller) {
		this.mainController = controller;
		emmulatorRoot.getChildren().add(emulationCanvas);
		bindSizes();
		emulator = new KeyboardEmulator(emulationCanvas, new KeyboardEffectContainer(controller.getEffectContainer(), 20, 6));
		//emulator = new KeyboardEmulator(emulationCanvas, new KeyboardEffectContainer(controller.getEffectContainer(), 21,7));
	}
	
	private void bindSizes() {
		emulationCanvas.heightProperty().bind(emmulatorRoot.heightProperty());
		emulationCanvas.widthProperty().bind(emmulatorRoot.widthProperty());
		
		emmulatorRoot.setPrefHeight(1_000_000);
		/*logitechRoot.setPrefHeight(1_000_000);
		razerRoot.setPrefHeight(1_000_000);
		steelseriesRoot.setPrefHeight(1_000_000);
		corsairRoot.setPrefHeight(1_000_000);*/
	}
}
