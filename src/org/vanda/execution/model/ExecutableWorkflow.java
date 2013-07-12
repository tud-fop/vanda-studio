package org.vanda.execution.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.execution.model.Runables.DefaultRunEventListener;
import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.execution.model.Runables.RunEventListener;
import org.vanda.execution.model.Runables.RunState;
import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

/**
 * a workflow with (multiple) assignment(s)
 * 
 * @author kgebhardt
 * 
 */
public final class ExecutableWorkflow extends MutableWorkflow implements
		Runable {
	private static double[] workflowDimension(AssignmentWorkflowInstance awi) {
		double xmin = 0, xmax = 0, ymin = 0, ymax = 0;
		if (awi.getSortedJobs().size() > 0) {
			Job ji = awi.getSortedJobs().get(0);
			xmin = ji.getX();
			xmax = ji.getX() + ji.getWidth();
			ymin = ji.getY();
			ymax = ji.getY() + ji.getHeight();
		}
		for (int i = 1; i < awi.getSortedJobs().size(); ++i) {
			Job ji = awi.getSortedJobs().get(i);
			if (xmin > ji.getX())
				xmin = ji.getX();
			if (xmax < ji.getX() + ji.getWidth())
				xmax = ji.getX() + ji.getWidth();
			if (ymin > ji.getY())
				ymin = ji.getY();
			if (ymax < ji.getY() + ji.getHeight())
				ymax = ji.getY() + ji.getHeight();
		}
		return new double[] { xmax - xmin, ymax - ymin };

	}

	private boolean connected;
	private Database db;
	private Type fragmentType;
	private String id;
	private Job[] jobs;
	private RunEventListener listener;
	private RunState state;
	private Map<Integer, AssignmentWorkflowInstance> workflows;

	public ExecutableWorkflow(MutableWorkflow iwf, Database db, Job[] sorted,
			Type fragmentType) {
		super(iwf.getName());
		this.db = db;
		this.jobs = sorted;
		this.fragmentType = fragmentType;
		this.workflows = new HashMap<Integer, AssignmentWorkflowInstance>();
	}

	@Override
	public void addChild(Job job) {
		// do nothing
	}

	@Override
	public void addConnection(ConnectionKey cc, Location variable) {
		// do nothing
	}

	@Override
	public void doCancel() {
		state = new StateCancelled();
	}

	@Override
	public void doFinish() {
		state = new StateDone();
	}

	@Override
	public void doRun() {
		state = new StateRunning();
	}

	@Override
	public Collection<Job> getChildren() {
		// ArrayList<Job> children = new ArrayList<Job>();
		// children.addAll(Arrays.asList(getSortedJobs()));
		// return children;
		List<Job> sorted = new ArrayList<Job>();
		for (AssignmentWorkflowInstance ewf : workflows.values()) {
			sorted.addAll(ewf.getSortedJobs());
		}
		return sorted;
	}

	@Override
	public List<ConnectionKey> getConnections() {
		List<ConnectionKey> connections = new ArrayList<ConnectionKey>();
		for (AssignmentWorkflowInstance awi : workflows.values()) {
			// FIXME : skip not selected assignments
			connections.addAll(awi.getConnections());
		}
		return connections;
	}

	@Override
	public ConnectionKey getConnectionSource(ConnectionKey cc) {
		return getVariableSource(getConnectionValue(cc));
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	@Override
	public String getID() {
		return id;
	}

	public ExecutableJob[] getSortedJobs() {
		List<ExecutableJob> sorted = new ArrayList<ExecutableJob>();
		for (AssignmentWorkflowInstance ewf : workflows.values()) {
			sorted.addAll(ewf.getSortedJobs());
		}
		return sorted.toArray(new ExecutableJob[sorted.size()]);
	}

	@Override
	public RunState getState() {
		return state;
	}

	// FIXME Legacy for Workflow Editor Inspectors
	// TODO maybe show the current assignment
	public String getValue(Location variable) {
		if (db.getSize() > 0)
			return workflows.get(0).getValue(variable);
		else
			return null;
	}

	@Override
	public ConnectionKey getVariableSource(Location variable) {
		ConnectionKey ck = null;
		for (AssignmentWorkflowInstance awi : workflows.values()) {
			ck = awi.getVariableSource(variable);
			if (ck != null)
				break;
		}
		return ck;
	}

	public void init() {
		Boolean cc = true;
		if (jobs == null)
			return;
		db.home();

		for (int i = 0; i < db.getSize(); ++i) {
			// TODO skip unselected Assignments
			String id = "AssingmentWorkflow" + ((Integer) i).toString();
			workflows.put((Integer) i,
					new AssignmentWorkflowInstance(db.getRow(i), jobs, id));
			cc = cc && workflows.get(i).isConnected();
		}

		connected = cc;
		state = new StateReady();
		listener = new DefaultRunEventListener(this);
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public void registerRunEventListener(MultiplexObserver<RunEvent> observable) {
		observable.addObserver(new Observer<RunEvent>() {

			@Override
			public void notify(RunEvent event) {
				event.doNotify(listener);
			}
		});
		for (AssignmentWorkflowInstance awi : workflows.values()) {
			awi.registerRunEventListener(observable);
		}
	}

	@Override
	public void removeChild(Job ji) {
		// do nothing
	}

	@Override
	public void removeConnection(ConnectionKey cc) {
		// do nothing
	}

	public void shift() {
		// move Workflows
		if (workflows.values().size() > 0) {
			double[] dims = { 0, 0 };
			for (AssignmentWorkflowInstance awi : workflows.values()) {
				dims = workflowDimension(awi);
				break;
			}

			int i = 0;
			for (AssignmentWorkflowInstance awi : workflows.values()) {
				if (i > 0)
					awi.shift(dims[0] * i, 0);
				++i;
			}
		}
	}

}
