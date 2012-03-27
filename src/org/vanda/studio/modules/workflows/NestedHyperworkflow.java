package org.vanda.studio.modules.workflows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * Nested composite of IHyperworkflow composite pattern
 * @author afischer
 */
public class NestedHyperworkflow extends IHyperworkflow{
	
	//TODO implement real cloning, a full deep copy
	public Object clone() throws CloneNotSupportedException { return new NestedHyperworkflow(this); }
	
	private MultiplexObserver<IHyperworkflow> addObservable;
	private MultiplexObserver<IHyperworkflow> modifyObservable;
	private MultiplexObserver<IHyperworkflow> removeObservable;
	private MultiplexObserver<Connection> connectObservable;
	private MultiplexObserver<Connection> disconnectObservable;
	
	public Observable<IHyperworkflow> getAddObservable() {return addObservable;}
	public Observable<Connection> getConnectObservable() {return connectObservable;}
	public Observable<Connection> getDisconnectObservable() {return disconnectObservable;}
	public Observable<IHyperworkflow> getModifyObservable() {return modifyObservable;}
	public Observable<IHyperworkflow> getRemoveObservable() {return removeObservable;}
	
	public void setDimensions(IHyperworkflow o, double[] d) {
		assert(children.contains(o));
		
		if (d[0] != o.getX() || d[1] != o.getY() || d[2] != o.getWidth() || d[3] != o.getHeight()) {
			o.setDimensions(d);
			modifyObservable.notify(o);
		}
	}
	public void ensureAbsence(IHyperworkflow o) {
		System.out.println("NHWF: ensureAbsence - " + o);
		if (removeChild(o, false)) {
			removeObservable.notify(o);
		}
	}
	public void ensureConnected(Connection c) {
		System.out.println("NHWF: ensureConnected - " + c);
		if (!addConnection(c)) {
			connectObservable.notify(c);
		}
	}
	public void ensureDisconnected(Connection c) {
		System.out.println("NHWF: ensureDisconnected - " + c);
		if (!removeConnection(c)) {
			connectObservable.notify(c);
		}
	}
	public void ensurePresence(IHyperworkflow o) {
		System.out.println("NHWF: ensurePresence - " + o);
		if (!addChild(o)) {
			addObservable.notify(o);
		}
	}
	
	//-------------------------------------------------------------------------

	private List<IHyperworkflow> children;
	private List<Connection> connections;
	private Map<IHyperworkflow, List<Port>> portBlockageMap;
	private List<String> spareIds;
	
	//-------------------------------------------------------------------------
	//----------------------------- constructors ------------------------------
	//-------------------------------------------------------------------------
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, List<Port> inputPorts, List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
		this.portBlockageMap = new HashMap<IHyperworkflow, List<Port>>();
		this.connections = new ArrayList<Connection>();
		this.children = new ArrayList<IHyperworkflow>();
		this.spareIds = new ArrayList<String>();
		
		//TODO involve these attributes in copy construction and cloning at some point
		this.addObservable = new MultiplexObserver<IHyperworkflow>();
		this.modifyObservable = new MultiplexObserver<IHyperworkflow>();
		this.removeObservable = new MultiplexObserver<IHyperworkflow>();
		this.connectObservable = new MultiplexObserver<Connection>();
		this.disconnectObservable = new MultiplexObserver<Connection>();
	}
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	public NestedHyperworkflow(String name) {
		this(null, name);
	}
	
	/**
	 * Copy constructor - creates a new NestedHyperworkflow instance (shallow copy of the original, except OR-nodes (deep copy))
	 * @param toCopy
	 */
	public NestedHyperworkflow(NestedHyperworkflow toCopy) {
		this(toCopy.getParent(), toCopy.getName(), new ArrayList<Port>(toCopy.getInputPorts()), new ArrayList<Port>(toCopy.getOutputPorts()));
		setId(toCopy.getId());
		
		this.children = new ArrayList<IHyperworkflow>();
		for (IHyperworkflow child : toCopy.children) {
			IHyperworkflow childCopy = null;
			//ensure that an or-child is copied and gets the current NestedHyperworkflow copy as parent
			if (child instanceof Or) childCopy = new Or((Or)child, this);	
			else childCopy = child;		//other children are just re-used
			children.add(childCopy);
		}
		
		this.connections = new ArrayList<Connection>(toCopy.connections);
		
		//copy the portBlockageMap, has to be done semi-manually since "new HashMap(toCopy.portBlockageMap)" only does a shallow copy and thus, refers to the SAME entry lists 
		this.portBlockageMap = new HashMap<IHyperworkflow, List<Port>>();
		for (IHyperworkflow hwf : toCopy.portBlockageMap.keySet()) {
			this.portBlockageMap.put(hwf, new ArrayList<Port>(toCopy.portBlockageMap.get(hwf)));
		}
		
		this.spareIds = new ArrayList<String>(toCopy.spareIds);
	}

	//-------------------------------------------------------------------------
	//-------------------------- getters/setters ------------------------------
	//-------------------------------------------------------------------------
	
	public Map<IHyperworkflow, List<Port>> getPortBlockageMap() { return portBlockageMap; }
	
	/** Sets the NestedHyperworkflow's id to the new value.
	 * If there was a change, the new Id is propagated so that the children can also reset their ids.
	 * @param newId - replaces the current id
	 * @return true if replacement was successful
	 */
	@Override
	public boolean setId(String newId) {
		if (newId != null && getId() != newId) { 
			super.setId(newId);
			//update children's ids as well
			for (IHyperworkflow child : children) {
				String[] idParts = child.getId().split("-");
				String newChildId = child.getParent().getId() + "-" + idParts[idParts.length - 1];
				child.setId(newChildId);
			}
			return true;
		}
		return false;
	}

	
	/** @return a list of connections */
	public List<Connection> getConnections() { return connections; 	}

	/** @return a list of direct IHyperworkflow children of the current NestedHyperworkflow */
	public List<IHyperworkflow> getChildren() {	return children; }
	
	//-------------------------------------------------------------------------
	//--------------------------- functionality -------------------------------
	//-------------------------------------------------------------------------
	
	/**
	 * Adds a new connection to the NestedHyperworkflow's connections-List.
	 * The connection must not exist already, the connection source and target has to be child of the current NestedHyperworkflow 
	 * or the NestedHyperworkflow itself and the target port must not be blocked by another connection
	 * @param conn - the Connection to add
	 * @return true, if adding was successful
	 */
	public boolean addConnection(Connection conn) {
		//TODO infer types as far as possible if conn now blocks a generic port
		//TODO prevent cycles by connection adding
		
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
				
				//set parent of added child to be the current NestedHyperworkflow
				hwf.setParent(this);
				
				//re-set the child's id
				if (!spareIds.isEmpty()) {
					//replace it with an id of a previously removed child (if there are any within spareIds-list)
					hwf.setId(spareIds.remove(0));
				} else {
					//create new id based on current child count
					hwf.setId(getId() + "-" + children.size());
				}
				
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
	public boolean removeChild(IHyperworkflow hwf, boolean removeNonconnectedChildren) {
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
				
				//save the id of the just removed child for later re-use
				spareIds.add(hwf.getId());
				
				//check for necessary removal of new inner ports
				if (propagatePortRemoval(hwf, hwf.getInputPorts(), hwf.getOutputPorts())) {
					if (removeNonconnectedChildren) removeDisconnectedChildren();
					return true;
				}
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
	 * Removes all children of the current NestedHyperworkflow that are not connected in a reasonable manner,
	 * i.e. <br/>
	 * a) algorithms with inputs and outputs where only one side is connected (then they don't have inputs which is useless or their results are not connected to
	 * 		any other node which is also useless)<br/>
	 * b) sources (a tool that only has output ports) that are not connected to any target tool<br/>
	 * c) sinks (a tool that only has input ports) that are not connected to anything, thus there is no input argument to save, display, etc.
	 */
	private void removeDisconnectedChildren() {
		List<IHyperworkflow> childrenToRemove = new ArrayList<IHyperworkflow>(children);
		List<IHyperworkflow> furtherChecking = new ArrayList<IHyperworkflow>();
		
		//iterate over all connections and find reasonably connected tools to keep them
		for (Connection c : connections) {
			
			IHyperworkflow source = c.getSource();
			if (childrenToRemove.contains(source)) {
				
				//if the source has no input ports (is a source) or its input ports are connected to something
				if (source.getInputPorts().isEmpty() || furtherChecking.contains(source)) {
					//keep the child, a.k.a. remove it from the list of soon-to-be-removed nodes
					childrenToRemove.remove(source);
					//and remove it from the list that saves nodes for double checking
					furtherChecking.remove(source);
				}
				//the source has input ports and we do not know if they are connected -> inspect the node later
				else furtherChecking.add(source);
			}
			
			IHyperworkflow target = c.getTarget();
			if (childrenToRemove.contains(target)) {
				
				//if the target has no output ports (is a sink) or its outputs are connected to something
				if (target.getOutputPorts().isEmpty() || furtherChecking.contains(target)) {
					//keep the child, a.k.a. remove it from the list of soon-to-be-removed nodes
					childrenToRemove.remove(target);
					//and remove it from the list that saves nodes for double checking
					furtherChecking.remove(target);
				}
				//the target has output ports and we do not know if they are connected -> inspect the node later
				else furtherChecking.add(target);
			}
		}
		
		//iterate over all useless tools
		for (int i = 0; i < childrenToRemove.size(); i++) {
			boolean removeNonconnected = false;
			
			//if we handle the last remaining entry of the list, set a flag to call this function recursively via removeChild()
			if (i == childrenToRemove.size() - 1) 
				removeNonconnected = true;
			
			//remove the first tool of the list
			removeChild(childrenToRemove.get(0), removeNonconnected);
		}
	}
	
	/**
	 * Adds the specified ports of a given Hyperworkflow hwf to the current NestedHyperworkflow and all its parents
	 * @param hwf
	 * @return true, if propagation process is successful. On occurrence of any problem the whole adding process is undone
	 */
	private boolean propagateAdditionalPorts(IHyperworkflow hwf, List<Port> additionalInputs, List<Port> additionalOutputs) {
		List<Port> innerInputPorts = new ArrayList<Port>();
		List<Port> innerOutputPorts = new ArrayList<Port>();
		
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
		
		//stop propagation if there is no more parent
		//FIXME: even the top most NestedHyperworkflow has input and output ports now!
		// -> this super-parent should not be shown to the user
		if (this.getParent() == null) return true;
		
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
		for (Connection c : getParent().getConnections()) {
			if (c.getTarget().equals(this) && innerInputPorts.contains(c.getTargPort())) incoming.add(c);
			if (c.getSource().equals(this) && innerOutputPorts.contains(c.getSrcPort())) outgoing.add(c);
		}
		
		//delete incoming connection to input ports that will be removed
		for (Connection c : incoming) {
			getParent().removeConnection(c);
		}
		//delete outgoing connections from output ports that will be removed
		for (Connection c : outgoing) {
			getParent().removeConnection(c);
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
			for (Connection c : incoming) getParent().addConnection(c);	//add previously removed incoming connections
			for (Connection c : outgoing) getParent().addConnection(c);	//add previously removed outgoing connections
			return false;	//report failure
		}
	}
	
	@Override
	public boolean equals(Object other) {
		//NestedHyperworkflows are equal if they have the same attributes (parent is ignored and not compared)
		boolean result = (other != null && other instanceof NestedHyperworkflow);
		if (result) {
			NestedHyperworkflow oh = (NestedHyperworkflow)other;
			result = (	getId() == oh.getId() &&
							getName().equals(oh.getName()) &&
							getChildren().equals(oh.getChildren()) &&
							getConnections().equals(oh.getConnections()) &&
							getInputPorts().equals(oh.getInputPorts()) &&
							getOutputPorts().equals(oh.getOutputPorts()) &&
							getPortBlockageMap().equals(oh.getPortBlockageMap()));
		}
		return result;
	}
	
	@Override
	public String toString() {
		return getName() + ": " + children + ", " + connections;
	}
	
	@Override
	public Collection<IHyperworkflow> unfold() {
		Map<NestedHyperworkflow, Collection<IHyperworkflow>> unfoldMap = new HashMap<NestedHyperworkflow, Collection<IHyperworkflow>>();
		int orCount = 0;
		
		for (IHyperworkflow child : this.children) {
			//unfold all nested children and write results into a map
			if (child instanceof NestedHyperworkflow) {
				Collection<IHyperworkflow> unfoldResult = child.unfold();
				unfoldMap.put((NestedHyperworkflow)child, unfoldResult);
			}
			//count number of or-nodes
			if (child instanceof Or) orCount++;
		}
		unfoldMap.keySet();
		
		//-------------------------------------------------------------------------------------------------------------
		//------------------ remove nested children and replace them by their unfolded versions -----------------------
		//-------------------------------------------------------------------------------------------------------------
		/*			-----	--------	----
		 * 	hwf1 = 	| a |---| 3x b |----| c |	where b is a nested child that represents 3 different workflows
		 * 			-----	--------	----
		 * 		|
		 * 		|
		 * 		--->	-----	---------	----
		 * 				| a |---|   b1  |---| c |	= hwf1_1
		 * 				-----	---------	----
		 * 				-----	---------	----
		 * 				| a |---|   b2  |---| c |	= hwf1_2
		 * 				-----	---------	----
		 * 				-----	---------	----
		 * 				| a |---|   b3  |---| c |	= hwf1_3
		 * 				-----	---------	----
		 */
		List<IHyperworkflow> hwfList = new ArrayList<IHyperworkflow>();	//working list that contains intermediate unfolding results
		hwfList.add(new NestedHyperworkflow(this));	//put current NestedHyperworkflow in the list to start unfolding
		//iterate over all nested children
		for (NestedHyperworkflow nested : unfoldMap.keySet()) {
			//current size of working list
			int workingListSize = hwfList.size();
			//iterate over all elements (intermediate results) of the working list in reverse order (allows manipulation of the list during iteration)
			for (int i = workingListSize - 1; i >= 0; i--) {
					
				//get incoming and outgoing connections
				List<Connection> incoming = new ArrayList<Connection>();
				List<Connection> outgoing = new ArrayList<Connection>();
				for (Connection c : ((NestedHyperworkflow)(hwfList.get(i))).getConnections()) {
					if (c.getTarget().equals(nested)) incoming.add(c);
					if (c.getSource().equals(nested)) outgoing.add(c);
				}
					
				//iterate over all unfolding results of the current nested child
				for (IHyperworkflow unfoldedChild : unfoldMap.get(nested)) {
					NestedHyperworkflow copy = new NestedHyperworkflow((NestedHyperworkflow)hwfList.get(i)); //create copy of original workflow
						
					//!!!
					copy.removeChild(nested,false);		//remove original folded child from current copy
					copy.addChild(unfoldedChild);	//add unfolded child to replace the removed original
					for (Connection c : incoming) {
						//add input connections
						copy.addConnection(new Connection(c.getSource(), c.getSrcPort(), unfoldedChild, c.getTargPort()));
					}
					for (Connection c : outgoing) {
						//add output connections
						copy.addConnection(new Connection(unfoldedChild, c.getSrcPort(), c.getTarget(), c.getTargPort()));
					}
					
					//add changed copy (see hwf1_i in the figure above) to the end of the result list if it does not already exist
					if (!hwfList.contains(copy)) hwfList.add(copy);	
				}
				
				//remove original (folded) workflow from list (see hwf1 in the figure above)
				hwfList.remove(i);	
			}
		}
		
		//-------------------------------------------------------------------------------------------------------------
		//------------------------------------------- unfold Or nodes -------------------------------------------------
		//-------------------------------------------------------------------------------------------------------------
		/*			-----	---------						-----	----
		 * 	hwf1 = 	| a |---|		|	-----				| a |---| c |	= hwf1_1
		 * 			-----	|		|---| c |		=>		-----	----
		 * 			-----	| OR_1	|	-----				-----	----
		 * 			| b |---|		|						| b |---| c |	= hwf1_2
		 * 			-----	---------						-----	----
		 */
		//iterate x times over the working list where x is the number of or nodes in the original NestedHyperworkflow
		for (int x = 0; x < orCount; x++) {
			int workingListSize = hwfList.size();
			//iterate over working list elements in reverse order (allows manipulation of the list during iteration)
			for (int i = workingListSize - 1; i >= 0; i--) {
				//get first or-node of current element
				Or firstOr = null;
				for (IHyperworkflow node : ((NestedHyperworkflow)(hwfList.get(i))).getChildren()){
					if (node instanceof Or) {
						firstOr = (Or)node;
						break;
					}
				}
				//there is at least one or-node (OR_1 in picture above)
				if (firstOr != null) {
					//unfold the or-node of the current IHyperworkflow copy (return the list containing hwf1_1 and hwf1_2 from picture above)
					Collection<IHyperworkflow> orUnfold = firstOr.unfold();
					for (IHyperworkflow instance : orUnfold) {
						//add all unfold() results that do not already exist
						if (!hwfList.contains(instance)) hwfList.add(instance);
					}
					//remove the original IHyperworkflow from the working list (hwf1 from picture above)
					hwfList.remove(i);
				}
			}
		}
		
		return hwfList;
	}
	
	/**
	 * Saves the current NestedHyperworkflow to the specified file
	 * @param pathToFile
	 * @return true, if saving the NestedHyperworkflow was successful
	 * @throws NullPointerException if the specified file path is <code>null</code>
	 */
	public boolean save(String pathToFile) {
		if (pathToFile == null) 
			throw new NullPointerException("File path is set to " + pathToFile + "!");
		
		//TODO revise an do some basic exception handling: ask user if specified file exists already and so on, move method somewhere else?
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(pathToFile);
		} catch (IOException e) {}
		
		if (fileWriter != null){
			Writer output = new BufferedWriter(fileWriter);
			XStream xs = new XStream();
			
			//TODO do NOT save whole NestedHyperworkflow! Only save a map of necessary attributes and load nhwf from this map upon loading
			xs.toXML(this, output);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Loads a NestedHyperworkflow from a specified file
	 * @param pathToFile
	 * @return the NestedHyperworkflow contained within the file
	 * @throws IllegalArgumentException if the specified file is not a saved NestedHyperworkflow
	 * @throws NullPointerException if the specified file path is <code>null</code>
	 */
	public static NestedHyperworkflow load(String pathToFile) {
		if (pathToFile == null) 
			throw new NullPointerException("File path is set to " + pathToFile + "!");
		
		//TODO revise and add more exception handling, move method somewhere else?
		File file = new File(pathToFile);
		XStream xs = new XStream();
		Object result = null;
		try {
			result = xs.fromXML(file);
		} catch (XStreamException xe) {
			throw new IllegalArgumentException("The specified file does not contain a NestedHyperworkflow! - " + pathToFile);
		}
		
		//loading and deserialization was successful, check if file contains a NestedHyperworkflow
		if (result != null && result instanceof NestedHyperworkflow) 
			return (NestedHyperworkflow)result;
		else
			return null;
	}
	
	public static void main(String[] args) {
		//Hyperworkflow parts
		NestedHyperworkflow root = new NestedHyperworkflow("root");
			IElement alpha = new Tool("alpha");
				alpha.getOutputPorts().add(new Port("out", EPortType.FILE));
			NestedHyperworkflow beta = new NestedHyperworkflow("beta");
				IElement beta1 = new Tool("beta1");
					beta1.getInputPorts().add(new Port("in", EPortType.FILE));
					beta1.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement beta2 = new Tool("beta2");
					beta2.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement beta3 = new Tool("beta3");
					beta3.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement orBeta = new Or("orBeta");
					orBeta.getInputPorts().add(new Port("in3", EPortType.GENERIC));
				IElement beta4 = new Tool("beta4");
					beta4.getInputPorts().add(new Port("in", EPortType.FILE));
					beta4.getOutputPorts().add(new Port("out", EPortType.FILE));
				beta.addChild(beta1);
				beta.addChild(beta2);
				beta.addChild(beta3);
				beta.addChild(orBeta);
				beta.addChild(beta4);
			IElement gamma = new Tool("gamma");
				gamma.getOutputPorts().add(new Port("out", EPortType.FILE));
			IElement or1 = new Or("or1");
			NestedHyperworkflow delta = new NestedHyperworkflow("delta");
				IElement delta1 = new Tool("delta1");
					delta1.getInputPorts().add(new Port("in", EPortType.FILE));
					delta1.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement delta2 = new Tool("delta2");
					delta2.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement orDelta = new Or("orDelta");
				IElement delta3 = new Tool("delta3");
					delta3.getInputPorts().add(new Port("in", EPortType.FILE));
					delta3.getOutputPorts().add(new Port("out", EPortType.FILE));
				delta.addChild(delta1);
				delta.addChild(delta2);
				delta.addChild(orDelta);
				delta.addChild(delta3);
			IElement epsilon = new Tool("epsilon");
				epsilon.getOutputPorts().add(new Port("out", EPortType.FILE));
			IElement or2 = new Or("or2");
			IElement eta = new Tool("eta");
				eta.getInputPorts().add(new Port("in", EPortType.FILE));
			root.addChild(alpha);
			root.addChild(beta);
			root.addChild(gamma);
			root.addChild(or1);
			root.addChild(delta);
			root.addChild(epsilon);
			root.addChild(or2);
			root.addChild(eta);
			
		//Connections within beta
		System.out.println(beta.addConnection(new Connection(beta, beta.getInputPorts().get(0), beta1, beta1.getInputPorts().get(0))));
		System.out.println(beta.addConnection(new Connection(beta1, beta1.getOutputPorts().get(0), orBeta, orBeta.getInputPorts().get(0))));
		System.out.println(beta.addConnection(new Connection(beta2, beta2.getOutputPorts().get(0), orBeta, orBeta.getInputPorts().get(1))));
		System.out.println(beta.addConnection(new Connection(beta3, beta3.getOutputPorts().get(0), orBeta, orBeta.getInputPorts().get(2))));
		System.out.println(beta.addConnection(new Connection(orBeta, orBeta.getOutputPorts().get(0), beta4, beta4.getInputPorts().get(0))));
		System.out.println(beta.addConnection(new Connection(beta4, beta4.getOutputPorts().get(0), beta, beta.getOutputPorts().get(0))));
		
		//Connections within delta
		System.out.println(delta.addConnection(new Connection(delta, delta.getInputPorts().get(0), delta1, delta1.getInputPorts().get(0))));
		System.out.println(delta.addConnection(new Connection(delta1, delta1.getOutputPorts().get(0), orDelta, orDelta.getInputPorts().get(0))));
		System.out.println(delta.addConnection(new Connection(delta2, delta2.getOutputPorts().get(0), orDelta, orDelta.getInputPorts().get(1))));
		System.out.println(delta.addConnection(new Connection(orDelta, orDelta.getOutputPorts().get(0), delta3, delta3.getInputPorts().get(0))));
		System.out.println(delta.addConnection(new Connection(delta3, delta3.getOutputPorts().get(0), delta, delta.getOutputPorts().get(0))));
		
		//Connections within root
		System.out.println(root.addConnection(new Connection(alpha, alpha.getOutputPorts().get(0), beta, beta.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(beta, beta.getOutputPorts().get(0), or1, or1.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(gamma, gamma.getOutputPorts().get(0), or1, or1.getInputPorts().get(1))));
		System.out.println(root.addConnection(new Connection(or1, or1.getOutputPorts().get(0), delta, delta.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(epsilon, epsilon.getOutputPorts().get(0), or2, or2.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(delta, delta.getOutputPorts().get(0), or2, or2.getInputPorts().get(1))));
		System.out.println(root.addConnection(new Connection(or2, or2.getOutputPorts().get(0), eta, eta.getInputPorts().get(0))));
		
		for (IHyperworkflow hwf : root.unfold()) {
			System.out.println(hwf);
		}
		System.out.println();
		
//		String filename = "/home/anja/test.hwf"; 
//		root.save(filename);
//		NestedHyperworkflow blub = NestedHyperworkflow.load(filename);
//		for (IHyperworkflow hwf : blub.unfold()) {
//			System.out.println(hwf);
//		}
		
		NestedHyperworkflow test = new NestedHyperworkflow("testroot");
		IElement tool = new Tool("tool");
		tool.getOutputPorts().add(new Port("out", EPortType.GENERIC));
		IElement tool2 = new Tool("tool2");
		tool2.getInputPorts().add(new Port("in", EPortType.GENERIC));
		IElement or = new Or("or");
		test.addChild(tool);
		test.addChild(or);
		test.addChild(tool2);
		
		NestedHyperworkflow nested = new NestedHyperworkflow("nested");
		IElement nestedTool = new Tool("nestedTool");
		nestedTool.getInputPorts().add(new Port("in", EPortType.GENERIC));
		nested.addChild(nestedTool);
		test.addChild(nested);
		
		test.addConnection(new Connection(tool, tool.getOutputPorts().get(0), or, or.getInputPorts().get(0)));
		test.addConnection(new Connection(or, or.getOutputPorts().get(0), tool2, tool2.getInputPorts().get(0)));
		nested.addConnection(new Connection(nested, nested.getInputPorts().get(0), nestedTool, nestedTool.getInputPorts().get(0)));
		test.addConnection(new Connection(or, or.getOutputPorts().get(0), nested, nested.getInputPorts().get(0)));
		test.save("/home/student/afischer/test-load.hwf");
		
		NestedHyperworkflow loadtest = NestedHyperworkflow.load("/home/student/afischer/test-load.hwf");
		System.out.println(loadtest);
		
	}
}
