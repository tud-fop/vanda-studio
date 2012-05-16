package org.vanda.studio.modules.workflows;

import java.util.List;

import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public final class Model<F> {

	public static class WorkflowSelection {
		public final List<Token> path;

		public WorkflowSelection(List<Token> path) {
			this.path = path;
		}
	}

	public abstract static class SingleObjectSelection extends
			WorkflowSelection {
		public final Token address;

		public SingleObjectSelection(List<Token> path, Token address) {
			super(path);
			this.address = address;
		}

		public abstract void remove(MutableWorkflow<?> root);
	}

	public static class ConnectionSelection extends SingleObjectSelection {

		public ConnectionSelection(List<Token> path, Token address) {
			super(path, address);
		}

		@Override
		public void remove(MutableWorkflow<?> root) {
			root.dereference(path.listIterator()).removeConnection(address);
		}
	}

	public static class JobSelection extends SingleObjectSelection {
		public JobSelection(List<Token> path, Token address) {
			super(path, address);
		}

		@Override
		public void remove(MutableWorkflow<?> root) {
			root.dereference(path.listIterator()).removeChild(address);
		}
	}

	protected final MutableWorkflow<F> hwf;
	protected ImmutableWorkflow<F> frozen;
	protected List<ImmutableWorkflow<F>> unfolded;
	protected WorkflowSelection selection;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Token>> addObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Token>> modifyObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Token>> removeObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Token>> connectObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Token>> disconnectObservable;
	protected final Observer<Pair<MutableWorkflow<?>, Token>> addObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Token>> modifyObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Token>> removeObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Token>> connectObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Token>> disconnectObserver;
	protected final MultiplexObserver<Model<F>> selectionChangeObservable;
	protected final MultiplexObserver<Model<F>> workflowCheckObservable;

	public Model(MutableWorkflow<F> hwf) {
		this.hwf = hwf;
		addObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Token>>();
		modifyObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Token>>();
		removeObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Token>>();
		connectObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Token>>();
		disconnectObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Token>>();
		selectionChangeObservable = new MultiplexObserver<Model<F>>();
		workflowCheckObservable = new MultiplexObserver<Model<F>>();
		addObserver = new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				Job<?> job = event.fst.getChild(event.snd);
				if (job instanceof CompositeJob)
					bind(((CompositeJob<?, ?>) job).getWorkflow());
				addObservable.notify(event);
			}
		};
		modifyObserver = new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				modifyObservable.notify(event);
			}
		};
		removeObserver = new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				Job<?> job = event.fst.getChild(event.snd);
				if (job instanceof CompositeJob)
					unbind(((CompositeJob<?, ?>) job).getWorkflow());
				removeObservable.notify(event);
			}
		};
		connectObserver = new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				connectObservable.notify(event);
			}
		};
		disconnectObserver = new Observer<Pair<MutableWorkflow<?>, Token>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Token> event) {
				disconnectObservable.notify(event);
			}
		};
		bind(hwf);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void bind(MutableWorkflow<?> wf) {
		wf.getAddObservable().addObserver((Observer) addObserver);
		wf.getModifyObservable().addObserver((Observer) modifyObserver);
		wf.getRemoveObservable().addObserver((Observer) removeObserver);
		wf.getConnectObservable().addObserver((Observer) connectObserver);
		wf.getDisconnectObservable().addObserver((Observer) disconnectObserver);
		for (Token address : wf.getChildren()) {
			Job<?> job = wf.getChild(address);
			if (job instanceof CompositeJob)
				bind(((CompositeJob<?, ?>) job).getWorkflow());
		}
	}

	public void checkWorkflow() throws Exception {
		frozen = hwf.freeze();
		// if (frozen.isSane()) {
		frozen.typeCheck();
		unfolded = frozen.unfold();
		// }
		workflowCheckObservable.notify(this);
	}

	public ImmutableWorkflow<F> getFrozen() {
		return frozen;
	}

	public MutableWorkflow<F> getRoot() {
		return hwf;
	}

	public WorkflowSelection getSelection() {
		return selection;
	}

	public Observable<Pair<MutableWorkflow<?>, Token>> getAddObservable() {
		return addObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Token>> getConnectObservable() {
		return connectObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Token>> getDisconnectObservable() {
		return disconnectObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Token>> getModifyObservable() {
		return modifyObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Token>> getRemoveObservable() {
		return removeObservable;
	}

	public Observable<Model<F>> getSelectionChangeObservable() {
		return selectionChangeObservable;
	}

	public Observable<Model<F>> getWorkflowCheckObservable() {
		return workflowCheckObservable;
	}

	public List<ImmutableWorkflow<F>> getUnfolded() {
		return unfolded;
	}

	public void setSelection(WorkflowSelection selection) {
		this.selection = selection;
		selectionChangeObservable.notify(this);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void unbind(MutableWorkflow<?> wf) {
		wf.getAddObservable().removeObserver((Observer) addObserver);
		wf.getModifyObservable().removeObserver((Observer) modifyObserver);
		wf.getRemoveObservable().removeObserver((Observer) removeObserver);
		wf.getConnectObservable().removeObserver((Observer) connectObserver);
		wf.getDisconnectObservable().removeObserver(
				(Observer) disconnectObserver);
		for (Token address : wf.getChildren()) {
			Job<?> job = wf.getChild(address);
			if (job instanceof CompositeJob)
				unbind(((CompositeJob<?, ?>) job).getWorkflow());
		}

	}

}
