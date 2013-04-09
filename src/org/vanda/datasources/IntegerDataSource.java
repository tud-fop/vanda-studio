package org.vanda.datasources;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.datasources.Elements.ElementEvent;
import org.vanda.datasources.Elements.ElementListener;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.Observer;

public class IntegerDataSource implements DataSource {

	private static final Type TYPE = new CompositeType("Integer");
	
	public IntegerDataSource() {
	}

	private class IntegerElement implements ElementSelector, Observer<ElementEvent<Element>>, ElementListener<Element> {

		private Element element;
		private JSpinner jNumber;

		public IntegerElement() {
			jNumber = new JSpinner(new SpinnerNumberModel());
			jNumber.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
			jNumber.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (element != null)
						element.setValue(jNumber.getModel().getValue().toString());
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
			try {
				jNumber.getModel().setValue(Integer.parseInt(e.getValue()));
			} catch (NumberFormatException nfe) {
				jNumber.getModel().setValue(0);
			}
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

}
