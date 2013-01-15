package org.vanda.workflows.toolinterfaces;

public class RepositoryItemBuilder {
	String id;
	String name;
	StringBuilder description;
	String version;
	String category;
	String contact;
	
	public RepositoryItemBuilder() {
		reset();
	}
	
	public void reset() {
		id = "";
		name = "";
		description = new StringBuilder();
		version = "";
		category = "";
		contact = "";
	}

}
