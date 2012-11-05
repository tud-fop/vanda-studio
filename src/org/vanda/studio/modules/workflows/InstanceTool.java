package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.app.WorkflowEditor;
import org.vanda.studio.model.Model;
import org.vanda.studio.model.Model.ConnectionSelection;
import org.vanda.studio.model.Model.JobSelection;
import org.vanda.studio.model.Model.SingleObjectSelection;
import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public class InstanceTool implements ToolFactory {

	private static final class Tool {
		private final WorkflowEditor wfe;
		private final Model m;
		private JList instanceList;
		private final JScrollPane scrollPane;
		private DefaultListModel listmodel;

		public Tool(WorkflowEditor wfe, Model m) {
			this.wfe = wfe;
			this.instanceList = new JList();
			this.m = m;
			this.m.getWorkflowCheckObservable().addObserver(
					new Observer<Model>() {
						@Override
						public void notify(Model event) {
							update(event);
						}
					});
			instanceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			instanceList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					highlightSelectedInstance();
				}
			});
			listmodel = new DefaultListModel();
			scrollPane = new JScrollPane(instanceList);
			scrollPane.setName("Workflow Instances");
			this.wfe.addToolWindow(scrollPane);
		}

		public List<SingleObjectSelection> retrieveWorkflowElements(
				MutableWorkflow mwf, ImmutableWorkflow iwf, List<Token> path) {

			List<SingleObjectSelection> elements = new ArrayList<SingleObjectSelection>();

			// // add all child nodes recursively to the SingleObjectSelection
			// // list
			// for (JobInfo ji : iwf.getChildren()) {
			// elements.add(new JobSelection(path, ji.job.getAddress()));
			//
			// if (ji.job instanceof CompositeImmutableJob) {
			// List<Token> newPath = new ArrayList<Token>(path);
			// Token currentAddress = ji.job.getAddress();
			// newPath.add(currentAddress);
			// assert (mwf.getChild(currentAddress) instanceof CompositeJob);
			// MutableWorkflow newMwf = ((CompositeJob) mwf
			// .getChild(currentAddress)).getWorkflow();
			// elements.addAll(retrieveWorkflowElements(newMwf,
			// ((CompositeImmutableJob) ji.job).getWorkflow(),
			// newPath));
			// }
			// }
			//
			// // add all connections to SingleObjectSelection list
			// for (Connection conn : mwf.getConnections()) {
			// boolean sourceFound = false;
			// boolean targetFound = false;
			// for (JobInfo info : iwf.getChildren()) {
			// if (info.job.getAddress().equals(conn.source))
			// sourceFound = true;
			// if (info.job.getAddress().equals(conn.target)
			// && info.inputs.get(conn.targetPort) != null) {
			// // int i =
			// // info.job.getInputPorts().indexOf(conn.targetPort);
			// // if (i != -1 && info.inputs.get(i) != null)
			// targetFound = true;
			// }
			// }
			// if (sourceFound && targetFound)
			// elements.add(new ConnectionSelection(path, conn.address));
			// }

			return elements;
		}

		public void highlightSelectedInstance() {
			if (instanceList.getSelectedIndex() > 0) {
				ImmutableWorkflow iwf = m.getUnfolded().get(
						instanceList.getSelectedIndex() - 1);
				assert (iwf.toString().equals(instanceList.getSelectedValue()));
				m.setMarkedElements(retrieveWorkflowElements(m.getRoot(), iwf,
						new ArrayList<Token>()));
			} else {
				// allow de-selection of instance and remove marks from elements
				m.setMarkedElements(new ArrayList<SingleObjectSelection>());
			}
		}

		public void update(Model model) {
			listmodel.clear();
			instanceList.setModel(listmodel);
			listmodel.add(0, "(all instances)");
			for (ImmutableWorkflow iwf : model.getUnfolded()) {
				listmodel.add(listmodel.getSize(), iwf.toString());
			}
		}
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model m) {
		return new Tool(wfe, m);
	}

}
