package org.vanda.execution.model;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.DatatypeConverter;

import org.vanda.execution.model.Runables.DefaultRunEventListener;
import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.execution.model.Runables.RunEventListener;
import org.vanda.execution.model.Runables.RunState;
import org.vanda.execution.model.Runables.RunStateVisitor;
import org.vanda.fragment.model.Fragments;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Jobs;
import org.vanda.workflows.hyper.Jobs.JobEvent;
import org.vanda.workflows.hyper.Location;

/**
 * workflow with a specific assignment
 * 
 * @author kgebhardt
 * 
 */
public class AssignmentWorkflowInstance implements Runable {
	private class JobListener implements Jobs.JobListener<Job> {

		@Override
		public void propertyChanged(Job j) {
			// check if the state of the job has changed
			((ExecutableJob) j).getState().visit(new RunStateVisitor() {

				@Override
				public void cancelled() {
					// Cancel other Jobs
					for (final ExecutableJob ej : jobs) {
						ej.getState().visit(new RunStateVisitor() {
							@Override
							public void cancelled() {
								// do nothing
							}

							@Override
							public void done() {
								// do nothing
							}

							@Override
							public void ready() {
								ej.doCancel();
							}

							@Override
							public void running() {
								ej.doCancel();
							}
						});
					}
				}

				@Override
				public void done() {
					// Check if all jobs are done
					done = true;
					for (ExecutableJob ej : jobs) {
						if (!ej.getState().isDone())
							done = false;
					}
					if (done)
						doFinish();
				}

				@Override
				public void ready() {
					// do nothing
				}

				@Override
				public void running() {
					// do nothing
				}

			});
		}
	}

	public static final String UNDEFINED = "UNDEFINED";

	private static String md5sum(String in) {
		try {
			byte[] bytesOfMessage = in.getBytes(Charset.forName("UTF-8"));
			MessageDigest md = MessageDigest.getInstance("MD5");
			return DatatypeConverter.printHexBinary(md.digest(bytesOfMessage));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("MD5 not supported by platform.");
			return null;
		}
	}

	private boolean connected;
	boolean done;
	private String id;
	private JobListener jobListener;
	private List<ExecutableJob> jobs;
	private RunEventListener listener;
	private RunState state;

	final Map<Location, ValuedLocation> translation;

	private WeakHashMap<Location, ConnectionKey> varSources;

	public AssignmentWorkflowInstance(final Map<Integer, String> dbRow,
			Job[] jobs, String id) {
		this.jobs = new ArrayList<ExecutableJob>();
		this.jobListener = new JobListener();
		this.id = id;
		translation = new HashMap<Location, ValuedLocation>();
		varSources = new WeakHashMap<Location, ConnectionKey>();
		init(dbRow, jobs);
	}

	private void appendValue(StringBuilder sb, ExecutableJob j, Port p) {
		sb.append(p.getIdentifier());
		sb.append('=');
		sb.append(j.getValuedBinding(p).getValue().replace('/', '#'));
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

	public List<ConnectionKey> getConnections() {
		// only for putting existing hypergraphs into the GUI
		LinkedList<ConnectionKey> conn = new LinkedList<ConnectionKey>();
		for (Job ji : jobs) {
			for (Port ip : ji.getInputPorts())
				if (ji.bindings.containsKey(ip))
					conn.add(new ConnectionKey(ji, ip));
		}
		return conn;
	}

	@Override
	public String getID() {
		return id;
	}

	public List<ExecutableJob> getSortedJobs() {
		return jobs;
	}

	@Override
	public RunState getState() {
		return state;
	}

	// FIXME legacy for Workflow Editor Inspectors
	public String getValue(Location variable) {
		return translation.get(variable).getValue();
	}

	public ConnectionKey getVariableSource(Location variable) {
		return varSources.get(variable);
	}

	public void init(final Map<Integer, String> dbRow, final Job[] sorted) {
		boolean cc = true;
		for (final Job ji : sorted) {
			if (ji.isConnected()) {
				ji.visit(new ElementVisitor() {
					@Override
					public void visitLiteral(Literal lit) {
						// String id; // TODO generate IDs!!
						// id = lit.getKey ??
						ExecutableJob ej = new ExecutableJob(ji, dbRow.get(lit
								.getKey()));
						ej.insert();
						Port op = ji.getOutputPorts().get(0);
						ValuedLocation valVar = new ValuedLocation(dbRow
								.get(lit.getKey()));
						ej.bindings.put(op, valVar);
						varSources.put(valVar, new ConnectionKey(ej, op));
						translation.put(ji.bindings.get(op), valVar);
						jobs.add(ej);
						ej.getObservable().addObserver(
								new Observer<JobEvent<Job>>() {

									@Override
									public void notify(JobEvent<Job> event) {
										event.doNotify(jobListener);
									}
								});

					}

					@Override
					public void visitTool(Tool t) {
						// String id; // -> tool prefix!

						ExecutableJob ej = new ExecutableJob(ji);
						ej.insert();

						// InPorts
						StringBuilder sb = new StringBuilder();
						sb.append('(');
						List<Port> ports = t.getInputPorts();
						if (ports.size() > 0) {
							ej.bindings.put(ports.get(0), translation
									.get(ji.bindings.get(ports.get(0))));
							appendValue(sb, ej, ports.get(0));
						}
						for (int i = 1; i < ports.size(); i++) {
							sb.append(',');
							ej.bindings.put(ports.get(i), translation
									.get(ji.bindings.get(ports.get(i))));
							appendValue(sb, ej, ports.get(i));
						}
						sb.append(')');
						String toolPrefix = Fragments.normalize(t.getId())
								+ "." + md5sum(sb.toString());
						ej.setToolPrefix(toolPrefix);

						// OutPorts
						for (Port op : t.getOutputPorts()) {
							String value = toolPrefix + "."
									+ op.getIdentifier();
							ej.bindings.put(op, new ValuedLocation(value));
							varSources.put(ej.bindings.get(op),
									new ConnectionKey(ej, op));
							translation.put(ji.bindings.get(op),
									(ValuedLocation) ej.bindings.get(op));
						}

						jobs.add(ej);

						if (ej.getObservable() != null)
							ej.getObservable().addObserver(
									new Observer<JobEvent<Job>>() {

										@Override
										public void notify(JobEvent<Job> event) {
											event.doNotify(jobListener);
										}
									});

					}

				});
			} else {
				ExecutableJob ej = new ExecutableJob(ji);
				ej.insert();
				for (Port ip : ji.getInputPorts()) {
					ej.bindings.put(ip, translation.get(ji.bindings.get(ip)));
				}
				for (Port op : ji.getOutputPorts()) {
					Location variable = ji.bindings.get(op);
					StringBuilder sb = new StringBuilder();
					sb.append('[');
					sb.append(UNDEFINED);
					sb.append("] ");
					sb.append(variable.toString());

					// FIXME -> id = not necessary ?! (will not compile)
					ej.bindings.put(op, new ValuedLocation(sb.toString()));
					translation.put(ji.bindings.get(op),
							(ValuedLocation) ej.bindings.get(op));

					jobs.add(ej);
				}
				cc = false;
			}
		}
		connected = cc;
		this.state = new StateReady();
		this.listener = new DefaultRunEventListener(this);
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

		for (ExecutableJob ej : getSortedJobs()) {
			ej.registerRunEventListener(observable);
		}
	}

	public void shift(double x, double y) {
		for (ExecutableJob j : jobs) {
			j.shift(x, y);
		}
	}
}
