package org.vanda.studio.modules.workflows.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.model.VObject;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.workflows.Connection;
import org.vanda.studio.modules.workflows.Hyperworkflow;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;
import org.vanda.studio.modules.workflows.Job;
import org.vanda.studio.util.Observer;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;

public class WorkflowEditor implements Editor<VWorkflow> {

	protected Application app;
	protected HashMap<String, WorkflowEditorTab> tabs;

	// protected JSplitPane mainpane;
	// protected JTabbedPane tabpane;
	// protected boolean visible;
	// protected UIModeObserver umo;

	public WorkflowEditor(Application a) {
		app = a;
		tabs = new HashMap<String, WorkflowEditorTab>();
		app.getUIModeObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application a) {
				for (Entry<String, WorkflowEditorTab> e : tabs.entrySet())
					e.getValue().notifyUIMode(a);
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

	@Override
	public void open(VWorkflow t) {
		// check if a tab is already open
		WorkflowEditorTab tab = tabs.get(t.getId());
		// if not, open one
		if (tab == null) {
			tab = new WorkflowEditorTab(t);
			tabs.put(t.getId(), tab);
			// tabpane.add(t.getName(), tab.getComponent());
		}
		// // select the tab
		// tabpane.setSelectedComponent(tab.getComponent());
		// // if editor is not visible, make it visible
		// if (!visible) {
		// updatePalette();
		// // TODO bind repository
		// app.getWindowSystem().addContentWindow("", "Term Editor", null,
		// mainpane);
		// visible = true;
		// }
		// focus the editor
		app.getWindowSystem().addContentWindow("", t.getName(), null,
				tab.getComponent());
		app.getWindowSystem().focusContentWindow(tab.getComponent());
		tab.getComponent().requestFocusInWindow();
	}

	public void close(VWorkflow t) {
		WorkflowEditorTab tab = tabs.remove(t.getId());
		if (tab == null)
			throw new UnsupportedOperationException(
					"attempt to close nonextant editor");
		// // hide editor if no tabs are open
		// if (tabs.size() == 0) {
		// app.getWindowSystem().removeContentWindow(mainpane);
		// visible = false;
		// }
		// remove tab
		app.getWindowSystem().removeContentWindow(tab.getComponent());
		// tabpane.remove(tab.getComponent());
	}

	protected class WorkflowEditorTab {
		protected VWorkflow vworkflow;
		protected NestedHyperworkflow nhwf;
		protected mxGraphComponent component;
		protected JGraphRenderer renderer;
		protected JGraphRendering.Graph palettegraph;
		protected mxGraphComponent palette;
		protected JSplitPane mainpane;

		public WorkflowEditorTab(VWorkflow t) {
			vworkflow = t;
			nhwf = vworkflow.load();
			System.out.println("loading " + nhwf);
			
			//XXX provide menu entry to save hyperworkflow
			app.getWindowSystem().addAction(new Action() {
				public String getName() {
					return "Save Workflow";
				}
				
				public void invoke() {
					nhwf.save("/home/student/afischer/test-load.hwf");
				}
			});
			
			renderer = new JGraphRenderer(nhwf);
			
			// add listeners to renderer - every change within the graph
			// (renderer) is propagated to the model

			renderer.getObjectAddObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							nhwf.ensurePresence(o);
						}
					});
			renderer.getObjectModifyObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							//TODO
						}
					});
			renderer.getObjectRemoveObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							nhwf.ensureAbsence(o);
						}
					});
			renderer.getConnectionAddObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection c) {
							nhwf.ensureConnected(c);
						}
					});
			renderer.getConnectionModifyObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection c) {
							//TODO
						}
					});
			renderer.getConnectionRemoveObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection c) {
							nhwf.ensureDisconnected(c);
						}
					});

			// add listeners to NestedHyperworkflow - changes within the model
			// are propagated to the graph (renderer)
			
			nhwf.getAddObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							renderer.ensurePresence(o);
						}
					});
			nhwf.getModifyObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							//TODO
						}
					});
			nhwf.getRemoveObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							renderer.ensureAbsence(o);
						}
					});
			nhwf.getConnectObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection conn) {
							renderer.ensureConnected(conn);
						}
					});
			nhwf.getDisconnectObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection conn) {
							renderer.ensureDisconnected(conn);
						}
					});
			
			palettegraph = JGraphRendering.createGraph();
			palettegraph.setCellsLocked(true);
			palette = new mxGraphComponent(palettegraph);
			palette.getGraphControl().addMouseListener(
					new EditMouseAdapter(app, palette));
			component = new mxGraphComponent(renderer.getGraph());
			component.setDragEnabled(false);
			component.getGraphControl().addMouseListener(
					new EditMouseAdapter(app, component));
			component.getGraphControl().addMouseWheelListener(
					new MouseZoomAdapter(app, component));
			updatePalette();
			mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component,
					palette);
			mainpane.setOneTouchExpandable(true);
			// mainpane.setDividerLocation(320);
			mainpane.setResizeWeight(1);
			mainpane.setDividerSize(6);
			mainpane.setBorder(null);
			
			// display nodes and edges of a loaded nhwf
			renderRecursively(nhwf);
		}

		public JComponent getComponent() {
			return mainpane;
		}

		protected void notifyUIMode(Application app) {
			setZoom(component, app.getUIMode().isLargeContent());
			setZoom(palette, app.getUIMode().isLargeContent());
		}

		private void renderRecursively(NestedHyperworkflow nested) {
						
			// render children recursivley
			ArrayList<Hyperworkflow> childrenList = new ArrayList<Hyperworkflow>(nested.getChildren());
			for (Hyperworkflow child : childrenList) {
				renderer.ensurePresence(child);
				if (child instanceof NestedHyperworkflow) {
					renderRecursively((NestedHyperworkflow) child);
				}
			}	

			// render connections of the current NestedHyperworkflow
			ArrayList<Connection> connectionList = new ArrayList<Connection>(nested.getConnections());
			for (Connection conn : connectionList) {
				renderer.ensureConnected(conn);
			}
		}
		
		protected void updatePalette() {
			palettegraph.getModel().beginUpdate();
			try {
				// clear seems to reset the zoom, so we call notify at the end
				((mxGraphModel) palettegraph.getModel()).clear();
				ArrayList<VObject> items = new ArrayList<VObject>(app
						.getGlobalRepository().getItems());
				Collections.sort(items, new Comparator<VObject>() {
					@Override
					public int compare(VObject o1, VObject o2) {
						return o1.getCategory().compareTo(o2.getCategory());
					}
				});
				
				// top left corner of first palette tool, width, height
				double[] d = { 20, 10, 100, 80 };
				for (VObject item : items) {
					Hyperworkflow to = new Job(item);
					to.setDimensions(d);
					JGraphRendering.render(to, palettegraph, null);
					d[1] += 90;
				}
			} finally {
				palettegraph.getModel().endUpdate();
			}
			notifyUIMode(app);
		}
	}

	protected static void setZoom(mxGraphComponent component, boolean large) {
		if (large)
			component.zoomTo(1.5, false);
		else
			component.zoomActual();
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

				if (value instanceof Hyperworkflow) {
					ArrayList<Action> as = new ArrayList<Action>();
					((Hyperworkflow) value).appendActions(as);
					if (as.size() > 0) {
						as.get(0).invoke();
					}
				}
			}
		}
	}

	/**
	 * enables mouse wheel zooming function within graph editor window
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
			if (e.getWheelRotation() > 0)
				component.zoomOut();
			else
				component.zoomIn();
		}
	}
}
