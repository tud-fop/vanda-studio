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

import org.vanda.studio.model.Port;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * Nested composite of Hyperworkflow composite pattern
 * 
 * @author afischer
 */
public class NestedHyperworkflow extends Hyperworkflow {

	private MultiplexObserver<Hyperworkflow> addObservable;
	private List<Hyperworkflow> children;
	private MultiplexObserver<Connection> connectObservable;
	private List<Connection> connections;
	private MultiplexObserver<Connection> disconnectObservable;
	private MultiplexObserver<Hyperworkflow> modifyObservable;
	private Map<Hyperworkflow, List<Port>> portBlockageMap;
	private MultiplexObserver<Hyperworkflow> removeObservable;
	private List<String> spareIds;

	public NestedHyperworkflow(String name) {
		this(null, name);
	}
	
	/**
	 * Copy constructor - creates a new NestedHyperworkflow instance (shallow
	 * copy of the original, except OR-nodes (deep copy))
	 * 
	 * @param toCopy
	 */
	public NestedHyperworkflow(NestedHyperworkflow toCopy) {
		this(toCopy.getParent(), toCopy.getName(), new ArrayList<Port>(toCopy
				.getInputPorts()), new ArrayList<Port>(toCopy.getOutputPorts()));
		setId(toCopy.getId());

		this.children = new ArrayList<Hyperworkflow>();
		for (Hyperworkflow child : toCopy.children) {
			Hyperworkflow childCopy = null;
			
			// ensure that an or-child is copied and gets the current
			// NestedHyperworkflow copy as parent
			if (child instanceof Or)
				childCopy = new Or((Or) child, this);
			else
				childCopy = child; // other children are just re-used
			children.add(childCopy);
		}

		// copy references of old connections to new NestedHyperworkflow
		// replace connections starting or ending at inner ports
		// by new ones
		this.connections = new ArrayList<Connection>();
		for (Connection c : toCopy.getConnections()) {
			Connection conn = c;
			if (conn.getSource().equals(toCopy)) conn.setSource(this);
			if (conn.getTarget().equals(toCopy)) conn.setTarget(this);
			this.connections.add(conn);
		}

		// copy the portBlockageMap, has to be done semi-manually since "new
		// HashMap(toCopy.portBlockageMap)" only does a shallow copy and thus,
		// refers to the SAME entry lists
		this.portBlockageMap = new HashMap<Hyperworkflow, List<Port>>();
		for (Hyperworkflow hwf : toCopy.portBlockageMap.keySet()) {
			this.portBlockageMap.put(hwf, new ArrayList<Port>(
					toCopy.portBlockageMap.get(hwf)));
		}

		this.spareIds = new ArrayList<String>(toCopy.spareIds);
	}
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	public NestedHyperworkflow(Hyperworkflow parent, String name,
			List<Port> inputPorts, List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
		this.portBlockageMap = new HashMap<Hyperworkflow, List<Port>>();
		this.connections = new ArrayList<Connection>();
		this.children = new ArrayList<Hyperworkflow>();
		this.spareIds = new ArrayList<String>();

		// TODO involve these attributes in copy construction and cloning at
		// some point
		this.addObservable = new MultiplexObserver<Hyperworkflow>();
		this.modifyObservable = new MultiplexObserver<Hyperworkflow>();
		this.removeObservable = new MultiplexObserver<Hyperworkflow>();
		this.connectObservable = new MultiplexObserver<Connection>();
		this.disconnectObservable = new MultiplexObserver<Connection>();
	}

	/**
	 * Adds a Hyperworkflow child to the NestedHyperworkflow
	 * 
	 * @param hwf - child to add
	 * @return true, if adding was successful
	 */
	public boolean addChild(Hyperworkflow hwf) {
		
		// check for null reference and make sure the new child does not exist
		// already
		if (hwf != null && !children.contains(hwf)) {

			// add child if possible
			if (children.add(hwf)) {

				// set parent of added child to be the current
				// NestedHyperworkflow
				hwf.setParent(this);

				// re-set the child's id
				if (!spareIds.isEmpty()) {
					// replace it with an id of a previously removed child (if
					// there are any within spareIds-list)
					hwf.setId(spareIds.remove(0));
				} else {
					// create new id based on current child count
					hwf.setId(getId() + "-" + children.size());
				}

				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a new connection to the NestedHyperworkflow's connections list. The
	 * connection must not exist already, the connection source and target has
	 * to be child of the current NestedHyperworkflow or the NestedHyperworkflow
	 * itself and the target port must not be blocked by another connection
	 * 
	 * @param conn - the Connection to add
	 * @return true, if adding was successful
	 */
	public boolean addConnection(Connection conn) {

		// check for null reference, ensure connection does not already exist,
		// check port compatibility
		if (conn == null) throw new NullPointerException("Connection has " +
				"null value.");
		if (connections.contains(conn)) 
			throw new IllegalArgumentException("Connection " + conn 
					+ "exists already.");
		if (!conn.getSrcPort().isCompatibleTo(conn.getTargPort()))
			throw new IllegalArgumentException("Input and output port are " +
					"not compatible.");
		
		// ensure that source is NestedHyperworkflow itself or a child and
		// has the specified source port
		if ((children.contains(conn.getSource()) && conn.getSource()
				.getOutputPorts().contains(conn.getSrcPort()))
				|| (conn.getSource().equals(this) && this.getInputPorts()
						.contains(conn.getSrcPort()))) {

			// ensure target is a child or current NestedHyperworkflow
			// itself and has the specified target port
			if ((children.contains(conn.getTarget()) && conn.getTarget()
					.getInputPorts().contains(conn.getTargPort()))
					|| (conn.getTarget().equals(this) && this
							.getOutputPorts().contains(conn.getTargPort()))) {

				// check if target port is not blocked already
				Connection targetBlocked;
				if (!portBlockageMap.containsKey(conn.getTarget()))
					portBlockageMap.put(conn.getTarget(),
							new ArrayList<Port>());
				List<Port> blockedPorts = portBlockageMap.get(conn
						.getTarget());
				if (!blockedPorts.contains(conn.getTargPort()))
					targetBlocked = null;
				else
					targetBlocked = new Connection(null, null, null, null);

				// if targetBlocked is null, the port is still open
				if (targetBlocked == null) {

					// try to add connection and block the previously free
					// target input port
					if (connections.add(conn)
							&& portBlockageMap.get(conn.getTarget()).add(
									conn.getTargPort())) {

						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
		//XXX return new NestedHyperworkflow(this);
	}
	
	public void ensureAbsence(Hyperworkflow o) {
		if (removeChild(o)) {
			removeObservable.notify(o);
		}
	}

	public void ensureConnected(Connection c) {
		if (addConnection(c)) {
			connectObservable.notify(c);
		}
	}

	public void ensureDisconnected(Connection c) {
		if (removeConnection(c)) {
			connectObservable.notify(c);
		}
	}

	public void ensurePresence(Hyperworkflow o) {
		if (addChild(o)) {
			addObservable.notify(o);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		// make sure other object is a NestedHyperworkflow
		boolean result = (other instanceof NestedHyperworkflow);
		if (result) {
			// compare attributes (except parent)
			NestedHyperworkflow oh = (NestedHyperworkflow) other;
			result = (getId() == oh.getId()
					&& getName().equals(oh.getName())
					&& getInputPorts().equals(oh.getInputPorts()) 
					&& getOutputPorts().equals(oh.getOutputPorts())
					&& getChildren().equals(oh.getChildren())
					&& getConnections().equals(oh.getConnections())
					&& getPortBlockageMap().equals(oh.getPortBlockageMap()));
		}
		return result;
	}

	public Observable<Hyperworkflow> getAddObservable() {
		return addObservable;
	}
	
	/**
	 * @return a list of direct IHyperworkflow children of the current
	 *         NestedHyperworkflow
	 */
	public List<Hyperworkflow> getChildren() {
		return children;
	}
	
	/** @return a list of connections */
	public List<Connection> getConnections() {
		return connections;
	}

	public Observable<Connection> getConnectObservable() {
		return connectObservable;
	}
	
	public Observable<Connection> getDisconnectObservable() {
		return disconnectObservable;
	}
	
	public Observable<Hyperworkflow> getModifyObservable() {
		return modifyObservable;
	}
	
	public Map<Hyperworkflow, List<Port>> getPortBlockageMap() {
		return portBlockageMap;
	}

	public Observable<Hyperworkflow> getRemoveObservable() {
		return removeObservable;
	}
	
	/**
	 * Loads a NestedHyperworkflow from a specified file
	 * 
	 * @param pathToFile
	 * @return the NestedHyperworkflow contained within the file
	 * @throws IllegalArgumentException - 
	 *             if the specified file is not a saved NestedHyperworkflow
	 * @throws NullPointerException - 
	 *             if the specified file path is <code>null</code>
	 */
	public static NestedHyperworkflow load(String pathToFile) {
		if (pathToFile == null)
			throw new NullPointerException("File path is set to " + pathToFile
					+ "!");

		// TODO revise and add more exception handling, move method somewhere
		// else?
		File file = new File(pathToFile);
		XStream xs = new XStream();
		Object result = null;
		try {
			result = xs.fromXML(file);
			
			// loading and deserialization was successful, check if file 
			// contains a NestedHyperworkflow
			if (result != null && result instanceof NestedHyperworkflow)
				return (NestedHyperworkflow) result;
			else
				return null;
		} catch (XStreamException xe) {
			throw new IllegalArgumentException(
					"The specified file does not contain a NestedHyperworkflow! - "
							+ pathToFile);
		}		
	}
	
	/**
	 * Removes a Hyperworkflow child from the NestedHyperworkflow, all
	 * associated connections, and, if present, nested children
	 * 
	 * @param hwf- child to remove
	 * @return true, if removal was successful
	 */
	public boolean removeChild(Hyperworkflow hwf) {
		if (hwf != null && children.contains(hwf)) {

			// partition connections into incoming and outgoing
			List<Connection> incoming = new ArrayList<Connection>();
			List<Connection> outgoing = new ArrayList<Connection>();
			for (Connection c : connections) {
				if (c.getTarget().equals(hwf))
					incoming.add(c);
				if (c.getSource().equals(hwf))
					outgoing.add(c);
			}

			// remove incoming connections
			while (incoming.size() > 0) {
				removeConnection(incoming.remove(0));
			}

			// remove outgoing connections
			while (outgoing.size() > 0) {
				removeConnection(outgoing.remove(0));
			}

			// remove child if possible
			if (children.remove(hwf)) {

				// save the id of the just removed child for later re-use
				spareIds.add(hwf.getId());

				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes an existing connection from the NestedHyperworkflow's
	 * connections list as frees the port from the portBlockageMap
	 * 
	 * @param conn - the Connection to remove
	 * @return true, if removal was successful
	 */
	public boolean removeConnection(Connection conn) {
		
		// check for null reference and make sure the connection exists
		if (conn != null && connections.contains(conn)) {

			// try to remove connection and free the previously blocked target
			// input port
			if (connections.remove(conn)
					&& portBlockageMap.get(conn.getTarget()).remove(
							conn.getTargPort())) {

				// if there are no more blocked ports for the target tool,
				// remove its map entries completely
				if (portBlockageMap.get(conn.getTarget()) != null
						&& portBlockageMap.get(conn.getTarget()).isEmpty())
					portBlockageMap.remove(conn.getTarget());

				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes all children of the current NestedHyperworkflow that are not
	 * connected in a reasonable manner, i.e. <br/> a) algorithms with inputs
	 * and outputs where only one side is connected (then they don't have inputs
	 * which is useless or their results are not connected to any other node
	 * which is also useless)<br/> b) sources (a tool that only has output
	 * ports) that are not connected to any target tool<br/> c) sinks (a tool
	 * that only has input ports) that are not connected to anything, thus there
	 * is no input argument to save, display, etc.
	 */
	private void removeDisconnectedChildren() {
		
		List<Hyperworkflow> childrenToRemove = new ArrayList<Hyperworkflow>(
				children);
		List<Hyperworkflow> furtherChecking = new ArrayList<Hyperworkflow>();

		// iterate over all connections and find reasonably connected tools to
		// keep them
		for (Connection c : connections) {

			Hyperworkflow source = c.getSource();
			if (childrenToRemove.contains(source)) {

				// if the source has no input ports (is a source) or its input
				// ports are connected to something
				if (source.getInputPorts().isEmpty()
						|| furtherChecking.contains(source)) {
					
					// keep the child, a.k.a. remove it from the list of
					// soon-to-be-removed nodes
					childrenToRemove.remove(source);
					
					// and remove it from the list that saves nodes for double
					// checking
					furtherChecking.remove(source);
					
				} else {
					// the source has input ports and we do not know if they are
					// connected -> inspect the node later
					furtherChecking.add(source);
				}
			}
			
			Hyperworkflow target = c.getTarget();
			if (childrenToRemove.contains(target)) {

				// if the target has no output ports (is a sink) or its outputs
				// are connected to something
				if (target.getOutputPorts().isEmpty()
						|| furtherChecking.contains(target)) {
					
					// keep the child, a.k.a. remove it from the list of
					// soon-to-be-removed nodes
					childrenToRemove.remove(target);
					
					// and remove it from the list that saves nodes for double
					// checking
					furtherChecking.remove(target);
					
				} else {
					// the target has output ports and we do not know if they are
					// connected -> inspect the node later
					furtherChecking.add(target);
				}
			}
			
		}

		// iterate over all useless tools, remove them
		for (int i = 0; i < childrenToRemove.size(); i++) {

			// remove the first tool of the list
			removeChild(childrenToRemove.get(i));
			
			// once last node has been removed call removeDisconnectedChildren 
			// again and check if there are more disconnected tools
			if (i == childrenToRemove.size() - 1) {
				removeDisconnectedChildren();
			}
		}
	}
	
	/**
	 * Saves the current NestedHyperworkflow to the specified file
	 * 
	 * @param pathToFile
	 * @return true, if saving the NestedHyperworkflow was successful
	 * @throws NullPointerException - 
	 *             if the specified file path is <code>null</code>
	 */
	public boolean save(String pathToFile) {
		if (pathToFile == null)
			throw new NullPointerException("File path is set to " + pathToFile
					+ "!");

		// TODO revise an do some basic exception handling
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(pathToFile);
			
			if (fileWriter != null) {
				Writer output = new BufferedWriter(fileWriter);
				XStream xs = new XStream();

				// TODO do NOT save whole NestedHyperworkflow!
				xs.toXML(this, output);
				return true;
			}
		} catch (IOException e) {
		}

		return false;
	}
	
	public void setDimensions(Hyperworkflow o, double[] d) {
		assert (children.contains(o));

		if (d[0] != o.getX() || d[1] != o.getY() || d[2] != o.getWidth()
				|| d[3] != o.getHeight()) {
			o.setDimensions(d);
			modifyObservable.notify(o);
		}
	}
	
	/**
	 * Sets the NestedHyperworkflow's id to the new value. If there was a
	 * change, the new Id is propagated so that the children can also reset
	 * their ids.
	 * 
	 * @param newId - replaces the current id
	 * @return true if replacement was successful
	 */
	@Override
	public boolean setId(String newId) {
		if (newId != null && getId() != newId) {
			
			super.setId(newId);
			
			// update children's ids as well
			for (Hyperworkflow child : children) {
				String[] idParts = child.getId().split("-");
				String newChildId = child.getParent().getId() + "-"
						+ idParts[idParts.length - 1];
				child.setId(newChildId);
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return getName() + ": " + children + ", " + connections;
	}

	@Override
	public Collection<Hyperworkflow> unfold() {
		Map<NestedHyperworkflow, Collection<Hyperworkflow>> unfoldMap 
			= new HashMap<NestedHyperworkflow, Collection<Hyperworkflow>>();
		int orCount = 0;

		for (Hyperworkflow child : this.children) {
			// count number of or-nodes
			if (child instanceof Or) {
				orCount++;
			} else {
				// unfold all other children and save unfolding results in a
				// map, if there are more than two results
				Collection<Hyperworkflow> unfoldResult = child.unfold();
				if (unfoldResult.size() > 1) {
					unfoldMap.put((NestedHyperworkflow)child, unfoldResult);
				}
			}
		}

		// ---------------------------------------------------------------------
		// ----- remove nested children and replace by unfolded versions -------
		// ---------------------------------------------------------------------
		/*
		 * 			----- 	-------- 	----- 
		 * hwf1 = 	| a |---| 3x b |----| c | 
		 * 			-----	--------	-----
		 * where b is a nested child that represents 3 different workflows 
		 * 			----- 	------ 	 -----  
		 * ===>  	| a |---| b1 |---| c | = hwf1_1
		 * 			----- 	------ 	 ---- 
		 * 			----- 	------ 	 ----- 
		 * 			| a |---| b2 |---| c | = hwf1_2 
		 * 			----- 	------ 	 ----- 
		 * 			----- 	------ 	 ----- 
		 * 			| a |---| b3 |---| c | = hwf1_3 
		 * 			----- 	------ 	 -----
		 */
		
		// working list that contains intermediate unfolding results
		List<Hyperworkflow> hwfList = new ArrayList<Hyperworkflow>(); 
		// put current NestedHyperworkflow in the list to start unfolding
		hwfList.add(new NestedHyperworkflow(this));
		
		// iterate over all nested children
		for (NestedHyperworkflow nested : unfoldMap.keySet()) {
			// current size of working list
			int workingListSize = hwfList.size();
			// iterate over all elements (intermediate results) of the working
			// list in reverse order (allows manipulation of the list during
			// iteration)
			for (int i = workingListSize - 1; i >= 0; i--) {

				// get incoming and outgoing connections
				List<Connection> incoming = new ArrayList<Connection>();
				List<Connection> outgoing = new ArrayList<Connection>();
				for (Connection c : ((NestedHyperworkflow) (hwfList.get(i)))
						.getConnections()) {
					if (c.getTarget().equals(nested))
						incoming.add(c);
					if (c.getSource().equals(nested))
						outgoing.add(c);
				}

				// iterate over all unfolding results of the 
				// current nested child
				for (Hyperworkflow unfoldedChild : unfoldMap.get(nested)) {
					
					// create copy of original NestedHyperworkflow
					NestedHyperworkflow copy = new NestedHyperworkflow(
							(NestedHyperworkflow) hwfList.get(i)); 

					// remove original folded child from current copy,
					// along with all its connections
					copy.removeChild(nested); 
					// add unfolded child to replace the removed original
					copy.addChild(unfoldedChild); 
					
					// insert incoming connections for new unfolded child
					for (Connection c : incoming) {				
						boolean addConnection = false;
						
						// check if an incoming connection is continued by
						// some inner connection of the unfolded nested hwf
						for (Connection ic : ((NestedHyperworkflow)unfoldedChild)
								.getConnections()) {
							
							// ensure inner connection starts at inner port of
							// current unfoldedChild
							if (ic.getSource().equals(unfoldedChild) 
									&& ic.getSrcPort().equals(c.getTargPort())) {
								
								addConnection = true;
								break;
							}	
						}
						
						// connection is continued by inner connection
						if (addConnection) {
							// add input connection
							copy.addConnection(new Connection(c.getSource(), c
									.getSrcPort(), unfoldedChild, c.getTargPort()));
						} else {
							// original connection is useless because it is not
							// continued within the unfolded child
							// -> remove useless input port
							unfoldedChild.getInputPorts().remove(c.getTargPort());
						}
					}
					
					// insert outgoing connections for new unfolded child
					for (Connection c : outgoing) {
						// add output connections
						copy.addConnection(new Connection(unfoldedChild, c
								.getSrcPort(), c.getTarget(), c.getTargPort()));
					}

					// add changed copy (see hwf1_i in the figure above) to the
					// end of the result list if it does not already exist
					if (!hwfList.contains(copy))
						hwfList.add(copy);
				}

				// remove original (folded) workflow from list 
				// (see hwf1 in the figure above)
				hwfList.remove(i);
			}
		}
		
		// ---------------------------------------------------------------------
		// -------------------- unfold Or nodes --------------------------------
		// ---------------------------------------------------------------------
		/*
		 * 			----- 	---------
		 * hwf1 = 	| a |---| 		|
		 * 			-----	|		|	-----
		 * 					| OR_1 	|---| c |
		 * 			-----	|		|	-----
		 * 		 	| b |---| 		|
		 * 			-----	--------- 
		 * -----	-----
		 * | a |----| c | = hwf1_1
		 * -----	-----
		 * -----	-----
		 * | b |----| c | = hwf1_2
		 * -----	-----
		 */
		
		// iterate x times over the working list where x is the number of or
		// nodes in the original NestedHyperworkflow
		for (int x = 0; x < orCount; x++) {
			
			int workingListSize = hwfList.size();
			
			// iterate over working list elements in reverse order (allows
			// manipulation of the list during iteration)
			for (int i = workingListSize - 1; i >= 0; i--) {
				
				// get first or-node of current element
				Or firstOr = null;
				for (Hyperworkflow node : ((NestedHyperworkflow) (hwfList
						.get(i))).getChildren()) {
					if (node instanceof Or) {
						firstOr = (Or) node;
						break;
					}
				}
				
				// there is at least one or-node (OR_1 in picture above)
				if (firstOr != null) {
					
					// unfold the or-node of the current IHyperworkflow copy
					// (return the list containing hwf1_1 and hwf1_2 from
					// picture above)
					Collection<Hyperworkflow> orUnfold = unfoldOr(firstOr);
					for (Hyperworkflow instance : orUnfold) {
						
						// add all unfold() results that do not already exist
						if (!hwfList.contains(instance))
							hwfList.add(instance);
					}
					
					// remove the original IHyperworkflow from the working list
					// (hwf1 from picture above)
					hwfList.remove(i);
				}
			}
		}
		
		return hwfList;
	}

	private Collection<Hyperworkflow> unfoldOr(Or orNode) {
		List<Hyperworkflow> hwfList = new ArrayList<Hyperworkflow>();

		// get incoming and outgoing connections that are connected to orNode
		List<Connection> incoming = new ArrayList<Connection>();
		List<Connection> outgoing = new ArrayList<Connection>();
		for (Connection c : ((NestedHyperworkflow)orNode.getParent())
				.getConnections()) {
			if (c.getTarget().equals(orNode))
				incoming.add(c);
			if (c.getSource().equals(orNode))
				outgoing.add(c);
		}

		for (int i = 0; i < incoming.size(); i++) {
			// copy parent NestedHyperworkflow of current or node
			NestedHyperworkflow parentCopy = new NestedHyperworkflow(
					(NestedHyperworkflow)orNode.getParent()); 
			
			// remove or node
			parentCopy.removeChild(orNode); 

			// connect i-th OR-input with all OR-outputs
			for (int j = 0; j < outgoing.size(); j++) {
				parentCopy.addConnection(new Connection(incoming.get(i)
						.getSource(), incoming.get(i).getSrcPort(), outgoing
						.get(j).getTarget(), outgoing.get(j).getTargPort()));
			}

			// remove the other inputs from the parent NestedHyperworkflow
			for (int j = incoming.size() - 1; j >= 0; j--) {
				if (j != i)
					parentCopy.removeChild(incoming.get(j).getSource());
			}

			if (!hwfList.contains(parentCopy))
				// add unfolded copy to result list
				hwfList.add(parentCopy); 
		}

		// remove unconnected nodes from hyperworkflows
		for (Hyperworkflow result : hwfList) {
			((NestedHyperworkflow)result).removeDisconnectedChildren();
		}
		
		return hwfList;
	}
	
	public static void main(String[] args) {
		 NestedHyperworkflow root = new NestedHyperworkflow("root");
		 Hyperworkflow alpha = new JobForTesting("alpha");
		 alpha.getOutputPorts().add(new Port("out", "type"));
		 NestedHyperworkflow beta = new NestedHyperworkflow("beta");
		 Hyperworkflow beta1 = new JobForTesting("beta1");
		 beta1.getInputPorts().add(new Port("in", "type"));
		 beta1.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow beta2 = new JobForTesting("beta2");
		 beta2.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow beta3 = new JobForTesting("beta3");
		 beta3.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow orBeta = new Or("orBeta");
		 orBeta.getInputPorts().add(new Port("in3", "type"));
		 Hyperworkflow beta4 = new JobForTesting("beta4");
		 beta4.getInputPorts().add(new Port("in", "type"));
		 beta4.getOutputPorts().add(new Port("out", "type"));
		 beta.addChild(beta1);
		 beta.addChild(beta2);
		 beta.addChild(beta3);
		 beta.addChild(orBeta);
		 beta.addChild(beta4);
		 beta.getInputPorts().add(new Port("in", "type"));
		 beta.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow gamma = new JobForTesting("gamma");
		 gamma.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow or1 = new Or("or1");
		 NestedHyperworkflow delta = new NestedHyperworkflow("delta");
		 Hyperworkflow delta1 = new JobForTesting("delta1");
		 delta1.getInputPorts().add(new Port("in", "type"));
		 delta1.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow delta2 = new JobForTesting("delta2");
		 delta2.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow orDelta = new Or("orDelta");
		 Hyperworkflow delta3 = new JobForTesting("delta3");
		 delta3.getInputPorts().add(new Port("in", "type"));
		 delta3.getOutputPorts().add(new Port("out", "type"));
		 delta.addChild(delta1);
		 delta.addChild(delta2);
		 delta.addChild(orDelta);
		 delta.addChild(delta3);
		 delta.getInputPorts().add(new Port("in", "type"));
		 delta.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow epsilon = new JobForTesting("epsilon");
		 epsilon.getOutputPorts().add(new Port("out", "type"));
		 Hyperworkflow or2 = new Or("or2");
		 Hyperworkflow eta = new JobForTesting("eta");
		 eta.getInputPorts().add(new Port("in", "type"));
		 root.addChild(alpha);
		 root.addChild(beta);
		 root.addChild(gamma);
		 root.addChild(or1);
		 root.addChild(delta);
		 root.addChild(epsilon);
		 root.addChild(or2);
		 root.addChild(eta);
					
		 //Connections within beta
		 System.out.println("beta connections:");
		 System.out.println(beta.addConnection(new Connection(beta,
		 beta.getInputPorts().get(0), beta1, beta1.getInputPorts().get(0))));
		 System.out.println(beta.addConnection(new Connection(beta1,
		 beta1.getOutputPorts().get(0), orBeta,
		 orBeta.getInputPorts().get(0))));
		 System.out.println(beta.addConnection(new Connection(beta2,
		 beta2.getOutputPorts().get(0), orBeta,
		 orBeta.getInputPorts().get(1))));
		 System.out.println(beta.addConnection(new Connection(beta3,
		 beta3.getOutputPorts().get(0), orBeta,
		 orBeta.getInputPorts().get(2))));
		 System.out.println(beta.addConnection(new Connection(orBeta,
		 orBeta.getOutputPorts().get(0), beta4,
		 beta4.getInputPorts().get(0))));
		 System.out.println(beta.addConnection(new Connection(beta4,
		 beta4.getOutputPorts().get(0), beta, beta.getOutputPorts().get(0))));
				
		 //Connections within delta
		 System.out.println("\ndelta connections:");
		 System.out.println(delta.addConnection(new Connection(delta,
		 delta.getInputPorts().get(0), delta1,
		 delta1.getInputPorts().get(0))));
		 System.out.println(delta.addConnection(new Connection(delta1,
		 delta1.getOutputPorts().get(0), orDelta,
		 orDelta.getInputPorts().get(0))));
		 System.out.println(delta.addConnection(new Connection(delta2,
		 delta2.getOutputPorts().get(0), orDelta,
		 orDelta.getInputPorts().get(1))));
		 System.out.println(delta.addConnection(new Connection(orDelta,
		 orDelta.getOutputPorts().get(0), delta3,
		 delta3.getInputPorts().get(0))));
		 System.out.println(delta.addConnection(new Connection(delta3,
		 delta3.getOutputPorts().get(0), delta,
		 delta.getOutputPorts().get(0))));
				
		 //Connections within root
		 System.out.println("\nroot connections:");
		 System.out.println(root.addConnection(new Connection(alpha,
		 alpha.getOutputPorts().get(0), beta, beta.getInputPorts().get(0))));
		 System.out.println(root.addConnection(new Connection(beta,
		 beta.getOutputPorts().get(0), or1, or1.getInputPorts().get(0))));
		 System.out.println(root.addConnection(new Connection(gamma,
		 gamma.getOutputPorts().get(0), or1, or1.getInputPorts().get(1))));
		 System.out.println(root.addConnection(new Connection(or1,
		 or1.getOutputPorts().get(0), delta, delta.getInputPorts().get(0))));
		 System.out.println(root.addConnection(new Connection(epsilon,
		 epsilon.getOutputPorts().get(0), or2, or2.getInputPorts().get(0))));
		 System.out.println(root.addConnection(new Connection(delta,
		 delta.getOutputPorts().get(0), or2, or2.getInputPorts().get(1))));
		 System.out.println(root.addConnection(new Connection(or2,
		 or2.getOutputPorts().get(0), eta, eta.getInputPorts().get(0))));
		 
		 System.out.println("\nunfold results:");
		 Collection<Hyperworkflow> unfoldList = root.unfold();
		 for (Hyperworkflow hwf : unfoldList) {
			 System.out.println(hwf);
		 }

//		 String filename = "/home/anja/test.hwf";
//		 root.save(filename);
//		 NestedHyperworkflow blub = NestedHyperworkflow.load(filename);
//		 for (Hyperworkflow hwf : blub.unfold()) {
//			 System.out.println(hwf);
//		 }

		NestedHyperworkflow test = new NestedHyperworkflow("testroot");
		Hyperworkflow tool = new JobForTesting("tool");
		tool.getOutputPorts().add(new Port("out", "type"));
		Hyperworkflow tool2 = new JobForTesting("tool2");
		tool2.getInputPorts().add(new Port("in", "type"));
		Hyperworkflow or = new Or("or");
		test.addChild(tool);
		test.addChild(or);
		test.addChild(tool2);

		NestedHyperworkflow nested = new NestedHyperworkflow("nested");
		Hyperworkflow nestedTool = new JobForTesting("nestedTool");
		nestedTool.getInputPorts().add(new Port("in", "type"));
		Hyperworkflow nestedToolA = new JobForTesting("nestedToolA");
		nestedToolA.getInputPorts().add(new Port("in", "type"));
		nestedToolA.getOutputPorts().add(new Port("out", "type"));
		nested.addChild(nestedTool);
		nested.addChild(nestedToolA);
		nested.getInputPorts().add(new Port("in", "type"));
		nested.addConnection(new Connection(nestedToolA, nestedToolA
				.getOutputPorts().get(0), nestedTool, nestedTool
				.getInputPorts().get(0)));
		nested.addConnection(new Connection(nested, nested.getInputPorts().get(
				0), nestedToolA, nestedToolA.getInputPorts().get(0)));

		test.addChild(nested);
		test.addConnection(new Connection(tool, tool.getOutputPorts().get(0),
				or, or.getInputPorts().get(0)));
		test.addConnection(new Connection(or, or.getOutputPorts().get(0),
				tool2, tool2.getInputPorts().get(0)));
		test.addConnection(new Connection(or, or.getOutputPorts().get(0),
				nested, nested.getInputPorts().get(0)));
		test.save("/home/student/afischer/test-load.hwf");

		NestedHyperworkflow loadtest = NestedHyperworkflow
				.load("/home/student/afischer/test-load.hwf");
		System.out.println(loadtest);
	}
}
