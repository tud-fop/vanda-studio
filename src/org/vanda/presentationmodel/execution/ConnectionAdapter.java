package org.vanda.presentationmodel.execution;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.Cells.CellListener;
import org.vanda.render.jgraph.Cells.SelectionChangedEvent;
import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.InPortCell;
import org.vanda.render.jgraph.OutPortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.studio.modules.workflows.impl.WorkflowEditorImpl.PopupMenu;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.ConnectionView;
import org.vanda.view.View;
import org.vanda.view.Views.*;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

public class ConnectionAdapter {
	private class ConnectionCellListener implements CellListener<Cell> {

		@Override
		public void insertCell(Cell c) {
			// do nothing
		}

		@Override
		public void markChanged(Cell c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void propertyChanged(Cell c) {
			// do nothing
		}

		@Override
		public void removeCell(Cell c) {
			if (connectionKey != null) {
				if (connectionKey.target.isInserted())
					view.getWorkflow().removeConnection(connectionKey);
			}
		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			// do nothing

		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			if (connectionKey != null) {
				ConnectionView cv = view.getConnectionView(connectionKey);
				cv.setSelected(selected);
			}
		}

		@Override
		public void rightClick(MouseEvent e) {
			PopupMenu menu = new PopupMenu(((ConnectionCell) visualization).toString());

			@SuppressWarnings("serial")
			JMenuItem item = new JMenuItem("Remove Connection") {
				@Override
				public void fireActionPerformed(ActionEvent _) {
					view.removeSelectedCell();
				}
			};

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			menu.add(item);
			menu.show(e.getComponent(), e.getX(), e.getY());
		}

	}

	private class ConnectionViewListener implements ViewListener<AbstractView<?>> {

		@Override
		public void highlightingChanged(AbstractView<?> v) {
			// TODO Auto-generated method stub

		}

		@Override
		public void markChanged(AbstractView<?> v) {
			if (v.isMarked())
				visualization.highlight(true);
			else
				visualization.highlight(false);
		}

		@Override
		public void selectionChanged(AbstractView<?> v) {
			visualization.getObservable().notify(new SelectionChangedEvent<Cell>(visualization, v.isSelected()));
		}
	}

	private ConnectionCellListener connectionCellListener;
	private Observer<CellEvent<Cell>> connectionCellObserver;

	private final ConnectionKey connectionKey;

	private ConnectionViewListener connectionViewListener;
	private Observer<ViewEvent<AbstractView<?>>> connectionViewObserver;

	private View view;

	private final ConnectionCell visualization;

	/**
	 * Constructor in case of hand-drawn edge creation
	 * 
	 * @param connectionKey
	 * @param visualization
	 * @param view
	 */
	public ConnectionAdapter(ConnectionKey connectionKey, ConnectionCell visualization, View view) {
		// System.out.println("Hand-Drawn edge!");
		this.visualization = visualization;
		this.connectionKey = connectionKey;
		this.view = view;

		// register at Connection View
		connectionCellListener = new ConnectionCellListener();
		connectionCellObserver = new Observer<CellEvent<Cell>>() {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(connectionCellListener);
			}

		};
		visualization.getObservable().addObserver(connectionCellObserver);

		// create ConnectionView
		// view.addConnectionView(connectionKey);

		connectionViewListener = new ConnectionViewListener();
		connectionViewObserver = new Observer<ViewEvent<AbstractView<?>>>() {

			@Override
			public void notify(ViewEvent<AbstractView<?>> event) {
				event.doNotify(connectionViewListener);
			}
		};

		// Register at ConnectionView
		view.getConnectionView(connectionKey).getObservable().addObserver(connectionViewObserver);

		// identify Source Job and Source Port
		OutPortCell sourcePortCell = visualization.getSourceCell();
		JobCell sourceJobCell = (JobCell) sourcePortCell.getParentCell();
		PresentationModel pm = (PresentationModel) ((WorkflowCell) visualization.getParentCell()).getDataInterface();
		Job sourceJob = null;
		JobAdapter sourceJobAdapter = null;
		Port sourcePort = null;

		for (JobAdapter ja : pm.getJobs()) {
			if (ja.getJobCell() == sourceJobCell) {
				sourceJob = ja.getJob();
				sourceJobAdapter = ja;
				break;
			}
		}
		for (Port op : sourceJob.getOutputPorts()) {
			if (sourceJobAdapter.getOutPortCell(op) == sourcePortCell) {
				sourcePort = op;
				break;
			}
		}

		// Add Connection to Workflow
		// This is done last, because it will trigger the typecheck,
		// which requires the ConnectionView to be created before
		view.getWorkflow().addConnection(connectionKey, sourceJob.bindings.get(sourcePort));
	}

	/**
	 * Constructor in case of loading a workflow
	 * 
	 * @param cc
	 * @param pm
	 * @param mwf
	 * @param view
	 */
	public ConnectionAdapter(ConnectionKey cc, PresentationModel pm, MutableWorkflow mwf, View view) {
		// System.out.println("loaded edge" + cc);
		this.connectionKey = cc;
		this.view = view;

		// find source and target JobCells
		Job sourceJob = mwf.getConnectionSource(cc).target;
		Job targetJob = cc.target;
		Port sourcePort = mwf.getConnectionSource(cc).targetPort;
		Port targetPort = cc.targetPort;
		JobAdapter sJA = null;
		JobAdapter tJA = null;
		for (JobAdapter jA : pm.getJobs()) {
			if (jA.getJob() == sourceJob)
				sJA = jA;
			if (jA.getJob() == targetJob)
				tJA = jA;
		}
		assert (sJA != null && tJA != null);
		OutPortCell source = sJA.getOutPortCell(sourcePort);
		InPortCell target = tJA.getInPortCell(targetPort);
		assert (source != null && target != null);

		visualization = new ConnectionCell(pm.getVisualization(), source, target);

		// Register at ConnectionView
		connectionViewListener = new ConnectionViewListener();
		AbstractView<?> connectionView = view.getConnectionView(connectionKey);
		connectionViewObserver = new Observer<ViewEvent<AbstractView<?>>>() {

			@Override
			public void notify(ViewEvent<AbstractView<?>> event) {
				event.doNotify(connectionViewListener);
			}

		};
		connectionView.getObservable().addObserver(connectionViewObserver);

		connectionCellListener = new ConnectionCellListener();
		connectionCellObserver = new Observer<CellEvent<Cell>>() {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(connectionCellListener);
			}

		};
		visualization.getObservable().addObserver(connectionCellObserver);
	}

	public void destroy(Graph graph) {
		if (visualization != null) {
			graph.removeCell(visualization);
		}
	}

}
