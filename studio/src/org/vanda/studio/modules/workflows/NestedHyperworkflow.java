package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author afischer
 */
public class NestedHyperworkflow extends Hyperworkflow{

	private List<Connection> connections;
	private List<Hyperworkflow> children;
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		super(parent, name, id, inputPorts, outputPorts);
		connections = new ArrayList<Connection>();
		children = new ArrayList<Hyperworkflow>();
	}
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, int id) {
		this(parent, name, id, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	/**
	 * @return a list of connections
	 */
	public List<Connection> getConnections() {
		return connections;
	}

	/**
	 * @return a list of direct Hyperworkflow children of the current NestedHyperworkflow
	 */
	public List<Hyperworkflow> getChildren() {
		return children;
	}
	
	/**
	 * Adds a new connection to the NestedHyperworkflow's connections-List.
	 * The connection must not exist already, the connection source and target has to be child of the current NestedHyperworkflow 
	 * or the NestedHyperworkflow itself and the target port must not be blocked by another connection
	 * @param conn - the Connection to add
	 * @return true, if adding was successful
	 */
	public boolean addConnection(Connection conn) {
		//check for null reference and if connection does not already exist
		if (conn != null && !connections.contains(conn)) {
			
			//ensure that source is NestedHyperworkflow itself or a child
			if (children.contains(conn.getSource()) || conn.getSource().equals(this)) {
				//ensure target is a child or current NestedHyperworkflow itself
				if (children.contains(conn.getTarget()) || conn.getTarget().equals(this)) {
					//check if target port is not blocked already
					Connection targetBlocked = conn.getTarget().getPortIncomingConnectionMap().get(conn.getTargPort());
					//if targetBlocked is null, the port is still open
					if (targetBlocked == null) {
						
						//FIXME first add connection, if successful block port, then propagate blocked ports, undo if problem occurs
						
						//if the connection is only between two simple Elements remove the now occupied ports from current NestedHyperworkflow
						if (conn.getSource() instanceof Element && conn.getTarget() instanceof Element) {
							List<Port> emptyList = new ArrayList<Port>();
							List<Port> inputs = new ArrayList<Port>();
							inputs.add(conn.getTargPort());
							List<Port> outputs = new ArrayList<Port>();
							outputs.add(conn.getSrcPort());
							//actual port removal from parent Hyperworkflows
							if (!this.propagatePortRemoval(conn.getSource(), emptyList, outputs) || !(this.propagatePortRemoval(conn.getTarget(), inputs, emptyList)))
								//if there occurs a problem, prevent further adding of child node 
								return false;
						}
						
						//block input port of target tool
						conn.getTarget().getPortIncomingConnectionMap().put(conn.getTargPort(), conn);
												
						//add connection to list
						return connections.add(conn);
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Removes an existing  connection from the NestedHyperworkflow's connections-List
	 * as well as the blockage flag of the connection's target port
	 * @param conn - the Connection to remove
	 * @return true, if removal was successful
	 */
	public boolean removeConnection(Connection conn) {
		//check for null reference and make sure the connection exists
		if (conn != null && connections.contains(conn)) {
			
			//FIXME first removal of connection, if successful free blocked port, then propagate free ports, undo if problem occurs
			
			//if the connection is only between two simple Elements add the now free ports to current NestedHyperworkflow
			if (conn.getSource() instanceof Element && conn.getTarget() instanceof Element) {
				List<Port> emptyList = new ArrayList<Port>();
				List<Port> inputs = new ArrayList<Port>();
				inputs.add(conn.getTargPort());
				List<Port> outputs = new ArrayList<Port>();
				outputs.add(conn.getSrcPort());
				//actual port adding to parent Hyperworkflows
				if (!this.propagateAdditionalPorts(conn.getSource(), emptyList, outputs) || !(this.propagateAdditionalPorts(conn.getTarget(), inputs, emptyList)))
					//if there occurs a problem, prevent further adding of child node 
					return false;
			}
			
			//remove target port blockage
			conn.getTarget().getPortIncomingConnectionMap().remove(conn.getTargPort());
			//remove connection itself
			return connections.remove(conn);
		}
		return false;
	}
	
	/**
	 * Adds a Hyperworkflow child to the NestedHyperworkflow
	 * @param hwf - child to add
	 * @return true, if adding was successful
	 */
	public boolean addChild(Hyperworkflow hwf) {
		//check for null reference and make sure the new child does not exist already
		if (hwf != null && !children.contains(hwf)) {
			
			//add child if possible
			if (children.add(hwf)) {
				//check for necessary creation of new inner ports
				if (propagateAdditionalPorts(hwf, hwf.getInputPorts(), hwf.getOutputPorts())) return true;
				else {
					//undo everything
					children.remove(hwf);
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Removes a Hyperworkflow child from the NestedHyperworkflow,
	 * all associated connections, and, if present, nested children
	 * @param hwf- child to remove
	 * @return true, if removal was successful
	 */
	public boolean removeChild(Hyperworkflow hwf) {
		if (hwf != null && children.contains(hwf)) {
			
			//remove outgoing connections
			List<Connection> outgoing = new ArrayList<Connection>();
			for (Connection c : connections) {
				if (c.getSource().equals(hwf)) outgoing.add(c);
			}
			while (outgoing.size() > 0) {
				removeConnection(outgoing.remove(0));
			}
			
			//remove incoming connections
			List<Connection> incoming = new ArrayList<Connection>();
			incoming.addAll(hwf.getPortIncomingConnectionMap().values());
			while (incoming.size() > 0) {
				 incoming.get(0).getSource().getParent().removeConnection(incoming.remove(0));
			}
			
			//remove child if possible
			if (children.remove(hwf)) {
				//check for necessary removal of new inner ports
				if (propagatePortRemoval(hwf, hwf.getInputPorts(), hwf.getOutputPorts())) return true;
				else {
					//undo everything
					children.add(hwf);
					for (Connection c : incoming) this.addConnection(c);
					for (Connection c : outgoing) this.addConnection(c);
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Adds the specified ports of a given Hyperworkflow hwf to the current NestedHyperworkflow and all its parents
	 * @param hwf
	 * @return true, if propagation process is successful. On occurrence of any problem the whole adding process is undone
	 */
	private boolean propagateAdditionalPorts(Hyperworkflow hwf, List<Port> additionalInputs, List<Port> additionalOutputs) {
		List<Port> innerInputPorts = new ArrayList<Port>();
		List<Port> innerOutputPorts = new ArrayList<Port>();
		
		if (this.getParent() == null) return true;
		
		for (Port p : additionalInputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (!this.getInputPorts().contains(newPort)) innerInputPorts.add(newPort);
		}
		for (Port p : additionalOutputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (!this.getOutputPorts().contains(newPort)) innerOutputPorts.add(newPort);
		}
		
		this.getInputPorts().addAll(innerInputPorts);
		this.getOutputPorts().addAll(innerOutputPorts);
		
		if (getParent().propagateAdditionalPorts(this, innerInputPorts, innerOutputPorts)) {
			return true;
		} else {
			this.getInputPorts().removeAll(innerInputPorts);
			this.getOutputPorts().removeAll(innerOutputPorts);
			return false;
		}
	}
	
	/**
	 * Removes the specified ports of a given Hyperworkflow hwf from the current NestedHyperworkflow and all its parents
	 * @param hwf
	 * @return true, if propagation process is successful. On occurrence of any problem the whole removal process is undone
	 */
	private boolean propagatePortRemoval(Hyperworkflow hwf, List<Port> removedInputs, List<Port> removedOutputs) {
		List<Port> innerInputPorts = new ArrayList<Port>();
		List<Port> innerOutputPorts = new ArrayList<Port>();
		
		if (this.getParent() == null) return true;
		
		for (Port p : removedInputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (this.getInputPorts().contains(newPort)) innerInputPorts.add(newPort);
			else return false;
		}
		for (Port p : removedOutputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (this.getOutputPorts().contains(newPort)) innerOutputPorts.add(newPort);
			else return false;
		}
		
		this.getInputPorts().removeAll(innerInputPorts);
		this.getOutputPorts().removeAll(innerOutputPorts);
		
		if (getParent().propagateAdditionalPorts(this, innerInputPorts, innerOutputPorts)) {
			return true;
		} else {
			this.getInputPorts().addAll(innerInputPorts);
			this.getOutputPorts().addAll(innerOutputPorts);
			return false;
		}
	}
	
	@Override
	public void unfold() {
		//TODO
	}
	
	public static void main(String[] args) {
		NestedHyperworkflow root = new NestedHyperworkflow(null, "root", 0);
		NestedHyperworkflow nested = new NestedHyperworkflow(root, "nested", 7);
		Element t1 = new Element(root, "t1", 1);
		t1.getOutputPorts().add(new Port("out", EPortType.FILE));
		Element t2 = new Element(root, "t2", 2);
		t2.getOutputPorts().add(new Port("out1", EPortType.FILE));
		t2.getOutputPorts().add(new Port("out2", EPortType.FILE));
		Element t3 = new Element(root, "t3", 3);
		t3.getInputPorts().add(new Port("in", EPortType.FILE));
		t3.getOutputPorts().add(new Port("out", EPortType.FILE));
		Element t4 = new Element(root, "t4", 4);
		t4.getInputPorts().add(new Port("in1", EPortType.FILE));
		t4.getInputPorts().add(new Port("in2", EPortType.FILE));
		Element t5 = new Element(root, "t5", 5);
		t5.getInputPorts().add(new Port("in1", EPortType.FILE));
		t5.getInputPorts().add(new Port("in2", EPortType.FILE));
		t5.getOutputPorts().add(new Port("out", EPortType.FILE));
		Element t6 = new Element(root, "t6", 6);
		t6.getInputPorts().add(new Port("in", EPortType.FILE));
		t6.getOutputPorts().add(new Port("out", EPortType.FILE));
		
		System.out.println("Add children: ");
		System.out.println("t1: " + root.addChild(t1));
		System.out.println(t1.getInputPorts() + " & " + t1.getOutputPorts());
		System.out.println("t2: " + root.addChild(t2));
		System.out.println(t2.getInputPorts() + " & " + t2.getOutputPorts());
		System.out.println("t3: " + root.addChild(t3));
		System.out.println(t3.getInputPorts() + " & " + t3.getOutputPorts());
		System.out.println("t4: " + root.addChild(t4));
		System.out.println(t4.getInputPorts() + " & " + t4.getOutputPorts());
		System.out.println("nested: " + root.addChild(nested));
		System.out.println("nested: " + nested.getInputPorts() + " & " + nested.getOutputPorts());
		System.out.println("nested.t5: " + nested.addChild(t5));
		System.out.println(t5.getInputPorts() + " & " + t5.getOutputPorts());
		System.out.println("nested.t6: " + nested.addChild(t6));
		System.out.println(t6.getInputPorts() + " & " + t6.getOutputPorts());
		System.out.println("nested: " + nested.getInputPorts() + " & " + nested.getOutputPorts());
		
		System.out.println("\nAdd connections: ");
		System.out.println("t1 -> nested: " + root.addConnection(new Connection(t1, t1.getOutputPorts().get(0), nested, nested.getInputPorts().get(1))));
		System.out.println("t2 -> t3: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(0), t3, t3.getInputPorts().get(0))));
		System.out.println("t2 -> nested: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(1), nested, nested.getInputPorts().get(0))));
		System.out.println("t3 -> t4: " + root.addConnection(new Connection(t3, t3.getOutputPorts().get(0), t4, t4.getInputPorts().get(0))));
		System.out.println("nested -> t4: " + root.addConnection(new Connection(nested, nested.getOutputPorts().get(1), t4, t4.getInputPorts().get(1))));
		System.out.println("nested -> t5: " + nested.addConnection(new Connection(nested, nested.getInputPorts().get(0), t5, t5.getInputPorts().get(0))));
		System.out.println("nested -> t5: " + nested.addConnection(new Connection(nested, nested.getInputPorts().get(1), t5, t5.getInputPorts().get(1))));
		System.out.println("nested.t5 -> nested.t6: " + nested.addConnection(new Connection(t5, t5.getOutputPorts().get(0), t6, t6.getInputPorts().get(0))));
		System.out.println("nested.t6 -> nested: " + nested.addConnection(new Connection(t6, t6.getOutputPorts().get(0), nested, nested.getOutputPorts().get(0))));
		
		
		System.out.println("\nExisting connections: ");
		System.out.println(root.getConnections());
		System.out.println(nested.getConnections());
	}
}
