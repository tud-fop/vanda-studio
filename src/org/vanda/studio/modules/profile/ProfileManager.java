package org.vanda.studio.modules.profile;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Profile;
import org.vanda.studio.app.Repository;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;

public final class ProfileManager {

	private final Application app;
	// private final SimpleRepository<Profile> repository;
	private final MultiplexObserver<ProfileManager> closeObservable;
	private JPanel contentPane;

	public ProfileManager(Application app, SimpleRepository<Profile> repository) {
		this.app = app;
		// this.repository = repository;
		closeObservable = new MultiplexObserver<ProfileManager>();
		JList list = new JList(new RepositoryListModel<Profile>(repository));
		JScrollPane listScroll = new JScrollPane(list);
		contentPane = new JPanel(new BorderLayout());
		contentPane.add(listScroll, BorderLayout.WEST);
		contentPane.setName("Fragment Profiles");
		app.getWindowSystem().addContentWindow(null, contentPane,
				new CloseAction());
		app.getWindowSystem().addAction(contentPane, new Action() {
			@Override
			public String getName() {
				return "Test";
			}

			@Override
			public void invoke() {

			}
		});

	}
	
	public void focus() {
		app.getWindowSystem().focusContentWindow(contentPane);
	}
	
	public Observable<ProfileManager> getCloseObservable() {
		return closeObservable;
	}

	private final class CloseAction implements Action {
		@Override
		public String getName() {
			return "Close";
		}

		@Override
		public void invoke() {
			app.getWindowSystem().removeContentWindow(contentPane);
			contentPane = null;
			closeObservable.notify(ProfileManager.this);
		}
	}

	private static final class RepositoryListModel<T extends RepositoryItem>
			implements ListModel, Observer<T> {

		private final Repository<T> repository;
		private List<T> items;
		private final Set<ListDataListener> listeners;

		public RepositoryListModel(Repository<T> repository) {
			this.repository = repository;
			listeners = new HashSet<ListDataListener>();
			items = new ArrayList<T>(repository.getItems());
			repository.getAddObservable().addObserver(this);
			repository.getRemoveObservable().addObserver(this);
			repository.getModifyObservable().addObserver(this);
		}

		@Override
		public int getSize() {
			return repository.getItems().size();
		}

		@Override
		public Object getElementAt(int index) {
			return items.get(index).getName();
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		@Override
		public void notify(T event) {
			for (ListDataListener l : listeners)
				l.contentsChanged(new ListDataEvent(this,
						ListDataEvent.INTERVAL_REMOVED, 0, items.size()));
			items.clear();
			items.addAll(repository.getItems());
			for (ListDataListener l : listeners)
				l.contentsChanged(new ListDataEvent(this,
						ListDataEvent.INTERVAL_ADDED, 0, items.size()));
		}

	}

}
