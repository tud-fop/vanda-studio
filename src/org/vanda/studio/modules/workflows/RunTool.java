package org.vanda.studio.modules.workflows;

import java.awt.Color;
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
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
		private final JTextPane tRuntool;
		private final JScrollPane sRuntool;
		private final JPanel pMain;
		private final JButton bClear, bCancel;
		private final JComboBox lRuns;
		private static final SimpleAttributeSet messageStyle, errorStyle,
				infoStyle;

		static {

			infoStyle = new SimpleAttributeSet();
			messageStyle = new SimpleAttributeSet();
			errorStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(errorStyle, Color.RED);
			StyleConstants.setForeground(infoStyle, Color.BLUE);

		}

		public Tool(WorkflowEditor wfe, Model m) {
			this.wfe = wfe;
			this.m = m;
			app = wfe.getApplication();
			prof = app.getProfileMetaRepository().getRepository()
					.getItem("fragment-profile");
			if (prof != null) {
				wfe.addAction(new GenerateAction(), KeyStroke.getKeyStroke(
						KeyEvent.VK_G, KeyEvent.CTRL_MASK));
				wfe.addAction(new RunAction(), KeyStroke.getKeyStroke(
						KeyEvent.VK_R, KeyEvent.CTRL_MASK));
			}
			runs = new ArrayList<Run>();

			tRuntool = new JTextPane();
			tRuntool.setEditable(false);
			sRuntool = new JScrollPane(tRuntool);
			DefaultCaret caret = (DefaultCaret) tRuntool.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

			pMain = new JPanel();
			pMain.setName("Console");
			pMain.setLayout(new GridBagLayout());

			bClear = new JButton(new CloseAction());
			bClear.setText("close");
			bCancel = new JButton(new CancelAction());
			bCancel.setText("cancel");

			lRuns = new JComboBox();
			lRuns.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					Run r = (Run) lRuns.getSelectedItem();
					if (r != null) {
						Document doc = ((Run) lRuns.getSelectedItem())
								.getDocument();
						tRuntool.setDocument(doc);
						tRuntool.setCaretPosition(doc.getLength());
					}
				}
			});

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;

			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;

			gbc.gridx = 1;
			gbc.gridy = 0;
			pMain.add(bCancel, gbc);

			gbc.gridx = 2;
			gbc.gridy = 0;
			pMain.add(bClear, gbc);

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			pMain.add(lRuns, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weighty = 1;
			gbc.gridwidth = 3;
			pMain.add(sRuntool, gbc);

			this.wfe.addToolWindow(pMain);
		}

		private static class StreamGobbler extends Thread {
			private final Application app;
			private final InputStream is;
			private final SimpleAttributeSet style;
			private final StyledDocument doc;

			public StreamGobbler(InputStream is, SimpleAttributeSet style,
					StyledDocument doc, Application app) {
				this.is = is;
				this.style = style;
				this.doc = doc;
				this.app = app;
			}

			public void run() {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				try {
					while ((line = br.readLine()) != null) {
						synchronized (doc) {
							try {
								if (line.startsWith("Running:")
										| line.startsWith("Done."))
									doc.insertString(doc.getLength(), line
											+ "\n", infoStyle);
								else
									doc.insertString(doc.getLength(), line
											+ "\n", style);
							} catch (BadLocationException e) {
								// ignore
							}
						}
					}
				} catch (IOException e) {
					// ignore
				}
			}
		}

		private static interface RunTransitions {
			void doCancel();

			void doFinish();

			void doRun();
		}

		private static class RunState {

			void cancel(RunTransitions rt) {

			}

			void finish(RunTransitions rt) {

			}

			void run(RunTransitions rt) {

			}

			String getString(Date date) {
				return date.toString();
			}
		}

		private static class StateInit extends RunState {

			void run(RunTransitions rt) {
				rt.doRun();
			}

			String getString(Date date) {
				return "[I] " + date.toString();
			}
		}

		private static class StateRunning extends RunState {
			private final Application app;
			private Process process;
			private StreamGobbler isg;
			private StreamGobbler esg;

			public StateRunning(Application app, Fragment f, StyledDocument doc) {
				this.app = app;
				try {
					process = Runtime.getRuntime().exec(
							RCChecker.getOutPath() + "/"
									+ Fragment.normalize(f.name), null, null);

				} catch (Exception e) {
					app.sendMessage(new ExceptionMessage(e));
				}

				InputStream stdin = process.getInputStream();
				InputStream stderr = process.getErrorStream();
				isg = new StreamGobbler(stdin, messageStyle, doc, app);
				esg = new StreamGobbler(stderr, errorStyle, doc, app);
				isg.start();
				esg.start();
			}

			@Override
			void cancel(RunTransitions rt) {
				process.destroy();
				process = null;
				rt.doCancel();
			}

			@Override
			void finish(RunTransitions rt) {
				int i = 0;
				try {
					i = process.waitFor();
				} catch (Exception e) {
					// ignore
				}
				if (i == 0)
					rt.doFinish();
				else
					rt.doCancel();
			}

			String getString(Date date) {
				return "[R] " + date.toString();
			}
		}

		private static class StateCancelled extends RunState {
			String getString(Date date) {
				return "[C] " + date.toString();
			}
		}

		private static class StateDone extends RunState {
			String getString(Date date) {
				return "[D] " + date.toString();
			}
		}

		private final class Run extends SwingWorker<String, String> implements
				RunTransitions {
			private StyledDocument doc;
			private Date date;
			private Fragment frag;
			private RunState state = new StateInit();

			public Run(Fragment fragment) {
				doc = new DefaultStyledDocument();
				doc.addDocumentListener(new DocumentListener() {

					@Override
					public void insertUpdate(DocumentEvent e) {
						if (lRuns.getSelectedItem() == Run.this) {
							tRuntool.setCaretPosition(tRuntool.getText()
									.length());
							// pMain.revalidate();
						}
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub

					}

				});
				date = new Date();
				frag = fragment;
				try {
					doc.insertString(doc.getLength(), date.toString() + "\n",
							messageStyle);
				} catch (BadLocationException e1) {
					// ignore
				}
			}

			public String toString() {
				return state.getString(date);
			}

			public StyledDocument getDocument() {
				return doc;
			}

			@Override
			public void doCancel() {
				state = new StateCancelled();
				lRuns.repaint();
				pMain.revalidate();
			}

			public void cancel() {
				super.cancel(true);
				state.cancel(this);
			}

			@Override
			public void doFinish() {
				state = new StateDone();
				lRuns.repaint();
				pMain.revalidate();
			}

			@Override
			public void doRun() {
				frag = generate();
				state = new StateRunning(app, frag, doc);
				lRuns.repaint();
				pMain.revalidate();
				state.finish(this);
			}

			@Override
			protected String doInBackground() {
				state.run(this);
				return null;
			}

		}

		private final class CloseAction extends AbstractAction {

			@Override
			public void actionPerformed(ActionEvent e) {
				Run r = (Run) lRuns.getSelectedItem();
				r.cancel();
				runs.remove(r);
				lRuns.setModel(new DefaultComboBoxModel(runs.toArray()));
				if (runs.size() > 0)
					lRuns.setSelectedIndex(lRuns.getItemCount() - 1);
				else
					tRuntool.setText("");

			}

		}

		private final class CancelAction extends AbstractAction {

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
				if (frag != null) {
					Run r = new Run(frag);
					runs.add(r);
					r.execute();
					lRuns.setModel(new DefaultComboBoxModel(runs.toArray()));
					wfe.focusToolWindow(pMain);
					lRuns.setSelectedIndex(lRuns.getItemCount() - 1);
					tRuntool.setDocument(r.getDocument());
				}
			}

		}

		private Fragment generate() {
			try {
				m.checkWorkflow();
			} catch (Exception e1) {
				app.sendMessage(new ExceptionMessage(e1));
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
