package org.vanda.presentationmodel;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.PortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.render.jgraph.Cell.CellEvent;
import org.vanda.render.jgraph.Cell.MarkChangedEvent;
import org.vanda.render.jgraph.Cell.SelectionChangedEvent;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

import com.mxgraph.util.mxStyleUtils;

public class ConnectionAdapter {
	private final ConnectionKey connectionKey;
	private final ConnectionCell visualization;
	View view;
	ConnectionCellListener connectionCellListener;
	Observer<ViewEvent<AbstractView>> connectionViewObserver;
	ConnectionViewListener connectionViewListener;
	
	private class ConnectionCellListener implements Cell.CellListener<Cell> {

		private ConnectionViewListener connectionViewListener;

		@Override
		public void propertyChanged(Cell c) {
			// do nothing
		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			// do nothing
			
		}

		@Override
		public void markChanged(Cell c) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeCell(Cell c) {
			if (connectionKey != null) {
				if (connectionKey.target.isInserted())
					view.getWorkflow().removeConnection(connectionKey);
			}		
		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			if (connectionKey != null)
				view.getConnectionView(connectionKey).setSelected(true);
			else 
				view.clearSelection();
		}

		@Override
		public void insertCell(Cell c) {
			assert(connectionKey != null);
			view.workflowListener.connectionAdded(view.getWorkflow(), connectionKey);
			connectionViewListener = new ConnectionViewListener();
			connectionViewObserver = new Observer<ViewEvent<AbstractView>> () {

				@Override
				public void notify(ViewEvent<AbstractView> event) {
					event.doNotify(connectionViewListener);
				}
			};
			
			// Register at ConnectionView
			view.getConnectionView(connectionKey).getObservable().addObserver(connectionViewObserver);
		
			
			PortCell sourcePortCell = (PortCell) visualization.getVisualization().getSource().getValue();
			JobCell sourceJobCell = (JobCell) sourcePortCell.getVisualization().getParent().getValue();
			PresentationModel pm = ((WorkflowCell) visualization.getVisualization().getParent().getValue()).getPresentationModel();
			Job sourceJob = null;
			JobAdapter sourceJobAdapter = null;
			Port sourcePort = null;
			
			for (JobAdapter ja : pm.getJobs()) {
				if (ja.getJobCell() == sourceJobCell) {
					sourceJob = ja.getJob();
					sourceJobAdapter = ja;
					break;
				}
			}
			for (Port op : sourceJob.getOutputPorts()) {
				if (sourceJobAdapter.getOutPortCell(op) == sourcePortCell) {
					sourcePort = op;
				}
			} 

			// Add Connection to Workflow
			// This is done last, because it will trigger the typecheck, 
			// which requires the ConnectionView to be created before
			view.getWorkflow().addConnection(connectionKey, sourceJob.bindings.get(sourcePort));
		}
			
		
	}

	private class ConnectionViewListener implements AbstractView.ViewListener<AbstractView> {
		
		@Override
		public void selectionChanged(AbstractView v) {
			visualization.getObservable().notify(new SelectionChangedEvent<Cell>(visualization, v.isSelected())); 
		}
	
		@Override
		public void markChanged(AbstractView v) {
			if (v.isMarked())
				visualization.getVisualization().setStyle(mxStyleUtils.addStylename(visualization.getVisualization().getStyle(),
						"highlightededge"));
			else 
			{
				String st = mxStyleUtils.removeStylename(visualization.getVisualization().getStyle(),
						"highlightededge");	
				visualization.getVisualization().setStyle(st);	
			}
			visualization.getObservable().notify(new MarkChangedEvent<Cell>(visualization));
		}
	
		@Override
		public void highlightingChanged(AbstractView v) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public ConnectionAdapter(ConnectionKey cc, PresentationModel pm, MutableWorkflow mwf, View view) {
		this.connectionKey = cc;
		
		// find source and target JobCells
		Job sourceJob = mwf.getConnectionSource(cc).target;
		Job targetJob = cc.target;
		Port sourcePort = mwf.getConnectionSource(cc).targetPort;
		Port targetPort = cc.targetPort;
		JobAdapter sJA = null;
		JobAdapter tJA = null;
		for (JobAdapter jA : pm.getJobs()) {
			if (jA.getJob() == sourceJob)
				sJA = jA;
			if (jA.getJob() == targetJob) 
				tJA = jA;				
		}
		assert (sJA != null && tJA != null);
		PortCell source = sJA.getOutPortCell(sourcePort);
		PortCell target = tJA.getInPortCell(targetPort);
		assert (source != null && target != null);

		this.visualization = new ConnectionCell(pm.getVisualization(), source, target);
		// Register at ConnectionView
		view.getConnectionView(connectionKey).getObservable().addObserver(new Observer<ViewEvent<AbstractView>> () {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(connectionViewListener);
			}
			
		});
		
		connectionCellListener = new ConnectionCellListener();
		visualization.getObservable().addObserver(new Observer<CellEvent<Cell>> () {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(connectionCellListener);
			}
			
		});
		this.view = view;
	}
	
	public ConnectionAdapter(ConnectionKey connectionKey, ConnectionCell visualization, View view) {
		this.visualization = visualization;
		this.connectionKey = connectionKey;
		this.view = view;
	}

	public void destroy(Graph graph) {
		if (visualization != null) {
			graph.getGraph().removeCells(new Object[] {visualization.getVisualization()});			
		}
	}


}
