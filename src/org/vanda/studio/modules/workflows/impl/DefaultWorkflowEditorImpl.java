package org.vanda.studio.modules.workflows.impl;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.vanda.fragment.model.SemanticAnalysis;
import org.vanda.fragment.model.SyntaxAnalysis;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.LayoutSelector;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class DefaultWorkflowEditorImpl implements WorkflowEditor, WorkflowListener<MutableWorkflow> {
	protected final Application app;
	protected mxGraphComponent component;
	protected final Database database;
	protected mxGraphOutline outline;
	protected SemanticAnalysis semA;
	protected SyntaxAnalysis synA;
	protected final View view;

	public DefaultWorkflowEditorImpl(Application app, Pair<MutableWorkflow, Database> phd) {
		this.app = app;
		view = new View(phd.fst);
		database = phd.snd;
	}

	@Override
	public void addAction(Action a, KeyStroke keyStroke) {
		app.getWindowSystem().addAction(component, a, keyStroke);
	}

	@Override
	public void addToolWindow(JComponent c, LayoutSelector layout) {
		app.getWindowSystem().addToolWindow(component, null, c, layout);
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
	public void focusToolWindow(JComponent c) {
		app.getWindowSystem().focusToolWindow(c);
	}

	@Override
	public Application getApplication() {
		return app;
	}

	@Override
	public Database getDatabase() {
		return database;
	}

	@Override
	public SemanticAnalysis getSemanticAnalysis() {
		return semA;
	}

	@Override
	public SyntaxAnalysis getSyntaxAnalysis() {
		return synA;
	}

	@Override
	public View getView() {
		return view;
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		if (mwf == view.getWorkflow()) {
			component.setName(mwf.getName());
			app.getWindowSystem().addContentWindow(null, component, null);
		}
	}

	@Override
	public void removeToolWindow(JComponent c) {
		app.getWindowSystem().removeToolWindow(component, c);
	}

	@Override
	public void setPalette(JComponent c) {
	}

	@Override
	public void updated(MutableWorkflow mwf) {
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

	public void close() {
		// remove tab
		app.getWindowSystem().removeContentWindow(component);
	}

	/**
	 * enables mouse wheel zooming function within graph editor window keeps the
	 * mouse cursor as zoom center
	 * 
	 * @author afischer
	 */
	protected static class MouseZoomAdapter implements MouseWheelListener {
		protected Application app;
		protected mxGraphComponent component;

		public MouseZoomAdapter(Application app, mxGraphComponent component) {
			this.app = app;
			this.component = component;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			Rectangle r = component.getViewport().getViewRect();

			mxGraphView view = component.getGraph().getView();
			// translate view to keep mouse point as fixpoint
			double factor = e.getWheelRotation() > 0 ? 1 / 1.2 : 1.2;
			double scale = view.getScale() * factor;
			view.setScale(scale);
			Rectangle rprime = new Rectangle((int) (r.x + e.getX() * (factor - 1.0)), (int) (r.y + e.getY()
					* (factor - 1.0)), r.width, r.height);
			component.getGraphControl().scrollRectToVisible(rprime);
		}
	}

	@SuppressWarnings("serial")
	protected static class MyMxGraphComponent extends mxGraphComponent {

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

					if (state != null && graph.getModel().getValue(cell) instanceof WorkflowCell) {
						state = graph.getView().getState(graph.getModel().getParent(cell));

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

	protected static class MyMxGraphHandler extends mxGraphHandler {

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
			if (cell != null && graphComponent.getGraph().getModel().getValue(cell) instanceof WorkflowCell) {

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

	protected void configureComponent() {
		component.setDragEnabled(false);
		component.getGraphControl().addMouseWheelListener(new MouseZoomAdapter(app, component));
		component.setPanning(true);
		component.getPageFormat().setOrientation(PageFormat.LANDSCAPE);
		component.setPageVisible(true);
		// component.setPageVisible(false);
		// component.setBackground(new Color(123, 123, 123));
		component.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		component.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		app.getUIModeObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application a) {
				if (a.getUIMode().isLargeContent())
					component.zoomTo(1.5, false);
				else
					component.zoomActual();
			}
		});
	}
	
	protected void setupOutline() {
		outline = new mxGraphOutline(component);
		outline.setPreferredSize(new Dimension(250, 250));
		outline.setName("Map");
		addToolWindow(outline, WindowSystem.SOUTHEAST);
	}

	@Override
	public void addAction(Action a, String imageName, KeyStroke keyStroke) {
		app.getWindowSystem().addAction(component, a, imageName, keyStroke);
	}
}
