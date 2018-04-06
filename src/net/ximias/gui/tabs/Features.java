package net.ximias.gui.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import net.ximias.gui.MainController;
import net.ximias.persistence.ApplicationConstants;

public class Features {
	private MainController mainController;
	
	@FXML
	private ListView<String> featuresList;
	
	@FXML
	private ChoiceBox<String> featuresChoice;
	
	@FXML
	private void openDiscordServer(ActionEvent e) {
		mainController.getHostServices().showDocument("https://discord.gg/WyRYbsw");
	}
	
	public void injectMainController(MainController controller) {
		mainController = controller;
		setupFeaturesTab();
	}
	
	private void setupFeaturesTab() {
		featuresChoice.setItems(FXCollections.observableArrayList("planned", "discarded"));
		ObservableList<String> planned = FXCollections.observableArrayList(ApplicationConstants.PLANNED_FEATURES);
		ObservableList<String> rejected = FXCollections.observableArrayList(ApplicationConstants.REJECTED_FEATURES);
		
		featuresChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> featuresList.setItems(newValue.intValue() == 0 ? planned : rejected));
		featuresChoice.getSelectionModel().selectFirst();
	}
}
