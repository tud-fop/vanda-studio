package org.vanda.studio.modules.workflows;

/**
 * @author afischer
 */
public class Connection {

	private Hyperworkflow source;
	private Port srcPort;
	private Hyperworkflow target;
	private Port targPort;

	public Connection() {
	}

	public Connection(Hyperworkflow source, Port srcPort, Hyperworkflow target,
			Port targPort) {
		this.source = source;
		this.srcPort = srcPort;
		this.target = target;
		this.targPort = targPort;
	}

	@Override
	public boolean equals(Object other) {
		boolean result = (other != null && other instanceof Connection);
		if (result) {
			Connection oc = (Connection) other;
			result = srcPort.equals(oc.srcPort) && targPort.equals(oc.targPort)
					&& source.equals(oc.source) && target.equals(oc.target);
		}
		return result;
	}

	/**
	 * @return the source Hyperworkflow
	 */
	public Hyperworkflow getSource() {
		return source;
	}

	/**
	 * @return the output port of the source Hyperworkflow
	 */
	public Port getSrcPort() {
		return srcPort;
	}

	/**
	 * @return the target Hyperworkflow
	 */
	public Hyperworkflow getTarget() {
		return target;
	}

	/**
	 * @return the input port of the target Hyperworkflow
	 */
	public Port getTargPort() {
		return targPort;
	}

	public void setSource(Hyperworkflow source) {
		this.source = source;
	}

	public void setSrcPort(Port srcPort) {
		this.srcPort = srcPort;
	}

	public void setTarget(Hyperworkflow target) {
		this.target = target;
	}

	public void setTargPort(Port targPort) {
		this.targPort = targPort;
	}

	@Override
	public String toString() {
		return source.getName() + "." + srcPort.getName() + " -> "
				+ target.getName() + "." + targPort.getName();
	}
}
