package org.vanda.studio.modules.workflows.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.modules.workflows.Connection;
import org.vanda.studio.modules.workflows.IHyperworkflow;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;
import org.vanda.studio.modules.workflows.gui.JGraphRendering.Port;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

import com.mxgraph.model.mxGeometry;
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
	
	protected JGraphRendering.Graph graph;
	protected ChangeListener changeListener;
	protected Map<IHyperworkflow, Object> nodes;
	protected Map<Connection, Object> edges;
	protected MultiplexObserver<Connection> connectionAddObservable;
	protected MultiplexObserver<Connection> connectionModifyObservable;
	protected MultiplexObserver<Connection> connectionRemoveObservable;
	protected MultiplexObserver<IHyperworkflow> objectAddObservable;
	protected MultiplexObserver<IHyperworkflow> objectModifyObservable;
	protected MultiplexObserver<IHyperworkflow> objectRemoveObservable;
	
	public JGraphRenderer() {
		nodes = new HashMap<IHyperworkflow, Object>();
		edges = new HashMap<Connection, Object>();
		connectionAddObservable = new MultiplexObserver<Connection>();
		connectionModifyObservable = new MultiplexObserver<Connection>();
		connectionRemoveObservable = new MultiplexObserver<Connection>();
		objectAddObservable = new MultiplexObserver<IHyperworkflow>();
		objectModifyObservable = new MultiplexObserver<IHyperworkflow>();
		objectRemoveObservable = new MultiplexObserver<IHyperworkflow>();
	
		graph = JGraphRendering.createGraph();
		
		// bind graph; for example, react on new, changed, or deleted elements
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
	}
	
	public void ensureAbsence(IHyperworkflow to) {
		Object cell = nodes.remove(to);
		if (cell != null) {
			graph.removeCells(new Object[] {cell});
		}
	}
	
	public void ensurePresence(IHyperworkflow to) {
		Object cell = nodes.get(to);
		if (cell == null) {
			JGraphRendering.render(to, graph, nodes.get(to.getParent()));
		}
		else {
			// make sure the cell has the same properties as the TermObject to
			// for now, we are only interested in the geometry
			mxIGraphModel model = graph.getModel();
			mxGeometry geo = model.getGeometry(cell);
			if (geo.getX() != to.getX()
				|| geo.getY() != to.getY()
				|| geo.getWidth() != to.getWidth()
				|| geo.getHeight() != to.getHeight())
			{
				mxGeometry ng = (mxGeometry)geo.clone();
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
			
			//determine the NestedHyperworkflow that contains the specified connection
			Object parentCell = null;
			if (conn.getSource() instanceof NestedHyperworkflow) {
				
				NestedHyperworkflow src = (NestedHyperworkflow)conn.getSource();
				if (src.getChildren().contains(conn.getTarget()))
					parentCell = nodes.get(conn.getSource());
				
			} else {
				parentCell = nodes.get(conn.getSource().getParent());
			}
			
			JGraphRendering.render(conn, graph, parentCell);
		}else {
			System.out.println("TODO: modify edge to match intended geometry...");
			//TODO
		}
	}
	
	public void ensureDisconnected(Connection conn) {
		Object cell = edges.remove(conn);
		if (cell != null) {
			graph.removeCells(new Object[] {cell});
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
	
	public Observable<IHyperworkflow> getObjectAddObservable() {
		return objectAddObservable;
	}
	
	public Observable<IHyperworkflow> getObjectModifyObservable() {
		return objectModifyObservable;
	}
	
	public Observable<IHyperworkflow> getObjectRemoveObservable() {
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
			
			assert(sval instanceof Port
				&& tval instanceof Port
				&& sparval instanceof IHyperworkflow
				&& tparval instanceof IHyperworkflow);
			assert(((Port)sval).index >= 0
				&& ((Port)sval).index < ((IHyperworkflow)sparval).getOutputPorts().size()
				&& !((Port)sval).input
				&& ((Port)tval).index >= 0
				&& ((Port)tval).index < ((IHyperworkflow)tparval).getInputPorts().size()
				&& ((Port)tval).input);
			
			if (value instanceof Connection)
				conn = (Connection)value;
			else {
				// check if a new edge is added
				conn = new Connection();
				if (edges.put(conn, cell) != null) {
					assert(false);
				}
			}
			conn.setSource((IHyperworkflow)sparval);
			conn.setTarget((IHyperworkflow)tparval);
			conn.setSrcPort(conn.getSource().getOutputPorts().get(((Port)sval).index));
			conn.setTargPort(conn.getTarget().getInputPorts().get(((Port)tval).index));
			// notify
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
		assert (model.isVertex(cell) && value instanceof IHyperworkflow);
		IHyperworkflow to = (IHyperworkflow)value;
		if (geo.getX() != to.getX()
			|| geo.getY() != to.getY()
			|| geo.getWidth() != to.getWidth()
			|| geo.getHeight() != to.getHeight())
		{
			double[] dim
				= { geo.getX(), geo.getY(), geo.getWidth(), geo.getHeight() };
			to.setDimensions(dim);
			// notify
			objectModifyObservable.notify(to);
		}
	}
	
	protected class ChangeListener implements mxIEventListener {
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			mxIGraphModel model = graph.getModel();
			mxUndoableEdit edit = (mxUndoableEdit)evt.getProperty("edit");
			List<mxUndoableChange> changes = edit.getChanges();
			for (mxUndoableChange c : changes) {
				// process the following changes:
				// - child change (add/remove)
				// - value change
				// - TODO geometry change
				// - TODO terminal change
				if (c instanceof mxChildChange) {
					mxChildChange cc = (mxChildChange)c;
					Object cell = cc.getChild();
					Object value = model.getValue(cell);
					if (cc.getParent() == null) {
						// something has been removed
						if (value instanceof IHyperworkflow) {
							if (nodes.remove(value) != null) {
								// notify
								objectRemoveObservable.notify((IHyperworkflow)value);
							}
						}
						else if (value instanceof Connection) {
							if (edges.remove(value) != null) {
								// notify
								connectionRemoveObservable.notify((Connection)value);
							}
						}
					}
					else {
						// something has been added
						if (value instanceof IHyperworkflow) {
							Object oldcell = nodes.put((IHyperworkflow)value, cell);
							if (oldcell == null) {
								// check geometry and notify if necessary
								updateNode(cell);
								// FIXME: currently, objectAdd is not used!
							}
							else {
								assert (cell == oldcell);
							}
						}
						else if (value instanceof Connection) {
							// TODO currently this is not supposed to happen
							assert(false);
						}
						else if (value == null) {
							// check if a new edge is added
							if (model.isEdge(cell)) {
								updateEdge(cell);
							}
						}
					}
				}
				else if (c instanceof mxValueChange) {
					assert(false);
				}
				else if (c instanceof mxGeometryChange) {
					Object cell = ((mxGeometryChange)c).getCell();
					if (model.getValue(cell) instanceof IHyperworkflow)
						updateNode(cell);
				}
				else if (c instanceof mxTerminalChange) {
					// just do the same thing as for an added edge
					Object cell = ((mxTerminalChange)c).getCell();
					updateEdge(cell);
				}
			}
		}
	}

}
