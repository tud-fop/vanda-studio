package org.vanda.fragment.bash.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.fragment.bash.ShellTool;
import org.vanda.types.Type;
import org.vanda.util.TokenSource;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.toolinterfaces.RendererSelector;
import org.vanda.workflows.toolinterfaces.RendererSelectors;

/**
 * Classical builder (pattern) for ShellTool.
 * 
 * @author mbue
 * 
 */
public final class Builder {
	Set<String> imports;
	String id;
	String name;
	StringBuilder description;
	String version;
	String category;
	String contact;
	RendererSelector rs;
	List<Port> inPorts;
	List<Port> outPorts;
	TokenSource ts;
	Map<String, Type> tVars;

	public Builder() {
		reset();
	}

	public void reset() {
		id = "";
		name = "";
		description = new StringBuilder();
		version = "";
		category = "";
		contact = "";
		rs = RendererSelectors.selectors[0];
		inPorts = new ArrayList<Port>();
		outPorts = new ArrayList<Port>();
		ts = new TokenSource();
		tVars = new HashMap<String, Type>();
	}

	public ShellTool build() {
		return new ShellTool(id, name, category, version, contact,
				description.toString(), imports);
	}
}