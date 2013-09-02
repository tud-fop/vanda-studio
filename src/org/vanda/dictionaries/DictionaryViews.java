package org.vanda.dictionaries;

public class DictionaryViews {
	public static class MutableDictionaryViewState {
		public DictionaryViewState value;
	}
	
	public interface DictionaryViewState {
		public void toTableViewState(ViewTransition vt);

		public void toBestViewState(ViewTransition vt);

		public void selectView(ViewTransition vt);

		public int getPrecision();

		public void setPrecision(int precision);
	}

	public interface ViewTransition {
		public void tableViewState();

		public void bestViewState();

		public void selectTableView();

		public void selectBestView();
	}

	private static abstract class DefaultViewState implements DictionaryViewState {
		private int precision = 4;
		
		@Override
		public int getPrecision() {
			return precision;
		}

		@Override
		public void setPrecision(int precision) {
			this.precision = precision;
		}

	}

	public static class TableViewState extends DefaultViewState {	
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

	public static class BestViewState extends DefaultViewState {

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
