package org.vanda.studio.modules.workflows.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vanda.studio.app.Application;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.TypeCheckingException;
import org.vanda.workflows.hyper.TopSorter.TopSortException;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

/**
 * Renews the SyntaxAnalysis on WorkflowChanges. TypeChanges are forwarded via ChildModifiedEvents of the Workflows.
 * Sets and resets syntax-error and top-sort-error-highlighting in the View.
 * @author kgebhardt
 *
 */
public class SyntaxUpdater implements Observer<WorkflowEvent<MutableWorkflow>>, WorkflowListener<MutableWorkflow> {

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
		checkWorkflow();
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
	}

	@Override
	public void updated(MutableWorkflow mwf) {
		checkWorkflow();
	}

	private final Application app;
	private final SyntaxAnalysis synA;
	private final View view;
	private Set<ConnectionKey> markedConnections;

	public SyntaxUpdater(Application app, SyntaxAnalysis synA, View view) {
		this.app = app;
		this.synA = synA;
		this.view = view;
	}

	private void checkWorkflow() {
		try {
			synA.checkWorkflow();
			// remove ErrorHighlighting
			if (markedConnections != null) {
				for (ConnectionKey cc : markedConnections)
					if (view.getConnectionView(cc) != null)
						view.getConnectionView(cc).setMarked(false);
				markedConnections = null;
			}
		} catch (TypeCheckingException e) {
			List<Pair<String, Set<ConnectionKey>>> errors = e.getErrors();
			HashSet<ConnectionKey> allErrors = new HashSet<ConnectionKey>();
			for (Pair<String, Set<ConnectionKey>> error : errors) {
				// TODO use new color in each iteration
				Set<ConnectionKey> eqs = error.snd;
				for (ConnectionKey eq : eqs) {
					view.getConnectionView(eq).setMarked(true);
					allErrors.add(eq);
				}
			}
			if (markedConnections != null) {
				markedConnections.removeAll(allErrors);
				for (ConnectionKey cc : markedConnections)
					view.getConnectionView(cc).setMarked(false);
			}
			markedConnections = allErrors;
		} catch (TopSortException e) {
			for (ConnectionKey cc : e.getCyclicConnections()) {
				if (markedConnections == null)
					markedConnections = new HashSet<ConnectionKey>();
				if (!markedConnections.contains(cc)) {
					markedConnections.add(cc);
					view.getConnectionView(cc).setMarked(true);
				}
			}

		} catch (Exception e) {
			// TOP-SORT error
			System.out.println("TOP-SORT error");
			app.sendMessage(new ExceptionMessage(e));
		}
	}

	@Override
	public void notify(WorkflowEvent<MutableWorkflow> event) {
		event.doNotify(this);
	}
}