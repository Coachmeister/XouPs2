package net.ximias.gui.tabs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import net.ximias.gui.MainController;
import net.ximias.gui.ResizableCanvas;
import net.ximias.gui.tabs.keyboard.KeyboardEmulator;
import net.ximias.peripheral.KeyboardEffectContainer;


public class Keyboard {
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
	private AnchorPane razerRoot;
	
	@FXML
	private AnchorPane steelseriesRoot;
	
	@FXML
	private AnchorPane corsairRoot;
	
	private Canvas emulationCanvas = new ResizableCanvas();
	private KeyboardEmulator emulator;
	
	@FXML
	void addWave(ActionEvent event) {
		emulator.addWave();
	}
	
	@FXML
	void emulatorEnable(ActionEvent event) {
		if (emulatorEnableBox.isSelected()){
			emulator.resumeRendering();
		}else{
			emulator.stop();
		}
	}
	
	
	public void injectMainController(MainController controller){
		this.mainController = controller;
		emmulatorRoot.getChildren().add(emulationCanvas);
		bindSizes();
		emulator = new KeyboardEmulator(emulationCanvas, new KeyboardEffectContainer(controller.getEffectContainer(), 6,20));
		//emulator = new KeyboardEmulator(emulationCanvas, new KeyboardEffectContainer(controller.getEffectContainer(), 7,21));
	}
	
	private void bindSizes() {
		emulationCanvas.heightProperty().bind(emmulatorRoot.heightProperty());
		emulationCanvas.widthProperty().bind(emmulatorRoot.widthProperty());
		
		emmulatorRoot.setPrefHeight(1_000_000);
		logitechRoot.setPrefHeight(1_000_000);
		razerRoot.setPrefHeight(1_000_000);
		steelseriesRoot.setPrefHeight(1_000_000);
		corsairRoot.setPrefHeight(1_000_000);
	}
}
