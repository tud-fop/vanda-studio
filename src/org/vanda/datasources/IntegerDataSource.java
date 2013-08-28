package org.vanda.datasources;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.datasources.Elements.ElementEvent;
import org.vanda.datasources.Elements.ElementListener;
import org.vanda.studio.app.Application;
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
			// disable digit grouping
			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(jNumber);
			editor.getFormat().setGroupingUsed(false);
			jNumber.setEditor(editor);
			jNumber.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
			jNumber.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (element != null)
						element.setValue(jNumber.getModel().getValue().toString());
				}
			});
			jNumber.addFocusListener(new FocusAdapter() {
				
				@Override
				public void focusLost(FocusEvent arg0) {
					if (element != null)
						element.setValue(jNumber.getModel().getValue().toString());
				}
			});
		}

		@Override
		public JComponent getComponent() {
			final JPanel pan = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			pan.add(jNumber, gbc);
			pan.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					pan.requestFocusInWindow();
				}
				
			});

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

	public class IntegerDataSourceEditor extends DataSourceEditor {

		@Override
		public JComponent getComponent() {
			return new JLabel("IntegerDataSource");
		}

		@Override
		public DataSource getDataSource() {
			return IntegerDataSource.this;
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
