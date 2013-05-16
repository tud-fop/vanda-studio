package org.vanda.studio.modules.workflows.impl;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.render.jgraph.mxDropTargetListener;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.LayoutSelector;
import org.vanda.studio.app.WindowSystem;

import org.vanda.studio.modules.workflows.model.WorkflowDecoration;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.HasActions;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.util.Util;

import org.vanda.view.AbstractView;
import org.vanda.view.View;

import org.vanda.workflows.data.Database;

import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class WorkflowEditorImpl implements WorkflowEditor, WorkflowListener<MutableWorkflow> {

	protected final Application app;

//	protected final WorkflowDecoration model;
	protected final View view;
	protected final PresentationModel presentationModel;

	protected final Database database;


	protected final MyMxGraphComponent component;
	protected final mxGraphOutline outline;
	protected JComponent palette;
	// protected final JSplitPane mainpane;

	public WorkflowEditorImpl(Application app, List<ToolFactory> toolFactories, Pair<MutableWorkflow, Database> phd) {
		this.app = app;

		view = new View(phd.fst);
//		model = new WorkflowDecoration(view);
		presentationModel = new PresentationModel(view, this);
		
		component = new MyMxGraphComponent(presentationModel.getVisualization().getGraph());
		new mxDropTargetListener(presentationModel, component);


		database = phd.snd;

		component.setDragEnabled(false);
		component.getGraphControl().addMouseListener(new EditMouseAdapter());
		component.getGraphControl().addMouseWheelListener(new MouseZoomAdapter(app, component));
		component.addKeyListener(new DelKeyListener());
		component.setPanning(true);
		component.getPageFormat().setOrientation(PageFormat.LANDSCAPE);
		component.setPageVisible(true);
		component.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		component.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		component.zoomActual();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				component.getVerticalScrollBar().setValue((int) (component.getVerticalScrollBar().getMaximum() * 0.35));

			}

		});
		// component.getGraphControl().scrollRectToVisible(new
		// Rectangle(133,1000,0,0));
		// component.setPanning(true); // too complic: must press SHIFT+CONTROL
		// (component.getGraph().getDefaultParent());
		outline = new mxGraphOutline(component);
		outline.setPreferredSize(new Dimension(250, 250));
//		mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component, outline);
//		mainpane.setOneTouchExpandable(true);
//		mainpane.setResizeWeight(0.9);
//		mainpane.setDividerSize(6);
//		mainpane.setBorder(null);
//		mainpane.setName(model.getRoot().getName());
//		mainpane.setDividerLocation(0.7);
		component.setName(view.getWorkflow().getName());

		app.getUIModeObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application a) {
				if (a.getUIMode().isLargeContent())
					component.zoomTo(1.5, false);
				else
					component.zoomActual();
			}
		});
		app.getWindowSystem().addContentWindow(null, component, null);
		app.getWindowSystem().focusContentWindow(component);
		component.requestFocusInWindow();

		for (ToolFactory tf : toolFactories)
			tf.instantiate(this);
		app.getWindowSystem().addAction(component, new ResetZoomAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK));
		app.getWindowSystem().addAction(component, new CloseWorkflowAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
		
		outline.setName("Map");
		addToolWindow(outline, WindowSystem.SOUTHEAST);

		view.getWorkflow().getObservable()
				.addObserver(new Observer<WorkflowEvent<MutableWorkflow>>() {


			@Override
			public void notify(WorkflowEvent<MutableWorkflow> event) {
				event.doNotify(WorkflowEditorImpl.this);
			}

		});
		// send some initial event ("updated" will be sent)
		view.getWorkflow().beginUpdate();
		view.getWorkflow().endUpdate();
	}

	static {
		try {
			mxGraphTransferable.dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
					+ "; class=com.mxgraph.swing.util.mxGraphTransferable");
		} catch (ClassNotFoundException cnfe) {
			// do nothing
			System.out.println("Problem!");
		}
	}

	public void close() {
		// remove tab
		app.getWindowSystem().removeContentWindow(component);
	}

	/**
	 * Handles KeyEvents such as removing cells when focused and pressing DEL
	 * 
	 * @author afischer
	 * 
	 */
	protected class DelKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {

			// check if KeyEvent occurred on graph component,
			// e.getSource().equals(component) &&
			// only handle DELETE-key
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				removeSelectedCell();
			}

		}
	}

	private void removeSelectedCell() {
//		WorkflowSelection ws = model.getSelection();
//		if (ws instanceof SingleObjectSelection)
		//	((SingleObjectSelection) ws).remove();
		List<AbstractView> selection = view.getCurrentSelection();
		for (AbstractView v : selection)
			v.remove(view);
	}

	/**
	 * Handles mouse actions: opens cell-specific views/editors on double-click,
	 * opens context menu on mouse right-click
	 * 
	 * @author buechse, afischer
	 * 
	 */
	protected class EditMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == 1 && e.getClickCount() == 2) {
				// double click using left mouse button
				Object cell = component.getCellAt(e.getX(), e.getY());
				Object value = component.getGraph().getModel().getValue(cell);

				if (value instanceof HasActions) {
					Action def = Util.getDefaultAction((HasActions) value);
					if (def != null)
						def.invoke();
				}
			} else if (e.getButton() == 3) {
				// show context menu when right clicking a node or an edge
				Object cell = component.getCellAt(e.getX(), e.getY());
				final Object value = component.getGraph().getModel().getValue(cell);

				PopupMenu menu = null;

				// create connection specific context menu
				Cell pmCell = (Cell) value;
//				if (value instanceof ConnectionAdapter) {
				if (value != null && pmCell.getType() == "ConnectionCell") {
					menu = new PopupMenu(((ConnectionCell) pmCell).toString());
					// menu = new PopupMenu(cell.toString());

					@SuppressWarnings("serial")
					JMenuItem item = new JMenuItem("Remove Connection") {
						@Override
						public void fireActionPerformed(ActionEvent _) {
							removeSelectedCell();
						}
					};

					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
					menu.add(item);
				}

				// create node specific context menu
//				if (value instanceof JobAdapter) {
				if (value != null && pmCell.getType() == "JobCell") {
					menu = new PopupMenu(((JobCell) pmCell).getLabel());
					@SuppressWarnings("serial")
					JMenuItem item = new JMenuItem("Remove Job") {
						@Override
						public void fireActionPerformed(ActionEvent _) {
							removeSelectedCell();
						}
					};
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
					menu.add(item);
				}

				if (menu != null) {
					if (value instanceof HasActions) {
						HasActions ha = (HasActions) value;
						LinkedList<Action> as = new LinkedList<Action>();
						ha.appendActions(as);
						for (final Action a : as) {
							@SuppressWarnings("serial")
							JMenuItem item = new JMenuItem(a.getName()) {
								@Override
								public void fireActionPerformed(ActionEvent _) {
									a.invoke();
								}
							};
							menu.add(item);
						}
					}
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	/**
	 * enables mouse wheel zooming function within graph editor window keeps the
	 * mouse cursor as zoom center
	 * 
	 * @author afischer
	 */
	protected static class MouseZoomAdapter implements MouseWheelListener {
		protected Application app;
		protected MyMxGraphComponent component;

		public MouseZoomAdapter(Application app, MyMxGraphComponent component) {
			this.app = app;
			this.component = component;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			Rectangle r = component.getViewport().getViewRect();

			mxGraphView view = component.getGraph().getView();
			// translate view to keep mouse point as fixpoint
			double factor = e.getWheelRotation() > 0 ? 1 / 1.2 : 1.2;
			double scale = (double) ((int) (view.getScale() * 100 * factor)) / 100;
			view.setScale(scale);
			Rectangle rprime = new Rectangle((int) (r.x + e.getX() * (factor - 1.0)), (int) (r.y + e.getY()
					* (factor - 1.0)), r.width, r.height);
			component.getGraphControl().scrollRectToVisible(rprime);
		}
	}

	/**
	 * a context popup menu that displays a components title
	 * 
	 * @author afischer
	 * 
	 */
	@SuppressWarnings("serial")
	protected static class PopupMenu extends JPopupMenu {

		public PopupMenu(String title) {
			add(new JLabel("<html><b>" + title + "</b></html>"));
			addSeparator();
		}
	}

	protected class CloseWorkflowAction implements Action {

		@Override
		public String getName() {
			return "Close Workflow";
		}

		@Override
		public void invoke() {
			close();
		}
	}

	protected class ResetZoomAction implements Action {

		@Override
		public String getName() {
			return "Reset Zoom";
		}

		@Override
		public void invoke() {
			component.zoomActual();
		}
	}

	@Override
	public void addToolWindow(JComponent c, LayoutSelector layout) {
		app.getWindowSystem().addToolWindow(component, null, c, layout);
	}

	@Override
	public void focusToolWindow(JComponent c) {
		app.getWindowSystem().focusToolWindow(c);
	}

	@Override
	public void removeToolWindow(JComponent c) {
		app.getWindowSystem().removeToolWindow(component, c);
	}

	@Override
	public void addAction(Action a, KeyStroke keyStroke) {
		app.getWindowSystem().addAction(component, a, keyStroke);
	}

	@Override
	public Application getApplication() {
		return app;
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		if (mwf == view.getWorkflow()) {
			component.setName(mwf.getName());
			app.getWindowSystem().addContentWindow(null, component, null);

		}
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void updated(MutableWorkflow mwf) {
	}

	@SuppressWarnings("serial")
	private static class MyMxGraphComponent extends mxGraphComponent {

		protected mxICell currentCollapsibleCell;
		protected List<mxICell> collapsedCells;

		public MyMxGraphComponent(mxGraph graph) {
			super(graph);
			this.graphHandler = new MyMxGraphHandler(this);
			// DO NOT change this setting, otherwise selecting an inner
			// workflow's parent job is kind of hard because the inner workflow
			// is always selected
			setSwimlaneSelectionEnabled(false);
			collapsedCells = new ArrayList<mxICell>();
		}

		@Override
		public boolean hitFoldingIcon(Object cell, int x, int y) {
			if (cell != null) {
				mxIGraphModel model = graph.getModel();

				// Draws the collapse/expand icons
				boolean isEdge = model.isEdge(cell);

				if (foldingEnabled && (model.isVertex(cell) || isEdge)) {
					mxCellState state = graph.getView().getState(cell);

					if (state != null
							&& graph.getModel().getValue(cell) instanceof WorkflowCell) {
						state = graph.getView().getState(
								graph.getModel().getParent(cell));


						ImageIcon icon = getFoldingIcon(state);

						if (icon != null) {
							if (getFoldingIconBounds(state, icon).contains(x, y)) {
								currentCollapsibleCell = (mxICell) graph.getModel().getParent(cell);
							} else
								currentCollapsibleCell = null;
						}
					}
				}
			}

			return super.hitFoldingIcon(cell, x, y);
		}

		@Override
		/**
		 * Note: This is not used during drag and drop operations due to limitations
		 * of the underlying API. To enable this for move operations set dragEnabled
		 * to false.
		 *
		 * @param event
		 * @return Returns true if the given event is a panning event.
		 */
		public boolean isPanningEvent(MouseEvent event) {
			return (event != null) && !event.isShiftDown() && event.isControlDown();
		}

	}

	private static class MyMxGraphHandler extends mxGraphHandler {

		private MyMxGraphComponent component;

		public MyMxGraphHandler(MyMxGraphComponent component) {
			super(component);
			this.component = component;
		}

		@Override
		protected Cursor getCursor(MouseEvent e) {
			Cursor cursor = super.getCursor(e);

			Object cell = graphComponent.getCellAt(e.getX(), e.getY(), false);
			mxIGraphModel m = graphComponent.getGraph().getModel();

			// if mouse is over an inner workflow's title bar
			if (cell != null && m.getValue(cell) instanceof WorkflowCell) {

				// check if the fold button of its parent job was hit and adjust
				// cursor image
				if ((component.currentCollapsibleCell != null && component.currentCollapsibleCell.equals(m
						.getParent(cell)))) {
					cursor = FOLD_CURSOR;
				}
			}

			return cursor;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);

			Object cell = graphComponent.getCellAt(e.getX(), e.getY(), false);

			// if an inner workflow cell was clicked
			if (cell != null
					&& graphComponent.getGraph().getModel().getValue(cell) instanceof WorkflowCell) {


				// if mouse is currently over the fold button of the parent job,
				// collapse or expand parent
				if (component.currentCollapsibleCell != null
						&& ((mxICell) cell).getParent().equals(component.currentCollapsibleCell)) {

					// if current collapsible cell is already in collapsed
					// state,
					// it is an element of the collapsedCells list
					boolean collapsed = component.collapsedCells.contains(component.currentCollapsibleCell);

					// FIXME? for some reason, getGraph().foldCells(...)
					// does not update the isCollapsed state of the changed cell
					// which is why there is a collapsedCells list that keeps
					// track
					// of all currently collapsed cells

					// collapse/expand depending on current state
					graphComponent.getGraph().foldCells(!collapsed, false,
							new Object[] { component.currentCollapsibleCell });

					// remove expanded cell from collapsedCells list
					if (collapsed) {
						component.collapsedCells.remove(component.currentCollapsibleCell);
					} else {
						component.collapsedCells.add(component.currentCollapsibleCell);
					}
				}

			}
		}
	}

	@Override
	public void setPalette(JComponent c) {
		if (palette != c) {
			if (palette != null)
				removeToolWindow(palette);
			palette = c;
			if (palette != null)
				addToolWindow(palette, WindowSystem.SOUTHWEST);
			// mainpane.setRightComponent(c);
		}
	}

	@Override
	public Database getDatabase() {
		return database;
	}


	@Override
	public View getView() {
		return view;
	}


}
