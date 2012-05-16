package org.vanda.studio.modules.workflows.jgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.HyperWorkflow;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.modules.workflows.Model.ConnectionSelection;
import org.vanda.studio.modules.workflows.Model.JobSelection;
import org.vanda.studio.modules.workflows.Model.WorkflowSelection;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel.mxSelectionChange;

/**
 * Translate workflow events into render/modify/remove calls -- they manipulate
 * the graph. Translate graph events into updateNode/updateEdge calls -- they
 * manipulate the workflow.
 * 
 * The workflow itself is represented as a node carrying a WorkflowAdapter
 * instance, which translates child and connection addresses into graph cells.
 * This translation is updated when the graph notifies us of changes (i.e., in
 * updateNode and updateEdge).
 * 
 * @author buechse
 * 
 */
public final class Adapter {

	protected class ChangeListener implements mxIEventListener {
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			mxIGraphModel gmodel = graph.getModel();
			mxUndoableEdit edit = (mxUndoableEdit) evt.getProperty("edit");
			List<mxUndoableChange> changes = edit.getChanges();
			for (mxUndoableChange c : changes) {
				// process the following changes:
				// - child change (add/remove)
				// - value change
				// - geometry change
				if (c instanceof mxChildChange) {
					mxChildChange cc = (mxChildChange) c;
					mxICell cell = (mxICell) cc.getChild();
					Object value = gmodel.getValue(cell);
					if (cc.getParent() == null) {
						// something has been removed
						// we make an exception and do not call helper methods
						if (value instanceof Token) {
							WorkflowAdapter wa = (WorkflowAdapter) ((mxICell) cc
									.getPrevious()).getValue();
							if (gmodel.isVertex(cell)) {
								if (wa.removeChild((Token) value) != null)
									wa.workflow.removeChild((Token) value);
							} else {
								if (wa.removeConnection((Token) value) != null)
									wa.workflow.removeConnection((Token) value);
							}
						} else if (value instanceof WorkflowAdapter) {
							System.out.println("Curious thing just happened!");
						}
					} else {
						// something has been added
						if (gmodel.isVertex(cell))
							updateNode(cell);
						else if (gmodel.isEdge(cell))
							updateEdge(cell);
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
					if (gmodel.isVertex(cell))
						updateNode(cell);
				} else if (c instanceof mxSelectionChange) {
					Object cell = graph.getSelectionCell();
					if (cell != null) {
						LinkedList<Token> path = new LinkedList<Token>();
						Object cl = (mxCell) gmodel.getParent(cell);
						while (cl != null) {
							if (gmodel.getValue(cl) instanceof Token)
								path.addFirst((Token) gmodel.getValue(cl));
							cl = gmodel.getParent(cl);
						}
						if (gmodel.isEdge(cell) && gmodel.getValue(cell) instanceof Token) {
							model.setSelection(new ConnectionSelection(path,
									(Token) gmodel.getValue(cell)));
						} else if (gmodel.isVertex(cell) && gmodel.getValue(cell) instanceof Token) {
							model.setSelection(new JobSelection(path, (Token) gmodel.getValue(cell)));
						} else if (gmodel.getValue(cell) instanceof WorkflowAdapter) {
							model.setSelection(new WorkflowSelection(path));
						}
					} else {
						List<Token> el = Collections.emptyList();
						model.setSelection(new WorkflowSelection(el));
					}
				}
			}
		}
	}

	protected final Model<?> model;
	protected final Graph graph;
	protected final ChangeListener changeListener;
	protected final Map<HyperWorkflow<?>, mxICell> translation;

	public <F> Adapter(Model<?> model) {
		this.model = model;
		translation = new HashMap<HyperWorkflow<?>, mxICell>();
		graph = new Graph();

		model.getAddObservable().addObserver(new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				renderChild((MutableWorkflow<?>) event.fst, event.snd);
			}
		});

		model.getModifyObservable().addObserver(new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				modifyChild(event.fst, event.snd);
			}
		});

		model.getRemoveObservable().addObserver(new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				removeChild(event.fst, event.snd);
			}
		});

		model.getConnectObservable().addObserver(new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				renderConnection((MutableWorkflow<?>) event.fst, event.snd);
			}
		});

		model.getDisconnectObservable().addObserver(new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				removeConnection(event.fst, event.snd);
			}
		});

		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
		graph.getSelectionModel().addListener(mxEvent.UNDO, changeListener);

		render(null, model.getRoot());
	}

	public mxGraph getGraph() {
		return graph;
	}

	public void modifyChild(MutableWorkflow<?> hwf, Token address) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getChild(address);
		Job<?> job = hwf.getChild(address);
		mxIGraphModel model = graph.getModel();
		mxGeometry geo = model.getGeometry(cell);
		if (geo.getX() != job.getX() || geo.getY() != job.getY()
				|| geo.getWidth() != job.getWidth()
				|| geo.getHeight() != job.getHeight()) {
			mxGeometry ng = (mxGeometry) geo.clone();
			ng.setX(job.getX());
			ng.setY(job.getY());
			ng.setWidth(job.getWidth());
			ng.setHeight(job.getHeight());
			model.setGeometry(cell, ng);
		}
	}

	/**
	 * keeps the size of a cell big enough to contain all its children properly
	 * 
	 * @param cell
	 */
	private void preventTooSmallNested(Object cell) {
		mxIGraphModel model = graph.getModel();
		// Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);

		if (/* hj instanceof CompositeJob<?, ?> */true) {
			double minWidth = 0;
			double minHeight = 0;

			// determine minimum bounds of cell that contains children
			for (int i = 0; i < model.getChildCount(cell); i++) {
				mxCell child = (mxCell) model.getChildAt(cell, i);

				if (child.getValue() instanceof Job<?>) {
					double childRightBorder = child.getGeometry().getX()
							+ child.getGeometry().getWidth();
					double childBottomBorder = child.getGeometry().getY()
							+ child.getGeometry().getHeight();
					if (childRightBorder > minWidth) {
						minWidth = childRightBorder;
					}
					if (childBottomBorder > minHeight) {
						minHeight = childBottomBorder;
					}
				}
			}

			// adjust x coordinate of cell according to appropriate size
			if (geo.getWidth() < minWidth && !model.isCollapsed(cell)) {
				geo.setWidth(minWidth);
				/*
				 * if (geo.getX() > hj.getX()) { geo.setX(hj.getX() +
				 * hj.getWidth() - minWidth); }
				 */
			}

			// adjust y coordinate of cell according to appropriate size
			if (geo.getHeight() < minHeight && !model.isCollapsed(cell)) {
				geo.setHeight(minHeight);
				/*
				 * if (geo.getY() > hj.getY()) { geo.setY(hj.getY() +
				 * hj.getHeight() - minHeight); }
				 */
			}

			// set the new geometry and refresh graph to make changes visible
			model.setGeometry(cell, geo);
			graph.refresh();
		}
	}

	public void removeChild(MutableWorkflow<?> hwf, Token address) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getChild(address);
		if (cell != null)
			graph.removeCells(new Object[] { cell });
	}

	public void removeConnection(MutableWorkflow<?> hwf, Token address) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getConnection(address);
		if (cell != null) {
			assert (cell.getValue() == address);
			graph.removeCells(new Object[] { cell });
		}
	}

	private void render(mxICell parent, MutableWorkflow<?> hwf) {
		if (!translation.containsKey(hwf)) {
			mxCell cell = null;
			if (parent != null) {
				mxGeometry geo = null;
				mxGeometry geop = parent.getGeometry();
				if (geop != null) {
					geo = new mxGeometry(0, 0, geop.getWidth(),
							geop.getHeight());
					geo.setRelative(true);
				}

				cell = new mxCell(new WorkflowAdapter(hwf), geo, "");
				cell.setVertex(true);

				graph.addCell(cell, parent);
			} else {
				cell = (mxCell) graph.getDefaultParent();
				cell.setValue(new WorkflowAdapter(hwf));
			}
			translation.put(hwf, cell);
			if (hwf != null) {
				for (Token address : hwf.getChildren())
					renderChild(hwf, address);
				for (Token address : hwf.getConnections())
					renderConnection(hwf, address);
			}
		}
	}

	public void renderChild(HyperWorkflow<?> parent, Token address) {
		mxICell parentCell = translation.get(parent);
		WorkflowAdapter wa = (WorkflowAdapter) parentCell.getValue();
		Job<?> hj = parent.getChild(address);
		mxICell cell = wa.removeInter(hj);
		if (cell != null) {
			wa.setChild(address, cell);
			cell.setValue(address);
		} else if (wa.getChild(address) == null) {
			cell = hj.selectRenderer(JobRendering.getRendererAssortment())
					.render(hj, graph, parentCell);
			// render recursively
			if (hj instanceof CompositeJob<?, ?>)
				render(cell, ((CompositeJob<?, ?>) hj).getWorkflow());
		}
	}

	public <F> void renderConnection(HyperWorkflow<F> parent, Token address) {
		mxICell parentCell = translation.get(parent);
		WorkflowAdapter wa = (WorkflowAdapter) parentCell.getValue();

		Connection cc = parent.getConnection(address);
		mxICell cell = wa.removeInter(cc);
		if (cell != null) {
			wa.setConnection(address, cell);
			cell.setValue(address);
			// update selection so it reflects the new value
			if (graph.getSelectionCell() == cell)
				graph.setSelectionCell(cell);
		} else if (wa.getConnection(address) == null) {
			mxICell source = wa.getChild(cc.source);
			mxICell target = wa.getChild(cc.target);

			if (source != null && target != null) {
				assert (source.getValue() == cc.source && target.getValue() == cc.target);

				graph.getModel().beginUpdate();
				try {
					graph.insertEdge(
							parent,
							null,
							address,
							source.getChildAt(cc.sourcePort
									+ parent.getChild(cc.source)
											.getInputPorts().size()),
							target.getChildAt(cc.targetPort));
				} finally {
					graph.getModel().endUpdate();
				}
			} else
				assert (false);
		}

	}

	protected void updateEdge(mxICell cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		Object parentCell = model.getParent(cell);
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(parentCell);
		if (value instanceof Token) {
			// a previously loaded connection is updated, don't change anything
			assert (wa.getConnection((Token) value) == cell);
		} else {
			assert ("".equals(value) || value == null);
			// a new connection has been inserted by the user via GUI
			Object source = model.getTerminal(cell, true);
			Object target = model.getTerminal(cell, false);

			// ignore "unfinished" edges
			if (source != null && target != null) {
				Object sval = model.getValue(source);
				Object tval = model.getValue(target);
				Object sparval = model.getValue(model.getParent(source));
				Object tparval = model.getValue(model.getParent(target));

				assert (sval instanceof PortAdapter
						&& tval instanceof PortAdapter
						&& sparval instanceof Token && tparval instanceof Token);

				Connection cc = new Connection((Token) sparval,
						((PortAdapter) sval).index, (Token) tparval,
						((PortAdapter) tval).index);
				wa.putInter(cc, cell);
				cell.setValue(cc);
				wa.workflow.addConnection(cc);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void updateNode(Object cell) {
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(model
				.getParent(cell));

		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);

		if (value instanceof Job) {
			// set dimensions of job
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			((Job) value).setDimensions(dim);
			wa.putInter((Job) value, (mxICell) cell);
			wa.workflow.addChild((Job) value);
		} else if (value instanceof Token) {
			assert (wa.getChild((Token) value) == cell);
			if (graph.isAutoSizeCell(cell))
				graph.updateCellSize(cell, true); // was: resizeToFitLabel(cell)
			preventTooSmallNested(cell);
			graph.extendParent(cell); // was: resizeParentOfCell(cell)
			Job<?> job = wa.workflow.getChild((Token) model.getValue(cell));
			if (geo.getX() != job.getX() || geo.getY() != job.getY()
					|| geo.getWidth() != job.getWidth()
					|| geo.getHeight() != job.getHeight()) {
				double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
						geo.getHeight() };
				job.setDimensions(dim);
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
