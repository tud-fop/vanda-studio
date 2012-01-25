package org.vanda.studio.modules.workflows;

/**
 * @author afischer
 */
public class Connection {
	private Tool source;
	private Port srcPort;
	private Tool target;
	private Port targPort;
		
	public Connection(Tool source, Port srcPort, Tool target, Port targPort) {
		this.source = source;
		this.srcPort = srcPort;
		this.target = target;
		this.targPort = targPort;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean result = (other != null && other instanceof Connection);
		if (result) {
			Connection oc = (Connection)other;
			result = srcPort.equals(oc.srcPort) && targPort.equals(oc.targPort) && source.equals(oc.source) && target.equals(oc.target);
		}
		return result;
	}

	/**
	 * @return the source Tool
	 */
	public Tool getSource() {
		return source;
	}

	/**
	 * @return the output port of the source Tool
	 */
	public Port getSrcPort() {
		return srcPort;
	}

	/**
	 * @return the target Tool
	 */
	public Tool getTarget() {
		return target;
	}

	/**
	 * @return the input port of the target Tool
	 */
	public Port getTargPort() {
		return targPort;
	}
}
