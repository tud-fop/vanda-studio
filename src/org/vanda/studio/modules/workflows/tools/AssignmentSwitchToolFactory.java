package org.vanda.studio.modules.workflows.tools;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.SwingUtilities;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;

public class AssignmentSwitchToolFactory implements ToolFactory {
	private static class PreviousAssignmentAction implements Action {
		private final Database db;

		public PreviousAssignmentAction(Database db) {
			this.db = db;
		}

		@Override
		public String getName() {
			return "Previous Assignment";
		}

		@Override
		public void invoke() {
			if (db.hasPrev())
				db.prev();
		}
	}

	private static class NextAssignmentAction implements Action {
		public final Database db;

		public NextAssignmentAction(Database db) {
			this.db = db;
		}

		@Override
		public String getName() {
			return "Next Assignment";
		}

		@Override
		public void invoke() {
			if (db.hasNext())
				db.next();
		}
	}

	private static class EditAssignmentTool {
		private class Listener implements DatabaseListener<Database> {

			@Override
			public void cursorChange(Database d) {
				aName.setText(d.getName());
				label.setText(d.getCursor() + 1 + " / " + d.getSize());
				// enable / disable prev/next-Buttons
				if (d.getCursor() == 0)
					prevButton.setEnabled(false);
				else
					prevButton.setEnabled(true);

				if (d.getCursor() == d.getSize() - 1)
					nextButton.setEnabled(false);
				else
					nextButton.setEnabled(true);

			}

			@Override
			public void dataChange(Database d, Object key) {
				// do nothing
			}

			@Override
			public void nameChange(Database d) {
				aName.setText(d.getName());
			}

		}

		private static int ICON_SIZE = 18;

		private JTextField aName;
		private JPanel pan;
		private final JButton prevButton, nextButton;
		private final JLabel label;

		public JComponent getComponent() {
			return pan;
		}

		public EditAssignmentTool(WorkflowEditor wfe) {
			// Panel and basic Layout
			pan = new JPanel() {
				private static final long serialVersionUID = -5904333485102276761L;

				@Override
				public Dimension getPreferredSize() {
					Dimension d = super.getPreferredSize();
					d.width = 250;
					return d;
				}
			};
			GroupLayout layout = new GroupLayout(pan);
			pan.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			pan.setName("Assignment");
			final Database db = wfe.getDatabase();

			// Buttons & Textfield
			PreviousAssignmentAction prev = new PreviousAssignmentAction(db);
			wfe.addAction(prev, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK), 5);
			prevButton = createNavigationButton(prev, "arrow-left", wfe.getApplication());

			NextAssignmentAction next = new NextAssignmentAction(db);
			wfe.addAction(next, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), 6);
			nextButton = createNavigationButton(next, "arrow-right", wfe.getApplication());

			aName = new JTextField() {
				private static final long serialVersionUID = 3301633459959703449L;

				@Override
				public Dimension getPreferredSize() {
					Dimension d = super.getPreferredSize();
					d.height = nextButton.getSize().height;
					return d;
				}
			};
			aName.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					db.setName(aName.getText());
				}
			});
			// unfocus text-field when clicking in the surrounding panel
			pan.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							pan.requestFocusInWindow();						
						}
					});
					
				}
			});

			label = new JLabel();

			final Listener dl = new Listener();

			// init Label
			dl.cursorChange(db);

			db.getObservable().addObserver(new Observer<DatabaseEvent<Database>>() {

				@Override
				public void notify(DatabaseEvent<Database> event) {
					event.doNotify(dl);
				}

			});

			SequentialGroup horizontal = layout.createSequentialGroup().addComponent(prevButton).addComponent(aName)
					.addComponent(label).addComponent(nextButton);
			ParallelGroup vertical = layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(prevButton)
					.addComponent(aName).addComponent(label).addComponent(nextButton);

			layout.setHorizontalGroup(horizontal);
			layout.setVerticalGroup(vertical);

			// add Tool Component
			wfe.addToolWindow(getComponent(), WindowSystem.EAST);

		}

		private JButton createNavigationButton(final Action a, String imageName, Application app) {
			URL url = ClassLoader.getSystemClassLoader().getResource(imageName + ".png");
			JButton b = new JButton();
			b.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					a.invoke();
				}
			});
			try {
				Image i = ImageIO.read(url);
				b.setIcon(new ImageIcon(i.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH)));
				b.setPreferredSize(new Dimension(ICON_SIZE + 2, ICON_SIZE + 2));
				b.setMargin(new Insets(0, 0, 0, 0));
				b.setToolTipText(a.getName());
			} catch (IOException e1) {
				app.sendMessage(new ExceptionMessage(e1));
				b.setText(a.getName());
			}
			return b;
		}

	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new EditAssignmentTool(wfe);
	}

}
