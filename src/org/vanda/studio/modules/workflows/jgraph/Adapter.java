package org.vanda.studio.modules.workflows.jgraph;


import org.vanda.studio.model.hyper.CompositeHyperJob;
import org.vanda.studio.model.hyper.HyperConnection;
import org.vanda.studio.model.hyper.HyperJob;
import org.vanda.studio.model.hyper.HyperWorkflow;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;

import com.mxgraph.view.mxGraph;

// TODO do something about all those unchecked conversions and raw types

public class Adapter {

	protected GraphRenderer renderer;
	
	public <F, V> Adapter(HyperWorkflow<F, V> root) {
		renderer = new GraphRenderer(root, false);
		renderer.getAddObservable().addObserver(rendererAddObserver);
		renderer.getModifyObservable().addObserver(rendererModifyObserver);
		renderer.getRemoveObservable().addObserver(rendererRemoveObserver);
		renderer.getConnectObservable().addObserver(rendererConnectObserver);
		renderer.getDisconnectObservable().addObserver(rendererDisconnectObserver);
		renderer.render(null, root);
	}
	
	@SuppressWarnings({ "unchecked" })
	private <F, V> void bind(HyperWorkflow<F, V> hwf) {
		hwf.getAddObservable().addObserver((Observer) hwfAddObserver);
		hwf.getModifyObservable().addObserver((Observer) hwfModifyObserver);
		hwf.getRemoveObservable().addObserver((Observer) hwfRemoveObserver);
		hwf.getConnectObservable().addObserver((Observer) hwfConnectObserver);
		hwf.getDisconnectObservable().addObserver((Observer) hwfDisconnectObserver);
	}
	
	@SuppressWarnings({ "unchecked" })
	private <F, V> void unbind(HyperWorkflow<F, V> hwf) {
		// XXX this could blow up big time
		hwf.getAddObservable().removeObserver((Observer) hwfAddObserver);
		hwf.getModifyObservable().removeObserver((Observer) hwfModifyObserver);
		hwf.getRemoveObservable().removeObserver((Observer) hwfRemoveObserver);
		hwf.getConnectObservable().removeObserver((Observer) hwfConnectObserver);
		hwf.getDisconnectObservable().removeObserver((Observer) hwfDisconnectObserver);
		for (HyperJob<V> c : hwf.getChildren()) {
			if (c instanceof CompositeHyperJob<?, ?, ?, ?>) {
				CompositeHyperJob<?, V, ?, ?> chj = (CompositeHyperJob<?, V, ?, ?>) c;
				unbind(chj.getWorkflow());
			}
		}
	}
	
	public mxGraph getGraph() {
		return renderer.getGraph();
	}
	
	// Observers that react on changes within the observed HyperWorkflow
	
	private Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> hwfAddObserver 
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			System.out.println("hwf added something");
			renderer.render((HyperWorkflow)event.fst, event.snd);
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> hwfModifyObserver 
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			//TODO
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> hwfRemoveObserver 
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			System.out.println("hwf removed something");
			renderer.remove(event.fst, event.snd);
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> hwfConnectObserver 
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperConnection<?>> event) {
			System.out.println("hwf connected something");
			renderer.render((HyperWorkflow)event.fst, event.snd);
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> hwfDisconnectObserver 
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperConnection<?>> event) {
			System.out.println("hwf disconnected something");
			renderer.remove(event.fst, event.snd);
		}
	};
	
	// Observers that react on changes induced by the renderer
	
	private Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> rendererAddObserver
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			// happens when rendering a loaded HyperWorkflow
			if (event.fst != null && event.snd == null) {
				System.out.println("bind Adapter to " + event.fst);
				bind(event.fst);
			} else {
				// happens whenever a user adds nodes from within the GUI
				
				// bind observer to newly added HyperWorkflow
				if (event.snd instanceof CompositeHyperJob<?,?,?,?>) {
					//System.out.println("bind...");
					//bind(((CompositeHyperJob)event.snd).getWorkflow());
				}
				
				System.out.println("renderer added HyperJob '" + event.snd.getName() + "' to " + event.fst);
				event.fst.addChild((HyperJob)event.snd);
			}
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> rendererModifyObserver
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			//TODO
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> rendererRemoveObserver
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			System.out.println("renderer removed HyperJob '" + event.snd.getName() + "' from " + event.fst);
			if (event.snd instanceof CompositeHyperJob<?, ?, ?, ?>) {
				unbind(((CompositeHyperJob<?, ?, ?, ?>) event.snd)
						.getWorkflow());
				System.out.println("unbind Adapter from " + event.snd);
			}
			HyperWorkflow.removeChildGeneric(event.snd);
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> rendererConnectObserver 
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperConnection<?>> event) {
			System.out.println("renderer added HyperConnection '" + event.snd + "' to " + event.fst);
			event.fst.addConnection((HyperConnection)event.snd);
		}
	};
	private Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> rendererDisconnectObserver 
		= new Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>() {
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperConnection<?>> event) {
			System.out.println("renderer removed HyperConnection '" + event.snd + "' from " + event.fst);
			HyperWorkflow.removeConnectionGeneric(event.snd);
		}
	};
}
