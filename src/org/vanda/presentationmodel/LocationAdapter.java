package org.vanda.presentationmodel;

import java.awt.event.MouseEvent;

import org.vanda.execution.model.Runables.RunState;
import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cells.CellListener;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManager;
import org.vanda.render.jgraph.LocationCell;
import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.Cells.MarkChangedEvent;
import org.vanda.render.jgraph.Cells.SelectionChangedEvent;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.view.LocationView;
import org.vanda.view.View;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;

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
			LocationView lv = view.getLocationView(variable);
			lv.setSelected(selected);
		}

		@Override
		public void rightClick(MouseEvent e) {
			// do nothing
		}

	}

	private class LocationViewListener implements
			AbstractView.ViewListener<AbstractView> {

		@Override
		public void highlightingChanged(AbstractView v) {
			// TODO Auto-generated method stub

		}

		@Override
		public void markChanged(AbstractView v) {
			locationCell.getObservable().notify(
					new MarkChangedEvent<Cell>(locationCell));
		}
		
		@Override
		public void runProgressUpdate(AbstractView _) {
			// do nothing
		}

		@Override
		public void selectionChanged(AbstractView v) {
			locationCell.getObservable().notify(
					new SelectionChangedEvent<Cell>(locationCell, v
							.isSelected()));
		}

		@Override
		public void runStateTransition(AbstractView v, RunState from, RunState to) {
			// do nothing
		}
	}

	LocationCell locationCell;
	LocationCellListener locationCellListener;
	LocationViewListener locationViewListener;

	Port port;

	Location variable;

	View view;

	public LocationAdapter(Graph g, View view, LayoutManager layoutManager,
			JobCell jobCell, Port port, Location location) {
		locationCell = new LocationCell(g, layoutManager, jobCell);

		this.locationCellListener = new LocationCellListener();
		locationCell.getObservable().addObserver(
				new Observer<CellEvent<Cell>>() {

					@Override
					public void notify(CellEvent<Cell> event) {
						event.doNotify(locationCellListener);
					}

				});

		this.view = view;
		this.port = port;
		this.variable = location;
		locationViewListener = new LocationViewListener();

		// Register at LocationView
		view.getLocationView(variable).getObservable()
				.addObserver(new Observer<ViewEvent<AbstractView>>() {

					@Override
					public void notify(ViewEvent<AbstractView> event) {
						event.doNotify(locationViewListener);
					}
				});
	}

	public void updateLocation(Job job) {
		variable = job.bindings.get(port);
	}

	public void destroy() {
		view.getLocationView(variable).setSelected(false);
		// FIXME due to an unknown reason the LocationAdapter is not garbage
		// collected but we want the reference-counts to the following objects
		// to be 0
		locationCell = null;
		port = null;
		variable = null;
	}
}
