package org.vanda.workflows.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Util;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.CursorChange;
import org.vanda.workflows.data.Databases.DataChange;

public final class Database {

	private final ArrayList<HashMap<Integer, String>> assignments;
	private int cursor;
	private final MultiplexObserver<DatabaseEvent<Database>> observable;
	private int update;
	private LinkedList<DatabaseEvent<Database>> events;

	public Database() {
		assignments = new ArrayList<HashMap<Integer, String>>();
		cursor = 0;
		observable = new MultiplexObserver<DatabaseEvent<Database>>();
		events = new LinkedList<DatabaseEvent<Database>>();
	}

	public void beginUpdate() {
		update++;
	}

	public void endUpdate() {
		update--;
		if (update == 0) {
			LinkedList<DatabaseEvent<Database>> ev = events;
			events = new LinkedList<DatabaseEvent<Database>>();
			Util.notifyAll(observable, ev);
		}
	}

	public String get(Integer key) {
		String result = null;
		if (cursor < assignments.size())
			result = assignments.get(cursor).get(key);
		if (result == null)
			result = "";
		return result;
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int c) {
		if (cursor != c) {
			try {
				beginUpdate();
				cursor = c;
				events.add(new CursorChange<Database>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public HashMap<Integer, String> getRow(int location) {
		return assignments.get(location);
	}

	public void addRow() {
		beginUpdate();
		try {
			HashMap<Integer, String> row = new HashMap<Integer, String>();
			for (Entry<Integer, String> e : assignments.get(cursor).entrySet()) {
				row.put(e.getKey(), e.getValue());
				events.add(new DataChange<Database>(this, e.getKey()));
			}
			assignments.add(row);
		} finally {
			endUpdate();
		}
	}

	public void delRow(int row) {
		if (row > -1 && row < assignments.size()) {
			beginUpdate();
			try {
				HashMap<Integer, String> theRow = assignments.get(row);
				assignments.remove(row);
				for (Entry<Integer, String> e : theRow.entrySet())
					events.add(new DataChange<Database>(this, e.getKey()));
			} finally {
				endUpdate();
			}
		}
	}

	public Observable<DatabaseEvent<Database>> getObservable() {
		return observable;
	}

	public int getSize() {
		return assignments.size();
	}

	public boolean hasNext() {
		return cursor < assignments.size();
	}

	public boolean hasPrev() {
		return cursor > 0;
	}

	public void home() {
		if (cursor != 0) {
			beginUpdate();
			try {
				cursor = 0;
				events.add(new CursorChange<Database>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public void next() {
		if (cursor < assignments.size()) {
			beginUpdate();
			try {
				cursor++;
				events.add(new CursorChange<Database>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public void prev() {
		if (cursor > 0) {
			beginUpdate();
			try {
				cursor--;
				events.add(new CursorChange<Database>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public void put(Integer key, String value) {
		HashMap<Integer, String> m;
		beginUpdate();
		try {
			String oldvalue;
			if (value == null || "".equals(value)) {
				value = null;
				oldvalue = null;
				if (cursor < assignments.size())
					oldvalue = assignments.get(cursor).remove(key);
			} else {
				if (cursor == assignments.size()) {
					m = new HashMap<Integer, String>();
					assignments.add(m);
				} else
					m = assignments.get(cursor);
				oldvalue = m.put(key, value);
			}
			if (value != oldvalue)
				events.add(new DataChange<Database>(this, key));
		} finally {
			endUpdate();
		}
	}

}
