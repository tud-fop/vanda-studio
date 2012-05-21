package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JComponent;

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
import org.vanda.studio.util.Observer;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class Palette {
	protected final Application app;
	//protected final DrecksAdapter palette;
	protected final mxGraphComponent component;

	public Palette(Application app) {
		this.app = app;
		component = new mxGraphComponent(new mxGraph());
		component.setDropTarget(null);
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

	public JComponent getComponent() {
		return component;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void update() {
		DrecksAdapter da = new DrecksAdapter(null);
		da.getGraph().setCellsLocked(true);
		da.getGraph().setDropEnabled(false);
		da.getGraph().getModel().beginUpdate();
		try {
			// clear seems to reset the zoom, so we call notify at the end
			//((mxGraphModel) palette.getGraph().getModel()).clear();
			ArrayList<RepositoryItem> items = new ArrayList<RepositoryItem>(app
					.getToolMetaRepository().getRepository().getItems());
			items.addAll(app
					.getLinkerMetaRepository().getRepository().getItems());
			items.add(new Choice());
			items.add(new Literal("String", ""));
			Collections.sort(items, new Comparator<RepositoryItem>() {
				@Override
				public int compare(RepositoryItem o1, RepositoryItem o2) {
					return o1.getCategory().compareTo(o2.getCategory());
				}
			});

			// top left corner of first palette tool, width, height
			double[] d = { 20, 10, 100, 80 };
			for (RepositoryItem item : items) {
				Job<?> hj = null;
				if (item instanceof Element)
					hj = new AtomicJob((Element) item);
				else if (item instanceof Linker)
					hj = new CompositeJob((Linker) item, new MutableWorkflow(
							((Linker) item).getInnerFragmentType()));
				if (hj != null) {
					hj.setDimensions(d);
					mxICell cell = da.renderChild(null, hj);
					/*hj.selectRenderer(JobRendering.getRendererAssortment())
							.render(hj, graph, null);*/
					d[1] += cell.getGeometry().getHeight() + 10;
				}
			}
		} finally {
			da.getGraph().getModel().endUpdate();
		}
		component.setGraph(da.getGraph());
	}

}
