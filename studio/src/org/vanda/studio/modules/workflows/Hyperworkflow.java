package org.vanda.studio.modules.workflows;

/**
 * @author afischer
 */
public abstract class Hyperworkflow {
	private NestedHyperworkflow parent;
	private String name;
	private int id;
	
	public Hyperworkflow(NestedHyperworkflow parent, String name, int id) {
		this.parent = parent;
		this.name = name;
		this.id = id;
	}
	
	/**
	 * @return the NestedHyperworkflow that contains the current Hyperworkflow
	 */
	public NestedHyperworkflow getParent() {
		return parent;
	}

	/**
	 * @return the name of the current Hyperworkflow
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the id of the current Hyperworkflow
	 */
	public int getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object other) {
		//Hyperworkflows are equal if they have the same id
		boolean result = (other != null && other instanceof Hyperworkflow);
		if (result) {
			Hyperworkflow oh = (Hyperworkflow)other;
			result = (id == oh.id);
		}
		return result;
	}
	
	//TODO method needed that checks if one Hyperworkflow is a copy of another one (comparison by child names rather than ids)
	
	public abstract void unfold();
}
