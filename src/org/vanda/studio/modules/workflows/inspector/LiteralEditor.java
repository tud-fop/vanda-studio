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
	
	public class BumsObserver implements Observer<org.vanda.datasources.Elements.ElementEvent<Element>> {
		final Literal l;
		
		public BumsObserver(Literal l) {
			this.l = l;
		}

		@Override
		public void notify(org.vanda.datasources.Elements.ElementEvent<Element> event) {
			// event.doNotify(this);
			Element e = event.getElement();
			String s = e.toString();
			if (!s.equals(l.getValue())) {
				l.setValue(s);
				l.setType(rds.getType(e));
			}
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
		public void typeChanged(Literal l) {
			// do nothing
		}

		@Override
		public void valueChanged(Literal l) {
			Element el = Element.fromString(l.getValue());
			ds.beginUpdate();
			try {
				ds.setPrefix(el.getPrefix());
				ds.setValue(el.getValue());
			} finally {
				ds.endUpdate();
			}
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
