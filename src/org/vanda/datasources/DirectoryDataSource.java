package org.vanda.datasources;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.types.Type;
import org.vanda.util.Observer;

public class DirectoryDataSource implements DataSource {

	public File dir;
	public String filter;
	public Type type;

	public DirectoryDataSource(Type type, String path, String filter) {
		this.type = type;
		this.filter = filter;
		this.dir = new File(path);
	}

	public class DirectoryElementSelector implements ElementSelector, Observer<Element> {

		private Element element;
		private JList selector;

		public DirectoryElementSelector() {
			selector = new JList();
			String[] l = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File _, String name) {
					return name.matches(filter);
				}
			});
			if (l != null)
				selector.setListData(l);
			selector.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (element != null)
						element.setValue(selector.getSelectedValue().toString());
				}
			});
		}

		@Override
		public JComponent getComponent() {
			return selector;
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
			selector.setSelectedValue(event.getValue(), true);
		}
	}

	@Override
	public ElementSelector createSelector() {
		return new DirectoryElementSelector();
	}

	@Override
	public String getValue(Element element) {
		return dir.getAbsolutePath() + "/" + element.getValue();
	}

	@Override
	public Type getType(Element element) {
		return type;
	}

}
