package org.vanda.studio.modules.workflows.jgraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.generation.Workflow;
import org.vanda.studio.model.hyper.CompositeHyperJob;
import org.vanda.studio.model.hyper.HyperConnection;
import org.vanda.studio.model.hyper.HyperJob;
import org.vanda.studio.model.hyper.HyperWorkflow;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

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

public class GraphRenderer {

	private boolean paletteRenderer;
	private mxGraph graph;
	private ChangeListener changeListener;
	private Map<Object, mxICell> translation;
	private MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> addObservable;
	private MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> modifyObservable;
	private MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> removeObservable;
	private MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> connectObservable;
	private MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> disconnectObservable;

	public <F, V> GraphRenderer(HyperWorkflow<F,V> root, boolean paletteRenderer) {
		this.paletteRenderer = paletteRenderer;
		
		addObservable = new MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>();
		modifyObservable = new MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>();
		removeObservable = new MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>();
		connectObservable = new MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>();
		disconnectObservable = new MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>();
		
		translation = new HashMap<Object, mxICell>();
		graph = new Graph();

		// bind defaultParent of the graph and the root hyperworkflow
		// to each other and save them in the node mapping
		((mxCell) graph.getDefaultParent()).setValue(root);
		//translation.put(null, (mxICell) graph.getDefaultParent());
		
		// bind graph; for example, react on new, changed, or deleted elements
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
	}
	
	public Observable<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> getAddObservable() {
		return addObservable;
	}
	public Observable<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> getModifyObservable() {
		return modifyObservable;
	}
	public Observable<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> getRemoveObservable() {
		return removeObservable;
	}
	public Observable<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> getConnectObservable() {
		return connectObservable;
	}
	public Observable<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> getDisconnectObservable() {
		return disconnectObservable;
	}
	
	protected void remove(HyperWorkflow<?,?> parent, HyperJob<?> hj) {
		if (translation.containsKey(hj)) {
			mxICell cell = translation.remove(hj);
			graph.removeCells(new Object[] {cell});
			
			// notify observer that a node has been removed
			removeObservable.notify(new Pair<HyperWorkflow<?,?>, HyperJob<?>>(parent, hj));
		}
	}
	
	protected void remove(HyperWorkflow<?,?> parent, HyperConnection<?> cc) {
		if (translation.containsKey(cc)) {
			mxICell cell = translation.remove(cc);
			graph.removeCells(new Object[] {cell});
			
			// notify observer that an edge has been removed
			disconnectObservable.notify(new Pair<HyperWorkflow<?,?>, HyperConnection<?>>(parent, cc));
		}
	}
	
	@SuppressWarnings({ "unchecked"})
	protected <F, V, IV> void render(CompositeHyperJob<F, V, IV, ?> parent,
			HyperWorkflow<F, IV> hwf) {
		
		if (!translation.containsKey(hwf)) {
			mxCell cell = null;
			if (parent != null) {
				mxICell parentCell = translation.get(parent);
				// cell = graph.insertVertex(parentCell, "", hwf, 0, 0, 1, 1,
				// "");
				mxGeometry geo = null;
				mxGeometry geop = parentCell.getGeometry();
				if (geop != null) {
					geo = new mxGeometry(10, 10, geop.getWidth()-20,
							geop.getHeight()-20);
					geo.setRelative(false);
				}

				cell = new mxCell(hwf, geo, "fillColor=#caffee;opacity=50");
				cell.setVertex(true);
				graph.addCell(cell, parentCell);
			} else
				cell = (mxCell) graph.getDefaultParent();
			translation.put(hwf, cell);
			// XXX this could blow up big time
			// bind
			addObservable.notify(new Pair<HyperWorkflow<?,?>, HyperJob<?>>(hwf, null));
			for (HyperJob<IV> c : hwf.getChildren())
				render(hwf, c);
			for (HyperConnection<IV> cc : hwf.getConnections())
				render(hwf, cc);
		}
	}

	@SuppressWarnings("unchecked")
	public <F, V, IV> void render(HyperWorkflow<F, V> parent, HyperJob<V> hj) {
		if (!translation.containsKey(hj)) {
			Object parentCell = translation.get(parent);
			hj.selectRenderer(JobRendering.getRendererAssortment()).render(hj,
					graph, parentCell);
			
			if (hj instanceof CompositeHyperJob<?, ?, ?, ?>) {
				// render recursively
				render((CompositeHyperJob<F, V, IV, ?>) hj,
						((CompositeHyperJob<F, V, IV, ?>) hj).getWorkflow());
			}
		}
	}

	protected <F, V> void render(HyperWorkflow<F, V> parent, HyperConnection<V> cc) {
		Object cell = translation.get(cc);

		if (cell == null) {
			Object parentCell = translation.get(parent);
			assert (parentCell != null);
			if (parentCell == null) {
				parentCell = graph.getDefaultParent();
			}

			mxICell source = (mxICell) translation.get(cc.getSource());
			mxICell target = (mxICell) translation.get(cc.getTarget());

			if (source != null && target != null) {
				assert (source.getValue() == cc.getSource());
				assert (target.getValue() == cc.getTarget());

				// ports are children of a vertex mxCell in the following order:
				// input ports, then output ports
				source = source.getChildAt(cc.getSource().getInputPorts().size() + cc.getSourcePort());
				// target ports are input ports, hence, they come first as children
				target = target.getChildAt(cc.getTargetPort());

				graph.getModel().beginUpdate();
				try {
					// create new edge based on given parent and retrieved
					// source and target
					mxCell edge = new mxCell(cc, new mxGeometry(), null);
					// edge.setId(null);
					edge.setEdge(true);
					edge.setSource(source);
					edge.setTarget(target);

					// add edge to the graph
					graph.addCell(edge, parentCell);

					// NOTE: g.insertEdge(parent, id, value, source, target)
					// does NOT work correctly. It fires too many
					// mxChildChanges
					// and does not insert the edge within the given parent
					// the problem seems to be the method
					// g.createEdge(parent, id, value, source, target,
					// style)
					// FIXME maybe the problem is not the jgraph API but
					// some
					// weird code I wrote concerning inner target ports
				} finally {
					graph.getModel().endUpdate();
				}
			} else
				assert (false);
		}

	}

	@SuppressWarnings({ "unchecked" })
	private void addNode(mxICell cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);
		assert (model.isVertex(cell) && value instanceof HyperJob<?>);
		HyperJob<?> hj = (HyperJob<?>) value;

		// make sure the node does not already exist (i.e. is in map)
		if (/*model.getParent(cell) != null &&*/ !translation.containsKey(hj)) {
			// add to map
			translation.put(hj, cell);
			
			// add the contained HyperWorkflow to translation map
			// FIXME: somehow, probably due to cloning process, the HyperWorkflow-mxCell
			// does not contain the same hyperWorkflow value as the parent HyperJob
			if (hj instanceof CompositeHyperJob<?,?,?,?>) {
				int index = -1;
				for (int i = 0; i < model.getChildCount(cell); i ++) {
					if (model.getValue(model.getChildAt(cell, i)) instanceof HyperWorkflow<?,?>) {
						assert (index < 0);
						index = i;
					}
				}
				assert (index >= 0);
				translation.put(((CompositeHyperJob<?,?,?,?>)hj).getWorkflow(), (mxICell) model.getChildAt(cell, index));
			}
			
			// add hyperjob to parent hyperworkflow
			// XXX this could blow up, typewise
			Object parent = ((mxCell) cell).getParent().getValue();
			
			assert (parent instanceof HyperWorkflow<?, ?>);
			
			// if the current HyperJob exists already as child of its parenting cell
			// because it was loaded from a previously saved file
			// only set the parenting HyperWorkflow correctly
			if (((HyperWorkflow<?, ?>) parent).getChildren().contains(hj)) {
				hj.setParent((HyperWorkflow) parent);
			} else {
				// the HyperJob was just added by the user via the GUI, hence
				// add it to its parent cell's HyperWorkflow
				
				// notify observer that a node has been added
				addObservable.notify(new Pair<HyperWorkflow<?,?>, HyperJob<?>>((HyperWorkflow) parent, hj));
			}
			
			// set dimensions of to
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			hj.setDimensions(dim);
			modifyObservable.notify(new Pair<HyperWorkflow<?,?>, HyperJob<?>>(hj.getParent(),hj));
		}
	}

	public mxGraph getGraph() {
		return graph;
	}

	@SuppressWarnings({ "unchecked" })
	private void updateEdge(mxICell cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		HyperConnection<?> conn = null;
		assert (model.isEdge(cell));
		Object source = model.getTerminal(cell, true);
		Object target = model.getTerminal(cell, false);

		// ignore "unfinished" edges
		if (source != null && target != null) {
			Object sval = model.getValue(source);
			Object tval = model.getValue(target);
			Object sparval = model.getValue(model.getParent(source));
			Object tparval = model.getValue(model.getParent(target));

			assert (sval instanceof PortAdapter && tval instanceof PortAdapter
					&& sparval instanceof HyperJob<?> && tparval instanceof HyperJob<?>);

			// a previously loaded connection is updated, don't change anything
			if (value instanceof HyperConnection<?>) {
				conn = (HyperConnection<?>) value;
				if (!translation.containsKey(conn))
					translation.put(conn, cell);
			} else {
				// a new connection has been inserted by the user via GUI
				conn = new HyperConnection((HyperJob<?>) sparval,
						((PortAdapter) sval).index, (HyperJob<?>) tparval,
						((PortAdapter) tval).index);
				
				//assert (conn.getSource().getParent() == conn.getTarget().getParent());
				if (conn.getSource().getParent() != conn.getTarget().getParent()) 
					assert(false);
					
				//assert (conn.getSource().getParent() == cell.getParent().getValue());
				if (conn.getSource().getParent() != cell.getParent().getValue()) 
					assert(false);
				
				translation.put(conn, cell);
				model.setValue(cell, conn);
				
				// notify observer that an edge has been added
				connectObservable.notify(new Pair<HyperWorkflow<?,?>, HyperConnection<?>>((HyperWorkflow) cell.getParent().getValue(), conn));
			}
		}
	}

	private void updateNode(Object cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);
		assert (model.isVertex(cell) && value instanceof HyperJob<?>);
		HyperJob<?> hj = (HyperJob<?>) value;
		
		// check if changes occurred to the given cell
		if (geo.getX() != hj.getX() || geo.getY() != hj.getY()
				|| geo.getWidth() != hj.getWidth()
				|| geo.getHeight() != hj.getHeight()) {
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			hj.setDimensions(dim);
			
			if (translation.containsKey(hj) && hj instanceof CompositeHyperJob<?,?,?,?>) {
				mxICell workflowCell = translation.get(((CompositeHyperJob<?,?,?,?>)hj).getWorkflow());
				workflowCell.setGeometry(new mxGeometry(10,10, hj.getWidth()-20, hj.getHeight()-20));
				graph.refresh();
			}
			
			modifyObservable.notify(new Pair<HyperWorkflow<?,?>, HyperJob<?>>(hj.getParent(), hj));
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
				// - geometry change
				// - ??? terminal change
				if (c instanceof mxChildChange) {
					mxChildChange cc = (mxChildChange) c;
					mxICell cell = (mxICell) cc.getChild();
					Object value = model.getValue(cell);
					if (cc.getParent() == null) {
						// something has been removed
						if (value instanceof HyperJob<?>) {
							HyperJob<?> hj = (HyperJob<?>) value;
							remove(hj.getParent(), hj);
						} else if (value instanceof HyperConnection<?>) {
							HyperConnection<?> conn = (HyperConnection<?>) value;
							remove(conn.getParent(), conn);
						}
					} else {
						// something has been added
						if (value instanceof HyperJob<?>) {
							if (paletteRenderer) {
								// palette renderer does not need a changeListener
								// except for filling translation map
								
								if (!translation.containsKey((HyperJob<?>)value))
									translation.put((HyperJob<?>)value, cell);
							} else {
								addNode(cell);
							}
						} else if (value instanceof HyperConnection<?>) {
							// happens when a loaded nhwf contains connection
							// and they are added to the graph
							updateEdge(cell);
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

					/*
					 * // assert (false);
					 * System.out.println("mxValueChange of: " + ((mxCell)
					 * ((mxValueChange) c).getCell()) .getValue());
					 */
				} else if (c instanceof mxGeometryChange) {
					Object cell = ((mxGeometryChange) c).getCell();
					if (model.getValue(cell) instanceof HyperJob<?>)
						updateNode(cell);
				} else if (c instanceof mxTerminalChange) {
					// NOTE this case should not happen
					// creation of a new edge or loading an edge calls
					// updateEdge already and as of now terminals do not
					// change, connection can only be created and/or removed
					
					// FIXME correction: this case should be removed entirely,
					// we do not care about terminal changes at all
					//assert (false);
					
					// just do the same thing as for an added edge
					// Object cell = ((mxTerminalChange) c).getCell();
					// updateEdge(cell);
				}
			}
		}
	}
}

/**
 * upon increasing a nodes size, also adjust the size of the parent cell
 * 
 * @param cell
 * 
 *            private void resizeParentOfCell(Object cell) { mxIGraphModel model
 *            = graph.getModel(); mxGeometry geo = model.getGeometry(cell);
 * 
 *            // node's left end is no longer inside its parent's bounding box
 *            // -> expand parent's bounds to the left if (geo.getX() < 0 ||
 *            geo.getY() < 0) { mxCell parentCell = (mxCell)
 *            model.getParent(cell); mxGeometry parentGeo = (mxGeometry)
 *            model.getGeometry(parentCell);
 * 
 *            // only update geometry if parent is not the root if (parentGeo !=
 *            null) { parentGeo.setX(Math.min(parentGeo.getX() + geo.getX(),
 *            parentGeo.getX()));
 *            parentGeo.setWidth(Math.max(parentGeo.getWidth() - geo.getX(),
 *            parentGeo.getWidth())); parentGeo.setY(Math.min(parentGeo.getY() +
 *            geo.getY(), parentGeo.getY())); parentGeo.setHeight(Math.max(
 *            parentGeo.getHeight() - geo.getY(), parentGeo.getHeight()));
 *            model.setGeometry(model.getParent(cell), parentGeo);
 * 
 *            // update current cell's geometry geo.setX(Math.max(0,
 *            geo.getX())); geo.setY(Math.max(0, geo.getY()));
 *            model.setGeometry(cell, geo);
 * 
 *            // update parent of currently resized node such that // recent
 *            resize actions are propagated to its parent...
 *            updateNode(model.getParent(cell));
 * 
 *            // update graph graph.refresh(); } else { // a node has been moved
 *            over left or top bounds of the // graph's view, scale the view to
 *            compensate
 *            graph.getView().scaleAndTranslate(graph.getView().getScale(),
 *            Math.max(-geo.getX(), 0), Math.max(-geo.getY(), 0));
 *            graph.getView().reload(); graph.refresh(); } } }
 **/

/**
 * prevents the cell from shrinking too much, ensures that the label still fits
 * in the cell
 * 
 * @param cell
 * 
 *            private void resizeToFitLabel(Object cell) { mxIGraphModel model =
 *            graph.getModel(); Object value = model.getValue(cell); mxGeometry
 *            geo = model.getGeometry(cell); assert (model.isVertex(cell) &&
 *            value instanceof HyperJob<?>); HyperJob<?> hj = (HyperJob<?>)
 *            value;
 * 
 *            // determine font and corresponding width/height of label Font
 *            font = mxUtils.getFont(graph.getCellStyle(cell)); FontMetrics fm =
 *            mxUtils.getFontMetrics(font); int labelWidth =
 *            fm.stringWidth(graph.getLabel(cell)) + 20; int numberOfLines =
 *            graph.getLabel(cell).split("\n").length; int labelHeight =
 *            numberOfLines * font.getSize() + 20;
 * 
 *            // adjust x coordinate of cell according to appropriate size if
 *            (geo.getWidth() < labelWidth) { geo.setWidth(labelWidth); if
 *            (geo.getX() > hj.getX()) { geo.setX(hj.getX() + hj.getWidth() -
 *            labelWidth); } }
 * 
 *            // adjust y coordinate of cell according to appropriate size if
 *            (geo.getHeight() < labelHeight) { geo.setHeight(labelHeight); if
 *            (geo.getY() > hj.getY()) { geo.setY(hj.getY() + hj.getHeight() -
 *            labelHeight); } }
 * 
 *            // reset geometry to changed value and update graph
 *            model.setGeometry(cell, geo); graph.refresh(); }
 */