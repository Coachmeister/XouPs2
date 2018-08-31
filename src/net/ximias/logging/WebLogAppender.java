package net.ximias.logging;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.logging.*;

public class WebLogAppender extends Handler {
	private WebEngine engine;
	private Document doc = null;
	private ArrayList<LogRecord> buffer = new ArrayList<>(32);
	private boolean errorState = false;
	
	public WebLogAppender() {
		setFormatter(new WebLogFormatter());
	}
	
	public void addEngine(WebEngine engine) {
		this.engine = engine;
		engine.loadContent("<html><head></head><body></body></html>", "text/html");
		engine.getLoadWorker().stateProperty().addListener(observable -> {
			doc = engine.getDocument();
			Text css = doc.createTextNode(".SEVERE{color:red;} .WARNING{color:yellow;} p{color:lightgray;} *{margin:0; padding:0; background:#333; font-family:monospaced; font-size:12;} hr{color:black; height:0; display:block; border-top: 1px solid #444; border-bottom: 1px solid #222; margin-bottom:5px;}");
			Element styleNode = doc.createElement("style");
			styleNode.appendChild(css);
			doc.getElementsByTagName("head").item(0).appendChild(styleNode);
			buffer.forEach(this::append);
			buffer = null;
		});
	}
	
	public void append(LogRecord record) {
		if (errorState) return;
		if (doc != null) {
			Platform.runLater(() -> engine.executeScript(
					//Append paragraph
					"var e = document.createElement(\"p\");" +
					" e.setAttribute(\"class\",\"" + record.getLevel() + "\");" +
					" e.innerHTML = \"" + paragraphText(record).replace("\"","\\\"") + "\"; " +
					"document.body.appendChild(e);" +
					//Append hr
					"document.body.appendChild(document.createElement(\"hr\"));"+
					//Scroll to bottom
					"window.scrollTo(0, document.body.scrollHeight);"));
		} else {
			buffer.add(record);
		}
	}
	
	private String paragraphText(LogRecord record) {
		return getFormatter().format(record);
	}
	
	
	@Override
	public void publish(LogRecord record) {
		append(record);
	}
	
	private void checkBodyTag() {
		if (errorState) return;
		if (doc != null) {
			if (doc.getElementsByTagName("body").item(0) == null) {
				errorState = true;
				throw new Error("Missing body tag" + engine.executeScript("document.documentElement.innerHTML"));
			}
		}
	}
	
	@Override
	public void flush() {
	
	}
	
	@Override
	public void close() throws SecurityException {
	
	}
}
