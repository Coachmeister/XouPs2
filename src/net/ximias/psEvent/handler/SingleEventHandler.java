package net.ximias.psEvent.handler;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.fileParser.InitializationException;
import net.ximias.fileParser.Initializer;
import net.ximias.network.CurrentPlayer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.*;
import org.json.JSONObject;

import java.util.HashMap;

public class SingleEventHandler extends Ps2EventHandler {
	/*
	Need to know:
	Event name
	Player or world event
	Effect to apply
	
	Condition for effect to apply or "all"
	
	 */
	
	
	private final EventCondition condition;
	private final Ps2EventType type;
	private final String eventName;
	public SingleEventHandler(EffectView view, EffectProducer effect, EventCondition condition, Ps2EventType type, String event , String name) {
		super(effect);
		this.name = name;
		this.condition = condition;
		this.type = type;
		eventName = event;
		setView(view);
	}
	
	public SingleEventHandler(JSONObject o){
		checkParameters(o);
		name = o.getString("name");
		eventName = o.getString("event");
		if (o.getString("condition").equals("all")) condition = null;
		else condition = new SingleCondition(o.getJSONObject("condition"));
		
		if (o.getString("type").equals("player")) type = Ps2EventType.PLAYER;
		else type = Ps2EventType.WORLD;
		
		if (o.has("effect")) effect = Initializer.effectProducerFromNameWhileInit(o.getString("effect"));
	}
	
	private void checkParameters(JSONObject o){
		if (o.has("type")&&o.has("event")&&o.has("condition")){
			if (!o.getString("type").equals("player") && !o.getString("type").equals("world")) {
				throw new InitializationException("Invalid type. Allowed types are player and world");
			}
		}else {
			throw new InitializationException("Missing parameters. Required parameters are: type, event, and condition");
		}
	}
	
	@Override
	public boolean conditionIsSatisfied(JSONObject payload) {
		return (condition==null || condition.evaluate(payload));
	}
	
	@Override
	public void register(Ps2EventStreamingConnection con) {
		if (effect!=null){
			if (type == Ps2EventType.PLAYER){
				con.subscribePlayerEvent(eventName, CurrentPlayer.getInstance().getPlayerID(),this);
			}else{
				con.subscribeWorldEvent(eventName,CurrentPlayer.getInstance().getValue("world_id"),this);
			}
		}
	}
	
	@Override
	public void registerOther(Ps2EventHandler handler, Ps2EventStreamingConnection con) {
		if (type == Ps2EventType.PLAYER){
			con.subscribePlayerEvent(eventName, CurrentPlayer.getInstance().getPlayerID(),handler);
		}else{
			con.subscribeWorldEvent(eventName,CurrentPlayer.getInstance().getWorld(),handler);
		}
	}
	
	
	public Ps2EventType getType() {
		return type;
	}
	
	/*public static void main(String[] args) {
		CurrentPlayer.getInstance().setPlayerID("8287548916321388337");
		ArrayList<ConditionData> data= new ArrayList<>(5);
		data.add(new EventData("13", ConditionDataSource.CONSTANT));
		data.add(new EventData("world_id", ConditionDataSource.EVENT));
		
		SingleCondition worldIdIs13 = new SingleCondition(data,Condition.EQUALS);
		SingleEventHandler event = new SingleEventHandler(new ConsoleView(), new TimedEffectProducer(Color.WHITE,1600),worldIdIs13, Ps2EventType.WORLD, "PlayerLogin", "aName");
		
		Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
		event.register(connection);
		
	}*/
	
	@Override
	public HashMap<String, String> toJson() {
		/*HashMap<String, String> h = new HashMap<>(15);
		h.put("effect", effect.getName());
		if (condition != null) {
			h.put("condition", condition.getJsonObject().toString());
		}
		return h;*/
		throw new UnsupportedOperationException("Later!");
	}
}
