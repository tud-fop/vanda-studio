package org.vanda.view;

import org.vanda.view.Views.*;
import org.vanda.workflows.hyper.Location;

public class LocationView extends AbstractView<Location> {

	public LocationView(ViewListener<AbstractView<?>> listener) {
		super(listener);
	}

	@Override
	public SelectionObject createSelectionObject(Location t) {
		return new LocationSelectionObject(t);
	}

}
