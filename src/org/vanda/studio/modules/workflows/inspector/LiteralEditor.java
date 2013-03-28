package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.datasources.CompositeDataSourceFactory;
import org.vanda.datasources.DataSource;
import org.vanda.studio.app.Application;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Elements.ElementEvent;
import org.vanda.workflows.elements.Elements.ElementListener;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.MutableWorkflow;

public class LiteralEditor implements ElementEditorFactory<Literal> {

	private CompositeDataSourceFactory fact;

	public LiteralEditor(Application app) {
		fact = new CompositeDataSourceFactory(app);
	}
	
	public class BumsObserver implements Observer<DataSource> {
		final Literal l;
		
		public BumsObserver(Literal l) {
			this.l = l;
		}
		
		@Override
		public void notify(DataSource ds) {
			String element = ds.getSelectedElement();
			l.setType(ds.getType(element));
			l.setValue(element);
		}
	}

	public class DingsObserver implements Observer<ElementEvent<Literal>>, ElementListener<Literal> {
		final DataSource ds;
		
		public DingsObserver(DataSource ds) {
			this.ds = ds;
		}
		
		@Override
		public void notify(ElementEvent<Literal> e) {
			e.doNotify(this);
		}

		@Override
		public void propertyChanged(Literal e) {
			ds.setSelectedElement(e.getValue());
		}
	}


	@Override
	public JComponent createEditor(final Application app, MutableWorkflow wf,
			final Literal l) {
		DataSource cds = fact.createInstance();
		if (l.getValue() != null)
			cds.setSelectedElement(l.getValue());
		cds.getObservable().addObserver(new BumsObserver(l));
		l.getObservable().addObserver(new DingsObserver(cds));  // FIXME memory leak
		return cds.getElementSelector();
	}

}
