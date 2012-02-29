package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nested composite of IHyperworkflow composite pattern
 * @author afischer
 *
 */
public class NestedHyperworkflow implements IHyperworkflow{

	private NestedHyperworkflow parent;
	private String name;
	private int id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	private Map<IHyperworkflow, List<Port>> portBlockageMap;
	
	private List<Connection> connections;
	private List<IHyperworkflow> children;
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = id;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
		this.portBlockageMap = new HashMap<IHyperworkflow, List<Port>>();
		connections = new ArrayList<Connection>();
		children = new ArrayList<IHyperworkflow>();
	}
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, int id) {
		this(parent, name, id, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	/**
	 * Copy constructor
	 * @param toCopy
	 */
	public NestedHyperworkflow(NestedHyperworkflow toCopy) {
		this(toCopy.parent, toCopy.name, toCopy.id, new ArrayList<Port>(toCopy.inputPorts), new ArrayList<Port>(toCopy.outputPorts));
		connections = new ArrayList<Connection>(toCopy.connections);
		children = new ArrayList<IHyperworkflow>(toCopy.children);

		//copy the portBlockageMap, has to be done semi-manually since "new HashMap(toCopy.portBlockageMap)" only does a shallow copy and thus, refers to the SAME entry lists 
		portBlockageMap = new HashMap<IHyperworkflow, List<Port>>();
		for (IHyperworkflow hwf : toCopy.portBlockageMap.keySet()) {
			portBlockageMap.put(hwf, new ArrayList<Port>(toCopy.portBlockageMap.get(hwf)));
		}
	}
	
	public NestedHyperworkflow getParent() { return parent; }
	public List<Port> getOutputPorts() {	return outputPorts; }
	public Map<IHyperworkflow, List<Port>> getPortBlockageMap() { return portBlockageMap; }
	public int getId() {	return id; }
	public List<Port> getInputPorts() { return inputPorts;	}
	public String getName() { return name; }
	
	/** @return a list of connections */
	public List<Connection> getConnections() { return connections; 	}

	/** @return a list of direct Hyperworkflow children of the current NestedHyperworkflow */
	public List<IHyperworkflow> getChildren() {	return children; }
	
	/**
	 * Adds a new connection to the NestedHyperworkflow's connections-List.
	 * The connection must not exist already, the connection source and target has to be child of the current NestedHyperworkflow 
	 * or the NestedHyperworkflow itself and the target port must not be blocked by another connection
	 * @param conn - the Connection to add
	 * @return true, if adding was successful
	 */
	public boolean addConnection(Connection conn) {
		//TODO infer types as far as possible if conn now blocks a generic port
		
		//check for null reference, ensure connection does not already exist, check port compatibility
		if (conn != null && !connections.contains(conn) && conn.getSrcPort().isCompatibleTo(conn.getTargPort())) {
			
			//ensure that source is NestedHyperworkflow itself or a child and has the specified source port
			if ((children.contains(conn.getSource()) && conn.getSource().getOutputPorts().contains(conn.getSrcPort())) || 
					(conn.getSource().equals(this) && this.getInputPorts().contains(conn.getSrcPort()))) {
				//ensure target is a child or current NestedHyperworkflow itself and has the specified target port
				if ((children.contains(conn.getTarget()) && conn.getTarget().getInputPorts().contains(conn.getTargPort())) || 
						(conn.getTarget().equals(this) && this.getOutputPorts().contains(conn.getTargPort()))) {
					//check if target port is not blocked already
					
					Connection targetBlocked;
					if (!portBlockageMap.containsKey(conn.getTarget())) portBlockageMap.put(conn.getTarget(), new ArrayList<Port>());
					List<Port> blockedPorts = portBlockageMap.get(conn.getTarget());
					if (!blockedPorts.contains(conn.getTargPort())) targetBlocked = null;
					else targetBlocked = new Connection(null, null, null, null); 
					
					//if targetBlocked is null, the port is still open
					if (targetBlocked == null) {
						
						//try to add connection and block the previously free target input port
						if (connections.add(conn) && portBlockageMap.get(conn.getTarget()).add(conn.getTargPort())) {

							//if the connection is only between two simple Elements remove the now occupied ports from current NestedHyperworkflow
							if (conn.getSource() instanceof IElement && conn.getTarget() instanceof IElement) {
								List<Port> emptyList = new ArrayList<Port>();
								List<Port> inputs = new ArrayList<Port>();
								inputs.add(conn.getTargPort());
								List<Port> outputs = new ArrayList<Port>();
								outputs.add(conn.getSrcPort());
								//actual port removal from parent Hyperworkflows
								if (!(this.propagatePortRemoval(conn.getSource(), emptyList, outputs) && this.propagatePortRemoval(conn.getTarget(), inputs, emptyList))) {
									//propagation failed -> UNDO EVERYTHING
									this.propagateAdditionalPorts(conn.getSource(), emptyList, outputs);
									this.propagateAdditionalPorts(conn.getTarget(), inputs, emptyList);
									
									//remove previously blocked port from portBlockageMap
									portBlockageMap.get(conn.getTarget()).remove(conn.getTargPort());
									
									//if there are no more blocked ports for the target tool, remove its map entries completely
									if (portBlockageMap.get(conn.getTarget()) != null && portBlockageMap.get(conn.getTarget()).isEmpty())
										portBlockageMap.remove(conn.getTarget());

									connections.remove(conn);
									return false;
								}
							}

							return true;
						}
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
		//TODO infer types as far as possible if the removal of conn frees an otherwise generic port
		
		//check for null reference and make sure the connection exists
		if (conn != null && connections.contains(conn)) {
			
			//try to remove connection and free the previously blocked target input port
			if (connections.remove(conn) && portBlockageMap.get(conn.getTarget()).remove(conn.getTargPort())) {
				
				//if the connection is only between two simple Elements add the now free ports to current NestedHyperworkflow
				if (conn.getSource() instanceof IElement && conn.getTarget() instanceof IElement) {
					List<Port> emptyList = new ArrayList<Port>();
					List<Port> inputs = new ArrayList<Port>();
					inputs.add(conn.getTargPort());
					List<Port> outputs = new ArrayList<Port>();
					outputs.add(conn.getSrcPort());
					//actual port adding to parent Hyperworkflows
					if (!(this.propagateAdditionalPorts(conn.getSource(), emptyList, outputs) && this.propagateAdditionalPorts(conn.getTarget(), inputs, emptyList))) {
						//propagation failed -> UNDO EVERYTHING
						this.propagatePortRemoval(conn.getSource(), emptyList, outputs);
						this.propagatePortRemoval(conn.getTarget(), inputs, emptyList);
						portBlockageMap.get(conn.getTarget()).add(conn.getTargPort());
						connections.add(conn);
						return false;
					}
				}
				
				//if there are no more blocked ports for the target tool, remove its map entries completely
				if (portBlockageMap.get(conn.getTarget()) != null && portBlockageMap.get(conn.getTarget()).isEmpty())
					portBlockageMap.remove(conn.getTarget());
				
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a Hyperworkflow child to the NestedHyperworkflow
	 * @param hwf - child to add
	 * @return true, if adding was successful
	 */
	public boolean addChild(IHyperworkflow hwf) {
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
	public boolean removeChild(IHyperworkflow hwf) {
		if (hwf != null && children.contains(hwf)) {
			
			//partition connections into incoming and outgoing
			List<Connection> incoming = new ArrayList<Connection>();
			List<Connection> outgoing = new ArrayList<Connection>();
			for (Connection c : connections) {
				if (c.getTarget().equals(hwf)) incoming.add(c);
				if (c.getSource().equals(hwf)) outgoing.add(c);
			}
			
			//remove incoming connections
			while (incoming.size() > 0) {
				removeConnection(incoming.remove(0));
			}
			
			//remove outgoing connections
			while (outgoing.size() > 0) {
				removeConnection(outgoing.remove(0));
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
	private boolean propagateAdditionalPorts(IHyperworkflow hwf, List<Port> additionalInputs, List<Port> additionalOutputs) {
		List<Port> innerInputPorts = new ArrayList<Port>();
		List<Port> innerOutputPorts = new ArrayList<Port>();
		
		if (this.getParent() == null) return true;
		
		//create new input ports based on the specified ports of a child node
		for (Port p : additionalInputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (!this.getInputPorts().contains(newPort)) innerInputPorts.add(newPort);
		}
		//create new output ports based on the specified ports of a child node
		for (Port p : additionalOutputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (!this.getOutputPorts().contains(newPort)) innerOutputPorts.add(newPort);
		}
		
		//add the new input and output ports
		this.getInputPorts().addAll(innerInputPorts);
		this.getOutputPorts().addAll(innerOutputPorts);
		
		//propagate the port adding to own parent
		if (getParent().propagateAdditionalPorts(this, innerInputPorts, innerOutputPorts)) {
			//all went well, thus the port adding was successful
			return true;
		} else {
			//some parent had a problem with the adding propagation -> UNDO EVERYTHING
			this.getInputPorts().removeAll(innerInputPorts);		//remove previously added input ports
			this.getOutputPorts().removeAll(innerOutputPorts);	//remove previously added output ports
			return false;
		}
	}
	
	/**
	 * Removes the specified ports of a given Hyperworkflow hwf from the current NestedHyperworkflow and all its parents 
	 * (along with all connections that are somehow linked to those ports)
	 * @param hwf
	 * @return true, if propagation process is successful. On occurrence of any problem the whole removal process is undone
	 */
	private boolean propagatePortRemoval(IHyperworkflow hwf, List<Port> removedInputs, List<Port> removedOutputs) {
		List<Port> innerInputPorts = new ArrayList<Port>();
		List<Port> innerOutputPorts = new ArrayList<Port>();
		
		if (this.getParent() == null) return true;
		
		//find inner input ports that will be removed
		for (Port p : removedInputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (this.getInputPorts().contains(newPort)) innerInputPorts.add(newPort);
			else return false;
		}
		//find inner output ports that will be removed
		for (Port p : removedOutputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (this.getOutputPorts().contains(newPort)) innerOutputPorts.add(newPort);
			else return false;
		}
		
		//get all incoming and outgoing connections that are linked to the soon-to-be-removed ports
		List<Connection> incoming = new ArrayList<Connection>();
		List<Connection> outgoing = new ArrayList<Connection>();
		for (Connection c : parent.getConnections()) {
			if (c.getTarget().equals(this) && innerInputPorts.contains(c.getTargPort())) incoming.add(c);
			if (c.getSource().equals(this) && innerOutputPorts.contains(c.getSrcPort())) outgoing.add(c);
		}
		
		//delete incoming connection to input ports that will be removed
		for (Connection c : incoming) {
			parent.removeConnection(c);
		}
		//delete outgoing connections from output ports that will be removed
		for (Connection c : outgoing) {
			parent.removeConnection(c);
		}
		
		//remove inner input and output ports
		this.getInputPorts().removeAll(innerInputPorts);
		this.getOutputPorts().removeAll(innerOutputPorts);
		
		//propagate the port removal to own parent
		if (getParent().propagateAdditionalPorts(this, innerInputPorts, innerOutputPorts)) {
			//all went well, thus the port removal was successful
			return true;
		} else {
			//some parent had a problem with the removal propagation -> UNDO EVERYTHING
			this.getInputPorts().addAll(innerInputPorts);	//add previously removed input ports
			this.getOutputPorts().addAll(innerOutputPorts);	//add previously removed output ports
			for (Connection c : incoming) parent.addConnection(c);	//add previously removed incoming connections
			for (Connection c : outgoing) parent.addConnection(c);	//add previously removed outgoing connections
			return false;	//report failure
		}
	}
	
	@Override
	public boolean equals(Object other) {
		//FIXME think of something more reasonable to find equal NestedHyperworkflows
		//NestedHyperworkflows are equal if they have the same id
		boolean result = (other != null && other instanceof NestedHyperworkflow);
		if (result) {
			NestedHyperworkflow oh = (NestedHyperworkflow)other;
			result = (this.getId() == oh.getId());
		}
		return result;
	}
	
	@Override
	public String toString() {
		return name + ": " + children + ", " + connections;
	}
	
	@Override
	public Collection<IHyperworkflow> unfold() {
		//TODO unfold Or nodes and NestedHyperworkflow children
		return null;
	}
	
	public static void main(String[] args) {
		NestedHyperworkflow root = new NestedHyperworkflow(null, "root", 0);
		IElement t1 = new Tool(root, "t1", 1);
		IElement t2 = new Tool(root, "t2", 2);
		IElement t3 = new Tool(root, "t3", 3);
		IElement or = new Or(root, "or", 4);
		t1.getOutputPorts().add(new Port("out", EPortType.FILE));
		t2.getOutputPorts().add(new Port("out", EPortType.FILE));
		t3.getInputPorts().add(new Port("in", EPortType.FILE));
		
		System.out.println("Add children: ");
		System.out.println("t1: " + root.addChild(t1));
		System.out.println("t2: " + root.addChild(t2));
		System.out.println("t3: " + root.addChild(t3));
		System.out.println("or: " + root.addChild(or));
		
		System.out.println("\nAdd connections: ");
		System.out.println("t1 -> or: " + root.addConnection(new Connection(t1, t1.getOutputPorts().get(0), or, or.getInputPorts().get(0))));
		System.out.println("t2 -> or: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(0), or, or.getInputPorts().get(1))));
		System.out.println("or -> t3: " + root.addConnection(new Connection(or, or.getOutputPorts().get(0), t3, t3.getInputPorts().get(0))));
		
		Collection<IHyperworkflow> results = or.unfold();
		System.out.println(results);
		
//		NestedHyperworkflow root = new NestedHyperworkflow(null, "root", 0);
//		NestedHyperworkflow nested = new NestedHyperworkflow(root, "nested", 7);
//		IElement t1 = new Tool(root, "t1", 1);
//		t1.getOutputPorts().add(new Port("out", EPortType.FILE));
//		IElement t2 = new Tool(root, "t2", 2);
//		t2.getOutputPorts().add(new Port("out1", EPortType.FILE));
//		t2.getOutputPorts().add(new Port("out2", EPortType.FILE));
//		IElement t3 = new Tool(root, "t3", 3);
//		t3.getInputPorts().add(new Port("in", EPortType.FILE));
//		t3.getOutputPorts().add(new Port("out", EPortType.FILE));
//		IElement t4 = new Tool(root, "t4", 4);
//		t4.getInputPorts().add(new Port("in1", EPortType.FILE));
//		t4.getInputPorts().add(new Port("in2", EPortType.FILE));
//		IElement t5 = new Tool(root, "t5", 5);
//		t5.getInputPorts().add(new Port("in1", EPortType.FILE));
//		t5.getInputPorts().add(new Port("in2", EPortType.FILE));
//		t5.getOutputPorts().add(new Port("out", EPortType.FILE));
//		IElement t6 = new Tool(root, "t6", 6);
//		t6.getInputPorts().add(new Port("in", EPortType.FILE));
//		t6.getOutputPorts().add(new Port("out", EPortType.FILE));
//		
//		System.out.println("Add children: ");
//		System.out.println("t1: " + root.addChild(t1));
//		System.out.println("t2: " + root.addChild(t2));
//		System.out.println("t3: " + root.addChild(t3));
//		System.out.println("t4: " + root.addChild(t4));
//		System.out.println("nested: " + root.addChild(nested));
//		System.out.println("nested.t5: " + nested.addChild(t5));
//		System.out.println("nested.t6: " + nested.addChild(t6));
//		
//		System.out.println("\nAdd connections: ");
//		System.out.println("t1 -> nested: " + root.addConnection(new Connection(t1, t1.getOutputPorts().get(0), nested, nested.getInputPorts().get(1))));
//		System.out.println("t2 -> t3: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(0), t3, t3.getInputPorts().get(0))));
//		System.out.println("t2 -> nested: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(1), nested, nested.getInputPorts().get(0))));
//		System.out.println("t3 -> t4: " + root.addConnection(new Connection(t3, t3.getOutputPorts().get(0), t4, t4.getInputPorts().get(0))));
//		System.out.println("nested -> t4: " + root.addConnection(new Connection(nested, nested.getOutputPorts().get(1), t4, t4.getInputPorts().get(1))));
//		System.out.println("nested -> t5: " + nested.addConnection(new Connection(nested, nested.getInputPorts().get(0), t5, t5.getInputPorts().get(0))));
//		System.out.println("nested -> t5: " + nested.addConnection(new Connection(nested, nested.getInputPorts().get(1), t5, t5.getInputPorts().get(1))));
//		System.out.println("nested.t5 -> nested.t6: " + nested.addConnection(new Connection(t5, t5.getOutputPorts().get(0), t6, t6.getInputPorts().get(0))));
//		System.out.println("nested.t6 -> nested: " + nested.addConnection(new Connection(t6, t6.getOutputPorts().get(0), nested, nested.getOutputPorts().get(0))));
//		
//		System.out.println("\nExisting connections: ");
//		System.out.println(root.getConnections());
//		System.out.println(nested.getConnections());
//		
//		System.out.println("\nRemove connections: ");
//		System.out.println("nested.t5 -> nested.t6: " + nested.removeConnection(new Connection(t5, t5.getOutputPorts().get(0), t6, t6.getInputPorts().get(0))));
//		System.out.println(nested.getConnections());
	}
}
