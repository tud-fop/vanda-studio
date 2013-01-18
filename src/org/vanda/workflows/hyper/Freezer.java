package org.vanda.workflows.hyper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.immutable.ImmutableWorkflow;
import org.vanda.workflows.immutable.JobInfo;

public final class Freezer {
	
	private final static class DJobInfo {
		public final Job job;
		public int inputsBlocked;

		public DJobInfo(Job j) {
			job = j;
			inputsBlocked = 0;
			for (Token t : j.inputs)
				if (t != null)
					inputsBlocked++;
		}
		
	}
	
	private MutableWorkflow wf;
	private DJobInfo[] children;
	private Map<Token, DJobInfo> varSource;
	
	private void initChildren() {
		children = new DJobInfo[wf.children.size()];
		for (int i = 0; i < children.length; i++) {
			// Job j = wf.children.get(i);
			children[i] = new DJobInfo(wf.children.get(i));
		}
		varSource = new HashMap<Token, DJobInfo>();
	}

	public ImmutableWorkflow freeze(MutableWorkflow workflow) throws Exception {
		wf = workflow;
		initChildren();
		// Two steps. Step 1: topological sort
		// compute source job for each variable
		for (DJobInfo ji : children) {
			if (ji != null) {
				for (Token t : ji.job.outputs)
					varSource.put(t, ji);
			}
		}
		// compute initial working set (jobs without inputs)
		// also, compute forward array: for each job which job can be reached
		Map<DJobInfo, LinkedList<DJobInfo>> forwA = new HashMap<DJobInfo, LinkedList<DJobInfo>>();
		LinkedList<DJobInfo> workingset = new LinkedList<DJobInfo>();
		int count = 0;
		for (DJobInfo ji : children) {
			if (ji != null) {
				for (Token t : ji.job.inputs) {
					if (t != null) {
						DJobInfo key = varSource.get(t);
						LinkedList<DJobInfo> ll = forwA.get(key);
						if (ll == null) {
							ll = new LinkedList<DJobInfo>();
							forwA.put(key, ll);
						}
						ll.add(ji);
					}
				}
				// ji.topSortInputsBlocked = ji.inputsBlocked;
				if (ji.inputsBlocked == 0)
					workingset.add(ji);
				count++;
			}
		}
		// topological sort
		ArrayList<DJobInfo> topsort = new ArrayList<DJobInfo>(count);
		while (!workingset.isEmpty()) {
			DJobInfo ji = workingset.pop();
			topsort.add(ji);
			LinkedList<DJobInfo> ll = forwA.get(ji);
			if (ll != null)
				for (DJobInfo ji2 : ll) {
					ji2.inputsBlocked--;
					if (ji2.inputsBlocked == 0)
						workingset.add(ji2);
				}
		}
		// Step 2: actual freeze
		if (topsort.size() == count) {
			ArrayList<JobInfo> imch = new ArrayList<JobInfo>(topsort.size());
			for (DJobInfo ji : topsort) {
				boolean connected = true;
				List<Port> ports = null;
				ports = ji.job.getInputPorts();
				ArrayList<Token> intoken = new ArrayList<Token>(ports.size());
				for (int i = 0; i < ports.size(); i++) {
					if (ports.get(i) != null) {
						Token t = ji.job.inputs[i];
						intoken.add(t);
						connected = connected && (t != null);
					}
				}
				ports = ji.job.getOutputPorts();
				ArrayList<Token> outtoken = new ArrayList<Token>(ports.size());
				for (int i = 0; i < ports.size(); i++) {
					if (ports.get(i) != null)
						outtoken.add(ji.job.outputs[i]);
				}
				imch.add(new JobInfo(ji.job.freeze(), ji.job.address, intoken,
						outtoken, connected));
			}
			// XXX removed ports for the time being!
//			List<Token> ports = null;
//			ports = wf.inputPorts;
			ArrayList<Port> inputPorts = new ArrayList<Port>();
			ArrayList<Token> inputPortVariables = new ArrayList<Token>();
//			for (int i = 0; i < ports.size(); i++) {
//				if (ports.get(i) != null) {
//					DJobInfo daPort = children.get(ports.get(i).intValue());
//					inputPorts.add(daPort.job.getOutputPorts().get(0));
//					inputPortVariables.add(daPort.job.outputs.get(0));
//				}
//			}
//			ports = wf.outputPorts;
			ArrayList<Port> outputPorts = new ArrayList<Port>();
			ArrayList<Token> outputPortVariables = new ArrayList<Token>();
//			for (int i = 0; i < ports.size(); i++) {
//				if (ports.get(i) != null) {
//					DJobInfo daPort = children.get(ports.get(i).intValue());
//					outputPorts.add(daPort.job.getInputPorts().get(0));
//					outputPortVariables.add(daPort.job.inputs.get(0));
//				}
//			}
			return new ImmutableWorkflow(wf.name, inputPorts, outputPorts,
					inputPortVariables, outputPortVariables, null, null, imch,
					wf.variableSource, wf.variableSource.getMaxToken());
		} else
			throw new Exception(
					"could not do topological sort; cycles probable");
	}

}
