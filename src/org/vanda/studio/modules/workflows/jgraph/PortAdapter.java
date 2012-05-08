package org.vanda.studio.modules.workflows.jgraph;

class PortAdapter implements Cloneable {
	public boolean input;
	public int index;

	public PortAdapter(boolean input, int index) {
		this.input = input;
		this.index = index;
	}
}