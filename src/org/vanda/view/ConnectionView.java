package org.vanda.view;

import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.view.Views.SelectionObject;
import org.vanda.view.Views.*;

public class ConnectionView extends AbstractView<ConnectionKey> {

	public ConnectionView(ViewListener<AbstractView<?>> listener) {
		super(listener);
	}
	
	@Override
	public SelectionObject createSelectionObject(ConnectionKey t) {
		return new ConnectionSelectionObject(t);
	}
}
