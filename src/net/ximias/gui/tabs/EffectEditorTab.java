package net.ximias.gui.tabs;


import javafx.fxml.FXML;
import net.ximias.datastructures.gui.data.EffectData;
import net.ximias.gui.MainController;
import net.ximias.gui.tabs.editor.controllers.*;

public class EffectEditorTab {
	private MainController mainController;
	
	@FXML
	private LinkingTabController linkingTabController;
	@FXML
	private EffectsTabController effectsTabController;
	@FXML
	private EventTabController eventsTabController;
	@FXML
	private MultiEventTabController multiEventsTabController;
	@FXML
	private ConditionTabController conditionsTabController;
	
	public void injectMainController(MainController mainController) {
		linkingTabController.injectEditorController(this);
		effectsTabController.injectEditorController(this);
		multiEventsTabController.injectEditorController(this);
		conditionsTabController.injectEditorController(this);
		eventsTabController.injectEditorController(this);
		this.mainController = mainController;
	}
	
	public EffectData getEffectData() {
		return mainController.getEffectData();
	}
}
