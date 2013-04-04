package org.vanda.studio.modules.workflows.inspector;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
			if (!s.equals(l.getValue())) {
				l.setValue(s);
				l.setType(rds.getType(e));
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
			value.setText(l.getValue());
			/*
			 * Element el = Element.fromString(l.getValue()); ds.beginUpdate();
			 * try { ds.setPrefix(el.getPrefix()); ds.setValue(el.getValue()); }
			 * finally { ds.endUpdate(); }
			 */
		}
	}

	public class DatabaseObserver implements Observer<DatabaseEvent<Database>>, DatabaseListener<Database> {
		
		final Element e;

		public DatabaseObserver(Element e) {
			this.e = e;
		}

		@Override
		public void notify(DatabaseEvent<Database> event) {
			event.doNotify(this);
		}

		@Override
		public void cursorChange(Database d) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataChange(Database d, Object key) {
			
		}

	}

	@Override
	public JComponent createEditor(Database d, MutableWorkflow wf, final Literal l) {
		JLabel label1 = new JLabel("Name");
		final JTextField value = new JTextField(l.getValue());

		Element cds = Element.fromString(l.getValue());
		cds.getObservable().addObserver(new ElementObserver(d, l));
		l.getObservable().addObserver(new LiteralObserver(value)); // FIXME memory leak
		d.getObservable().addObserver(new DatabaseObserver(cds));
		ElementSelector selector = rds.createSelector();
		selector.setElement(cds);

		value.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.setValue(value.getText());
			}
		});

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
