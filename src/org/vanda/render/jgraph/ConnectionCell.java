package org.vanda.render.jgraph;

import java.util.Observable;
import java.util.Observer;

import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class ConnectionCell extends Cell {
	private final ConnectionKey connectionKey;
	private class ConnectionViewObserver implements Observer {
		@Override
		public void update(Observable arg0, Object arg1) {
			notify(); // CellSelectionListener in graph
		}
	}
	
	public ConnectionCell(ConnectionKey connectionKey, final Graph graph, PortCell source, PortCell target) {
		this.connectionKey = connectionKey;
		graph.getView().getConnectionView(connectionKey).addObserver(new ConnectionViewObserver());
		getObservable().addObserver(new org.vanda.util.Observer<CellEvent<Cell>> () {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(graph.getCellChangeListener());	
			}
			
		});
		//addObserver(graph.getCellSelectionListener());
		mxICell sourceVis = source.getVisualization();
		mxICell targetVis = target.getVisualization();

		if (sourceVis != null && targetVis != null) {
			graph.getGraph().getModel().beginUpdate();
			try {
				visualization = (mxCell) graph.getGraph().createEdge(graph.getGraph().getDefaultParent()
					, null, this, sourceVis, targetVis
					, null);
			
				graph.getGraph().addEdge(visualization, graph.getGraph().getDefaultParent(),
						sourceVis, targetVis, null);
			} finally {
				graph.getGraph().getModel().endUpdate();
			}
		} else
			assert (false);
		
	}
	
	@Override
	public String getType() {
		return "ConnectionCell";
	}

	@Override
	public void setSelection(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemove(mxICell previous) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInsert(mxGraph graph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean inModel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onResize(mxGraph graph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractView getView(View view) {
		return view.getConnectionView(connectionKey);
	}

}
