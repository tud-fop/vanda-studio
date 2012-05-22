package org.vanda.studio.modules.workflows.jgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.modules.workflows.Model.ConnectionSelection;
import org.vanda.studio.modules.workflows.Model.JobSelection;
import org.vanda.studio.modules.workflows.Model.SingleObjectSelection;
import org.vanda.studio.modules.workflows.Model.WorkflowSelection;
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
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
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
					Object value = gmodel.getValue(cell);
					boolean migrateSelection = false;
					if (cc.getPrevious() != null) {
						// something has been removed
						// we make an exception and do not call helper methods
						if (cell == graph.getSelectionCell()) {
							migrateSelection = true;
							if (model != null)
								model.setSelection(null);
						}
						if (value instanceof JobAdapter) {
							JobAdapter ja = (JobAdapter) value;
							WorkflowAdapter wa = (WorkflowAdapter) ((mxICell) cc
									.getPrevious()).getValue();
							Token address = ja.job.getAddress();
							if (address != null
									&& wa.removeChild(address) != null) {
								wa.workflow.removeChild(address);
							}
						} else if (value instanceof ConnectionAdapter) {
							ConnectionAdapter ca = (ConnectionAdapter) value;
							WorkflowAdapter wa = (WorkflowAdapter) ((mxICell) cc
									.getPrevious()).getValue();
							Token address = ca.cc.address;
							if (address != null
									&& wa.removeConnection(address) != null) {
								wa.workflow.removeConnection(address);
							}
						} else if (value instanceof WorkflowAdapter) {
							System.out.println("Curious thing just happened!");
						}
					}
					if (cc.getParent() != null) {
						// something has been added
						if (value instanceof JobAdapter)
							updateNode(cell);
						else if (gmodel.isEdge(cell))
							updateEdge(cell);
					}
					if (migrateSelection) {
						graph.setSelectionCell(cell);
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
					mxICell cell = (mxICell) ((mxGeometryChange) c).getCell();
					if (gmodel.isVertex(cell) && gmodel.getParent(cell) != null)
						updateNode(cell);
				} else if (c instanceof mxSelectionChange && model != null) {
					Object cell = graph.getSelectionCell();
					if (cell != null) {
						LinkedList<Token> path = new LinkedList<Token>();
						Object cl = (mxCell) gmodel.getParent(cell);
						while (cl != null) {
							if (gmodel.getValue(cl) instanceof JobAdapter)
								path.addFirst(((JobAdapter) gmodel
										.getValue(cl)).job.getAddress());
							cl = gmodel.getParent(cl);
						}
						if (gmodel.getValue(cell) instanceof ConnectionAdapter) {
							model.setSelection(new ConnectionSelection(
									path,
									((ConnectionAdapter) gmodel.getValue(cell)).cc.address));
						} else if (gmodel.getValue(cell) instanceof JobAdapter) {
							model.setSelection(new JobSelection(path,
									((JobAdapter) gmodel.getValue(cell)).job
											.getAddress()));
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

	protected final Model model;
	protected final mxGraph graph;
	protected final ChangeListener changeListener;
	protected final Map<MutableWorkflow, mxICell> translation;

	public DrecksAdapter(Model model) {
		this.model = model;
		translation = new HashMap<MutableWorkflow, mxICell>();
		graph = new Graph();

		if (model != null) {
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
							unhighlightCells(transformElementsToCells(model.getPreviouslyMarkedElements()));
							highlightCells(transformElementsToCells(model.getMarkedElements()));
						}
					});
		}

		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
		graph.getSelectionModel().addListener(mxEvent.UNDO, changeListener);

		if (model != null)
			render(null, model.getRoot());
		else
			render(null, null);
	}

	public mxGraph getGraph() {
		return graph;
	}
	
	private void highlightCells(List<mxICell> cells) {
		for (mxICell cell : cells) {
			String highlightedStyle = cell.getStyle();
			if (highlightedStyle.length() > 0) 
				highlightedStyle = highlightedStyle + ";";
			highlightedStyle = highlightedStyle 
				+ mxConstants.STYLE_STROKECOLOR + "=#FF0000;" 
				+ mxConstants.STYLE_STROKEWIDTH + "=3";
			cell.setStyle(highlightedStyle);
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

				if (child.getValue() instanceof Job) {
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
		// two reasons why we might not know about hwf:
		// a) it is not in the graph
		// b) it is in the graph, but because of (complex) drag'n'drop
		if (!translation.containsKey(hwf)) {
			mxCell cell = null;
			if (parent != null) {
				// check whether we have case b)
				int i = parent.getChildCount() - 1;
				while (i >= 0
						&& !(parent.getChildAt(i).getValue() instanceof WorkflowAdapter))
					i--;
				if (i >= 0
						&& parent.getChildAt(i).getValue() instanceof WorkflowAdapter) {
					// case b) -- make new WorkflowAdapter as the old one is
					// most likely obsolete
					cell = (mxCell) parent.getChildAt(i);
					WorkflowAdapter wa = new WorkflowAdapter(hwf);
					for (i = 0; i < cell.getChildCount(); i++) {
						mxICell cl = cell.getChildAt(i);
						if (cl.getValue() instanceof JobAdapter)
							wa.setChild(((JobAdapter) cl.getValue()).job
									.getAddress(), cl);
						else if (cl.getValue() instanceof ConnectionAdapter)
							wa.setConnection(
									((ConnectionAdapter) cl.getValue()).cc.address,
									cl);
					}
					cell.setValue(wa);
				} else {
					// case a)
					mxGeometry geo = null;
					mxGeometry geop = parent.getGeometry();
					if (geop != null) {
						geo = new mxGeometry(5, 5, geop.getWidth() - 10,
								geop.getHeight() - 10);
						geo.setRelative(false);
					}

					cell = new mxCell(new WorkflowAdapter(hwf), geo, "workflow");
					cell.setVertex(true);

					graph.addCell(cell, parent);
				}
			} else {
				// only a) applies here
				cell = (mxCell) graph.getDefaultParent();
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

	private List<mxICell> transformElementsToCells(List<SingleObjectSelection> elements) {
		List<mxICell> cellList = new ArrayList<mxICell>();
		
		for (SingleObjectSelection element : elements) {
			WorkflowAdapter adapter = (WorkflowAdapter) translation.get(model.getRoot()).getValue();
			for (Token pathToken : element.path) {
				mxICell cell = adapter.getChild(pathToken);
				if (cell.getValue() instanceof JobAdapter) {
					for (int i = 0; i < cell.getChildCount(); i++) {
						if (cell.getChildAt(i).getValue() instanceof WorkflowAdapter) {
							adapter = (WorkflowAdapter) cell.getChildAt(i).getValue();
						}
					}
				}
			}
			
			mxICell cell = null;
			if (element instanceof JobSelection) {
				cell = adapter.getChild(element.address);
			} else {
				cell = adapter.getConnection(element.address);
			}
			
			cellList.add(cell);
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
				assert (source.getValue() instanceof JobAdapter
						&& ((JobAdapter) source.getValue()).job.getAddress() == cc.source
						&& target.getValue() instanceof JobAdapter && ((JobAdapter) target
							.getValue()).job.getAddress() == cc.target);

				graph.getModel().beginUpdate();
				try {
					cell = (mxICell) graph.insertEdge(
							parentCell,
							null,
							new ConnectionAdapter(cc),
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
	
	private void unhighlightCells(List<mxICell> cells) {
		for (mxICell cell : cells) {
			String[] highlightedStyle = cell.getStyle().split(";");
			String unhighlightedStyle = "";
			for (String s : highlightedStyle) {
				if (!s.contains(mxConstants.STYLE_STROKEWIDTH) 
						&& !s.contains(mxConstants.STYLE_STROKECOLOR))
					unhighlightedStyle = unhighlightedStyle + s + ";"; 
			}
			if (unhighlightedStyle.length() > 0) 
				unhighlightedStyle = unhighlightedStyle.substring(0, unhighlightedStyle.length()-1);
			cell.setStyle(unhighlightedStyle);
		}
		graph.refresh();
	}
	
	protected void updateEdge(mxICell cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		Object parentCell = model.getParent(cell);
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(parentCell);
		if (value instanceof ConnectionAdapter) {
			// a previously loaded connection is updated, don't change anything
			Token address = ((ConnectionAdapter) value).cc.address;
			if (wa.getConnection(address) == null)
				wa.setConnection(address, cell);
			assert (wa.getConnection(address) == cell);
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
						&& sparval instanceof JobAdapter && tparval instanceof JobAdapter);

				Connection cc = new Connection(
						((JobAdapter) sparval).job.getAddress(),
						((PortAdapter) sval).index,
						((JobAdapter) tparval).job.getAddress(),
						((PortAdapter) tval).index);
				wa.putInter(cc, cell);
				cell.setValue(new ConnectionAdapter(cc));
				if (wa.workflow != null)
					wa.workflow.addConnection(cc);
				
				// propagate value change to selection listeners
				if (graph.getSelectionCell() == cell)
					graph.setSelectionCell(cell);
			}
		}
	}

	protected void updateNode(mxICell cell) {		
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		if (value instanceof WorkflowAdapter) {
			// do nothing
		} else {
			assert (value instanceof JobAdapter);
			WorkflowAdapter wa = (WorkflowAdapter) model.getValue(model
					.getParent(cell));
			JobAdapter ja = (JobAdapter) value;

			mxGeometry geo = model.getGeometry(cell);

			if (ja.job.getAddress() == null) {
				// set dimensions of job
				double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
						geo.getHeight() };
				ja.job.setDimensions(dim);
				wa.putInter(ja.job, (mxICell) cell);
				if (wa.workflow != null)
					wa.workflow.addChild((Job) ja.job);
			} else {
				if (wa.getChild(ja.job.getAddress()) == null)
					wa.setChild(ja.job.getAddress(), cell);
				// the following condition can be violated when dragging stuff
				if (wa.getChild(ja.job.getAddress()) == cell) {
					if (graph.isAutoSizeCell(cell))
						graph.updateCellSize(cell, true); // was:
															// resizeToFitLabel(cell)
					preventTooSmallNested(cell);
					graph.extendParent(cell); // was: resizeParentOfCell(cell)

					if (geo.getX() != ja.job.getX()
							|| geo.getY() != ja.job.getY()
							|| geo.getWidth() != ja.job.getWidth()
							|| geo.getHeight() != ja.job.getHeight()) {

						double[] dim = { geo.getX(), geo.getY(),
								geo.getWidth(), geo.getHeight() };
						ja.job.setDimensions(dim);
						
						//XXX testCode for highlighting cells
						// currently a single node is highlighted when it's updated
						List<SingleObjectSelection> elementList = new ArrayList<SingleObjectSelection>();
						Token address = ((Job) ja.job).getAddress();
						List<Token> path = new ArrayList<Token>();
						mxICell pCell = cell;
						while (model.getValue(pCell.getParent().getParent()) != null) {
							JobAdapter jobAd = (JobAdapter) model.getValue(pCell.getParent().getParent());
							path.add(0, jobAd.job.getAddress());
							pCell = pCell.getParent().getParent();
						}
						elementList.add(new JobSelection(path, address));
						this.model.setMarkedElements(elementList);
						//XX end testCode
						
						if (ja.job instanceof CompositeJob) {
							mxICell wCell = translation
									.get(((CompositeJob) ja.job).getWorkflow());
							wCell.setGeometry(new mxGeometry(5, 5, ja.job
									.getWidth() - 10, ja.job.getHeight() - 10));
							graph.refresh();
						}
					}
				}
			}
		}
	};
}
