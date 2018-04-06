package net.ximias.gui.tabs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import net.ximias.fileSearch.PsDirectoryLocator;
import net.ximias.gui.MainController;
import net.ximias.gui.ResizableCanvas;
import net.ximias.gui.tabs.keyboard.KeyboardEmulator;
import net.ximias.gui.tabs.keyboard.logitech.Logitech;
import net.ximias.peripheral.KeyEffect;
import net.ximias.peripheral.Keyboard;
import net.ximias.peripheral.KeyboardEffectContainer;
import net.ximias.peripheral.effects.KeymapColoring;
import net.ximias.peripheral.effects.WaveEffectDirection;
import net.ximias.peripheral.effects.WaveEffectProducer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class KeyboardTab {
	private MainController mainController;
	
	@FXML
	private AnchorPane keyboardTab;
	
	@FXML
	private AnchorPane keybindRoot;
	
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
	
	@FXML
	private CheckBox keybindEnable;
	
	@FXML
	private ChoiceBox<File> keybindFileSelect;
	
	@FXML
	private TableView<Map.Entry<String, Color>> actionColorTable;
	
	@FXML
	private ColorPicker actionColorSelection;
	
	private Canvas emulationCanvas = new ResizableCanvas();
	private KeyboardEmulator emulator;
	
	private Keyboard keyboard;
	private KeymapColoring keyColor;
	
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
		if (keyboard != null) {
			if (keyboard.getEffectContainer() != null) {
				keyboard.getEffectContainer().addEffect(effect);
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
		if (keyboard == null) {
			keyboard = new Logitech(mainController.getEffectContainer(), logitechPerKey.isSelected());
		}
		if (keyboard instanceof Logitech){
			if (logiEnable.isSelected()) {
				keyboard.enable();
			} else {
				keyboard.disable();
			}
		}
	}
	
	@FXML
	void logiPerKeyToggle(ActionEvent event) {
		if (keyboard != null) {
			keyboard.setMultiKey(logitechPerKey.isSelected());
		}
	}
	
	@FXML
	void colorSelected(ActionEvent event){
		
		ObservableList<Map.Entry<String, Color>> selectedItems = actionColorTable.getSelectionModel().getSelectedItems();
		
		int[] selected = actionColorTable.getSelectionModel().getSelectedIndices().stream().mapToInt(it->it.intValue()).toArray();
		for (Map.Entry<String, Color> stringColorEntry : selectedItems) {
			stringColorEntry.setValue(actionColorSelection.getValue());
		}
		actionColorTable.sort();
		actionColorTable.getSelectionModel().clearSelection();
		actionColorTable.getSelectionModel().selectIndices(selected[0], selected);
		toggleKeybinds(null);
	}
	
	@FXML
	void toggleKeybinds(ActionEvent event){
		if (keyboard == null || !keyboard.isMultiKey() || keybindFileSelect.getValue() == null || !keybindFileSelect.getValue().exists()) {
			keybindEnable.setSelected(false);
			return;
		}
		if (keybindEnable.isSelected()){
			keyColor = new KeymapColoring(keybindFileSelect.getValue());
			actionColorTable.setItems(FXCollections.observableArrayList(keyColor.getActionColorMap().entrySet()));
			actionColorTable.sort();
			keyboard.setAndExemptColors(keyColor.getKeyColors());
		}else {
			keyboard.resetExemptions();
			actionColorTable.setItems(FXCollections.emptyObservableList());
		}
	}
	
	
	public void injectMainController(MainController controller) {
		this.mainController = controller;
		emmulatorRoot.getChildren().add(emulationCanvas);
		bindSizes();
		emulator = new KeyboardEmulator(emulationCanvas, new KeyboardEffectContainer(controller.getEffectContainer(), 20, 6));
		PsDirectoryLocator psDirectoryLocator = PsDirectoryLocator.getInstance();
		Thread directoryLocatorThread = new Thread(psDirectoryLocator);
		directoryLocatorThread.setDaemon(true);
		directoryLocatorThread.start();
		psDirectoryLocator.onFinished(files -> keybindFileSelect.setItems(FXCollections.observableArrayList(files)));
		keybindFileSelect.valueProperty().addListener(observable -> toggleKeybinds(null));
		bindTableWiew();
		//emulator = new KeyboardEmulator(emulationCanvas, new KeyboardEffectContainer(controller.getEffectContainer(), 21,7));
	}
	
	private void bindSizes() {
		emulationCanvas.heightProperty().bind(emmulatorRoot.heightProperty());
		emulationCanvas.widthProperty().bind(emmulatorRoot.widthProperty());
		
		emmulatorRoot.setPrefHeight(10_000);
		keybindRoot.setPrefHeight(10_000);
		/*logitechRoot.setPrefHeight(1_000_000);
		razerRoot.setPrefHeight(1_000_000);
		steelseriesRoot.setPrefHeight(1_000_000);
		corsairRoot.setPrefHeight(1_000_000);*/
	}
	
	private void bindTableWiew() {
		actionColorTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		TableColumn<Map.Entry<String, Color>, String> actionColumn = (TableColumn<Map.Entry<String, Color>, String>) actionColorTable.getColumns().get(0);
		actionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));
		actionColorTable.getSortOrder().add(actionColumn);
		
		TableColumn<Map.Entry<String, Color>, String> colorColumn = (TableColumn<Map.Entry<String, Color>, String>) actionColorTable.getColumns().get(1);
		colorColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().toString()));
		
		colorColumn.setCellFactory(new Callback<TableColumn<Map.Entry<String, Color>, String>, TableCell<Map.Entry<String, Color>, String>>() {
			@Override
			public TableCell<Map.Entry<String, Color>, String> call(TableColumn<Map.Entry<String, Color>, String> param) {
				return new TableCell<Map.Entry<String, Color>, String>(){
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						
						if (item == null || empty) {
							setText(null);
							setStyle("");
						} else {
							setText(item);
							Color color = Color.valueOf(item);
							setBackground(new Background(new BackgroundFill(color,null, null)));
							setTextFill(color.getBrightness() > 0.5 ? Color.BLACK : Color.WHITE);
						}
					}
				};
			}
		});
	}
}
