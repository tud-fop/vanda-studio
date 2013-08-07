package org.vanda.studio.modules.workflows.tools;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.workflows.data.Database;

public class AssignmentSwitchToolFactory implements ToolFactory {
	private class PreviousAssignmentAction implements Action {
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

	private class NextAssignmentAction implements Action {
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

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		Action prev = new PreviousAssignmentAction(wfe.getDatabase());
		wfe.addAction(prev, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK));
		Action next = new NextAssignmentAction(wfe.getDatabase());
		wfe.addAction(next, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_MASK));
		return null;
	}

}
