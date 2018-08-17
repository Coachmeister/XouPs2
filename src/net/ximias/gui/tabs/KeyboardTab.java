package net.ximias.gui.tabs;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import net.ximias.fileSearch.PsDirectoryLocator;
import net.ximias.gui.MainController;
import net.ximias.datastructures.gui.nodes.ResizableCanvas;
import net.ximias.datastructures.gui.nodes.SortedStringColorTable;
import net.ximias.peripheral.keyboard.hardware.KeyboardEmulator;
import net.ximias.peripheral.keyboard.hardware.logitech.Logitech;
import net.ximias.peripheral.keyboard.KeyEffect;
import net.ximias.peripheral.keyboard.Keyboard;
import net.ximias.peripheral.keyboard.effects.KeymapColoring;
import net.ximias.peripheral.keyboard.effects.WaveEffectDirection;
import net.ximias.peripheral.keyboard.effects.WaveEffectProducer;
import net.ximias.persistence.Persisted;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

/**
 * Tab containing the hardware specific settings.
 */
/*
Might need to be split into multiple, soon.
 */
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
	private ChoiceBox<String> addActionChoice;
	
	@FXML
	private ColorPicker addActionColor;
	
	@FXML
	private Button addActionButton;
	
	//private TableView<Map.Entry<String, Color>> actionColorTable;
	private SortedStringColorTable<Map.Entry<String, Color>> sortedActionColorTable = new SortedStringColorTable<>();
	
	@FXML
	private ColorPicker actionColorSelection;
	
	@FXML
	private Button removeSelectedButton;
	
	@FXML
	private HBox tableContainer;
	
	//private ProgressIndicator searchingProgress;
	
	@FXML
	private Button manualSearch;
	
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
	
	/**
	 * adds a waveEffect to the hardware and emulator.
	 * @param direction the direction of the wave.
	 */
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
	
	/**
	 * Called on action from the enable checkbox on the emulator pane.
	 * @param event the action event. May be null.
	 */
	@FXML
	void emulatorEnable(ActionEvent event) {
		if (emulatorEnableBox.isSelected()) {
			emulator.resumeRendering();
		} else {
			emulator.stop();
		}
	}
	
	/**
	 * Called on action from the enable checkbox on the logitech pane.
	 * @param event the action event. May be null.
	 */
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
	
	/**
	 * Called on action from the perkey checkbox in the logitech pane.
	 * @param event the action event. May be null.
	 */
	@FXML
	void logiPerKeyToggle(ActionEvent event) {
		if (keyboard != null) {
			keyboard.setMultiKey(logitechPerKey.isSelected());
		}
	}
	
	/**
	 * Called on action from the enable checkbox on the keybinds pane.
	 * @param event the action event. may be null.
	 */
	@FXML
	void loadKeys(ActionEvent event){
		if (keyboard == null || !keyboard.isMultiKey() || keybindFileSelect.getValue() == null || !keybindFileSelect.getValue().exists()) {
			keybindEnable.setSelected(false);
			setDisabled(true);
			return;
		}
		if (keybindEnable.isSelected()){
			setDisabled(false);
			keyColor = new KeymapColoring(keybindFileSelect.getValue());
			sortedActionColorTable.setItems(keyColor.getActionColorMap().entrySet());
			keyboard.setAndExemptColors(keyColor.getKeyColors());
			setupActionSelect();
		}else {
			setDisabled(true);
			keyboard.resetExemptions();
			sortedActionColorTable.clear();
			addActionChoice.getItems().clear();
		}
	}
	
	private void setDisabled(boolean disabled) {
		keybindRoot.setDisable(disabled);
	}
	
	/**
	 * Called on action from the colorpicker inside the keybonds pane.
	 * @param event the action event. May be null.
	 */
	@FXML
	void colorSelected(ActionEvent event){
		ObservableList<Map.Entry<String, Color>> selectedItems = sortedActionColorTable.getSelectionModel().getSelectedItems();
		
		sortedActionColorTable.saveSelection();
		for (Map.Entry<String, Color> stringColorEntry : selectedItems) {
			stringColorEntry.setValue(actionColorSelection.getValue());
		}
		sortedActionColorTable.restoreSelection();
		loadKeys(null);
	}
	
	/**
	 * Called on action from the restart search button.
	 * @param event the action event. may be null.
	 */
	@FXML
	public void restartSearch(ActionEvent event) {
		PsDirectoryLocator psDirectoryLocator = PsDirectoryLocator.getInstance();
		Thread directoryLocatorThread = new Thread(psDirectoryLocator);
		directoryLocatorThread.setDaemon(true);
		directoryLocatorThread.start();
		//searchingProgress.setVisible(true);
		keybindFileSelect.setDisable(true);
		manualSearch.setDisable(true);
		psDirectoryLocator.onFinished(files -> {
			Platform.runLater(()-> setPsDirectories(files));
			Persisted.getInstance().PLANETSIDE_INPUT_PROFILE.addAll(files);
		});
	}
	
	/**
	 * Used to set the directories and present it to the user.
	 * @param files the inputProfile files to present to the user.
	 */
	private void setPsDirectories(HashSet<File> files) {
		keybindFileSelect.setItems(FXCollections.observableArrayList(files));
		keybindFileSelect.setDisable(false);
		//searchingProgress.setVisible(false);
		manualSearch.setDisable(false);
		setDisabled(false);
		keybindFileSelect.getSelectionModel().select(Persisted.getInstance().LAST_SELECTED_INPUT_PROFILE);
		if (keybindFileSelect.getSelectionModel().getSelectedItem() == null) keybindFileSelect.getSelectionModel().selectFirst();
	}
	
	/**
	 * Called in action from the manual button.
	 * NOTE: the manual button is disables while an automatic search is in progress.
	 * @param event the action event. May be null.
	 */
	@FXML
	public void manualSearch(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Point me towards Planetside directory. I'll do the rest.");
		File directory = fileChooser.showOpenDialog(null);
		if (!directory.isDirectory()) directory = directory.getParentFile();
		PsDirectoryLocator psDirectoryLocator = PsDirectoryLocator.getInstance();
		keybindFileSelect.getItems().add(psDirectoryLocator.locateInSubDirectory(directory));
	}
	
	@FXML
	void removeSelections(ActionEvent event){
		if (!keybindEnable.isSelected()) return;
		keyColor.removeAll(sortedActionColorTable.getSelectionModel().getSelectedItems());
		sortedActionColorTable.setItems(keyColor.getActionColorMap().entrySet());
		sortedActionColorTable.getSelectionModel().selectFirst();
		loadKeys(null);
	}
	
	/**
	 * Called on action from the add button in the add action HBox.
	 * @param event the action event. May be null.
	 */
	@FXML
	void addAction(ActionEvent event){
		keyColor.addActionColor(addActionChoice.getValue(), addActionColor.getValue());
		sortedActionColorTable.setItems(keyColor.getActionColorMap().entrySet());
		setupActionSelect();
		loadKeys(null);
	}
	
	/**
	 * Sets the values of the add action choice box to all available actions not already in use.
	 */
	private void setupActionSelect() {
		addActionChoice.setItems(FXCollections.observableArrayList(keyColor.getUnusedActions()));
		addActionChoice.getItems().sort(String::compareTo);
	}
	
	/**
	 * used to inject the main controller into this tab.
	 * @param controller the main controller, containing this tab.
	 */
	public void injectMainController(MainController controller) {
		this.mainController = controller;
		emmulatorRoot.getChildren().add(emulationCanvas);
		bindSizes();
		emulator = new KeyboardEmulator(emulationCanvas,mainController.getEffectContainer(),20, 6);
		keybindFileSelect.valueProperty().addListener((observable,oldValue, newValue)-> {
			loadKeys(null);
			Persisted.getInstance().LAST_SELECTED_INPUT_PROFILE = newValue;		});
		HashSet<File> dirs = Persisted.getInstance().PLANETSIDE_INPUT_PROFILE;
		if (dirs.isEmpty()) restartSearch(null);
		else setPsDirectories(dirs);
		setupTableView();
		addActionChoice.valueProperty().addListener((observable, oldValue, newValue) -> addActionButton.setDisable(newValue==null));
		setDisabled(true);
		//emulator = new KeyboardEmulator(emulationCanvas, new KeyboardEffectContainer(controller.getEffectContainer(), 21,7));
	}
	
	/**
	 * used to bind the sizes of the canvas in the emulator and force the heights of the titled panes.
	 */
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
	
	/**
	 * Used to format the table view and tell it to behave itself.
	 */
	private void setupTableView() {
		tableContainer.getChildren().add(sortedActionColorTable);
		sortedActionColorTable.prefWidthProperty().bind(tableContainer.widthProperty());
		sortedActionColorTable.focusedProperty().addListener(disableOnLostFocus(actionColorSelection));
		actionColorSelection.focusedProperty().addListener(disableOnLostFocus(actionColorSelection));
		sortedActionColorTable.focusedProperty().addListener(disableOnLostFocus(removeSelectedButton));
		removeSelectedButton.focusedProperty().addListener(disableOnLostFocus(removeSelectedButton));
	}
	
	private ChangeListener<? super Boolean> disableOnLostFocus(Node otherFocus) {
		return (observable, oldValue, newValue) -> otherFocus.setDisable(!newValue && !otherFocus.isFocused());
	}
}
