package org.vanda.studio.modules.workflows;

import java.util.List;

import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
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
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Job<?>>> addObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Job<?>>> modifyObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Job<?>>> removeObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Connection>> connectObservable;
	protected final MultiplexObserver<Pair<MutableWorkflow<?>, Connection>> disconnectObservable;
	protected final Observer<Pair<MutableWorkflow<?>, Job<?>>> addObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Job<?>>> modifyObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Job<?>>> removeObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Connection>> connectObserver;
	protected final Observer<Pair<MutableWorkflow<?>, Connection>> disconnectObserver;
	protected final MultiplexObserver<Model<F>> selectionChangeObservable;
	protected final MultiplexObserver<Model<F>> workflowCheckObservable;

	public Model(MutableWorkflow<F> hwf) {
		this.hwf = hwf;
		addObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Job<?>>>();
		modifyObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Job<?>>>();
		removeObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Job<?>>>();
		connectObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Connection>>();
		disconnectObservable = new MultiplexObserver<Pair<MutableWorkflow<?>, Connection>>();
		selectionChangeObservable = new MultiplexObserver<Model<F>>();
		workflowCheckObservable = new MultiplexObserver<Model<F>>();
		addObserver = new Observer<Pair<MutableWorkflow<?>, Job<?>>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Job<?>> event) {
				if (event.snd instanceof CompositeJob)
					bind(((CompositeJob<?, ?>) event.snd).getWorkflow());
				addObservable.notify(event);
			}
		};
		modifyObserver = new Observer<Pair<MutableWorkflow<?>, Job<?>>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Job<?>> event) {
				modifyObservable.notify(event);
			}
		};
		removeObserver = new Observer<Pair<MutableWorkflow<?>, Job<?>>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Job<?>> event) {
				if (event.snd instanceof CompositeJob)
					unbind(((CompositeJob<?, ?>) event.snd).getWorkflow());
				if (selection != null
						&& Model.this.hwf.dereference(selection.path
								.listIterator()) == event.fst)
					setSelection(null);
				removeObservable.notify(event);
			}
		};
		connectObserver = new Observer<Pair<MutableWorkflow<?>, Connection>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Connection> event) {
				connectObservable.notify(event);
			}
		};
		disconnectObserver = new Observer<Pair<MutableWorkflow<?>, Connection>>() {
			@Override
			public void notify(Pair<MutableWorkflow<?>, Connection> event) {
				if (selection != null
						&& Model.this.hwf.dereference(selection.path
								.listIterator()) == event.fst)
					setSelection(null);
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
		for (Job<?> job : wf.getChildren()) {
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

	public Observable<Pair<MutableWorkflow<?>, Job<?>>> getAddObservable() {
		return addObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Connection>> getConnectObservable() {
		return connectObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Connection>> getDisconnectObservable() {
		return disconnectObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Job<?>>> getModifyObservable() {
		return modifyObservable;
	}

	public Observable<Pair<MutableWorkflow<?>, Job<?>>> getRemoveObservable() {
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
		for (Job<?> job : wf.getChildren()) {
			if (job instanceof CompositeJob)
				unbind(((CompositeJob<?, ?>) job).getWorkflow());
		}

	}

}
