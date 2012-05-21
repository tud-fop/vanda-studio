package org.vanda.studio.modules.workflows;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Choice;
import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.hyper.AtomicJob;
import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;

public class Palette {
//	protected final Application app;
//	//protected final DrecksAdapter palette;
//	protected final mxGraphComponent component;
//
//	public Palette(Application app) {
//		this.app = app;
//		component = new mxGraphComponent(new mxGraph());
//		component.setDropTarget(null);
//		app.getUIModeObservable().addObserver(new Observer<Application>() {
//			@Override
//			public void notify(Application a) {
//				if (a.getUIMode().isLargeContent())
//					component.zoomTo(1.5, false);
//				else
//					component.zoomActual();
//			}
//		});
//	}
//
//	public JComponent getComponent() {
//		return component;
//	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public void update() {
//		DrecksAdapter da = new DrecksAdapter(null);
//		da.getGraph().setCellsLocked(true);
//		da.getGraph().setDropEnabled(false);
//		da.getGraph().getModel().beginUpdate();
//		try {
//			// clear seems to reset the zoom, so we call notify at the end
//			//((mxGraphModel) palette.getGraph().getModel()).clear();
//			ArrayList<RepositoryItem> items = new ArrayList<RepositoryItem>(app
//					.getToolMetaRepository().getRepository().getItems());
//			items.addAll(app
//					.getLinkerMetaRepository().getRepository().getItems());
//			items.add(new Choice());
//			items.add(new Literal("String", ""));
//			Collections.sort(items, new Comparator<RepositoryItem>() {
//				@Override
//				public int compare(RepositoryItem o1, RepositoryItem o2) {
//					return o1.getCategory().compareTo(o2.getCategory());
//				}
//			});
//
//			// top left corner of first palette tool, width, height
//			double[] d = { 20, 10, 100, 80 };
//			for (RepositoryItem item : items) {
//				Job<?> hj = null;
//				if (item instanceof Element)
//					hj = new AtomicJob((Element) item);
//				else if (item instanceof Linker)
//					hj = new CompositeJob((Linker) item, new MutableWorkflow(
//							((Linker) item).getInnerFragmentType()));
//				if (hj != null) {
//					hj.setDimensions(d);
//					mxICell cell = da.renderChild(null, hj);
//					/*hj.selectRenderer(JobRendering.getRendererAssortment())
//							.render(hj, graph, null);*/
//					d[1] += cell.getGeometry().getHeight() + 10;
//				}
//			}
//		} finally {
//			da.getGraph().getModel().endUpdate();
//		}
//		component.setGraph(da.getGraph());
//	}

	protected final Application app;
	protected JXTaskPaneContainer taskPaneContainer;
	protected JScrollPane paletteComponent;

	public Palette(Application app) {
		this.app = app;		
		taskPaneContainer = new JXTaskPaneContainer();
		paletteComponent = new JScrollPane();
	}

	public JComponent getComponent() {
		return paletteComponent;
	}

	public void update() {
		JXTaskPane searchPane = new JXTaskPane("Search");
		JTextField searchField = new JTextField();
		searchField.addKeyListener(new TextFieldListener(searchField, searchPane));
		
		searchPane.add(searchField);
		taskPaneContainer.add(searchPane);
		
		// get all palette items
		ArrayList<RepositoryItem> items = new ArrayList<RepositoryItem>(app
				.getToolMetaRepository().getRepository().getItems());
		items.addAll(app
				.getLinkerMetaRepository().getRepository().getItems());
		items.add(new Choice());
		items.add(new Literal("String", ""));
		
		// partition items into categories
		Map<String, List<RepositoryItem>> catMap = new HashMap<String, List<RepositoryItem>>();
		for (RepositoryItem item : items) {
			if (catMap.containsKey(item.getCategory())) {
				catMap.get(item.getCategory()).add(item);
			} else {
				List<RepositoryItem> itemList = new ArrayList<RepositoryItem>();
				itemList.add(item);
				catMap.put(item.getCategory(), itemList);
			}
		}
		
		// create a new TaskPane for every category and fill it with
		// an mxGraphComponent that contains all tools of this category
		List<String> categories = new ArrayList<String>();
		categories.addAll(catMap.keySet());
		Collections.sort(categories);
		for (String category : categories) {

			// sort items by name
			Collections.sort(catMap.get(category), new Comparator<RepositoryItem>() {
				@Override
				public int compare(RepositoryItem o1, RepositoryItem o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			// render every tool
			DrecksAdapter da = new DrecksAdapter(null);
			da.getGraph().setCellsLocked(true);
			da.getGraph().setDropEnabled(false);
			da.getGraph().getModel().beginUpdate();
			try {
				// top left corner of first palette tool, width, height
				double[] d = { 20, 10, 100, 80 };
				for (RepositoryItem item : catMap.get(category)) {
					Job hj = null;
					if (item instanceof Element)
						hj = new AtomicJob((Element) item);
					else if (item instanceof Linker) {
						MutableWorkflow mwf = new MutableWorkflow("inner workflow");
						hj = new CompositeJob((Linker) item, mwf);
					}
					if (hj != null) {
						hj.setDimensions(d);
						mxICell cell = da.renderChild(null, hj);
						d[1] += cell.getGeometry().getHeight() + 10;
					}
				}
			} finally {
				da.getGraph().getModel().endUpdate();
			}
			
			// create new task pane for the current category and add all necessary components
			JXTaskPane categoryPane = new JXTaskPane(category);
			mxGraphComponent graphComp = new mxGraphComponent(da.getGraph());
			graphComp.getGraph().setCellsLocked(true);
			graphComp.setConnectable(false);
			graphComp.setDropTarget(null);
			categoryPane.add(graphComp);
			categoryPane.setCollapsed(true);
			taskPaneContainer.add(categoryPane);
		}
		
		paletteComponent.getViewport().add(taskPaneContainer);
	}

	private void updateSearch(String text, JXTaskPane searchPane) {

		// remove previous search results from display, keep the textField
		while (searchPane.getContentPane().getComponentCount() > 1) {
			searchPane.remove(searchPane.getContentPane().getComponentCount() - 1);
		}

		//XXX dirty hack: removes ALL components including searchField when input text is empty
		// and adds them once more to the parent task papne container
		// (otherwise the previous search results are not deleted from display, when the search string is empty)
		if (text.length() == 0) {
			JTextField textField = (JTextField) searchPane.getContentPane().getComponent(0);
			searchPane.removeAll();
			searchPane.add(textField);
			textField.requestFocus();
		}

		// get all palette items and sort them by name
		ArrayList<RepositoryItem> items = new ArrayList<RepositoryItem>(app
				.getToolMetaRepository().getRepository().getItems());
		items.addAll(app
				.getLinkerMetaRepository().getRepository().getItems());
		items.add(new Choice());
		items.add(new Literal("String", ""));
		Collections.sort(items, new Comparator<RepositoryItem>() {
			@Override
			public int compare(RepositoryItem o1, RepositoryItem o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		// find all items that contain the entered search string (except the empty string)
		List<RepositoryItem> searchResults = new ArrayList<RepositoryItem>();
		for (RepositoryItem item : items) {
			if (!text.isEmpty() && item.getName().toLowerCase().contains(text.toLowerCase())) searchResults.add(item);
		}
		
		
		// render all search results
		DrecksAdapter da = new DrecksAdapter(null);
		da.getGraph().setCellsLocked(true);
		da.getGraph().setDropEnabled(false);
		da.getGraph().getModel().beginUpdate();
		try {
			// top left corner of first palette tool, width, height
			double[] d = { 20, 10, 100, 80 };
			for (RepositoryItem item : searchResults) {
				Job hj = null;
				if (item instanceof Element)
					hj = new AtomicJob((Element) item);
				else if (item instanceof Linker) {
					MutableWorkflow mwf = new MutableWorkflow("inner workflow");
					hj = new CompositeJob((Linker) item, mwf);
				}
				if (hj != null) {
					hj.setDimensions(d);
					mxICell cell = da.renderChild(null, hj);
					d[1] += cell.getGeometry().getHeight() + 10;
				}
			}
		} finally {
			da.getGraph().getModel().endUpdate();
		}
		
		// create graph with search results and add all components to dsiplay
		mxGraphComponent searchGraph = new mxGraphComponent(da.getGraph());
		searchGraph.getGraph().setCellsLocked(true);
		searchGraph.setConnectable(false);		
		if (!searchResults.isEmpty()) searchPane.add(searchGraph);
		searchPane.setCollapsed(false);
	}
	
	/**
	 * Listens for text inputs in the search TextField and updates 
	 * search results accordingly
	 * @author feryn
	 *
	 */
	protected class TextFieldListener extends KeyAdapter {
		
		JTextField textField;
		JXTaskPane searchPane;
		
		public TextFieldListener(JTextField textField, JXTaskPane searchPane) {
			this.textField = textField;
			this.searchPane = searchPane;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			updateSearch(textField.getText(), searchPane);
		}
	}
}
