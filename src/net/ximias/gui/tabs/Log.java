package net.ximias.gui.tabs;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Callback;
import net.ximias.datastructures.RandomAccessFileTextReader;
import net.ximias.datastructures.collections.EvictingObservableList;
import net.ximias.gui.MainController;
import net.ximias.gui.tabs.log.Filter;
import net.ximias.logging.*;

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;

public class Log implements CollectionLogReciever {
	private final Filter filter = new Filter(this);
	private final LogfileReader logfileReader = new LogfileReader(this, filter);
	@FXML
	public ListView<String> list;
	@FXML
	public ChoiceBox<String> categoryChoice;
	@FXML
	public ChoiceBox<String> levelChoice;
	@FXML
	public CheckBox filtersAnd;
	private static final int LINES_PR_SCREEN = 1000;
	private static final int BUFFERED_SCREENS = 1;
	public static final int LINES_TO_READ = (BUFFERED_SCREENS * LINES_PR_SCREEN) / 2;
	public static final char COMPARE_SUBSTRING_CHAR = '[';
	
	/*
	 * 2 bytes pr char
	 * 250 char pr line
	 * 150 lines pr screen
	 
	 * 75000 bytes pr screen
	 * 10% of screen pr scrollInAbove
	 * */
	private VirtualFlow<IndexedCell<String>> visibleItems;
	private ScrollBar verticalScroll;
	private final EvictingObservableList<String> recentMessages = new EvictingObservableList<>(LINES_PR_SCREEN);
	private final EvictingObservableList<String> pastMessages = new EvictingObservableList<>(BUFFERED_SCREENS * LINES_PR_SCREEN);
	private boolean autoscroll = true;
	private boolean cached = true;
	LinkedList<String> toRemove = new LinkedList<>();
	private final Logger logger = Logger.getLogger(getClass().getName());
	private int msg = 1;
	
	public Log() {
		logfileReader.lineReaderThread.setDaemon(true);
	}
	
	public void injectMainController(MainController controller, RandomAccessFile logFile) {
		logfileReader.fileReader = new RandomAccessFileTextReader(logFile, FileLogAppender.UTF16);
		recentMessages.add("Log tab initializing...");
		populateFilters();
		setupListView();
		
		
		controller.addProjectLevelLoggerHandler(new CollectionLogAppender(this));
		/*new Timer(true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (msg % 2 == 0) {
					logger.general().warning("Fill message: " + msg++);
				} else {
					logger.general().info("Fill message: " + msg++);
				}
			}
		}, 0, 500);*/
	}
	
	private void setupListView() {
		verticalScroll = getScrollbar();
		if (verticalScroll == null) {
			throw new Error("Scrollbar not found");
		}
		
		list.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> param) {
				return new ListCell<String>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (item == null || empty) {
							setText(null);
							setStyle("");
						} else {
							setText(item);
							getStyleClass().clear();
							getStyleClass().add("list-cell");
							if (item.contains("[INFO]")) {
								getStyleClass().add("info");
							}
							if (item.contains("[WARNING]")) {
								getStyleClass().add("warning");
							}
							if (item.contains("[SEVERE]")) getStyleClass().add("severe");
						}
					}
				};
			}
		});
		
		recentMessages.addListener((ListChangeListener<? super String>) (c) -> {
			if (autoscroll) verticalScroll.setValue(verticalScroll.getMax());
		});
		
		verticalScroll.valueProperty().addListener((observable, oldValue, newValue) -> {
			double position = newValue.doubleValue();
			ScrollBar scrollBar = getScrollbar();
			if (position == scrollBar.getMax()) {
				scrollBottomReached();
			} else if (position == scrollBar.getMin()) {
				scrollTopReached();
			} else {
				autoscroll = false;
			}
		});
		// I left the warning until I know that this will always work.
		visibleItems = (VirtualFlow<IndexedCell<String>>) ((ListViewSkin<?>) list.getSkin()).getChildren().get(0);
		
		list.setItems(recentMessages);
	}
	
	private void populateFilters() {
		LinkedList<String> categories = new LinkedList<>();
		categories.add("All");
		Arrays.stream(Category.values()).forEach(it -> categories.add(it.getName()));
		categoryChoice.setItems(FXCollections.observableList(categories));
		categoryChoice.getSelectionModel().select("All");
		
		levelChoice.setItems(FXCollections.observableArrayList("SEVERE", "WARNING", "INFO"));
		levelChoice.getSelectionModel().select("INFO");
		levelChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			switch (newValue) {
				case "SEVERE":
					filter.setCurrentFilter(Filter.SEVERE_FILTER);
					break;
				case "WARNING":
					filter.setCurrentFilter(Filter.WARNING_FILTER);
					break;
				default:
					filter.setCurrentFilter(Filter.INFO_FILTER);
					break;
			}
			applyFilter();
		});
		categoryChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
			filter.setCategory(newValue);
			applyFilter();
		});
		filtersAnd.selectedProperty().addListener((observable, oldValue, newValue) -> {
			filter.setAnd(newValue);
			applyFilter();
		});
		applyFilter();
	}
	
	private synchronized void applyFilter() {
		recentMessages.clear();
		pastMessages.clear();
		
		logfileReader.readPastLinesOnLineReaderThread(this::filterApplyRead);
		
	}
	
	private synchronized void filterApplyRead(LinkedList<String> lines) {
		Platform.runLater(() -> {
			recentMessages.addAll(lines);
			pastMessages.addAll(lines);
			logfileReader.setTopReached(false);
			bottomOfFileReached();
			if (list.getItems().size() < LINES_PR_SCREEN) {
				System.out.println("Filter Padding...");
				logfileReader.readPastLinesOnLineReaderThread(this::padWithPastLines);
			}
		});
	}
	
	private synchronized void padWithPastLines(LinkedList<String> lines) {
		Platform.runLater(() -> {
			while (!lines.isEmpty() && list.getItems().size() < LINES_PR_SCREEN) {
				System.out.println("Added: " + lines.getLast());
				recentMessages.addFirst(lines.getLast());
				pastMessages.addFirst(lines.getLast());
				lines.removeLast();
			}
			if (list.getItems().size() < LINES_PR_SCREEN && !logfileReader.isTopReached()) {
				System.out.println("Recurse Padding...");
				logfileReader.readPastLinesOnLineReaderThread(this::padWithPastLines);
			} else {
				logfileReader.setTopReached(false);
			}
		});
	}
	
	private ScrollBar getScrollbar() {
		for (Node node : list.lookupAll(".scroll-bar")) {
			if (node instanceof ScrollBar) {
				if (((ScrollBar) node).getOrientation() == Orientation.VERTICAL) return (ScrollBar) node;
			}
		}
		return null;
	}
	
	private void scrollTopReached() {
		if (list.getItems().size() < LINES_PR_SCREEN) {
			System.out.println("Not full");
			return;
		}
		scrollUp();
	}
	
	private void scrollBottomReached() {
		if (cached) return;
		if ((pastMessages.isEmpty() && recentMessages.isEmpty()) || pastMessages.getLast().equals(recentMessages.getLast())) {
			bottomOfFileReached();
		} else {
			scrollDown();
		}
	}
	
	public void bottomOfFileReached() {
		Platform.runLater(() -> {
			cached = true;
			autoscroll = true;
			list.setItems(recentMessages);
		});
	}
	
	private void scrollUp() {
		System.out.println("Scroll up");
		if (cached) {
			list.setItems(pastMessages);
			pastMessages.addAll(recentMessages);
			cached = false;
			logfileReader.setTopReached(false);
			logfileReader.readPastLinesOnLineReaderThread(this::onPastLinesRead);
		} else if (!logfileReader.isTopReached()) {
			logfileReader.readPastLinesOnLineReaderThread(this::onPastLinesRead); //TODO REMEMBER TO SCROLL
		}
	}
	
	private void scrollDown() {
		if (!cached) {
			logfileReader.readFutureLinesOnLineReaderThread(this::onFutureLinesRead);
		}
	}
	
	@Override
	public void receiveMessage(String logMessage) {
		//System.out.println("Message: "+logMessage+" In filter: "+inFilter(logMessage));
		//System.out.println(recentMessages == list.getItems());
		if (filter.inFilter(logMessage)) {
			Platform.runLater(() -> recentMessages.addLast(logMessage));
		}
	}
	
	public void onFutureLinesRead(LinkedList<String> lines) {
		Platform.runLater(() -> pastMessages.addAll(lines));
	}
	
	public void onPastLinesRead(LinkedList<String> lines) {
		Platform.runLater(() -> {
			while (!lines.isEmpty()) {
				pastMessages.addFirst(lines.getLast());
				lines.removeLast();
			}
		});
	}
	
	public String getNewestLoadedMessage() {
		return list.getItems().get(list.getItems().size() - 1);
	}
	
	public String getOldestLoadedMessage() {
		return list.getItems().get(0);
	}
	
	public boolean isPastMessagesEmpty() {
		return pastMessages.isEmpty();
	}
}
