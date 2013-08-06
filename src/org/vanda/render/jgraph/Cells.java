package org.vanda.render.jgraph;

import java.awt.event.MouseEvent;

public final class Cells {
	public static interface CellEvent<C> {
		void doNotify(CellListener<C> cl);
	}

	public static interface CellListener<C> {
		void insertCell(C c);

		void markChanged(C c);

		void propertyChanged(C c);

		void removeCell(C c);

		void selectionChanged(C c, boolean selected);

		void setSelection(C c, boolean selected);

		void rightClick(MouseEvent e);
	}

	public static class InsertCellEvent<C> implements CellEvent<C> {
		private final C c;

		public InsertCellEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.insertCell(c);
		}

	}

	public static class MarkChangedEvent<C> implements CellEvent<C> {
		private final C c;

		public MarkChangedEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.markChanged(c);
		}

	}

	public static class PropertyChangedEvent<C> implements CellEvent<C> {
		private final C c;

		public PropertyChangedEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.propertyChanged(c);
		}
	}

	public static class RemoveCellEvent<C> implements CellEvent<C> {
		private final C c;

		public RemoveCellEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.removeCell(c);
		}

	}

	public static class SelectionChangedEvent<C> implements CellEvent<C> {
		private final C c;
		boolean selected;

		public SelectionChangedEvent(C c, boolean selected) {
			this.c = c;
			this.selected = selected;

		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.selectionChanged(c, selected);
		}

	}

	public static class SetSelectionEvent<C> implements CellEvent<C> {
		private final C c;
		private final boolean selected;

		public SetSelectionEvent(C c, boolean selected) {
			this.c = c;
			this.selected = selected;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.setSelection(c, selected);
		}

	}
	
	public static class RightClickEvent<C> implements CellEvent<C> {
		private final MouseEvent e;
		
		public RightClickEvent(MouseEvent e) {
			this.e = e;
		}
		
		@Override
		public void doNotify(CellListener<C> cl) {
			cl.rightClick(e);
		}
	}
	
	public static class RunVisualizationChangedEvent<C> implements CellEvent<C> {
		private final C c;

		public RunVisualizationChangedEvent (C c){
			this.c = c;
		}
		
		@Override
		public void doNotify(CellListener<C> cl) {
			cl.propertyChanged(c);
		}
		
	}
}
