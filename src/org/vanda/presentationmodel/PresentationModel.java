package org.vanda.presentationmodel;

import java.util.List;
import java.util.Map;

import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.LayoutManagerFactoryInterface;
import org.vanda.render.jgraph.LayoutManagerInterface;
import org.vanda.render.jgraph.NaiveLayoutManagerFactory;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.util.Observer;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;

public class PresentationModel {
	protected final Graph graph;
	protected final WorkflowCell workflowCell;
	private final WorkflowListener workflowListener;
	private int update = 0;
	List<JobAdapter> jobs;
	Map<ConnectionKey, ConnectionAdapter> connections;
	View view;
	LayoutManagerFactoryInterface layoutManager = new NaiveLayoutManagerFactory();
	/* map that holds Layouts for all Job-Types, where String is <code>Job.getName()</code> */
	//static final Map<String, LayoutManager> layouts = null; //TODO define some Layouts
	
	void beginUpdate() {
		update++;
	}
	
	void endUpdate() {
		update--;
	}
	
	public Graph getVisualization () {
		return graph;
	}
	
	public void addJobAdapter(Job job) {
		if (!view.getWorkflow().getChildren().contains(job)) {
			view.getWorkflow().addChild(job);
		}
		jobs.add(new JobAdapter(job, selectLayout(job), graph));
		graph.refresh();
	}

	private LayoutManagerInterface selectLayout(Job job) {
		//return layouts.get(job.getName());
		return layoutManager.getLayoutManager(job);
	}
	
	/**
	 * Listens for changes in Model, i.e. Job, Connections, ... 
	 * and keeps Presentation Model up to date 
	 * and enforces mxGraph updates
	 * 
	 * @author kgebhardt
	 *
	 */
	protected class WorkflowListener implements
	Workflows.WorkflowListener<MutableWorkflow> {

		@Override
		public void childAdded(MutableWorkflow mwf, Job j) {
			if (update == 0) {
				addJobAdapter(j);
			}
		}

		@Override
		public void childModified(MutableWorkflow mwf, Job j) {
//			modifyJobAdapter(j);
//			graph.refresh();
		}

		@Override
		public void childRemoved(MutableWorkflow mwf, Job j) {
			if (update == 0) {
				removeJobAdatper(mwf, j);
			}
		}

		@Override
		public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
			if (update == 0) {
				addConnectionAdapter(mwf, cc);
			}
		}

		@Override
		public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
			if (update == 0) {
				removeConnectionAdapter(mwf, cc);
			}
		}

		@Override
		public void propertyChanged(MutableWorkflow mwf) {
			// TODO improve
//			if (mwf != DrecksAdapter.this.model.getRoot())
//				graph.refresh();
		}

		@Override
		public void updated(MutableWorkflow mwf) {
		}

	}

	public View getView() {
		return view;
	}

	public void removeConnectionAdapter(MutableWorkflow mwf, ConnectionKey cc) {
		connections.remove(cc);
	}

	public void addConnectionAdapter(MutableWorkflow mwf, ConnectionKey cc) {
		if (!connections.containsKey(cc))
			connections.put(cc, new ConnectionAdapter(cc, this, mwf));
	}

	public void removeJobAdatper(MutableWorkflow mwf, Job j) {
		jobs.remove(j);
	}

	public List<JobAdapter> getJobs() {
		return jobs;
	}
	
	public PresentationModel(View view) {
		this.view = view;
		this.workflowCell = new WorkflowCell(this);
		graph = new Graph(view, workflowCell);
		workflowListener = new WorkflowListener();
		view.getWorkflow()
			.getObservable()
			.addObserver(
				new Observer<Workflows.WorkflowEvent<MutableWorkflow>>() {

					@Override
					public void notify(WorkflowEvent<MutableWorkflow> event) {
						event.doNotify(workflowListener);
						
					}
					
				}); 
		setupPresentationModel();		
	}

	private void setupPresentationModel() {
		for (Job j : view.getWorkflow().getChildren()) {
			jobs.add(new JobAdapter(j, selectLayout(j), graph));
		}
		for (ConnectionKey ck : view.getWorkflow().getConnections()) {
			connections.put(ck, new ConnectionAdapter(ck, this, view.getWorkflow()));
		}
	}

	public void addConnectionAdapter(ConnectionCell connectionCell,
			ConnectionKey connectionKey) {
		connections.put(connectionKey, new ConnectionAdapter(connectionKey, connectionCell));
	}

}
