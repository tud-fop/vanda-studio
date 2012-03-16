package org.vanda.studio.modules.workflows;

import java.util.Collection;
import java.util.List;

import org.vanda.studio.modules.workflows.gui.JGraphRendering.JGraphRendererSelection;

/**
 * Superclass component of IHyperworkflow composite pattern
 * @author afischer
 */
public interface IHyperworkflow {
	
	public boolean equals(Object other);
	
	/** @return the id of the current IHyperworkflow */
	public String getId();
	
	/** @return a list of input ports */
	public List<Port> getInputPorts();	
	
	/** @return the name of the current IHyperworkflow */
	public String getName();
	
	/** @return a list of output ports */
	public List<Port> getOutputPorts();
	
	/** @return the NestedHyperworkflow that contains the current IHyperworkflow */
	public NestedHyperworkflow getParent();
	
	/**
	 * @param newId - replaces the current id
	 * @return true if replacement was successful
	 */
	public boolean setId(String newId);
	
	public String toString();
	
	/** @return a (duplicate-free) collection of IHyperworkflows where all OR nodes have been removed */
	public Collection<IHyperworkflow> unfold();
	
	//methods required for gui stuff
	public void selectRenderer(JGraphRendererSelection rs);
	public double getX();
	public double getY();
	public double getWidth();
	public double getHeight();
	public IHyperworkflow clone() throws CloneNotSupportedException;
	public void setDimensions(double[] dim);
}
