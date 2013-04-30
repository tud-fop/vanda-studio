package org.vanda.render.jgraph;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxGraph;

public class ConnectionCell extends Cell {
	private ConnectionKey connectionKey;
	private ConnectionViewListener connectionViewListener;
	private Observer<ViewEvent<AbstractView>> connectionViewObserver;
	private class ConnectionViewListener implements AbstractView.ViewListener<AbstractView> {
	
		@Override
		public void selectionChanged(AbstractView v) {
			getObservable().notify(new SelectionChangedEvent<Cell>(ConnectionCell.this)); 
		}

		@Override
		public void markChanged(AbstractView v) {
			if (v.isMarked())
				visualization.setStyle(mxStyleUtils.addStylename(visualization.getStyle(),
						"highlightededge"));
			else 
			{
				String st = mxStyleUtils.removeStylename(visualization.getStyle(),
						"highlightededge");	
				visualization.setStyle(st);	
			}
			getObservable().notify(new MarkChangedEvent<Cell>(ConnectionCell.this));
		}

		@Override
		public void highlightingChanged(AbstractView v) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public ConnectionCell() {	
		connectionKey = null;
		this.observable = new CellObservable();
	}
	
	public ConnectionCell(ConnectionKey connectionKey, final Graph graph, PortCell source, PortCell target) {
		this.connectionKey = connectionKey;
		this.connectionViewListener = new ConnectionViewListener();
		this.observable = new CellObservable();
		
		// Register at ConnectionView
		graph.getView().getConnectionView(connectionKey).getObservable().addObserver(new Observer<ViewEvent<AbstractView>> () {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(connectionViewListener);
			}
			
		});
		
		// Register at Graph
		getObservable().addObserver(new org.vanda.util.Observer<CellEvent<Cell>> () {

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
		if (connectionKey != null)
			view.getConnectionView(connectionKey).setSelected(true);
		else 
			view.clearSelection();
	}

	@Override
	public void onRemove(View view) {
		if (connectionKey != null) {
			if (connectionKey.target.isInserted())
				view.getWorkflow().removeConnection(connectionKey);
		}
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		// not in the model -> hand-drawn edge
		if (connectionKey == null) {
			mxIGraphModel model = graph.getGraph().getModel();
			Object source = model.getTerminal(cell, true);
			Object target = model.getTerminal(cell, false);

			// ignore "unfinished" edges
			if (source != null && target != null) {
				
				PortCell sval = (PortCell) model.getValue(source);
				PortCell tval = (PortCell) model.getValue(target);
				JobCell sparval = (JobCell) model.getValue(model
						.getParent(source));
				JobCell tparval = (JobCell) model.getValue(model
						.getParent(target));

				connectionKey = new ConnectionKey(tparval.job, tval.port);
				visualization = (mxCell) cell;
				
				// Add ConnectionAdapter to PM 
				PresentationModel pm = (PresentationModel) 
						((WorkflowCell) sparval.getVisualization()
								.getParent().getValue()).getPresentationModel();
								
				pm.addConnectionAdapter(this, connectionKey);
				
				// Create ConnectionView
				graph.getView().workflowListener.connectionAdded(graph.getView().getWorkflow(), connectionKey);
							
				this.connectionViewListener = new ConnectionViewListener();
				this.connectionViewObserver = new Observer<ViewEvent<AbstractView>> () {

					@Override
					public void notify(ViewEvent<AbstractView> event) {
						event.doNotify(connectionViewListener);
					}
				};
				
				// Register at ConnectionView
				graph.getView().getConnectionView(connectionKey).getObservable().addObserver(connectionViewObserver);
									
				
				// register graph for cell changes
				getObservable().addObserver(new Observer<CellEvent<Cell>> () {

					@Override
					public void notify(CellEvent<Cell> event) {
						event.doNotify(graph.getCellChangeListener());	
					}
					
				});
				
				// Add Connection to Workflow
				// This is done last, because it will trigger the typecheck, 
				// which requires the ConnectionView to be created before
				graph.getView().getWorkflow().addConnection(connectionKey, 
							sparval.job.bindings.get(sval.port));
				

			}
		} 		
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
