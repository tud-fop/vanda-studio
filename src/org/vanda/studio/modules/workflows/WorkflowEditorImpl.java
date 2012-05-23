package org.vanda.studio.modules.workflows;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.modules.workflows.Model.SingleObjectSelection;
import org.vanda.studio.modules.workflows.Model.WorkflowSelection;
import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.ExceptionMessage;
import org.vanda.studio.util.HasActions;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Util;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;

public class WorkflowEditorImpl implements WorkflowEditor {

	protected final Application app;
	protected final Model model;
	protected final mxGraphComponent component;
	protected final DrecksAdapter renderer;
	protected final Palette palette;
	protected final JSplitPane mainpane;
	protected final Observer<Object> recheckObserver;

	public WorkflowEditorImpl(Application a, MutableWorkflow hwf,
			List<ToolFactory> tools) {
		app = a;
		model = new Model(hwf);
		renderer = new DrecksAdapter(model);
		palette = new Palette(app);
		palette.update();

		component = new mxGraphComponent(renderer.getGraph());
		//component.setDragEnabled(false);
		component.getGraphControl().addMouseListener(new EditMouseAdapter());
		component.getGraphControl().addMouseWheelListener(
				new MouseZoomAdapter(app, component));
		component.addKeyListener(new DelKeyListener());
		mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component,
				palette.getComponent());
		mainpane.setOneTouchExpandable(true);
		mainpane.setResizeWeight(1);
		mainpane.setDividerSize(6);
		mainpane.setBorder(null);
		mainpane.setName(model.getRoot().getName());

		app.getUIModeObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application a) {
				if (a.getUIMode().isLargeContent())
					component.zoomTo(1.5, false);
				else
					component.zoomActual();
			}
		});
		app.getWindowSystem().addAction(mainpane, new CheckWorkflowAction());
		app.getWindowSystem().addContentWindow(null, mainpane, null);
		app.getWindowSystem().focusContentWindow(mainpane);
		mainpane.requestFocusInWindow();
		
		for (ToolFactory tf : tools)
			tf.instantiate(this, model);
		app.getWindowSystem().addAction(mainpane, new CloseWorkflowAction());

		recheckObserver = new Observer<Object>() {
			@Override
			public void notify(Object event) {
				try {
					model.checkWorkflow();
				} catch (Exception e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
		};

		recheckObserver.notify(null);

		model.getAddObservable().addObserver(recheckObserver);
		model.getRemoveObservable().addObserver(recheckObserver);
		model.getConnectObservable().addObserver(recheckObserver);
		model.getDisconnectObservable().addObserver(recheckObserver);
		
		model.getNameChangeObservable().addObserver(new Observer<MutableWorkflow>() {
			@Override
			public void notify(MutableWorkflow event) {
				if (event == model.getRoot()) {
					mainpane.setName(event.getName());
					app.getWindowSystem().addContentWindow(null, mainpane, null);
				}
			}
		});
	}

	static {
		try {
			mxGraphTransferable.dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ "; class=com.mxgraph.swing.util.mxGraphTransferable");
		} catch (ClassNotFoundException cnfe) {
			// do nothing
			System.out.println("Problem!");
		}
	}

	public void close() {
		// remove tab
		app.getWindowSystem().removeContentWindow(mainpane);
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
				WorkflowSelection ws = model.getSelection();
				if (ws instanceof SingleObjectSelection)
					((SingleObjectSelection) ws).remove(model.getRoot());

			}

		}
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
				final Object value = component.getGraph().getModel()
						.getValue(cell);

				PopupMenu menu = null;

				// create connection specific context menu
				if (value instanceof Connection) {
					// menu = new PopupMenu(((Connection<?>) value).toString());
					menu = new PopupMenu(cell.toString());

					@SuppressWarnings("serial")
					JMenuItem item = new JMenuItem("Remove Connection") {
						@Override
						public void fireActionPerformed(ActionEvent e) {
							/*
							 * MutableWorkflow
							 * .removeConnectionGeneric((Connection) value);
							 */
						}
					};

					item.setAccelerator(KeyStroke.getKeyStroke(
							KeyEvent.VK_DELETE, 0));
					menu.add(item);
					// Connection c = (Connection) value;
				}

				// create node specific context menu
				if (value instanceof Job) {
					menu = new PopupMenu(((Job) value).getItem().getName());

					// only create a remove action if it's not a palette tool
					/*
					 * if (((Job<?>) value).getParent() != null) {
					 * 
					 * @SuppressWarnings("serial") JMenuItem item = new
					 * JMenuItem("Remove Vertex") {
					 * 
					 * @Override public void fireActionPerformed(ActionEvent e)
					 * { MutableWorkflow .removeChildGeneric((Job<?>) value); }
					 * };
					 * 
					 * item.setAccelerator(KeyStroke.getKeyStroke(
					 * KeyEvent.VK_DELETE, 0)); menu.add(item); }
					 */

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
								public void fireActionPerformed(ActionEvent e) {
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
		protected mxGraphComponent component;

		public MouseZoomAdapter(Application app, mxGraphComponent component) {
			this.app = app;
			this.component = component;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			if (e.getWheelRotation() > 0) {
				component.zoomOut();
			} else {
				component.zoomIn();
			}

			// translate view to keep mouse point as fixpoint
			double scaleAfter = component.getGraph().getView().getScale();
			component
					.getGraph()
					.getView()
					.scaleAndTranslate(scaleAfter,
							-e.getX() * (1.0 - 1.0 / scaleAfter),
							-e.getY() * (1.0 - 1.0 / scaleAfter));
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

	protected class CheckWorkflowAction implements Action {

		@Override
		public String getName() {
			return "Check Workflow";
		}

		@Override
		public void invoke() {
			recheckObserver.notify(null);
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

	@Override
	public void addToolWindow(JComponent c) {
		app.getWindowSystem().addToolWindow(mainpane, null, c);
	}

	@Override
	public void focusToolWindow(JComponent c) {
		app.getWindowSystem().focusToolWindow(c);
	}

	@Override
	public void removeToolWindow(JComponent c) {
		app.getWindowSystem().removeToolWindow(mainpane, c);
	}

	@Override
	public void addAction(Action a) {
		app.getWindowSystem().addAction(mainpane, a);
	}
}
