package org.vanda.workflows.serialization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.vanda.workflows.data.Database;
import org.vanda.xml.Factory;

public class DatabaseBuilder {
	LinkedList<HashMap<String, String>> assignments;
	LinkedList<String> names;

	public DatabaseBuilder() {
		assignments = new LinkedList<HashMap<String, String>>();
		names = new LinkedList<String>();
	}

	public static Factory<DatabaseBuilder> createFactory() {
		return new Fäctory();
	}

	public static final class Fäctory implements Factory<DatabaseBuilder> {
		@Override
		public DatabaseBuilder create() {
			return new DatabaseBuilder();
		}
	}

	public Database build() {
		Database result = new Database();
		for (int i = 0; i < assignments.size(); ++i) {
			HashMap<String, String> a = assignments.get(i);
			for (Map.Entry<String, String> e : a.entrySet()) {
				result.put(e.getKey(), e.getValue());
			}
			String name = names.get(i);
			result.setName( name != null ? name : "" );
			result.next();
		}
		result.home();
		return result;
	}

}
