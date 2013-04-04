package org.vanda.workflows.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.workflows.data.Databases.DatabaseEvent;

public class Database {

	private final ArrayList<HashMap<Object, String>> assignments;
	private int cursor;
	private final MultiplexObserver<DatabaseEvent<Database>> observable;
	
	public Database() {
		assignments = new ArrayList<HashMap<Object, String>>();
		cursor = 0;
		observable = new MultiplexObserver<DatabaseEvent<Database>>();
	}
	
	public String get(String key) {
		String result = "";
		if (cursor < assignments.size())
			result = assignments.get(cursor).get(key);
		return result;
	}
	
	public Observable<DatabaseEvent<Database>> getObservable() {
		return observable;
	}
	
	public boolean hasNext() {
		return cursor < assignments.size();
	}
	
	public boolean hasPrev() {
		return cursor > 0;
	}
	
	public void next() {
		if (cursor < assignments.size())
			cursor++;
	}
	
	public void prev() {
		if (cursor > 0)
			cursor--;
	}
	
	public void put(Object key, String value) {
		HashMap<Object, String> m;
		if (value == null || "".equals(value)) {
			if (cursor < assignments.size())
				assignments.get(cursor).remove(key);
		} else {
			if (cursor == assignments.size()) {
				m = new HashMap<Object, String>();
				assignments.add(m);
			} else
				m = assignments.get(cursor);
			m.put(key, value);
		}
	}
	
}
