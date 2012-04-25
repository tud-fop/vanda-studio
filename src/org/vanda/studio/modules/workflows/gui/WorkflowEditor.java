package org.vanda.studio.modules.workflows.gui;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.model.Tool;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.workflows.Connection;
import org.vanda.studio.modules.workflows.Hyperworkflow;
import org.vanda.studio.modules.workflows.Job;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;
import org.vanda.studio.util.Observer;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.view.mxGraph;

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

		// enable save button in menu
		app.getWindowSystem().enableAction(
				new WorkflowModule.WorkflowModuleInstance.SaveWorkflowAction());
		
		// enable close button in menu
		app.getWindowSystem().enableAction(
				new WorkflowModule.WorkflowModuleInstance.CloseWorkflowAction());
	}

	public void close(VWorkflow t) {
		WorkflowEditorTab tab = tabs.remove(t.getId());
		if (tab == null)
			throw new UnsupportedOperationException(
					"attempt to close nonexistant editor");
		// // hide editor if no tabs are open
		// if (tabs.size() == 0) {
		// app.getWindowSystem().removeContentWindow(mainpane);
		// visible = false;
		// }
		// remove tab
		app.getWindowSystem().removeContentWindow(tab.getComponent());
		// tabpane.remove(tab.getComponent());

		// check if all tabs are closed
		if (tabs.isEmpty()) {
			// disable saving option in menu
			app.getWindowSystem().disableAction(
				new WorkflowModule.WorkflowModuleInstance.SaveWorkflowAction());
			// disable closing option in menu
			app.getWindowSystem().disableAction(
					new WorkflowModule.WorkflowModuleInstance.CloseWorkflowAction());
		}
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
			nhwf = vworkflow.load(app);
			System.out.println("loading " + nhwf);

			// XXX provide global menu entry to save hyperworkflow
			app.getWindowSystem().addAction(new Action() {
				public String getName() {
					return "Save Workflow";
				}

				public void invoke() {

					// create a new file opening dialog
					JFileChooser chooser = new JFileChooser(""){
						@Override
						public void approveSelection(){
							File f = getSelectedFile();
							if(f.exists() && getDialogType() == SAVE_DIALOG){
								int result = JOptionPane.showConfirmDialog(this,
										"The file exists already. Replace?"
										,"Existing file"
										,JOptionPane.YES_NO_CANCEL_OPTION);
												switch(result){
												case JOptionPane.YES_OPTION:
													super.approveSelection();
													return;
												case JOptionPane.NO_OPTION:
													return;
												case JOptionPane.CANCEL_OPTION:
													cancelSelection();
													return;
												default: return;
												}
							}
							super.approveSelection();
						}
					};

					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chooser.setFileFilter(
							new FileNameExtensionFilter(
									"Nested Hyperworkflows (*.nhwf)", "nhwf"));
							chooser.setVisible(true);
							int result = chooser.showSaveDialog(null);

							// once file choice is approved, save to chosen file
							if (result == JFileChooser.APPROVE_OPTION) {
								File chosenFile = chooser.getSelectedFile();
								String filePath = chosenFile.getPath();
								nhwf.save(filePath);
							}
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
							// TODO
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
							// TODO
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

			nhwf.getAddObservable().addObserver(new Observer<Hyperworkflow>() {
				@Override
				public void notify(Hyperworkflow o) {
					renderer.ensurePresence(o);
				}
			});
			nhwf.getModifyObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							// TODO
						}
					});
			nhwf.getRemoveObservable().addObserver(
					new Observer<Hyperworkflow>() {
						@Override
						public void notify(Hyperworkflow o) {
							renderer.ensureAbsence(o);
							}
					});
			nhwf.getConnectObservable().addObserver(new Observer<Connection>() {
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
			component.addKeyListener(
					new DelKeyListener(app, component));
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
			ArrayList<Hyperworkflow> childrenList = new ArrayList<Hyperworkflow>(
					nested.getChildren());

			for (Hyperworkflow child : childrenList) {
				renderer.ensurePresence(child);
				if (child instanceof NestedHyperworkflow) {
					renderRecursively((NestedHyperworkflow) child);
				}
			}

			// render connections of the current NestedHyperworkflow
			ArrayList<Connection> connectionList = new ArrayList<Connection>(
					nested.getConnections());

			for (Connection conn : connectionList) {
				if (conn.getSource().getName().equals("nestedTool"))
					System.out.println();
				renderer.ensureConnected(conn);
			}
		}

		protected void updatePalette() {
			palettegraph.getModel().beginUpdate();
			try {
				// clear seems to reset the zoom, so we call notify at the end
				((mxGraphModel) palettegraph.getModel()).clear();
				ArrayList<Tool> items = new ArrayList<Tool>(app
						.getGlobalRepository().getItems());
				Collections.sort(items, new Comparator<Tool>() {
					@Override
					public int compare(Tool o1, Tool o2) {
						return o1.getCategory().compareTo(o2.getCategory());
					}
				});

				// top left corner of first palette tool, width, height
				double[] d = { 20, 10, 100, 80 };
				for (Tool item : items) {
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

	/**
	 * Handles KeyEvents such as removing cells when focussed and pressing DEL
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
			List<Connection> connList = new ArrayList<Connection>();
			List<Hyperworkflow> hwfList = new ArrayList<Hyperworkflow>();
			
			// check if KeyEvent occurred on graph component,
			// only handle DELETE-key 			
			if (e.getSource().equals(component) 
					&& e.getKeyCode() == KeyEvent.VK_DELETE) {
				
				// get selected cells
				Object[] cells = g.getSelectionCells();
				for (Object o : cells) {
					
					// depending on whether the cell is an edge or vertex
					// add their values to different lists
					if (mod.isEdge(o)) {
						connList.add((Connection)mod.getValue(o));
					}
					if (mod.isVertex(o)) {
						hwfList.add((Hyperworkflow)mod.getValue(o));
					}
				}
				
				final NestedHyperworkflow root 
					= (NestedHyperworkflow) ((mxCell) component
						.getGraph().getDefaultParent()).getValue();
				
				// delete connections first, followed by nodes
				for (Connection c : connList) {
					root.ensureDisconnected(c);
				}
				for (Hyperworkflow hwf : hwfList) {
					root.ensureAbsence(hwf);
				}
			}
			
		}
	}
	
	/**
	 * Handles mouse actions: opens cell-specific views/editors on double-click,
	 * opens context menu on mouse right-click
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

				if (value instanceof Hyperworkflow) {
					ArrayList<Action> as = new ArrayList<Action>();
					((Hyperworkflow) value).appendActions(as);
					if (as.size() > 0) {
						as.get(0).invoke();
					}
				}
			}

			// show context menu when right clicking a node or an edge
			if (e.getButton() == 3) {
				Object cell = component.getCellAt(e.getX(), e.getY());
				final Object value = component.getGraph().getModel().getValue(
						cell);
				final NestedHyperworkflow root 
					= (NestedHyperworkflow) ((mxCell) component
						.getGraph().getDefaultParent()).getValue();

				PopupMenu menu;

				// create connection specific context menu
				if (value instanceof Connection) {
					menu = new PopupMenu(((Connection) value).toString());

					JMenuItem item = new JMenuItem("Remove Connection") {						
						@Override
						public void fireActionPerformed(ActionEvent e) {
							root.ensureDisconnected((Connection) value);
						}
					};
					
					item.setAccelerator(KeyStroke.getKeyStroke(
							KeyEvent.VK_DELETE, 0));
					menu.add(item);
					menu.show(e.getComponent(), e.getX(), e.getY());
					Connection c = (Connection)value;
					System.out.println("parent of: " + c + " is " + c.getSource().getParent());
				}

				// create node specific context menu
				if (value instanceof Hyperworkflow) {
					menu = new PopupMenu(((Hyperworkflow) value).getName());

					
					
					// only create a remove action if it's not a palette tool
					if (((Hyperworkflow) value).getParent() != null) {
						JMenuItem item = new JMenuItem("Remove Vertex") {
							@Override
							public void fireActionPerformed(ActionEvent e) {
								root.ensureAbsence((Hyperworkflow) value);
							}
						};
						
						item.setAccelerator(KeyStroke.getKeyStroke(
								KeyEvent.VK_DELETE, 0));
						menu.add(item);
					}

					menu.show(e.getComponent(), e.getX(), e.getY());
				}

			}
		}
	}

	/**
	 * enables mouse wheel zooming function within graph editor window
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
			if (e.getWheelRotation() > 0)
				component.zoomOut();
			else
				component.zoomIn();
		}
	}

	/**
	 * a context popup menu that displays a components title
	 * @author afischer
	 *
	 */
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
}
