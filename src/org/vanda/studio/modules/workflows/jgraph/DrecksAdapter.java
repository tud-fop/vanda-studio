package org.vanda.studio.modules.workflows.jgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.Model.ConnectionSelection;
import org.vanda.studio.modules.workflows.model.Model.SingleObjectSelection;
import org.vanda.util.Observer;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxStyleUtils;
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
public final class DrecksAdapter {

	protected class ChangeListener implements mxIEventListener {
		// edges: childChange, terminalChange, terminalChange, geometryChange,
		// terminalChange
		// the we are only interested in the final childChange!
		// the first one is ignored using an additional conjunct (see below)
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
					Adapter value = (Adapter) gmodel.getValue(cell);
					boolean refreshSelection = false;
					if (cell == graph.getSelectionCell()) {
						refreshSelection = true;
						if (model != null)
							model.setSelection(null);
					}
					if (cc.getPrevious() != null) {
						// something has been removed
						value.onRemove((mxICell) cc.getPrevious());
					}
					// the second conjunct is necessary for edges
					if (cc.getParent() != null
							&& cell.getParent() == cc.getParent())
						value.onInsert(graph, cell.getParent(), cell);
					if (refreshSelection)
						graph.setSelectionCell(cell);
				} else if (c instanceof mxValueChange) {
					// not supposed to happen, or not relevant
				} else if (c instanceof mxGeometryChange) {
					mxICell cell = (mxICell) ((mxGeometryChange) c).getCell();
					Adapter value = (Adapter) gmodel.getValue(cell);
					if (cell.getParent() != null && value.inModel())
						value.onResize(graph, cell.getParent(), cell);
				} else if (c instanceof mxSelectionChange && model != null) {
					Object cell = graph.getSelectionCell();
					if (cell != null) {
						((Adapter) gmodel.getValue(cell)).setSelection(model);
					} else
						model.setSelection(null);
				}
			}
		}
	}

	protected class WorkflowListener implements
			Workflows.WorkflowListener<MutableWorkflow> {

		@Override
		public void childAdded(MutableWorkflow mwf, Job j) {
			renderChild(mwf, j);
		}
		
		@Override
		public void childModified(MutableWorkflow mwf, Job j) {
			modifyChild(mwf, j);
		}

		@Override
		public void childRemoved(MutableWorkflow mwf, Job j) {
			removeChild(mwf, j);
		}

		@Override
		public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
			renderConnection(mwf, cc);
		}

		@Override
		public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
			removeConnection(mwf, cc);
		}

		@Override
		public void propertyChanged(MutableWorkflow mwf) {
			// TODO improve
			if (mwf != DrecksAdapter.this.model.getRoot())
				graph.refresh();
		}

		@Override
		public void updated(MutableWorkflow mwf) {
		}

	}

	protected final Model model;
	protected final Graph graph;
	protected final ChangeListener changeListener;
	protected final WorkflowListener workflowListener;
	protected final Map<MutableWorkflow, mxICell> translation;

	public DrecksAdapter(Model model) {
		this.model = model;
		translation = new HashMap<MutableWorkflow, mxICell>();

		// adapter is responsible for graph component that holds
		// the current workflow
		if (model != null) {
			graph = new Graph();
			graph.setAutoOrigin(true);
			/*
			 * System.out.println(graph.getModel().getRoot());
			 * System.out.println(((mxCell)
			 * graph.getModel().getRoot()).getChildAt(0));
			 * System.out.println(graph.getDefaultParent()); ((mxCell)
			 * graph.getModel().getRoot()).getChildAt(0).setStyle("algorithm");
			 * ((mxCell)
			 * graph.getModel().getRoot()).getChildAt(0).setGeometry(new
			 * mxGeometry(0, 0, 1000, 1000));
			 */

			workflowListener = new WorkflowListener();
			model.getRoot()
					.getObservable()
					.addObserver(
							new Observer<Workflows.WorkflowEvent<MutableWorkflow>>() {
								@Override
								public void notify(
										Workflows.WorkflowEvent<MutableWorkflow> event) {
									event.doNotify(workflowListener);
								}
							});

			model.getMarkedElementsObservable().addObserver(
					new Observer<Model>() {
						@Override
						public void notify(Model m) {
							List<mxICell> markedCells = transformElementsToCells(m
									.getMarkedElements());

							List<mxICell> inverseOfMarkedCells = calculateInverseCellList(
									graph.getDefaultParent(), markedCells);

							unhighlightCells(inverseOfMarkedCells);
							highlightCells(markedCells);
						}
					});
		} else {
			workflowListener = null;
			// adapter is responsible for palette graph component,
			// prevent selection of inner workflows
			graph = new Graph() {

				@Override
				public boolean isCellSelectable(Object cell) {
					if (getModel().getValue(cell) instanceof WorkflowAdapter)
						return false;
					return super.isCellSelectable(cell);
				}

			};
		}

		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
		graph.getSelectionModel().addListener(mxEvent.UNDO, changeListener);

		if (model != null)
			render(null, model.getRoot());
		else
			render(null, null);
	}

	private List<mxICell> calculateInverseCellList(Object cell,
			List<mxICell> cells) {

		List<mxICell> inverseList = new ArrayList<mxICell>();

		Object[] objects = graph.getChildCells(cell, true, true);
		for (Object o : objects) {
			mxICell oc = (mxICell) o;
			// add current cell to inverseList if it is not in given cell list
			if (!cells.contains(o)) {
				inverseList.add(oc);
			}

			// check children of current cell for membership within given
			// cell list, add them to inverse list otherwise
			if (oc.getValue() instanceof JobAdapter) {
				for (int i = 0; i < oc.getChildCount(); i++) {

					// check nested children recursively
					if (oc.getChildAt(i).getValue() instanceof WorkflowAdapter) {
						inverseList.addAll(calculateInverseCellList(
								oc.getChildAt(i), cells));
					}

					// unhighlight ports of deselected cell
					if (oc.getChildAt(i).getValue() instanceof PortAdapter) {
						inverseList.add(oc.getChildAt(i));
					}
				}
			}
		}

		return inverseList;
	}

	public mxGraph getGraph() {
		return graph;
	}

	private void highlightCells(List<mxICell> cells) {
		for (mxICell cell : cells) {
			if (cell.isVertex()) {
				cell.setStyle(mxStyleUtils.addStylename(cell.getStyle(),
						"highlighted"));
			}
			if (cell.isEdge()) {
				cell.setStyle(mxStyleUtils.addStylename(cell.getStyle(),
						"highlightededge"));
			}
		}
		graph.refresh();
	}

	public void modifyChild(MutableWorkflow hwf, Job job) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getChild(job);
		mxIGraphModel m = graph.getModel();
		mxGeometry geo = m.getGeometry(cell);
		if (geo.getX() != job.getX() || geo.getY() != job.getY()
				|| geo.getWidth() != job.getWidth()
				|| geo.getHeight() != job.getHeight()) {
			mxGeometry ng = (mxGeometry) geo.clone();
			ng.setX(job.getX());
			ng.setY(job.getY());
			ng.setWidth(job.getWidth());
			ng.setHeight(job.getHeight());
			m.setGeometry(cell, ng);
		}
		// TODO make this principled
		if (graph.isAutoSizeCell(cell))
			graph.updateCellSize(cell, true);
		graph.refresh();
	}

	public void removeChild(MutableWorkflow hwf, Job job) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getChild(job);
		if (cell != null)
			graph.removeCells(new Object[] { cell });
	}

	public void removeConnection(MutableWorkflow hwf, ConnectionKey cc) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getConnection(cc);
		if (cell != null)
			graph.removeCells(new Object[] { cell });
	}

	private void render(mxICell parent, MutableWorkflow hwf) {
		if (!translation.containsKey(hwf)) {
			mxICell cell = null;
			if (parent != null) {
				cell = ((CompositeJobAdapter) parent.getValue())
						.renderWorkflowCell(graph, parent, hwf);
			} else {
				// only a) applies here
				cell = (mxICell) graph.getDefaultParent();
				cell.setValue(new WorkflowAdapter(hwf));
			}
			translation.put(hwf, cell);
			if (hwf != null) {
				for (Job job : hwf.getChildren())
					renderChild(hwf, job);
				for (ConnectionKey cc : hwf.getConnections())
					renderConnection(hwf, cc);
			}
		}
	}

	public <F> mxICell renderChild(MutableWorkflow parent, Job job) {
		mxICell parentCell = translation.get(parent);
		WorkflowAdapter wa = (WorkflowAdapter) parentCell.getValue();
		mxICell cell = wa.getChild(job);
		if (cell != null) {
			// wa.setChild(job.getAddress(), cell);
			// ((Adapter) cell.getValue()).onInsert(graph, parentCell, cell);
		} else {
			cell = job.selectRenderer(JobRendering.getRendererAssortment())
					.render(job, graph, parentCell);
		}
		return cell;
	}

	public <F> void renderConnection(MutableWorkflow parent, ConnectionKey cc) {
		mxICell parentCell = translation.get(parent);
		WorkflowAdapter wa = (WorkflowAdapter) parentCell.getValue();

		mxICell cell = wa.getConnection(cc);
		if (cell != null) {
			// ((Adapter) cell.getValue()).onInsert(graph, parentCell, cell);
		} else {
			ConnectionKey scc = parent.getConnectionSource(cc);
			mxICell source = wa.getChild(scc.target);
			mxICell target = wa.getChild(cc.target);

			if (source != null && target != null) {
				graph.getModel().beginUpdate();
				try {
					mxICell scell = null;
					mxICell tcell = null;
					for (int i = 0; i < source.getChildCount(); i++) {
						mxICell cl = source.getChildAt(i);
						Object value = cl.getValue();
						if (value instanceof PortAdapter
								&& !((PortAdapter) value).input
								&& ((PortAdapter) value).port == scc.targetPort)
							scell = cl;
					}
					for (int i = 0; i < target.getChildCount(); i++) {
						mxICell cl = target.getChildAt(i);
						Object value = cl.getValue();
						if (value instanceof PortAdapter
								&& ((PortAdapter) value).input
								&& ((PortAdapter) value).port == cc.targetPort)
							tcell = cl;
					}
					assert (scell != null && tcell != null);
					graph.insertEdge(parentCell, null,
							new ConnectionAdapter(cc), scell, tcell);
				} finally {
					graph.getModel().endUpdate();
				}
			} else
				assert (false);
		}
	}

	private List<mxICell> transformElementsToCells(
			final List<SingleObjectSelection> elements) {
		List<mxICell> cellList = new ArrayList<mxICell>();
		List<mxICell> activeInputPorts = new ArrayList<mxICell>();

		for (SingleObjectSelection e : elements) {
			// XXX nested Workflows are not supported by this code
			MutableWorkflow hwf = model.getRoot();
			if (e instanceof ConnectionSelection) {
				mxICell c = ((WorkflowAdapter) translation.get(hwf).getValue())
						.getConnection(((ConnectionSelection) e).cc);
				cellList.add(c);
			}
		}

		// check all suspected active input ports by looking for an edge cell
		// that ends in the current input port cell suspect
		for (mxICell suspectPort : activeInputPorts) {
			for (int i = 0; i < cellList.size(); i++) {
				mxICell highlightedCell = cellList.get(i);
				if (highlightedCell.isEdge()
						&& highlightedCell.getTerminal(false).equals(
								suspectPort)) {

					cellList.add(suspectPort);
					break;
				}
			}

		}
		return cellList;
	}

	private void unhighlightCells(List<mxICell> cells) {

		for (mxICell cell : cells) {
			if (cell.isVertex()) {
				cell.setStyle(mxStyleUtils.removeStylename(cell.getStyle(),
						"highlighted"));
			}
			if (cell.isEdge()) {
				// TODO fix style resetting to usual style
				String st = mxStyleUtils.removeStylename(cell.getStyle(),
						"highlightededge");
				// System.out.println(st);
				cell.setStyle(st);
			}
		}
		graph.refresh();
	}
}
