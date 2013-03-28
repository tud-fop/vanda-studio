package org.vanda.datasources;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.studio.app.Application;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;

public class IntegerSourceFactory extends DataSourceFactory {

	public IntegerSourceFactory(Application app) {
		super(app, "Integer");
	}

	private class IntegerSource extends DataSource {

		private JSpinner jNumber;

		public IntegerSource(String name) {
			super(name);
			jNumber = new JSpinner(new SpinnerNumberModel());
			jNumber.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					observers.notify(IntegerSource.this);
				}
			});
		}

		@Override
		public String getValue(String element) {
			String[] arr = element.split(":");
			if (!arr[0].equals(getName()))
				return null;
			int r = Integer.parseInt(arr[1]);
			return Integer.toString(r);
		}

		@Override
		public Type getType(String element) {
			return new CompositeType("Integer");
		}

		@Override
		public JComponent getElementSelector() {
			return jNumber;
		}

		@Override
		public String getSelectedElement() {
			return getName() + ":" + jNumber.getValue();
		}

		@Override
		public void setSelectedElement(String element) {
			jNumber.setValue(Integer.parseInt(element.split(":")[1]));
		}

	}

	@Override
	public String getCategory() {
		return "Data Sources";
	}

	@Override
	public String getContact() {
		return "Tobias.Denkinger@mailbox.tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Integer Data Source.";
	}

	@Override
	public String getName() {
		return "IntegerSource";
	}

	@Override
	public String getVersion() {
		return "2012-03-27";
	}

	@Override
	public DataSource createInstance() {
		return new IntegerSource(getPrefix());
	}

}
