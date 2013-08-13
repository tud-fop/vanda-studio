package org.vanda.workflows.data;

public final class Databases {
	
	public interface DatabaseEvent<D> {
		void doNotify(DatabaseListener<D> l);
		D getDatabase();
	}
	
	public interface DatabaseListener<D> {
		void cursorChange(D d);
		void dataChange(D d, Object key);
		void nameChange(D d);
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
		private final Object key;
		
		public DataChange(D d, Object key) {
			this.d = d;
			this.key = key;
		}

		@Override
		public void doNotify(DatabaseListener<D> l) {
			l.dataChange(d, key);
		}

		@Override
		public D getDatabase() {
			return d;
		}
		
	}
	
	public static class NameChange<D> implements DatabaseEvent<D> {
		private final D d;

		public NameChange(D d) {
			this.d = d;
		}
		
		@Override
		public void doNotify(DatabaseListener<D> l) {
			l.nameChange(d);			
		}

		@Override
		public D getDatabase() {
			return d;
		}
		
	}

}
