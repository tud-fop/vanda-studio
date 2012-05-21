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
import org.vanda.studio.app.Module;
import org.vanda.studio.app.Profile;
import org.vanda.studio.app.Repository;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.Observer;

public class ProfileModule implements Module {

	@Override
	public String getName() {
		return "Simple Fragment Profile";
	}

	@Override
	public Object createInstance(Application a) {
		return new ProfileModuleInstance(a);
	}

	private static final class ProfileModuleInstance {
		private final Application app;
		private final SimpleRepository<Profile> repository;
		private JPanel manager;

		public ProfileModuleInstance(Application app) {
			this.app = app;
			repository = new SimpleRepository<Profile>(null);
			repository.addItem(new FragmentProfile());
			manager = null;
			app.getProfileMetaRepository().addRepository(repository);
			app.getWindowSystem().addAction(null, new Action() {
				@Override
				public String getName() {
					return "Manage Fragment Profiles...";
				}

				@Override
				public void invoke() {
					openManager();
				}
			});
		}

		public void openManager() {
			if (manager == null) {
				JList list = new JList(new RepositoryListModel<Profile>(
						repository));
				JScrollPane listScroll = new JScrollPane(list);
				manager = new JPanel(new BorderLayout());
				manager.add(listScroll, BorderLayout.WEST);
				manager.setName("Fragment Profiles");
				app.getWindowSystem().addContentWindow(null, manager,
						new Action() {
							@Override
							public String getName() {
								return "Close";
							}

							@Override
							public void invoke() {
								app.getWindowSystem().removeContentWindow(
										manager);
								manager = null;
							}
						});
				app.getWindowSystem().addAction(manager, new Action() {

					@Override
					public String getName() {
						return "Test";
					}

					@Override
					public void invoke() {

					}

				});
			}
			app.getWindowSystem().focusContentWindow(manager);
		}
	}

	private static class RepositoryListModel<T extends RepositoryItem>
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
