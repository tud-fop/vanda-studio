package org.vanda.workflows.toolinterfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.types.Type;
import org.vanda.util.TokenSource;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.elements.ToolInterface;

public class ToolBuilder extends RepositoryItemBuilder {
	RendererSelector rs;
	List<Port> inPorts;
	List<Port> outPorts;
	ToolInterface ti;
	TokenSource ts;
	Map<String, Type> tVars;

	public ToolBuilder() {
		reset();
	}

	public void reset() {
		super.reset();
		rs = RendererSelectors.selectors[0];
		inPorts = new ArrayList<Port>();
		outPorts = new ArrayList<Port>();
		ts = new TokenSource();
		tVars = new HashMap<String, Type>();
	}

	public Tool build() {
		return new StaticTool(id, name, category, version, contact,
				description.toString(), inPorts, outPorts, rs, ti);
	}

}
