package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author afischer
 */
public class NestedHyperworkflow extends Hyperworkflow{

	private List<Connection> connections;
	private List<Hyperworkflow> children;
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, int id) {
		super(parent, name, id);
		connections = new ArrayList<Connection>();
		children = new ArrayList<Hyperworkflow>();
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
	 * The connection must not exist already, the connection source has to be child of the current NestedHyperworkflow
	 * and the target port must not be blocked by another connection
	 * @param conn - the Connection to add
	 * @return true, if adding was successful
	 */
	public boolean addConnection(Connection conn) {
		//check for null reference and if connection does not already exist
		if (conn != null && !connections.contains(conn)) {
			//make sure that connection source exists as child of current NestedGHyperworkflow
			if (children.contains((Hyperworkflow)conn.getSource())) {
				//retrieve already existing incoming connection of target Tool
				Connection targetBlocked = conn.getTarget().getInputBlockageMap().get(conn.getTargPort());
				//if it is null, there is no incoming connection at target port
				if (targetBlocked == null) {
					//block input port of target tool
					conn.getTarget().getInputBlockageMap().put(conn.getTargPort(), conn);
					//add connection to list
					return connections.add(conn);
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
			//remove target port blockage
			conn.getTarget().getInputBlockageMap().remove(conn.getTargPort());
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
		if (hwf != null && !children.contains(hwf)) return children.add(hwf);
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
			
			//delete a tool
			if (hwf instanceof Tool) {
				Tool thwf = (Tool)hwf;
				
				//remove outgoing connections
				List<Connection> outgoing = new ArrayList<Connection>();
				for (Connection c : connections) {
					if (c.getSource().equals(thwf)) outgoing.add(c);
				}
				while (outgoing.size() > 0) {
					removeConnection(outgoing.remove(0));
				}
				
				//remove incoming connections
				List<Connection> incoming = new ArrayList<Connection>();
				incoming.addAll(thwf.getInputBlockageMap().values());
				while (incoming.size() > 0) {
					 incoming.get(0).getSource().getParent().removeConnection(incoming.remove(0));
				}
				
				//remove tool itself 
				return children.remove(hwf);
			}
			
			//delete a NestedHyperworkflow recursively
			if (hwf instanceof NestedHyperworkflow) {
				NestedHyperworkflow nhwf = (NestedHyperworkflow)hwf;
				while (nhwf.children.size() > 0) {
					//remove all the children of the NestedHyperworkflow
					nhwf.removeChild(nhwf.children.get(0));
				}
				//remove Nestedhyperworkflow itself
				return children.remove(hwf);
			}
		}
		return false;
	}
	
	@Override
	public void unfold() {
		//TODO
	}
	
	public static void main(String[] args) {
		NestedHyperworkflow root = new NestedHyperworkflow(null, "root", 0);
		NestedHyperworkflow nested = new NestedHyperworkflow(root, "nested", 7);
		Tool t1 = new Tool(root, "t1", 1);
		t1.getOutputPorts().add(new Port("out", EPortType.FILE));
		Tool t2 = new Tool(root, "t2", 2);
		t2.getOutputPorts().add(new Port("out1", EPortType.FILE));
		t2.getOutputPorts().add(new Port("out1", EPortType.FILE));
		Tool t3 = new Tool(root, "t3", 3);
		t3.getInputPorts().add(new Port("in", EPortType.FILE));
		t3.getOutputPorts().add(new Port("out", EPortType.FILE));
		Tool t4 = new Tool(root, "t4", 4);
		t4.getInputPorts().add(new Port("in1", EPortType.FILE));
		t4.getInputPorts().add(new Port("in2", EPortType.FILE));
		Tool t5 = new Tool(root, "t5", 5);
		t5.getInputPorts().add(new Port("in1", EPortType.FILE));
		t5.getInputPorts().add(new Port("in2", EPortType.FILE));
		t5.getOutputPorts().add(new Port("out", EPortType.FILE));
		Tool t6 = new Tool(root, "t6", 6);
		t6.getInputPorts().add(new Port("in", EPortType.FILE));
		t6.getOutputPorts().add(new Port("out", EPortType.FILE));
		
		System.out.println("Add children: ");
		System.out.println("t1: " + root.addChild(t1));
		System.out.println("t2: " + root.addChild(t2));
		System.out.println("t3: " + root.addChild(t3));
		System.out.println("t4: " + root.addChild(t4));
		System.out.println("nested: " + root.addChild(nested));
		System.out.println("nested.t5: " + nested.addChild(t5));
		System.out.println("nested.t6: " + nested.addChild(t6));
		
		System.out.println("\nAdd connections: ");
		System.out.println("t1 -> nested.t5: " + root.addConnection(new Connection(t1, t1.getOutputPorts().get(0), t5, t5.getInputPorts().get(1))));
		System.out.println("t2 -> nested.t5: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(1), t5, t5.getInputPorts().get(0))));
		System.out.println("t2 -> t3: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(0), t3, t3.getInputPorts().get(0))));
		System.out.println("t3 -> t4: " + root.addConnection(new Connection(t3, t3.getOutputPorts().get(0), t4, t4.getInputPorts().get(0))));
		System.out.println("nested.t5 -> nested.t6: " + nested.addConnection(new Connection(t5, t5.getOutputPorts().get(0), t6, t6.getInputPorts().get(0))));
		System.out.println("nested.t6 -> t4: " + nested.addConnection(new Connection(t6, t6.getOutputPorts().get(0), t4, t4.getInputPorts().get(1))));		
	}
}
