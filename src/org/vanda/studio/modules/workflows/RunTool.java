package org.vanda.studio.modules.workflows;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Profile;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.ExceptionMessage;
import org.vanda.studio.util.RCChecker;

public class RunTool implements ToolFactory {

	private static final class Tool {
		private final WorkflowEditor wfe;
		private final Model m;
		private final Application app;
		private Profile prof;
		private Fragment frag;
		private final List<Run> runs;
		private final JTextArea tRuntool;
		private final JScrollPane sRuntool;
		private final JPanel pMain;
		private final JButton bClear, bCancel;
		private final JComboBox lRuns;

		public Tool(WorkflowEditor wfe, Model m) {
			this.wfe = wfe;
			this.m = m;
			app = wfe.getApplication();
			prof = app.getProfileMetaRepository().getRepository()
					.getItem("fragment-profile");
			if (prof != null){
				wfe.addAction(new GenerateAction(), KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
				wfe.addAction(new RunAction(), KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
			}
			runs = new ArrayList<Run>();
			
			tRuntool = new JTextArea();
			sRuntool = new JScrollPane(tRuntool);
			DefaultCaret caret = (DefaultCaret)tRuntool.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			
			pMain = new JPanel();
			pMain.setName("Run");
			pMain.setLayout(new GridBagLayout());
			
			bClear = new JButton(new ClearAction());
			bClear.setText("clear");
			bCancel = new JButton(new CancelAction());
			bCancel.setText("cancel");
			
			lRuns = new JComboBox();
			lRuns.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					Run r = (Run) lRuns.getSelectedItem();
					if (r != null) {
						// String txt = r.getText();
						tRuntool.setText(((Run) lRuns.getSelectedItem()).getText());
					}
				}
			});
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			
			gbc.weightx = 0; gbc.weighty = 0;
			gbc.gridwidth = 1; gbc.gridheight = 1;
			
			gbc.gridx = 1; gbc.gridy = 0;
			pMain.add(bCancel, gbc);
			
			gbc.gridx = 2; gbc.gridy = 0;
			pMain.add(bClear, gbc);

			gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
			pMain.add(lRuns, gbc);
			
			gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 1; gbc.gridwidth = 3;
			pMain.add(sRuntool, gbc);
					
			this.wfe.addToolWindow(pMain);
		}
		
		private final class Run {
			private String text = "";
			private Date date;
			private Fragment frag;
			private SwingWorker<String, String> sw;
			private boolean running = true, finished = false;
			
			public Run(Fragment fragment) {
				date = new Date();
				frag = fragment;
				this.sw = new SwingWorker<String, String>() {

					@Override
					protected String doInBackground() throws Exception {
						runFragment();
						return null;
					}
					
				};
				append(date.toString());
				sw.execute();
			}
			
			public String toString() {
				if (running)
					return "[R] " + date.toString();
				if (finished)
					return "[D] " + date.toString();
				return "[C] " + date.toString();
			}
			
			public String getText() {
				return text;
			}
			
			public void append(String line) {
				text += line + "\n";
				if (lRuns.getSelectedItem() == this){
					tRuntool.setText(getText());
					pMain.revalidate();
				}
			}
			
			public void cancel() {
				sw.cancel(true);
				running = false;
				lRuns.setModel(new DefaultComboBoxModel(runs.toArray()));
				lRuns.revalidate();
				pMain.revalidate();
			}
			
			public void finish() {
				finished = true;
				running = false;
				lRuns.setModel(new DefaultComboBoxModel(runs.toArray()));
				lRuns.revalidate();
				pMain.revalidate();
			}
			
			private void runFragment() {
				frag = generate();
				String fileName = Fragment.normalize(frag.name);
				Runtime rt = Runtime.getRuntime();
				try {
					Process p = rt.exec(RCChecker.getOutPath() + "/" + fileName, null, null);
					
					InputStream stdin = p.getInputStream();
					InputStreamReader isr = new InputStreamReader(stdin);
					BufferedReader br = new BufferedReader(isr);

					String line = null;
					while ( (line = br.readLine()) != null){
					     append(line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finish();
			}
			
		}
		
		private final class ClearAction extends AbstractAction {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				List<Run> runs2 = new ArrayList<Run>();
				for (Run r : runs){
					if (r.running)
						runs2.add(r);
				}
				runs.clear();
				runs.addAll(runs2);
				lRuns.setModel(new DefaultComboBoxModel(runs.toArray()));
				if (runs.size() > 0)
					lRuns.setSelectedIndex(runs.size() - 1);
				else
					tRuntool.setText("");
				
			}
			
		}
		
		private final class CancelAction extends AbstractAction {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				((Run) lRuns.getSelectedItem()).cancel();
				pMain.revalidate();
			}
			
		}

		private final class GenerateAction implements Action {

			@Override
			public String getName() {
				return "Generate";
			}

			@Override
			public void invoke() {
				frag = generate();
			}

		}
		
		private final class RunAction implements Action {

			@Override
			public String getName() {
				return "Run";
			}

			@Override
			public void invoke() {
				frag = generate();
				if (frag != null){
					Run r = new Run(generate());
					runs.add(r);
					lRuns.setModel(new DefaultComboBoxModel(runs.toArray()));
					lRuns.setSelectedItem(r);
					pMain.revalidate();
				}
			}

		}

		private Fragment generate() {
			try {
				m.checkWorkflow();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			List<ImmutableWorkflow> unfolded = m.getUnfolded();
			if (unfolded != null
					&& unfolded.size() != 0
					&& Types.canUnify(unfolded.get(0).getFragmentType(),
							prof.getRootType())) {
				ImmutableWorkflow root = new ImmutableWorkflow(m.getRoot()
						.getName(), unfolded);
				try {
					return prof.createGenerator().generate(root);
				} catch (IOException e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
			return null;
		}
		
		
		
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model m) {
		return new Tool(wfe, m);
	}

}
