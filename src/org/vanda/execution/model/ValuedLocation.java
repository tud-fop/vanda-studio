package org.vanda.execution.model;

import org.vanda.workflows.hyper.Location;

public class ValuedLocation extends Location {
	private String value;

	public ValuedLocation(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
