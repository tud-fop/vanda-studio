package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Choice;
import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.hyper.AtomicJob;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.modules.workflows.jgraph.JobRendering;
import org.vanda.studio.util.Observer;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class Palette {
	protected final Application app;
	protected final mxGraph graph;
	protected final mxGraphComponent component;
	
	public Palette(Application app) {
		this.app = app;
		graph = JobRendering.createGraph();
		graph.setCellsLocked(true);
		component = new mxGraphComponent(graph);
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

	public void update() {
		graph.getModel().beginUpdate();
		try {
			// clear seems to reset the zoom, so we call notify at the end
			((mxGraphModel) graph.getModel()).clear();
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
				@SuppressWarnings("rawtypes")
				Job<?> hj = new AtomicJob(item);
				hj.setDimensions(d);
				hj.selectRenderer(JobRendering.getRendererAssortment()).render(
						hj, graph, null);
				d[1] += 60;
			}
		} finally {
			graph.getModel().endUpdate();
		}
	}

}
