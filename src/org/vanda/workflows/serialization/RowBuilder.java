package org.vanda.workflows.serialization;

import java.util.HashMap;

import org.vanda.xml.Factory;

public class RowBuilder {
	
	HashMap<String, String> assignment;

	public RowBuilder() {
		assignment = new HashMap<String, String>();
	}

	public static Factory<RowBuilder> createFactory() {
		return new Fäctory();
	}

	public static final class Fäctory implements Factory<RowBuilder> {
		@Override
		public RowBuilder create() {
			return new RowBuilder();
		}
	}
	
}
