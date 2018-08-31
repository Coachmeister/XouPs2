package net.ximias.gui.tabs;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import net.ximias.gui.MainController;
import net.ximias.gui.StatusSeverity;
import net.ximias.datastructures.gui.nodes.StatusIndicator;
import net.ximias.network.CensusConnection;
import net.ximias.network.CurrentPlayer;
import net.ximias.persistence.Persisted;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import net.ximias.logging.Logger;


public class LoginTab {
	@FXML
	private ChoiceBox<JSONObject> characterChoice;
	@FXML
	private TextField nameField;
	@FXML
	private Button selectionButton;
	private static final String LOGIN_NAME_CHANGE_TIMER = "Login name change timer";
	private static final SimpleBooleanProperty init = new SimpleBooleanProperty(true);
	private MainController mainController;
	private final ArrayList<String> errors = new ArrayList<>(5);
	private final Logger logger = Logger.getLogger(getClass().getName());
	private Timer nameUpdateTimer = new Timer(LOGIN_NAME_CHANGE_TIMER,true);
	
	public void injectMainController(MainController controller) {
		this.mainController = controller;
		nameField.textProperty().addListener((observable, oldValue, newValue) -> {
			nameUpdateTimer.cancel();
			nameUpdateTimer = new Timer(LOGIN_NAME_CHANGE_TIMER,true);
			characterChoice.setDisable(true);
			nameUpdateTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Platform.runLater(() -> nameChanged());
				}
			}, 300);
		});
		selectionButton.setDisable(true);
		characterChoice.setDisable(true);
		characterChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null){
				selectionButton.setDisable(true);
				characterChoice.setDisable(true);
			}else{
				if (init.get()){
					characterChoice.setDisable(false);
					selectionButton.setDisable(false);
				} else {
					JSONObject selectedItem = characterChoice.getSelectionModel().getSelectedItem();
					characterChoice.setDisable(false);
					selectionButton.setDisable(!(selectedItem != null && !selectedItem.get("character_id").equals(CurrentPlayer.getInstance().getPlayerID())));
				}
			}
		});
	}
	
	public void login(ActionEvent actionEvent) {
		String character_id = characterChoice.getSelectionModel().getSelectedItem().getString("character_id");
		selectionButton.setDisable(true);
		if (init.get()) {
			CurrentPlayer.initCurrentPlayer(character_id);
			init.set(false);
		} else {
			CurrentPlayer.getInstance().setPlayerID(character_id);
			mainController.getEffectData().playerIDUpdated();
		}
	}
	
	private synchronized void nameChanged() {
		try {
			logger.general().warning("Name changed");
			String partlyPlayerName = nameField.getText();
			if (partlyPlayerName.length() < 5) {
				characterChoice.setItems(FXCollections.emptyObservableList());
				return;
			}
			Persisted.getInstance().LAST_LOGIN = partlyPlayerName;
			JSONArray playerNameList;
			
			
			playerNameList = CensusConnection.listPlayersStartsWith(partlyPlayerName);
			performCharacterSelection(playerNameList);
		} catch (IOException e) {
			String errorText = "Error occurred when trying to login: " + e;
			errors.add(errorText);
			StatusIndicator.getInstance().addStatus(errorText, StatusSeverity.INTERUPTION);
		}
		for (String error : errors) {
			StatusIndicator.getInstance().removeStatus(error);
		}
	}
	
	private void performCharacterSelection(JSONArray results) {
		JSONObject[] options = getLimitedOptions(results);
		
		ObservableList<JSONObject> value = FXCollections.observableArrayList(options);
		SortedList<JSONObject> sortedList = value.sorted(Comparator.comparing(o -> o.getJSONObject("name").getString("first")));
		
		characterChoice.setItems(sortedList);
		characterChoice.setConverter(new StringConverter<JSONObject>() {
			@Override
			public String toString(JSONObject object) {
				return object.getJSONObject("name").getString("first");
			}
			
			@Override
			public JSONObject fromString(String string) {
				return null;
			}
		});
		characterChoice.getSelectionModel().selectFirst();
	}
	
	private JSONObject[] getLimitedOptions(JSONArray results) {
		
		ArrayList<JSONObject> fullOptions = new ArrayList<>((int) (results.length() * 1.5));
		for (int i = 0; i < results.length(); i++) {
			fullOptions.add(results.getJSONObject(i));
		}
		fullOptions.sort(Comparator.comparing(o -> o.getJSONObject("name").getString("first")));
		
		JSONObject[] options = new JSONObject[Math.min(results.length(), 10)];
		for (int i = 0; i < options.length; i++) {
			options[i] = fullOptions.get(i);
		}
		return options;
	}
	
	public boolean isInit() {
		return init.get();
	}
	
	public SimpleBooleanProperty initProperty() {
		return init;
	}
}
