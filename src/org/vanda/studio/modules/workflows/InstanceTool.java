package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.modules.workflows.Model.JobSelection;
import org.vanda.studio.modules.workflows.Model.SingleObjectSelection;
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

		public void highlightSelectedInstance() {
			System.out.println(instanceList.getSelectedIndex());
			if (instanceList.getSelectedIndex() >= 0) {
				ImmutableWorkflow iwf = m.getUnfolded().get(instanceList.getSelectedIndex());
				List<SingleObjectSelection> elements = new ArrayList<SingleObjectSelection>();
				
				List<Token> path = new ArrayList<Token>();
				for (JobInfo ji : iwf.getChildren()) {
					elements.add(new JobSelection(path, ji.job.getAddress()));
				}
				//TODO nesting... path calculation
				
				m.setMarkedElements(elements);
			}
		}
		
		public void update(Model model) {
			listmodel.clear();
			instanceList.setModel(listmodel);
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
