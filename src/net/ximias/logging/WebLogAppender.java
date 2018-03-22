package net.ximias.logging;

import javafx.scene.web.WebEngine;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.logging.*;

public class WebLogAppender extends Handler{
	private WebEngine engine;
	private Document doc = null;
	private ArrayList<LogRecord> buffer = new ArrayList<>(32);
	
	public WebLogAppender() {
		setFormatter(new WebLogFormatter());
	}
	
	public void addEngine(WebEngine engine){
		this.engine = engine;
		engine.loadContent("<html><head></head><body style=\"background:#333; font-family:monospaced; font-size:12; padding:0; margin:2px;\"></body></html>","text/html");
		engine.getLoadWorker().stateProperty().addListener(observable -> {
			doc = engine.getDocument();
			Text css =doc.createTextNode(".SEVERE{color:red;} .WARNING{color:yellow;} p{color:lightgray;} *{margin:0; padding:0} hr{color:black; height:0; display:block; border-top: 1px solid #444; border-bottom: 1px solid #222; margin-bottom:5px;}");
			Element styleNode = doc.createElement("style");
			styleNode.appendChild(css);
			doc.getElementsByTagName("head").item(0).appendChild(styleNode);
			buffer.forEach(this::append);
			buffer = null;
		});
	}
	
	public void append(LogRecord record){
		if (doc != null) {
			Node root =  doc.getElementsByTagName("body").item(0);
			String paragraphText = paragraphText(record);
			for (String line : paragraphText.split("\n", paragraphText.length() - 1)) {
				root.appendChild(getParagraph(line,getStyle(record.getLevel())));
			}
			root.appendChild(doc.createElement("hr"));
			engine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
		}else{
			buffer.add(record);
		}
	}
	
	private Element getParagraph(String text, String style){
		Element paragraph = doc.createElement("p");
		paragraph.setTextContent(text);
		Attr styleAttr = doc.createAttribute("class");
		styleAttr.setValue(style);
		paragraph.setAttributeNode(styleAttr);
		return paragraph;
	}
	
	private String paragraphText(LogRecord record){
		return getFormatter().format(record);
	}
	
	private String getStyle(Level level){
		return level.getName();
	}
	/*
	private String getStyle(Level level){
		if (level == Level.SEVERE){
			return "\"color:red;\"";
		}else if (level == Level.WARNING){
			return "\"color:gold;\"";
		}else{
			return "\"color:gold;\"";
		}
	}*/
	
	@Override
	public void publish(LogRecord record) {
		append(record);
	}
	
	@Override
	public void flush() {
	
	}
	
	@Override
	public void close() throws SecurityException {
	
	}
}
