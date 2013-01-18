package org.vanda.fragment.model;

import java.util.List;
import java.util.Set;

import org.vanda.workflows.elements.Port;

/**
 * Fragments represent small compositional snippets of code.
 * 
 * @author buechse
 * 
 */
public interface Fragment {
	
	public String getId();
	public String getText();
	public List<Port> getInputPorts();
	public List<Port> getOutputPorts();
	public Set<String> getDependencies();
	public Set<String> getImports();
	
}
