package org.vanda.studio.modules.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.ToolInstance;

public class SimpleToolInstance implements ToolInstance {
	
	protected Map<String,Object> defaults;
	protected HashMap<String,Object> parameters;
	
	public SimpleToolInstance() {
		this(null);
	}
	
	public SimpleToolInstance(Map<String,Object> defaults) {
		if (defaults == null) {
			this.defaults = Collections.emptyMap();
		}
		else {
			this.defaults = defaults;
		}
		parameters = new HashMap<String,Object>(this.defaults);
	}
	
	@Override
	public void appendActions(List<Action> as) {
	}
	
	@Override
	public void loadFromMap(Map<String,Object> map) {
		for (Map.Entry<String,Object> e : parameters.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			Object newvalue = map.get(key);
			if (newvalue == null) {
				parameters.put(key, defaults.get(key));
			}
			else if (value.getClass().isAssignableFrom(newvalue.getClass()))
			{
				parameters.put(key, newvalue);
			}
		}
	}
	
	@Override
	public void saveToMap(Map<String,Object> map) {
		for (Map.Entry<String,Object> e : parameters.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			if (value != defaults.get(key)) {
				map.put(key, value);
			}
		}
	}
}
