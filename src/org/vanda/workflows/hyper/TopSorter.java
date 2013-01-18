package org.vanda.workflows.hyper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.workflows.elements.Port;

public final class TopSorter {
	
	private final static class InputBlock {
		
		private final static class MutableInteger {
			public int value;
			
			public MutableInteger(int value) {
				this.value = value;
			}
		}
		
		private final WeakHashMap<Job, MutableInteger> m;
		
		public InputBlock() {
			m = new WeakHashMap<Job, MutableInteger>();
		}
		
		public void init(Collection<Job> jobs) {
			for (Job j : jobs) {
				int i = 0;
				for (Port ip : j.getInputPorts())
					if (j.bindings.containsKey(ip))
						i++;
				m.put(j, new MutableInteger(i));
			}
		}
		
		public void addUnblocked(Collection<Job> dest) {
			for (Map.Entry<Job, MutableInteger> e : m.entrySet())
				if (e.getValue().value == 0)
					dest.add(e.getKey());
		}
		
		public boolean unblock(Job j) {
			MutableInteger ib = m.get(j);
			ib.value--;
			return ib.value == 0;
		}
	}
	
	private final static class ForwardArray {
		
		private final Map<Job, LinkedList<Job>> m;
		
		public ForwardArray() {
			m = new HashMap<Job, LinkedList<Job>>();
		}
		
		public void init(MutableWorkflow workflow) {
			for (Job ji : workflow.getChildren()) {
				for (Port ip : ji.getInputPorts()) {
					Location var = ji.bindings.get(ip);
					if (var != null) {
						ConnectionKey src = workflow.getVariableSource(var);
						LinkedList<Job> ll = m.get(src.target);
						if (ll == null) {
							ll = new LinkedList<Job>();
							m.put(src.target, ll);
						}
						ll.add(ji);
					}
				}
			}
		}
		
		private List<Job> getForward(Job j) {
			List<Job> result = m.get(j);
			if (result == null)
				result = Collections.emptyList();
			return result;
		}
	}
	
	private final static class Result {
		private Job[] a;
		private int i;
		
		public void init(int size) {
			a = new Job[size];
			i = size - 1;
		}
		
		public void add(Job j) {
			a[i] = j;
			i--;
		}
		
		public boolean isComplete() {
			return i == -1;
		}
		
		public Job[] toArray() {
			return a;
		}
	}
	
	private final ForwardArray forwardArray = new ForwardArray();
	private final InputBlock inputBlock = new InputBlock();
	private final Result result = new Result();
	
	public void init(MutableWorkflow workflow) {
		forwardArray.init(workflow);
		inputBlock.init(workflow.getChildren());
		result.init(workflow.getChildren().size());		
	}

	public void proceed() {
		LinkedList<Job> workingset = new LinkedList<Job>();
		inputBlock.addUnblocked(workingset);
		while (!workingset.isEmpty()) {
			Job ji = workingset.pop();
			result.add(ji);
			for (Job ji2 : forwardArray.getForward(ji)) {
				if (inputBlock.unblock(ji2))
					workingset.add(ji2);
			}
		}
	}
	
	public Job[] getSorted() throws Exception {
		if (result.isComplete()) {
			return result.toArray();
		} else
			throw new Exception(
					"could not do topological sort; cycles probable");
	}

}
