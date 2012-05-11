package org.vanda.studio.modules.workflows;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Choice;
import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.hyper.AtomicJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.hyper.Serialization;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.modules.workflows.jgraph.Adapter;
import org.vanda.studio.modules.workflows.jgraph.JobRendering;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.ExceptionMessage;
import org.vanda.studio.util.HasActions;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Util;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.view.mxGraph;

public class WorkflowEditor {

	protected Application app;

	protected MutableWorkflow<?> hwf;
	protected mxGraphComponent component;
	protected Adapter renderer;
	protected mxGraph palettegraph;
	protected mxGraphComponent palette;
	protected JSplitPane mainpane;
	protected JTextArea inspector;

	public WorkflowEditor(Application a, MutableWorkflow<?> hwf) {
		app = a;

		this.hwf = hwf;

		renderer = new Adapter(hwf);

		palettegraph = JobRendering.createGraph();
		palettegraph.setCellsLocked(true);
		palette = new mxGraphComponent(palettegraph);
		// palette.getGraphControl().addMouseListener(
		// new EditMouseAdapter(app, palette));
		component = new mxGraphComponent(renderer.getGraph());
		component.setDragEnabled(false);
		component.getGraphControl().addMouseListener(
				new EditMouseAdapter(app, component));
		component.getGraphControl().addMouseWheelListener(
				new MouseZoomAdapter(app, component));
		component.addKeyListener(new DelKeyListener(app, component));
		updatePalette();
		mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component,
				palette);
		mainpane.setOneTouchExpandable(true);
		// mainpane.setDividerLocation(320);
		mainpane.setResizeWeight(1);
		mainpane.setDividerSize(6);
		mainpane.setBorder(null);
		
		inspector = new JTextArea();
		
		JScrollPane therealinspector = new JScrollPane(inspector);
		therealinspector.setName("Inspector");

		getComponent().setName("Workflow");

		app.getUIModeObservable().addObserver(new UIModeObserver(app, palette));
		app.getUIModeObservable().addObserver(
				new UIModeObserver(app, component));
		// enable save button in menu
		app.getWindowSystem().addAction(getComponent(),
				new SaveWorkflowAction());
		// enable close button in menu
		app.getWindowSystem().addAction(getComponent(),
				new CheckWorkflowAction());
		app.getWindowSystem().addAction(getComponent(),
				new CloseWorkflowAction());
		app.getWindowSystem().addContentWindow("", getComponent().getName(),
				null, getComponent(), null
				/*
				 * new WorkflowModule.WorkflowModuleInstance .CloseWorkflowAction( this)
				 */);
		app.getWindowSystem().addToolWindow(getComponent(), "inspector", "Inspector", null, therealinspector);
		app.getWindowSystem().focusContentWindow(getComponent());
		getComponent().requestFocusInWindow();
		
		Observer<Object> recheckObserver = new Observer<Object>() {

			@Override
			public void notify(Object event) {
				checkWorkflow();
			}
			
		};
		
		hwf.getAddObservable().addObserver(recheckObserver);
		hwf.getRemoveObservable().addObserver(recheckObserver);
		hwf.getConnectObservable().addObserver(recheckObserver);
		hwf.getDisconnectObservable().addObserver(recheckObserver);
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
	
	protected void checkWorkflow() {
		try {
			ImmutableWorkflow iwf = hwf.freeze();
			StringBuilder sb = new StringBuilder();
			iwf.appendText(sb);
			//System.out.print(sb);
			sb.append("Instances\n");
			List<ImmutableWorkflow> iwfs = iwf.unfold();
			for (ImmutableWorkflow i : iwfs) {
				sb.append("-------\n\n");
				i.appendText(sb);
				sb.append("\n");
			}
			inspector.setText(sb.toString());
		}
		catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
		}
	}

	public void close() {
		// remove tab
		app.getWindowSystem().removeContentWindow(getComponent());
	}

	public JComponent getComponent() {
		return mainpane;
	}

	protected void updatePalette() {
		palettegraph.getModel().beginUpdate();
		try {
			// clear seems to reset the zoom, so we call notify at the end
			((mxGraphModel) palettegraph.getModel()).clear();
			ArrayList<Element> items = new ArrayList<Element>(app
					.getToolMetaRepository().getRepository().getItems());
			items.add(new Choice());
			items.add(new Literal("String", ""));
			Collections.sort(items, new Comparator<Element>() {
				@Override
				public int compare(Element o1, Element o2) {
					return o1.getCategory().compareTo(o2.getCategory());
				}
			});

			// top left corner of first palette tool, width, height
			double[] d = { 20, 10, 100, 80 };
			for (Element item : items) {
				Job<?> hj = new AtomicJob(item);
				hj.setDimensions(d);
				hj.selectRenderer(JobRendering.getRendererAssortment()).render(
						hj, palettegraph, null);
				d[1] += 60;
			}
		} finally {
			palettegraph.getModel().endUpdate();
		}
		// TODO notifyUIMode(app);
	}

	/**
	 * Handles KeyEvents such as removing cells when focussed and pressing DEL
	 * 
	 * @author afischer
	 * 
	 */
	protected static class DelKeyListener extends KeyAdapter {
		protected Application app;
		protected mxGraphComponent component;

		public DelKeyListener(Application app, mxGraphComponent component) {
			this.app = app;
			this.component = component;
		}

		@Override
		public void keyPressed(KeyEvent e) {

			mxGraph g = component.getGraph();
			mxIGraphModel mod = g.getModel();

			// check if KeyEvent occurred on graph component,
			// only handle DELETE-key
			if (e.getSource().equals(component)
					&& e.getKeyCode() == KeyEvent.VK_DELETE) {

				// get selected cells
				Object[] cells = g.getSelectionCells();
				// delete connections first, followed by nodes
				for (Object o : cells) {
					// FIXME this is no longer possible
					if (mod.isEdge(o))
						((Job) ((Connection) mod.getValue(o)).getSource())
								.getParent().removeConnection(
										(Connection) mod.getValue(o));
					// HyperWorkflow.removeConnectionGeneric((Connection<?>)
					// mod.getValue(o));
				}
				for (Object o : cells) {
					if (mod.isVertex(o) && mod.getValue(o) instanceof Job<?>)
						MutableWorkflow.removeChildGeneric((Job<?>) mod
								.getValue(o));
				}
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
	protected static class EditMouseAdapter extends MouseAdapter {
		protected Application app;
		protected mxGraphComponent component;

		public EditMouseAdapter(Application app, mxGraphComponent component) {
			this.app = app;
			this.component = component;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// double click using left mouse button
			if (e.getButton() == 1 && e.getClickCount() == 2) {
				Object cell = component.getCellAt(e.getX(), e.getY());
				Object value = component.getGraph().getModel().getValue(cell);

				if (value instanceof HasActions) {
					Action def = Util.getDefaultAction((HasActions) value);
					if (def != null)
						def.invoke();
				}
			}

			// show context menu when right clicking a node or an edge
			if (e.getButton() == 3) {
				Object cell = component.getCellAt(e.getX(), e.getY());
				final Object value = component.getGraph().getModel()
						.getValue(cell);

				PopupMenu menu = null;

				// create connection specific context menu
				if (value instanceof Connection<?>) {
					// menu = new PopupMenu(((Connection<?>) value).toString());
					menu = new PopupMenu(cell.toString());

					@SuppressWarnings("serial")
					JMenuItem item = new JMenuItem("Remove Connection") {
						@Override
						public void fireActionPerformed(ActionEvent e) {
							// FIXME oh noes, see above
							// HyperWorkflow.removeConnectionGeneric((Connection<?>)
							// value);
						}
					};

					item.setAccelerator(KeyStroke.getKeyStroke(
							KeyEvent.VK_DELETE, 0));
					menu.add(item);
					// Connection c = (Connection) value;
				}

				// create node specific context menu
				if (value instanceof Job<?>) {
					menu = new PopupMenu(((Job<?>) value).getName());

					// only create a remove action if it's not a palette tool
					if (((Job<?>) value).getParent() != null) {
						@SuppressWarnings("serial")
						JMenuItem item = new JMenuItem("Remove Vertex") {
							@Override
							public void fireActionPerformed(ActionEvent e) {
								MutableWorkflow
										.removeChildGeneric((Job<?>) value);
							}
						};

						item.setAccelerator(KeyStroke.getKeyStroke(
								KeyEvent.VK_DELETE, 0));
						menu.add(item);
					}

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

	protected static class UIModeObserver implements Observer<Application> {
		protected mxGraphComponent component;

		public UIModeObserver(Application app, mxGraphComponent component) {
			this.component = component;
			notify(app);
		}

		@Override
		public void notify(Application a) {
			if (a.getUIMode().isLargeContent())
				component.zoomTo(1.5, false);
			else
				component.zoomActual();
		}
	}

	protected class SaveWorkflowAction implements Action {

		public SaveWorkflowAction() {
		}

		@Override
		public String getName() {
			return "Save Workflow";
		}

		@Override
		public void invoke() {
			// create a new file opening dialog
			@SuppressWarnings("serial")
			JFileChooser chooser = new JFileChooser("") {
				@Override
				public void approveSelection() {
					File f = getSelectedFile();
					if (f.exists() && getDialogType() == SAVE_DIALOG) {
						int result = JOptionPane.showConfirmDialog(this,
								"The file exists already. Replace?",
								"Existing file",
								JOptionPane.YES_NO_CANCEL_OPTION);
						switch (result) {
						case JOptionPane.YES_OPTION:
							super.approveSelection();
							return;
						case JOptionPane.NO_OPTION:
							return;
						case JOptionPane.CANCEL_OPTION:
							cancelSelection();
							return;
						default:
							return;
						}
					}
					super.approveSelection();
				}
			};

			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(new FileNameExtensionFilter(
					"Hyperworkflows (*.hwf)", "hwf"));
			chooser.setVisible(true);
			int result = chooser.showSaveDialog(null);

			// once file choice is approved, save the chosen file
			if (result == JFileChooser.APPROVE_OPTION) {
				File chosenFile = chooser.getSelectedFile();
				String filePath = chosenFile.getPath();
				Serialization.save(hwf, filePath);
			}
		}
	}

	protected class CheckWorkflowAction implements Action {

		@Override
		public String getName() {
			return "Check Workflow";
		}

		@Override
		public void invoke() {
			checkWorkflow();
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
}
