package org.vanda.presentationmodel.palette;

import java.awt.event.MouseEvent;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cells.CellListener;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManager;
import org.vanda.render.jgraph.LocationCell;
import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.util.Observer;

public class LocationAdapter {
	private class LocationCellListener implements CellListener<Cell> {

		@Override
		public void insertCell(Cell c) {
			// do nothing

		}

		@Override
		public void markChanged(Cell c) {
			// do nothing

		}

		@Override
		public void propertyChanged(Cell c) {
			// do nothing

		}

		@Override
		public void removeCell(Cell c) {
			// do nothing

		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			// do nothing

		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			// do nothing
		}

		@Override
		public void rightClick(MouseEvent e) {
			// do nothing
		}

	}

	LocationCell locationCell;
	private LocationCellListener locationCellListener;
	private Observer<CellEvent<Cell>> locationCellObserver;

	public LocationAdapter(Graph g, LayoutManager layoutManager, JobCell jobCell) {
		locationCell = new LocationCell(g, layoutManager, jobCell);
		locationCellListener = new LocationCellListener();
		locationCellObserver = new Observer<CellEvent<Cell>>() {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(locationCellListener);
			}

		};
		locationCell.getObservable().addObserver(locationCellObserver);
	}


}
