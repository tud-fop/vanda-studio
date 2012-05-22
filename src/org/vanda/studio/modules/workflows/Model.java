package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.List;

import org.vanda.studio.model.hyper.AtomicJob;
import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.JobVisitor;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public final class Model {
	
	public static interface SelectionVisitor {
		void visitWorkflow(List<Token> path, MutableWorkflow wf);
		void visitConnection(List<Token> path, Token address, MutableWorkflow wf, Connection cc);
		void visitJob(List<Token> path, Token address, MutableWorkflow wf, Job j);
	}

	public static class WorkflowSelection {
		public final List<Token> path;

		public WorkflowSelection(List<Token> path) {
			this.path = path;
		}
		
		public void visit(MutableWorkflow root, SelectionVisitor v) {
			v.visitWorkflow(path, root.dereference(path.listIterator()));
		}
	}

	public abstract static class SingleObjectSelection extends
			WorkflowSelection {
		public final Token address;

		public SingleObjectSelection(List<Token> path, Token address) {
			super(path);
			this.address = address;
		}
		
		@Override
		public abstract void visit(MutableWorkflow root, SelectionVisitor v);

		public abstract void remove(MutableWorkflow root);
	}

	public static class ConnectionSelection extends SingleObjectSelection {

		public ConnectionSelection(List<Token> path, Token address) {
			super(path, address);
		}

		@Override
		public void remove(MutableWorkflow root) {
			root.dereference(path.listIterator()).removeConnection(address);
		}

		@Override
		public void visit(MutableWorkflow root, SelectionVisitor v) {
			root = root.dereference(path.listIterator());
			v.visitConnection(path, address, root, root.getConnection(address));
		}
	}

	public static class JobSelection extends SingleObjectSelection {
		public JobSelection(List<Token> path, Token address) {
			super(path, address);
		}

		@Override
		public void remove(MutableWorkflow root) {
			root.dereference(path.listIterator()).removeChild(address);
		}

		@Override
		public void visit(MutableWorkflow root, SelectionVisitor v) {
			root = root.dereference(path.listIterator());
			v.visitJob(path, address, root, root.getChild(address));
		}
	}

	protected final MutableWorkflow hwf;
	protected ImmutableWorkflow frozen;
	protected List<ImmutableWorkflow> unfolded;
	protected WorkflowSelection selection;
	protected List<SingleObjectSelection> markedElements;
	protected List<SingleObjectSelection> previouslyMarkedElements;
	protected final MultiplexObserver<Pair<MutableWorkflow, Job>> addObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow, Job>> modifyObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow, Job>> removeObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow, Connection>> connectObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow, Connection>> disconnectObservable;
	protected final MultiplexObserver<MutableWorkflow> nameChangeObservable;
	protected final Observer<Pair<MutableWorkflow, Job>> addObserver;
	protected final Observer<Pair<MutableWorkflow, Job>> modifyObserver;
	protected final Observer<Pair<MutableWorkflow, Job>> removeObserver;
	protected final Observer<Pair<MutableWorkflow, Connection>> connectObserver;
	protected final Observer<Pair<MutableWorkflow, Connection>> disconnectObserver;
	protected final Observer<MutableWorkflow> nameChangeObserver;
	protected final MultiplexObserver<Model> selectionChangeObservable;
	protected final MultiplexObserver<Model> markedElementsObservable;
	protected final MultiplexObserver<Model> workflowCheckObservable;
	protected final JobVisitor bindVisitor;
	protected final JobVisitor unbindVisitor;

	public Model(MutableWorkflow hwf) {
		this.hwf = hwf;
		this.markedElements = new ArrayList<SingleObjectSelection>();
		this.previouslyMarkedElements = new ArrayList<SingleObjectSelection>();
		addObservable = new MultiplexObserver<Pair<MutableWorkflow, Job>>();
		modifyObservable = new MultiplexObserver<Pair<MutableWorkflow, Job>>();
		removeObservable = new MultiplexObserver<Pair<MutableWorkflow, Job>>();
		connectObservable = new MultiplexObserver<Pair<MutableWorkflow, Connection>>();
		disconnectObservable = new MultiplexObserver<Pair<MutableWorkflow, Connection>>();
		nameChangeObservable = new MultiplexObserver<MutableWorkflow>();
		selectionChangeObservable = new MultiplexObserver<Model>();
		markedElementsObservable = new MultiplexObserver<Model>();
		workflowCheckObservable = new MultiplexObserver<Model>();
		bindVisitor = new JobVisitor() {
			@Override
			public void visitAtomicJob(AtomicJob aj) {
			}

			@Override
			public void visitCompositeJob(CompositeJob cj) {
				bind(cj.getWorkflow());
			}
			
		};
		unbindVisitor = new JobVisitor() {
			@Override
			public void visitAtomicJob(AtomicJob aj) {
			}

			@Override
			public void visitCompositeJob(CompositeJob cj) {
				unbind(cj.getWorkflow());
			}
			
		};
		addObserver = new Observer<Pair<MutableWorkflow, Job>>() {
			@Override
			public void notify(Pair<MutableWorkflow, Job> event) {
				event.snd.visit(bindVisitor);
				addObservable.notify(event);
			}
		};
		modifyObserver = new Observer<Pair<MutableWorkflow, Job>>() {
			@Override
			public void notify(Pair<MutableWorkflow, Job> event) {
				modifyObservable.notify(event);
			}
		};
		removeObserver = new Observer<Pair<MutableWorkflow, Job>>() {
			@Override
			public void notify(Pair<MutableWorkflow, Job> event) {
				event.snd.visit(unbindVisitor);
				if (selection != null
						&& Model.this.hwf.dereference(selection.path
								.listIterator()) == event.fst)
					setSelection(null);
				removeObservable.notify(event);
			}
		};
		connectObserver = new Observer<Pair<MutableWorkflow, Connection>>() {
			@Override
			public void notify(Pair<MutableWorkflow, Connection> event) {
				connectObservable.notify(event);
			}
		};
		disconnectObserver = new Observer<Pair<MutableWorkflow, Connection>>() {
			@Override
			public void notify(Pair<MutableWorkflow, Connection> event) {
				if (selection != null
						&& Model.this.hwf.dereference(selection.path
								.listIterator()) == event.fst)
					setSelection(null);
				disconnectObservable.notify(event);
			}
		};
		nameChangeObserver = new Observer<MutableWorkflow>() {
			@Override
			public void notify(MutableWorkflow event) {
				nameChangeObservable.notify(event);
			}
		};
		bind(hwf);
	}

	private void bind(MutableWorkflow wf) {
		wf.getAddObservable().addObserver(addObserver);
		wf.getModifyObservable().addObserver(modifyObserver);
		wf.getRemoveObservable().addObserver(removeObserver);
		wf.getConnectObservable().addObserver(connectObserver);
		wf.getDisconnectObservable().addObserver(disconnectObserver);
		wf.getNameChangeObservable().addObserver(nameChangeObserver);
		wf.visitAll(bindVisitor);
	}

	public void checkWorkflow() throws Exception {
		frozen = hwf.freeze();
		frozen.typeCheck();
		unfolded = frozen.unfold();
		workflowCheckObservable.notify(this);
	}

	public ImmutableWorkflow getFrozen() {
		return frozen;
	}

	public List<SingleObjectSelection> getMarkedElements() {
		return markedElements;
	}
	
	public List<SingleObjectSelection> getPreviouslyMarkedElements() {
		return previouslyMarkedElements;
	}
	
	public MutableWorkflow getRoot() {
		return hwf;
	}

	public WorkflowSelection getSelection() {
		return selection;
	}

	public Observable<Pair<MutableWorkflow, Job>> getAddObservable() {
		return addObservable;
	}

	public Observable<Pair<MutableWorkflow, Connection>> getConnectObservable() {
		return connectObservable;
	}

	public Observable<Pair<MutableWorkflow, Connection>> getDisconnectObservable() {
		return disconnectObservable;
	}

	public Observable<Pair<MutableWorkflow, Job>> getModifyObservable() {
		return modifyObservable;
	}

	public Observable<MutableWorkflow> getNameChangeObservable() {
		return nameChangeObservable;
	}

	public Observable<Pair<MutableWorkflow, Job>> getRemoveObservable() {
		return removeObservable;
	}

	public Observable<Model> getSelectionChangeObservable() {
		return selectionChangeObservable;
	}

	public Observable<Model> getMarkedElementsObservable() {
		return markedElementsObservable;
	}

	public Observable<Model> getWorkflowCheckObservable() {
		return workflowCheckObservable;
	}

	public List<ImmutableWorkflow> getUnfolded() {
		return unfolded;
	}

	public void setSelection(WorkflowSelection selection) {
		this.selection = selection;
		selectionChangeObservable.notify(this);
	}

	public void setMarkedElements(List<SingleObjectSelection> elements) {
		previouslyMarkedElements = markedElements;
		markedElements = elements;
		markedElementsObservable.notify(this);
	}

	public void unbind(MutableWorkflow wf) {
		wf.getAddObservable().removeObserver(addObserver);
		wf.getModifyObservable().removeObserver(modifyObserver);
		wf.getRemoveObservable().removeObserver(removeObserver);
		wf.getConnectObservable().removeObserver(connectObserver);
		wf.getDisconnectObservable().removeObserver(disconnectObserver);
		wf.getNameChangeObservable().removeObserver(nameChangeObserver);
		wf.visitAll(unbindVisitor);
	}

}
