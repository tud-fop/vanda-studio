package org.vanda.datasources;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.Observer;

public class IntegerDataSource implements DataSource {

	private static final Type TYPE = new CompositeType("Integer");
	
	public IntegerDataSource() {
	}

	private class IntegerElement implements ElementSelector, Observer<Element> {

		private Element element;
		private JSpinner jNumber;

		public IntegerElement() {
			jNumber = new JSpinner(new SpinnerNumberModel());
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
			return jNumber;
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
					notify(element);
				}
			}
		}

		@Override
		public void notify(Element event) {
			try {
				jNumber.getModel().setValue(Integer.parseInt(event.getValue()));
			} catch (NumberFormatException nfe) {
				jNumber.getModel().setValue(0);
			}
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
