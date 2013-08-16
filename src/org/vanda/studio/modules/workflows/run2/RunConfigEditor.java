package org.vanda.studio.modules.workflows.run2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;

import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;

/**
 * Dialogue to select Run-Directory, Assignments, Run-System
 * 
 * @author kgebhardt
 * 
 */
public class RunConfigEditor {
	public interface Runner {
		public void evokeExecution(List<Integer> assignmentSelection, String filePath,
				Map<Pair<Job, Integer>, Integer> prioMap);
	}

	private JPanel pan;
	private JTextField tFolder;
	private JLabel lFolder;
	public File dir;
	private List<Integer> assignmentSelection;
	private Map<Integer, JSpinner> priorityMap;

	public JComponent getComponent() {
		return pan;
	}

	public RunConfigEditor(final Collection<Job> jobs, Database db, String path, final Runner r) {
		// Panel and basic Layout
		pan = new JPanel();
		GroupLayout layout = new GroupLayout(pan);
		pan.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		// Execution Environment Folder Selection
		dir = new File(path);
		lFolder = new JLabel("Execution Environment");
		tFolder = new JTextField(dir.getAbsolutePath());
		tFolder.setMaximumSize(new Dimension(Short.MAX_VALUE, JTextField.HEIGHT));
		tFolder.setPreferredSize(new Dimension(200, 20));
		JButton bFolder = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = -2965900290520148139L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(new File(tFolder.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					tFolder.setText(file.getAbsolutePath());
				}
			}
		});
		bFolder.setMaximumSize(new Dimension(bFolder.getPreferredSize().width, JTextField.HEIGHT));

		SequentialGroup exexutionEnvironmentHorizontal = layout.createSequentialGroup().addComponent(lFolder)
				.addComponent(tFolder).addComponent(bFolder);
		ParallelGroup executionEnvironmentVertical = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lFolder).addComponent(tFolder).addComponent(bFolder);

		// AssignmentSelectionTable
		assignmentSelection = new ArrayList<Integer>();
		priorityMap = new HashMap<Integer, JSpinner>();

		ParallelGroup leftColumn = layout.createParallelGroup();
		ParallelGroup rightColumn = layout.createParallelGroup();
		SequentialGroup tableRows = layout.createSequentialGroup();

		// Table Head
		JLabel headerLeft = new JLabel("Assignment");
		JLabel headerRight = new JLabel("Priority");
		leftColumn.addComponent(headerLeft);
		rightColumn.addComponent(headerRight);
		tableRows.addGroup(layout.createParallelGroup().addComponent(headerLeft).addComponent(headerRight));

		// Table Content
		// TODO remember previous selections and priorities
		for (int i = 0; i < db.getSize(); ++i) {
			final Integer a_i = new Integer(i);
			String name = db.getName(i);
			JCheckBox assignment = new JCheckBox(new AbstractAction(name != null ? name : "") {
				private static final long serialVersionUID = 1827258959703699422L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (assignmentSelection.contains(a_i))
						assignmentSelection.remove(a_i);
					else
						assignmentSelection.add(a_i);
				}
			});
			boolean selectable = DatabaseValueChecker.checkDatabseRow(jobs, db.getRow(i));
			JSpinner priority = new JSpinner(new SpinnerNumberModel(i, 0, 1000, 1));
			
			if (!selectable) {
				assignment.setEnabled(false);
				priority.setEnabled(false);
			}
			
			priority.setMaximumSize(new Dimension(20, JSpinner.HEIGHT));
			priorityMap.put((Integer) i, priority);
			ParallelGroup row = layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(assignment)
					.addComponent(priority);
			leftColumn.addComponent(assignment);
			rightColumn.addComponent(priority);
			tableRows.addGroup(row);
		}
		SequentialGroup tableColumns = layout.createSequentialGroup().addGroup(leftColumn)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 50)
				.addGroup(rightColumn);

		// ExecutionSystem Selection
		// TODO read out available Systems from somewhere
		JLabel exLabel = new JLabel("Execution System");
		JComboBox<String> exSystem = new JComboBox<String>();
		exSystem.setMaximumSize(new Dimension(Short.MAX_VALUE, JComboBox.HEIGHT));
		exSystem.addItem("Shell Compiler");
		JButton exButton = new JButton(new AbstractAction("Run") {
			private static final long serialVersionUID = 3626621817499179974L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Map<Pair<Job, Integer>, Integer> priorities = new HashMap<Pair<Job, Integer>, Integer>();
				for (Integer i : priorityMap.keySet()) {
					for (Job j : jobs) {
						priorities.put(new Pair<Job, Integer>(j, i), (Integer) priorityMap.get(i).getValue());
					}
				}
				Collections.sort(assignmentSelection);
				r.evokeExecution(assignmentSelection, dir.getAbsolutePath(), priorities);
			}
		});

		SequentialGroup executionSystemHorizontal = layout.createSequentialGroup().addComponent(exLabel)
				.addComponent(exSystem).addComponent(exButton);
		ParallelGroup executionSystemVertical = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(exLabel).addComponent(exSystem).addComponent(exButton);

		// Setup entire layout
		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(exexutionEnvironmentHorizontal)
				.addGroup(tableColumns).addGroup(executionSystemHorizontal));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(executionEnvironmentVertical)
				.addGroup(tableRows).addGroup(executionSystemVertical));

	}
	
	private static class DatabaseValueChecker {
		private static class LiteralVisitor implements ElementVisitor {
			private final Map<String, String> row;
			private boolean b = true;
			public LiteralVisitor(Map<String, String> row) {
				this.row = row;
			}
			
			@Override
			public void visitLiteral(Literal l) {
				if (row.get(l.getKey()) == null || row.get(l.getKey()).equals(":"))
					b = false;
			}

			@Override
			public void visitTool(Tool t) {
				// do nothing
			}
			
			public boolean getValue() {
				return b;
			}

		}
		public static boolean checkDatabseRow(Collection<Job> jobs, final HashMap<String, String> row) {
			LiteralVisitor v = new LiteralVisitor(row);
			for (Job j : jobs) {
				j.visit(v);
			}
			return v.getValue();
		}
	}
}
