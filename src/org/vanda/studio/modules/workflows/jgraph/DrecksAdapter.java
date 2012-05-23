package org.vanda.studio.modules.workflows.jgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.modules.workflows.Model.JobSelection;
import org.vanda.studio.modules.workflows.Model.SingleObjectSelection;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxEventSource.mxIEventListener;
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
					boolean migrateSelection = false;
					if (cc.getPrevious() != null) {
						// something has been removed
						if (cell == graph.getSelectionCell()) {
							migrateSelection = true;
							if (model != null)
								model.setSelection(null);
						}
						value.remove((mxICell) cc.getPrevious());
					}
					if (cc.getParent() != null && cell.getParent() != null)
						value.update(graph, cell.getParent(), cell);
					// value.update(graph, (mxICell) cc.getParent(), cell);
					if (migrateSelection)
						graph.setSelectionCell(cell);
				} else if (c instanceof mxValueChange) {
					// not supposed to happen, or not relevant
				} else if (c instanceof mxGeometryChange) {
					mxICell cell = (mxICell) ((mxGeometryChange) c).getCell();
					Adapter value = (Adapter) gmodel.getValue(cell);
					if (cell.getParent() != null)
						value.update(graph, cell.getParent(), cell); // XXX
					// risky?
					// if (gmodel.isVertex(cell) && gmodel.getParent(cell) !=
					// null)
					// updateNode(cell);
				} else if (c instanceof mxSelectionChange && model != null) {
					Object cell = graph.getSelectionCell();
					if (cell != null) {
						LinkedList<Token> path = new LinkedList<Token>();
						Object cl = (mxCell) gmodel.getParent(cell);
						while (cl != null) {
							Object value = gmodel.getValue(cl);
							if (value != null)
								((Adapter) value).prependPath(path);
							cl = gmodel.getParent(cl);
						}
						((Adapter) gmodel.getValue(cell)).setSelection(model,
								path);
					} else
						model.setSelection(null);
				}
			}
		}
	}

	protected final Model model;
	protected final mxGraph graph;
	protected final ChangeListener changeListener;
	protected final Map<MutableWorkflow, mxICell> translation;

	public DrecksAdapter(Model model) {
		this.model = model;
		translation = new HashMap<MutableWorkflow, mxICell>();
		
		// adapter is responsible for graph component that holds 
		// the current workflow
		if (model != null) {
			graph = new Graph();
			
			model.getAddObservable().addObserver(
					new Observer<Pair<MutableWorkflow, Job>>() {
						@Override
						public void notify(Pair<MutableWorkflow, Job> event) {
							renderChild(event.fst, event.snd);
						}
					});

			model.getModifyObservable().addObserver(
					new Observer<Pair<MutableWorkflow, Job>>() {
						@Override
						public void notify(Pair<MutableWorkflow, Job> event) {
							modifyChild(event.fst, event.snd);
						}
					});

			model.getRemoveObservable().addObserver(
					new Observer<Pair<MutableWorkflow, Job>>() {
						@Override
						public void notify(Pair<MutableWorkflow, Job> event) {
							removeChild(event.fst, event.snd);
						}
					});

			model.getConnectObservable().addObserver(
					new Observer<Pair<MutableWorkflow, Connection>>() {
						@Override
						public void notify(
								Pair<MutableWorkflow, Connection> event) {
							renderConnection(event.fst, event.snd);
						}
					});

			model.getDisconnectObservable().addObserver(
					new Observer<Pair<MutableWorkflow, Connection>>() {
						@Override
						public void notify(
								Pair<MutableWorkflow, Connection> event) {
							removeConnection(event.fst, event.snd);
						}
					});

			model.getNameChangeObservable().addObserver(
					new Observer<MutableWorkflow>() {
						@Override
						public void notify(MutableWorkflow event) {
							// TODO improve
							if (event != DrecksAdapter.this.model.getRoot())
								graph.refresh();
						}
					});
			model.getMarkedElementsObservable().addObserver(
					new Observer<Model>() {
						@Override
						public void notify(Model model) {
							List<mxICell> markedCells = transformElementsToCells(model
									.getMarkedElements());

							List<mxICell> inverseOfMarkedCells = calculateInverseCellList(
									graph.getDefaultParent(), markedCells);

							unhighlightCells(inverseOfMarkedCells);
							highlightCells(markedCells);
						}
					});
		} else {
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
					if (oc.getChildAt(i).getValue() instanceof WorkflowAdapter) {
						inverseList.addAll(calculateInverseCellList(oc
								.getChildAt(i), cells));
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
		mxICell cell = wa.getChild(job.getAddress());
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
		// TODO make this principled
		if (graph.isAutoSizeCell(cell))
			graph.updateCellSize(cell, true);
		graph.refresh();
	}

	public void removeChild(MutableWorkflow hwf, Job job) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getChild(job.getAddress());
		if (cell != null)
			graph.removeCells(new Object[] { cell });
	}

	public void removeConnection(MutableWorkflow hwf, Connection cc) {
		WorkflowAdapter wa = (WorkflowAdapter) translation.get(hwf).getValue();
		mxICell cell = wa.getConnection(cc.address);
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
				for (Connection cc : hwf.getConnections())
					renderConnection(hwf, cc);
			}
		}
	}

	public <F> mxICell renderChild(MutableWorkflow parent, Job job) {
		mxICell parentCell = translation.get(parent);
		WorkflowAdapter wa = (WorkflowAdapter) parentCell.getValue();
		mxICell cell = wa.removeInter(job);
		if (cell != null) {
			wa.setChild(job.getAddress(), cell);
		} else if (job.getAddress() == null
				|| wa.getChild(job.getAddress()) == null) {
			cell = job.selectRenderer(JobRendering.getRendererAssortment())
					.render(job, graph, parentCell);
		}
		// render recursively / recompute WorkflowAdapters
		if (job instanceof CompositeJob)
			render(cell, ((CompositeJob) job).getWorkflow());
		return cell;
	}

	private List<mxICell> transformElementsToCells(
			List<SingleObjectSelection> elements) {
		List<mxICell> cellList = new ArrayList<mxICell>();
		mxICell rootCell = translation.get(model.getRoot());

		for (SingleObjectSelection element : elements) {
			mxICell wfcell = ((Adapter) rootCell.getValue()).dereference(
					element.path.listIterator(), rootCell);
			if (wfcell != null) {
				WorkflowAdapter adapter = (WorkflowAdapter) wfcell.getValue();
				mxICell cell = null;
				if (element instanceof JobSelection) {
					cell = adapter.getChild(element.address);
				} else {
					cell = adapter.getConnection(element.address);
				}
				cellList.add(cell);
			}
		}

		return cellList;
	}

	public <F> void renderConnection(MutableWorkflow parent, Connection cc) {
		mxICell parentCell = translation.get(parent);
		WorkflowAdapter wa = (WorkflowAdapter) parentCell.getValue();

		mxICell cell = wa.removeInter(cc);
		if (cell != null) {
			wa.setConnection(cc.address, cell);
		} else if (wa.getConnection(cc.address) == null) {
			mxICell source = wa.getChild(cc.source);
			mxICell target = wa.getChild(cc.target);

			if (source != null && target != null) {
				graph.getModel().beginUpdate();
				try {
					cell = (mxICell) graph.insertEdge(parentCell, null,
							new ConnectionAdapter(cc), source
									.getChildAt(cc.sourcePort
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

	private void unhighlightCells(List<mxICell> cells) {

		for (mxICell cell : cells) {
			if (cell.isVertex()) {
				cell.setStyle(mxStyleUtils.removeStylename(cell.getStyle(),
						"highlighted"));
			}
			if (cell.isEdge()) {
				//TODO fix style resetting to usual style
				String st = mxStyleUtils.removeStylename(cell.getStyle(),
				"highlightededge");
				System.out.println(st);
				cell.setStyle(null);
			}
		}
		graph.refresh();
	}
}
