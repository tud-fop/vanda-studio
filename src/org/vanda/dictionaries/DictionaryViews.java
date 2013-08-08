package org.vanda.dictionaries;

public class DictionaryViews {
	public interface DictionaryViewState {
		public void toTableViewState(ViewTransition vt);
		public void toBestViewState(ViewTransition vt);
		public void selectView(ViewTransition vt);	
	}
	public interface ViewTransition {
		public void tableViewState();
		public void bestViewState();
		public void selectTableView();
		public void selectBestView();
	}
	
	public static class TableViewState implements DictionaryViewState {

		@Override
		public void toTableViewState(ViewTransition vt) {
		}

		@Override
		public void toBestViewState(ViewTransition vt) {
			vt.bestViewState();		
		}

		@Override
		public void selectView(ViewTransition vt) {
			vt.selectTableView();		
		}
		
	}
	
	public static class BestViewState implements DictionaryViewState {

		@Override
		public void toTableViewState(ViewTransition vt) {
			vt.tableViewState();
		}

		@Override
		public void toBestViewState(ViewTransition vt) {			
		}

		@Override
		public void selectView(ViewTransition vt) {
			vt.selectBestView();			
		}
		
	}
}
