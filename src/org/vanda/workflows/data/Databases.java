package org.vanda.workflows.data;

public final class Databases {
	
	public interface DatabaseEvent<D> {
		void doNotify(DatabaseListener<D> l);
		D getDatabase();
	}
	
	public interface DatabaseListener<D> {
		void cursorChange(D d);
		void dataChange(D d);
	}
	
	public static class CursorChange<D> implements DatabaseEvent<D> {
		
		private final D d;
		
		public CursorChange(D d) {
			this.d = d;
		}

		@Override
		public void doNotify(DatabaseListener<D> l) {
			l.cursorChange(d);
		}

		@Override
		public D getDatabase() {
			return d;
		}
		
	}
	
	public static class DataChange<D> implements DatabaseEvent<D> {
		
		private final D d;
		
		public DataChange(D d) {
			this.d = d;
		}

		@Override
		public void doNotify(DatabaseListener<D> l) {
			l.dataChange(d);
		}

		@Override
		public D getDatabase() {
			return d;
		}
		
	}

}
