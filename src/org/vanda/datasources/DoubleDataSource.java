package org.vanda.datasources;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
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
		private JFormattedTextField jNumber;

		public IntegerElement() {
			jNumber = new JFormattedTextField(NumberFormat.getNumberInstance());
			final InputVerifier iv = new InputVerifier() {
				
				@Override
				public boolean verify(JComponent arg0) {
					try { 
						Double.parseDouble(jNumber.getText());
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
				}
			};
			jNumber.setInputVerifier(iv);
			jNumber.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
			jNumber.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (element != null && iv.verify(null))
						element.setValue(jNumber.getText());
				}
			});
			jNumber.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent arg0) {
					if (element != null && iv.verify(null))
						element.setValue(jNumber.getText());
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

	public class DoubleDataSourceEditor extends DataSourceEditor {

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
		return new DoubleDataSourceEditor();
	}
}
