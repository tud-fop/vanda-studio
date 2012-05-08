package org.vanda.studio.model.generation;

import java.util.HashSet;
import java.util.Set;

public abstract class Workflow<C extends WorkflowElement, CC extends Connection<C>> {
	// not final becaus of HyperWorkflow.clone()
	protected Set<C> children;

	// not final becaus of HyperWorkflow.clone()
	protected Set<CC> connections;

	public Workflow() {
		children = new HashSet<C>();
		connections = new HashSet<CC>();
	}
	
	public void addChild(C c) {
		children.add(c);
	}
	
	public void addConnection(CC cc) {
		connections.add(cc);
	}
}
