package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.datasources.ElementSelector;
import org.vanda.datasources.RootDataSource;
import org.vanda.datasources.Element;
import org.vanda.studio.app.Application;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Elements.ElementEvent;
import org.vanda.workflows.elements.Elements.ElementListener;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.MutableWorkflow;

public class LiteralEditor implements ElementEditorFactory<Literal> {

	private RootDataSource rds;

	public LiteralEditor(Application app) {
		rds = app.getRootDataSource();
	}
	
	public class BumsObserver implements Observer<Element> {
		final Literal l;
		
		public BumsObserver(Literal l) {
			this.l = l;
		}
		
		@Override
		public void notify(Element ds) {
			l.setValue(ds.toString());
			l.setType(rds.getType(ds));
		}
	}

	public class DingsObserver implements Observer<ElementEvent<Literal>>, ElementListener<Literal> {
		final Element ds;
		
		public DingsObserver(Element ds) {
			this.ds = ds;
		}
		
		@Override
		public void notify(ElementEvent<Literal> e) {
			e.doNotify(this);
		}

		@Override
		public void propertyChanged(Literal l) {
			Element el = Element.fromString(l.getValue());
			ds.setElement(el.getPrefix(), el.getValue());
		}
	}

	@Override
	public JComponent createEditor(final Application app, MutableWorkflow wf,
			final Literal l) {
		Element cds = Element.fromString(l.getValue());
		cds.getObservable().addObserver(new BumsObserver(l));
		l.getObservable().addObserver(new DingsObserver(cds));  // FIXME memory leak
		ElementSelector selector = rds.createSelector();
		selector.setElement(cds);
		return selector.getComponent();
	}

}
