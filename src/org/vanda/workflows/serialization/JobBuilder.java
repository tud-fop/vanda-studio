package org.vanda.workflows.serialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.util.Pair;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ElementAdapter;
import org.vanda.workflows.hyper.Job;
import org.vanda.xml.Factory;

public class JobBuilder {

	public ElementAdapter element;
	public double[] dimensions;
	public Map<String, String> bindings = new HashMap<String, String>();
	public String id;

	public Pair<Job, Map<Port, String>> build() {
		Job j = new Job(element, id);
		System.out.println(id);
		j.setDimensions(dimensions);
		Map<Port, String> bs = new HashMap<Port, String>();
		doIt(element.getInputPorts(), bs);
		doIt(element.getOutputPorts(), bs);
		return new Pair<Job, Map<Port, String>>(j, bs);
	}
	
	private void doIt(List<Port> ps, Map<Port, String> m) {
		for (Port p : ps) {
			String s = bindings.get(p.getIdentifier());
			if (s != null)
				m.put(p, s);
		}		
	}
	
	public static Factory<JobBuilder> createFactory() {
		return new Fäctory();
	}
	
	public static final class Fäctory implements Factory<JobBuilder> {
		@Override
		public JobBuilder create() {
			return new JobBuilder();
		}
	}
}
