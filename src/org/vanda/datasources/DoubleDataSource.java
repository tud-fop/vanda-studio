package org.vanda.datasources;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.vanda.datasources.Elements.ElementEvent;
import org.vanda.datasources.Elements.ElementListener;
import org.vanda.studio.app.Application;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.Observer;

public class DoubleDataSource implements DataSource {

	private static final Type TYPE = new CompositeType("Double");
	
	public DoubleDataSource() {
	}

	private class IntegerElement implements ElementSelector, Observer<ElementEvent<Element>>, ElementListener<Element> {

		private Element element;
		private JTextField jNumber;

		public IntegerElement() {
			jNumber = new JTextField();
			jNumber.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
			jNumber.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (element != null)
						element.setValue(jNumber.getText());
				}
			});
		}

		@Override
		public JComponent getComponent() {
			JPanel pan = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			pan.add(jNumber, gbc);
			return pan;
		}

		@Override
		public Element getElement() {
			return element;
		}

		@Override
		public void setElement(Element e) {
			if (element != e) {
				if (element != null)
					element.getObservable().removeObserver(this);
				element = e;
				if (element != null) {
					element.getObservable().addObserver(this);
					valueChanged(element);
				}
			}
		}

		@Override
		public void prefixChanged(Element e) {
			// do nothing
		}

		@Override
		public void valueChanged(Element e) {
			jNumber.setText(e.getValue());
		}

		@Override
		public void notify(ElementEvent<Element> event) {
			event.doNotify(this);
		}

	}

	@Override
	public ElementSelector createSelector() {
		return new IntegerElement();
	}

	@Override
	public String getValue(Element element) {
		return element.getValue();
	}

	@Override
	public Type getType(Element element) {
		return TYPE;
	}
	
	public class IntegerDataSourceEditor extends DataSourceEditor {

		@Override
		public JComponent getComponent() {
			return new JLabel("DoubleDataSource");
		}

		@Override
		public DataSource getDataSource() {
			return DoubleDataSource.this;
		}

		@Override
		public void write() {
			// Do nothing.
		}
		
	}

	@Override
	public DataSourceEditor createEditor(Application app) {
		return new IntegerDataSourceEditor();
	}
}
