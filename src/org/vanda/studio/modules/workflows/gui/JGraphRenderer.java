package org.vanda.studio.modules.workflows.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.modules.workflows.Connection;
import org.vanda.studio.modules.workflows.Hyperworkflow;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;
import org.vanda.studio.modules.workflows.gui.JGraphRendering.Port;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxTerminalChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

public class JGraphRenderer {

	protected Hyperworkflow root;
	protected JGraphRendering.Graph graph;
	protected ChangeListener changeListener;
	protected Map<Hyperworkflow, Object> nodes;
	protected Map<Connection, Object> edges;
	protected MultiplexObserver<Connection> connectionAddObservable;
	protected MultiplexObserver<Connection> connectionModifyObservable;
	protected MultiplexObserver<Connection> connectionRemoveObservable;
	protected MultiplexObserver<Hyperworkflow> objectAddObservable;
	protected MultiplexObserver<Hyperworkflow> objectModifyObservable;
	protected MultiplexObserver<Hyperworkflow> objectRemoveObservable;

	public JGraphRenderer(Hyperworkflow root) {
		this.root = root;
		nodes = new HashMap<Hyperworkflow, Object>();
		edges = new HashMap<Connection, Object>();
		connectionAddObservable = new MultiplexObserver<Connection>();
		connectionModifyObservable = new MultiplexObserver<Connection>();
		connectionRemoveObservable = new MultiplexObserver<Connection>();
		objectAddObservable = new MultiplexObserver<Hyperworkflow>();
		objectModifyObservable = new MultiplexObserver<Hyperworkflow>();
		objectRemoveObservable = new MultiplexObserver<Hyperworkflow>();

		graph = JGraphRendering.createGraph();

		// bind defualtParent of the graph and the root hyperworkflow 
		// to each other and save them in the node mapping
		((mxCell)graph.getDefaultParent()).setValue(root);
		nodes.put(root, graph.getDefaultParent());
		
		// bind graph; for example, react on new, changed, or deleted elements
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
	}

	protected void addNode(Object cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);
		assert (model.isVertex(cell) && value instanceof Hyperworkflow);
		Hyperworkflow to = (Hyperworkflow) value;
		
		// make sure the node does not already exist (i.e. is in nodes-map) 
		if (model.getParent(cell) != null && !nodes.containsKey(to)) {
			// add to nodes-map
			nodes.put(to, cell);

			// obtain value of parent cell that holds the current cell
			Object parent = ((mxCell)cell).getParent().getValue();
			
			// the current cell is not inserted into any existing cell, 
			// it is direct child of root
			if (parent == null) {
				// set parent to root
				to.setParent(root);
				((mxCell)cell).setParent((mxCell)graph.getDefaultParent());
			} else {
				// cell has been dropped into an existing cell,
				// set parent accordingly
			
				if (parent instanceof Hyperworkflow) {
					to.setParent((Hyperworkflow)parent);
					((mxCell)cell).setParent((mxCell)nodes.get(to.getParent()));
				} else {
					// some error occurred, parent cell has to 
					// hold a Hyperworkflow
					assert(false);
				}
			}
			
			// set dimensions of to
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
						geo.getHeight() };
			to.setDimensions(dim);
				
			System.out.println("addNode(): added " + to.getName());
			
			// notify 
			objectAddObservable.notify(to);
		}	
	}
	
	public void ensureAbsence(Hyperworkflow to) {
		Object cell = nodes.remove(to);
		if (cell != null) {
			graph.removeCells(new Object[] { cell });
		}
	}

	public void ensurePresence(Hyperworkflow to) {
		Object cell = nodes.get(to);
		if (cell == null) {
			System.out.println("renderer.ensurePresence(): " + to.getName());
			JGraphRendering.render(to, graph, nodes.get(to.getParent()));
		} else {
			// make sure the cell has the same properties as the Job "to"
			// for now, we are only interested in the geometry
			mxIGraphModel model = graph.getModel();
			mxGeometry geo = model.getGeometry(cell);
			if (geo.getX() != to.getX() || geo.getY() != to.getY()
					|| geo.getWidth() != to.getWidth()
					|| geo.getHeight() != to.getHeight()) {
				mxGeometry ng = (mxGeometry) geo.clone();
				ng.setX(to.getX());
				ng.setY(to.getY());
				ng.setWidth(to.getWidth());
				ng.setHeight(to.getHeight());
				model.setGeometry(cell, ng);
			}
		}
	}

	public void ensureConnected(Connection conn) {
		Object cell = edges.get(conn);
		if (cell == null) {

			// determine the NestedHyperworkflow that contains the specified
			// connection
			Object parentCell = null;
			if (conn.getSource() instanceof NestedHyperworkflow) {

				NestedHyperworkflow src = (NestedHyperworkflow) conn
						.getSource();
				
				if (src.getChildren().contains(conn.getTarget())
						|| src.equals(conn.getTarget())) {
					
					parentCell = nodes.get(conn.getSource());
				}
			} else {
				parentCell = nodes.get(conn.getSource().getParent());
			}

			// parentCell is the Graph-cell of the NestedHyperworkflow
			// containing connection conn
			JGraphRendering.render(conn, graph, parentCell);
		} else {
			// TODO
		}
	}

	public void ensureDisconnected(Connection conn) {
		Object cell = edges.remove(conn);
		if (cell != null) {
			graph.removeCells(new Object[] { cell });
		}
	}

	public Observable<Connection> getConnectionAddObservable() {
		return connectionAddObservable;
	}

	public Observable<Connection> getConnectionModifyObservable() {
		return connectionModifyObservable;
	}

	public Observable<Connection> getConnectionRemoveObservable() {
		return connectionRemoveObservable;
	}

	public mxGraph getGraph() {
		return graph;
	}

	public Observable<Hyperworkflow> getObjectAddObservable() {
		return objectAddObservable;
	}

	public Observable<Hyperworkflow> getObjectModifyObservable() {
		return objectModifyObservable;
	}

	public Observable<Hyperworkflow> getObjectRemoveObservable() {
		return objectRemoveObservable;
	}

	protected void updateEdge(Object cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		Connection conn = null;
		assert (model.isEdge(cell));
		Object source = model.getTerminal(cell, true);
		Object target = model.getTerminal(cell, false);
		
		// ignore "unfinished" edges
		if (source != null && target != null) {
			Object sval = model.getValue(source);
			Object tval = model.getValue(target);
			Object sparval = model.getValue(model.getParent(source));
			Object tparval = model.getValue(model.getParent(target));

			assert (sval instanceof Port && tval instanceof Port
					&& sparval instanceof Hyperworkflow && tparval instanceof Hyperworkflow);
//			assert (((Port) sval).index >= 0
//					&& ((Port) sval).index < ((Hyperworkflow) sparval)
//							.getOutputPorts().size()
//					&& !((Port) sval).input
//					&& ((Port) tval).index >= 0
//					&& ((Port) tval).index < ((Hyperworkflow) tparval)
//							.getInputPorts().size() && ((Port) tval).input);

			if (value instanceof Connection) {
				conn = (Connection) value;
				if (!edges.containsKey(conn)) {
					edges.put(conn, cell);
				}
			}
			else {
				// check if a new edge is added
				conn = new Connection();
				if (edges.put(conn, cell) != null) {
					assert (false);
				}
			}
			conn.setSource((Hyperworkflow) sparval);
			conn.setTarget((Hyperworkflow) tparval);

			// check if Connection starts/ends from/at inner port of a
			// NestedHyperworkflow
			boolean innerSource = false;
			boolean innerTarget = false;
			if (conn.getSource() instanceof NestedHyperworkflow
					&& (((NestedHyperworkflow) conn.getSource()).getChildren()
							.contains(conn.getTarget()) || conn.getSource()
							.equals(conn.getTarget())))
				innerSource = true;
			
			if (conn.getTarget() instanceof NestedHyperworkflow
					&& (((NestedHyperworkflow) conn.getTarget()).getChildren()
							.contains(conn.getSource()) || conn.getTarget()
							.equals(conn.getSource())))
				innerTarget = true;

			// depending on whether source/target are inner ports use the
			// correct portList
			if (innerSource) {
				// within the model there don't exist inner ports, only regular
				// ports. Thus, index of inner port has to be reduced by number
				// of regular input ports
				conn.setSrcPort(conn.getSource().getInputPorts().get(
						((Port) sval).index - conn.getSource().getInputPorts().size()));
				
			} else {
				conn.setSrcPort(conn.getSource().getOutputPorts().get(
						((Port) sval).index));
			}
			
			if (innerTarget) {
				// within the model there don't exist inner ports, only regular
				// ports. Thus, index of inner port has to be reduced by number
				// of regular output ports
				conn.setTargPort(conn.getTarget().getOutputPorts().get(
						((Port) tval).index - conn.getTarget().getOutputPorts().size()));
			} else {
				conn.setTargPort(conn.getTarget().getInputPorts().get(
						((Port) tval).index));
			}
				
			// notify model
			if (conn != value) {
				model.setValue(cell, conn);
				connectionAddObservable.notify(conn);
			} else {
				connectionModifyObservable.notify(conn);
			}
		}
	}
	
	protected void updateNode(Object cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);
		assert (model.isVertex(cell) && value instanceof Hyperworkflow);
		Hyperworkflow to = (Hyperworkflow) value;
		
		// check if changes occurred to the given cell
		if (geo.getX() != to.getX() || geo.getY() != to.getY()
				|| geo.getWidth() != to.getWidth()
				|| geo.getHeight() != to.getHeight()) {
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			to.setDimensions(dim);
			
			//------------------------------------------------------------------
			//------- resizing a child requires resizing of parent -------------
			//------------------------------------------------------------------
			
			// node's left end is no longer inside its parent's bounding box
			// -> expand parent's bounds to the left
			if (geo.getX() < 0) {
				mxCell parentCell = (mxCell) model.getParent(cell);
				mxGeometry parentGeo = (mxGeometry)model.getGeometry(parentCell);
				
				// only update geometry is parent is not the root
				if (parentGeo != null) {
					parentGeo.setX(parentGeo.getX() + geo.getX());
					parentGeo.setWidth(parentGeo.getWidth() - geo.getX());
					model.setGeometry(model.getParent(cell), parentGeo);
				
					// add offset to children nodes geometries so that they
					// appear to remain on their previous positions
					for (int i = 0; i < parentCell.getChildCount(); i++) {
						mxCell child = (mxCell)parentCell.getChildAt(i);
						
						if (child.getValue() instanceof Hyperworkflow) {
							mxGeometry childGeo = model.getGeometry(child);
							childGeo.setX(childGeo.getX() - geo.getX());
							model.setGeometry(child, childGeo);
						}
					}
				
					// update parent of currently resized node such that
					// recent resize actions are propagated to its parent...
					updateNode(model.getParent(cell));
					
					// update graph
					graph.refresh();
				}
			}
			
			// node's bottom end is no longer inside its parent's bounding box
			if (geo.getY() < 0) {
				mxCell parentCell = (mxCell) model.getParent(cell);
				mxGeometry parentGeo = (mxGeometry)model.getGeometry(parentCell);
				
				// only update geometry is parent is not the root
				if (parentGeo != null) {
					parentGeo.setY(parentGeo.getY() + geo.getY());
					parentGeo.setHeight(parentGeo.getHeight() - geo.getY());
					model.setGeometry(model.getParent(cell), parentGeo);
				
					// add offset to children nodes geometries so that they
					// appear to remain on their previous positions
					for (int i = 0; i < parentCell.getChildCount(); i++) {
						mxCell child = (mxCell)parentCell.getChildAt(i);
						
						if (child.getValue() instanceof Hyperworkflow) {
							mxGeometry childGeo = model.getGeometry(child);
							childGeo.setY(childGeo.getY() - geo.getY());
							model.setGeometry(child, childGeo);
						}
					}
				
					// update parent of currently resized node such that
					// recent resize actions are propagated to its parent...
					updateNode(model.getParent(cell));
					
					// update graph
					graph.refresh();
				}
			}
			
			// notify
			objectModifyObservable.notify(to);
		}
		
		// check if parent changed (only if both hwf and corresponding cell have parents)
		if (model.getParent(cell) != null && to.getParent() != null 
				&& !model.getParent(cell).equals(nodes.get(to.getParent()))) {
			System.out.println("parent of " + to + " has changed to " 
					+ ((Hyperworkflow)model.getValue(model.getParent(cell))).getName());
			
			Hyperworkflow newParent = (Hyperworkflow) model.getValue(model.getParent(cell));
			objectRemoveObservable.notify(to);
			to.setParent(newParent);
			objectAddObservable.notify(to);
			graph.refresh();
			//FIXME maybe it's a better idea to use modificationObservable
		}
	}
	
	protected class ChangeListener implements mxIEventListener {
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			mxIGraphModel model = graph.getModel();
			mxUndoableEdit edit = (mxUndoableEdit) evt.getProperty("edit");
			List<mxUndoableChange> changes = edit.getChanges();
			for (mxUndoableChange c : changes) {
				// process the following changes:
				// - child change (add/remove)
				// - value change
				// - TODO geometry change
				// - TODO terminal change
				if (c instanceof mxChildChange) {
					mxChildChange cc = (mxChildChange) c;
					Object cell = cc.getChild();
					Object value = model.getValue(cell);
					if (cc.getParent() == null) {
						// something has been removed
						if (value instanceof Hyperworkflow) {
							if (nodes.remove(value) != null) {
								// notify
								objectRemoveObservable
										.notify((Hyperworkflow) value);
							}
						} else if (value instanceof Connection) {
							if (edges.remove(value) != null) {
								// notify
								connectionRemoveObservable
										.notify((Connection) value);
							}
						}
					} else {
						// something has been added
						if (value instanceof Hyperworkflow) {
//							Object oldcell = nodes.put((Hyperworkflow) value,
//									cell);
//							if (oldcell == null) {
//								// check geometry and notify if necessary
//								updateNode(cell);
//								// FIXME: currently, objectAdd is not used!
//							} else {
//								assert (cell == oldcell);
//							}
							addNode(cell);
						} else if (value instanceof Connection) {
							// happens when a loaded nhwf contains connection
							// and they are added to the graph
							
							// TODO currently this is not supposed to happen
							//assert (false);
						} else if (value == null || value.equals("")) {
							// check if a new edge is added
							if (model.isEdge(cell)) {
								updateEdge(cell);
							}
						}
					}
				} else if (c instanceof mxValueChange) {
					// fires when a connection was inserted and then any 
					// component is moved to change its geometry
					// maybe this is the geometryChange of connections?
					
					// assert (false);
					System.out.println("mxValueChange of: " 
							+ ((mxCell)((mxValueChange)c).getCell()).getValue());
				} else if (c instanceof mxGeometryChange) {
					Object cell = ((mxGeometryChange) c).getCell();
					if (model.getValue(cell) instanceof Hyperworkflow)
						updateNode(cell);
				} else if (c instanceof mxTerminalChange) {
					// just do the same thing as for an added edge
					Object cell = ((mxTerminalChange) c).getCell();
					updateEdge(cell);
				}
			}
		}
	}

}
