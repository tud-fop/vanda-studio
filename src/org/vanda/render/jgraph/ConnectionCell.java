package org.vanda.render.jgraph;

import org.vanda.util.Observer;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxGraph;

public class ConnectionCell extends Cell {
	boolean handdrawn;
	
	public ConnectionCell() {
		handdrawn = true;
		this.observable = new CellObservable();
	}

	public ConnectionCell(final Graph graph, PortCell source, PortCell target) {
		handdrawn = false;
		this.observable = new CellObservable();

		// Register at Graph
		getObservable().addObserver(
				new org.vanda.util.Observer<CellEvent<Cell>>() {

					@Override
					public void notify(CellEvent<Cell> event) {
						event.doNotify(graph.getCellChangeListener());
					}

				});

		// create mxCell and add it to Graph
		mxICell sourceVis = source.getVisualization();
		mxICell targetVis = target.getVisualization();

		if (sourceVis != null && targetVis != null) {
			graph.getGraph().getModel().beginUpdate();
			try {
				visualization = (mxCell) graph.getGraph().createEdge(
						graph.getGraph().getDefaultParent(), null, this,
						sourceVis, targetVis, null);

				graph.getGraph().addEdge(visualization,
						graph.getGraph().getDefaultParent(), sourceVis,
						targetVis, null);
			} finally {
				graph.getGraph().getModel().endUpdate();
			}
		} else
			assert (false);

	}

	public PortCell getSourceCell() {
		return (PortCell) visualization.getSource().getValue();
	}

	@Override
	public String getType() {
		return "ConnectionCell";
	}

	@Override
	public void highlight(boolean highlight) {
		if (highlight) {
			getVisualization().setStyle(
					mxStyleUtils.addStylename(getVisualization().getStyle(),
							"highlightededge"));
		} else {
			getVisualization().setStyle(
					mxStyleUtils.removeStylename(visualization.getStyle(),
							"highlightededge"));
		}
		getObservable().notify(new MarkChangedEvent<Cell>(this));

	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		// no observers (-> no ConnectionAdapter) -> hand-drawn edge
		if (handdrawn) {
			mxIGraphModel model = graph.getGraph().getModel();
			Object source = model.getTerminal(cell, true);
			Object target = model.getTerminal(cell, false);

			// ignore "unfinished" edges
			if (source != null && target != null) {
				visualization = (mxCell) cell;

				PortCell tval = (PortCell) model.getValue(target);
				JobCell tparval = (JobCell) model.getValue(model
						.getParent(target));

				// register graph for cell changes
				getObservable().addObserver(new Observer<CellEvent<Cell>>() {

					@Override
					public void notify(CellEvent<Cell> event) {
						event.doNotify(graph.getCellChangeListener());
					}

				});

				// Create ConnectionAdapter
				((WorkflowCell) ((mxICell) graph.getGraph().getDefaultParent())
						.getValue()).getDataInterface().createConnection(this, tparval, tval);

			}
		}
	}

	@Override
	public void onRemove() {
		getObservable().notify(new RemoveCellEvent<Cell>(this));
	}

	@Override
	public void onResize(mxGraph graph) {
		// do nothing
	}

	@Override
	public void setSelection(boolean selected) {
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

}
