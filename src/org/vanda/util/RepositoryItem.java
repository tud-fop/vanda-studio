package org.vanda.util;

public interface RepositoryItem {

	/**
	 * The category is used like a path in a file system. The separator is a
	 * period.
	 */
	String getCategory();

	String getContact();
	
	String getDescription();

	String getId();

	String getName();

	String getVersion();
	
}
