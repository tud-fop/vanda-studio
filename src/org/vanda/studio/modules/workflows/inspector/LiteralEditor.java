package org.vanda.studio.modules.workflows.inspector;

import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.vanda.datasources.ElementSelector;
import org.vanda.datasources.RootDataSource;
import org.vanda.datasources.Element;
import org.vanda.studio.app.Application;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
import org.vanda.workflows.elements.Elements.ElementEvent;
import org.vanda.workflows.elements.Elements.ElementListener;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.MutableWorkflow;

public class LiteralEditor implements ElementEditorFactory<Literal> {

	private RootDataSource rds;

	public LiteralEditor(Application app) {
		rds = app.getRootDataSource();
	}

	public class ElementObserver implements Observer<org.vanda.datasources.Elements.ElementEvent<Element>> {
		final Database d;
		final Literal l;

		public ElementObserver(Database d, Literal l) {
			this.d = d;
			this.l = l;
		}

		@Override
		public void notify(org.vanda.datasources.Elements.ElementEvent<Element> event) {
			// event.doNotify(this);
			Element e = event.getElement();
			String s = e.toString();
			if (!s.equals(d.get(l.getKey()))) {
				l.setType(rds.getType(e));
				d.put(l.getKey(), s);
			}
		}
	}

	public class LiteralObserver implements Observer<ElementEvent<Literal>>, ElementListener<Literal> {
		// final Element ds;
		final JTextField value;

		public LiteralObserver(JTextField value) {
			this.value = value;
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
			value.setText(l.getName());
		}
	}

	public class DatabaseObserver implements Observer<DatabaseEvent<Database>>, DatabaseListener<Database> {

		final Literal l;
		final Element e;

		public DatabaseObserver(Literal l, Element e) {
			this.l = l;
			this.e = e;
		}

		@Override
		public void notify(DatabaseEvent<Database> event) {
			event.doNotify(this);

		}

		@Override
		public void cursorChange(Database d) {
			// Database d = event.getDatabase();
			Element el = Element.fromString(d.get(l.getKey()));	
			e.beginUpdate();
			try {
				e.setPrefix(el.getPrefix());
				e.setValue(el.getValue());
			} finally {
				e.endUpdate();
			}
		}

		@Override
		public void dataChange(Database d, Object key) {
			if (key.equals(l.getKey())) {
				Element el = Element.fromString(d.get(l.getKey()));
				e.beginUpdate();
				try {
					e.setPrefix(el.getPrefix());
					e.setValue(el.getValue());
				} finally {
					e.endUpdate();
				}
			}
		}

		@Override
		public void nameChange(Database d) {
		}

	}

	@Override
	public JComponent createEditor(Database d, MutableWorkflow wf, final Literal l) {
		JLabel label1 = new JLabel("Name");
		final JTextField value = new JTextField(l.getName());
		value.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				l.setName(value.getText());
			}
		});

		// FIXME memory leaks probable due to all the observers
		final Element e = Element.fromString(d.get(l.getKey()));
		e.getObservable().addObserver(new ElementObserver(d, l));
		l.getObservable().addObserver(new LiteralObserver(value));
		d.getObservable().addObserver(new DatabaseObserver(l, e));
		ElementSelector selector = rds.createSelector();
		selector.setElement(e);

		JPanel editor = new JPanel();
		GroupLayout layout = new GroupLayout(editor);
		editor.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(label1))
				.addGroup(layout.createParallelGroup().addComponent(value)));

		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label1).addComponent(value)));

		JPanel main = new JPanel(new BorderLayout());
		main.add(editor, BorderLayout.NORTH);
		main.add(selector.getComponent(), BorderLayout.CENTER);
		return main;
	}

}
