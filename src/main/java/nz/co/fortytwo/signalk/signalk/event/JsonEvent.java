package nz.co.fortytwo.signalk.signalk.event;

import mjson.Json;

public class JsonEvent {
	public enum EventType  { ADD, EDIT,DEL};
	private Json json;
	private EventType type;

	public JsonEvent(Json json, EventType type) {
		this.json = json;
		this.type=type;
	}

	public Json getJson() {
		return json;
	}

	public EventType getType() {
		return type;
	}

}
