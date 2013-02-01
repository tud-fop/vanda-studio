package org.vanda.workflows.serialization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.util.Pair;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public class WorkflowBuilder {

	public String name;

	public final List<Pair<Job, Map<Port, String>>> jbs;

	public WorkflowBuilder() {
		jbs = new LinkedList<Pair<Job, Map<Port, String>>>();
	}

	public MutableWorkflow build() {
		Map<String, Location> vars = new HashMap<String, Location>();
		MutableWorkflow result = new MutableWorkflow(name);
		result.beginUpdate();
		try {
			for (Pair<Job, Map<Port, String>> e : jbs) {
				result.addChild(e.fst);
				for (Port op : e.fst.getOutputPorts()) {
					String s = e.snd.get(op);
					if (s != null)
						vars.put(s, e.fst.bindings.get(op));
				}
			}
			for (Pair<Job, Map<Port, String>> e : jbs) {
				for (Port ip : e.fst.getInputPorts()) {
					String s = e.snd.get(ip);
					if (s != null)
						result.addConnection(new ConnectionKey(e.fst, ip),
								vars.get(s));
				}
			}
		} finally {
			result.endUpdate();
		}
		return result;
	}

}
