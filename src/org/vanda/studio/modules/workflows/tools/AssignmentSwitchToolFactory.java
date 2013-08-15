package org.vanda.studio.modules.workflows.tools;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

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
		
		private static int ICON_SIZE = 24;
		
		private JTextField aName;
		JPanel pan;

		public JComponent getComponent() {
			return pan;
		}

		public EditAssignmentTool(WorkflowEditor wfe) {
			// Panel and basic Layout
			pan = new JPanel();
			GroupLayout layout = new GroupLayout(pan);
			pan.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			pan.setName("Assignment");
			final Database db = wfe.getDatabase();

			// Buttons & Textfield
			PreviousAssignmentAction prev = new PreviousAssignmentAction(db);
			wfe.addAction(prev, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK), 5);
			JButton prevButton = createNavigationButton(prev, "arrow-left", wfe.getApplication());
			
			
			NextAssignmentAction next = new NextAssignmentAction(db);
			wfe.addAction(next,KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), 6);
			JButton nextButton = createNavigationButton(next, "arrow-right", wfe.getApplication());
			
			aName = new JTextField();
			aName.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent arg0) {
					db.setName(aName.getText());
				}

				@Override
				public void focusGained(FocusEvent arg0) {
					// do nothing
				}
			});
			final Listener dl = new Listener();
			
			// init Label
			dl.cursorChange(db);
			
			db.getObservable().addObserver(new Observer<DatabaseEvent<Database>> () {

				@Override
				public void notify(DatabaseEvent<Database> event) {
					event.doNotify(dl);
				}
				
			});
			
			SequentialGroup horizontal = layout.createSequentialGroup().addComponent(prevButton)
					.addComponent(aName).addComponent(nextButton);
			ParallelGroup vertical = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(prevButton).addComponent(aName).addComponent(nextButton);

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
