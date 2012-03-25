package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.modules.workflows.gui.JGraphRendering;
import org.vanda.studio.modules.workflows.gui.JGraphRendering.JGraphRendererSelection;

/**
 * Superclass component of IHyperworkflow composite pattern
 * @author afischer
 */
public abstract class IHyperworkflow {
	
	private NestedHyperworkflow parent;
	private String name;
	private String id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	
	//-------------------------------------------------------------------------
	//----------------------------- constructors ------------------------------
	//-------------------------------------------------------------------------
	
	public IHyperworkflow(NestedHyperworkflow parent, String name, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = "0";
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
	}
	
	public IHyperworkflow(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	public IHyperworkflow(String name) {
		this(null, name);
	}	
	
	//-------------------------------------------------------------------------
	//-------------------------- getters/setters ------------------------------
	//-------------------------------------------------------------------------
	
	/** @return the id of the current IHyperworkflow */
	public String getId() {return id;}
	
	/** @return a list of input ports */
	public List<Port> getInputPorts() {return inputPorts;}	
	
	/** @return the name of the current IHyperworkflow */
	public  String getName() {return name;}
	
	/** @return a list of output ports */
	public  List<Port> getOutputPorts() {return outputPorts;}
	
	/** @return the NestedHyperworkflow that contains the current IHyperworkflow */
	public NestedHyperworkflow getParent() {return parent;}
	
	/**
	 * @param newId - replaces the current id
	 * @return true if replacement was successful
	 */
	public boolean setId(String newId) { 
		if (newId != null) {
			id = newId;
			return true;
		}
		return false; 
	}
	
	/**
	 * @param newParent - replaces the current parent
	 */
	public void setParent(NestedHyperworkflow newParent) {this.parent = newParent;}
	
	@Override
	public String toString() {return name;}
	
	//-------------------------------------------------------------------------
	//--------------------- abstract functionality ----------------------------
	//-------------------------------------------------------------------------
	
	/** @return a (duplicate-free) collection of IHyperworkflows where all OR nodes have been removed */
	public abstract Collection<IHyperworkflow> unfold();
	
	@Override
	public abstract boolean equals(Object other);
	
	@Override
	public abstract Object clone() throws CloneNotSupportedException;
	
	//-------------------------------------------------------------------------
	//----------------------------- gui stuff ---------------------------------
	//-------------------------------------------------------------------------

	private double[] dimensions = {0, 0, 0, 0};
	
	public void appendActions(List<Action> as) {}
	public double getX() { return dimensions[0]; }
	public double getY() { return dimensions[1]; }
	public double getWidth() { return dimensions[2]; }
	public double getHeight() { return dimensions[3]; }
	public void selectRenderer(RendererSelection rs) {rs.selectTermRenderer();}
	public void setDimensions(double[] dim) { if (dim.length == 4) this.dimensions = dim; }
}
