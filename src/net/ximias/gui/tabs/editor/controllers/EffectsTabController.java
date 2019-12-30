package net.ximias.gui.tabs.editor.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import net.ximias.gui.tabs.EffectEditorTab;

public class EffectsTabController {
	public HBox effectListContainer;
	public TextField nameField;
	public ChoiceBox typeChoice;
	public ColorPicker startColor;
	public ColorPicker endColor;
	public TextField durationInput;
	public Slider durationSlider;
	public ChoiceBox sourceChoice;
	public TextField valueField;
	public Button addButton;
	
	public void injectEditorController(EffectEditorTab editorController) {
	
	}
	
	public void addEffect(ActionEvent actionEvent) {
	
	}
}
