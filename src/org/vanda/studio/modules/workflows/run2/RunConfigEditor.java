package org.vanda.studio.modules.workflows.run2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;

import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.JobVisitor;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

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
	private List<JCheckBox> assignmentCheckboxes;
	private Map<Integer, JSpinner> priorityMap;

	public JComponent getComponent() {
		return pan;
	}

	public RunConfigEditor(final MutableWorkflow mwf, Database db, String path, final Runner r) {
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
		JPanel tablePane = new JPanel();
		GroupLayout tableLayout = new GroupLayout(tablePane);
		tablePane.setLayout(tableLayout);
		assignmentSelection = new ArrayList<Integer>();
		priorityMap = new HashMap<Integer, JSpinner>();
		assignmentCheckboxes = new ArrayList<JCheckBox>();

		ParallelGroup leftColumn = tableLayout.createParallelGroup();
		ParallelGroup rightColumn = tableLayout.createParallelGroup();
		SequentialGroup tableRows = tableLayout.createSequentialGroup();

		// Table Head
		JLabel headerLeft = new JLabel("Assignment");
		JLabel headerRight = new JLabel("Priority");
		leftColumn.addComponent(headerLeft);
		rightColumn.addComponent(headerRight);
		tableRows.addGroup(tableLayout.createParallelGroup().addComponent(headerLeft).addComponent(headerRight));

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
			assignmentCheckboxes.add(assignment);
			boolean selectable = DatabaseValueChecker.checkDatabseRow(mwf, db.getRow(i));
			JSpinner priority = new JSpinner(new SpinnerNumberModel(i, 0, 1000, 1));

			if (!selectable) {
				assignment.setEnabled(false);
				priority.setEnabled(false);
			}

			priority.setMaximumSize(new Dimension(20, JSpinner.HEIGHT));
			priorityMap.put((Integer) i, priority);
			ParallelGroup row = tableLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(assignment).addComponent(priority);
			leftColumn.addComponent(assignment);
			rightColumn.addComponent(priority);
			tableRows.addGroup(row);
		}
		SequentialGroup tableColumns = tableLayout.createSequentialGroup().addGroup(leftColumn)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 50)
				.addGroup(rightColumn);

		tableLayout.setHorizontalGroup(tableColumns);
		tableLayout.setVerticalGroup(tableRows);

		tablePane.revalidate();
		tablePane.repaint();
		JScrollPane tableScrollPane = new JScrollPane(tablePane);

		// Select All / None Buttons
		JButton selectAllButton = new JButton(new AbstractAction("select all") {
			private static final long serialVersionUID = -7778511164140696020L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (JCheckBox acb : assignmentCheckboxes) {
					if (!acb.isSelected() && acb.isEnabled()) {
						acb.setSelected(true);
						acb.getAction().actionPerformed(null);
					}
				}

			}
		});

		JButton selectNoneButton = new JButton(new AbstractAction("clear selection") {
			private static final long serialVersionUID = 1004649182223613515L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (JCheckBox acb : assignmentCheckboxes) {
					if (acb.isSelected()) {
						acb.setSelected(false);
						acb.getAction().actionPerformed(null);
					}
				}

			}

		});
		SequentialGroup buttonsHori = layout.createSequentialGroup().addComponent(selectAllButton)
				.addComponent(selectNoneButton);
		ParallelGroup buttonsVert = layout.createParallelGroup().addComponent(selectAllButton)
				.addComponent(selectNoneButton);

		// ExecutionSystem Selection
		// TODO read out available Systems from somewhere
		JLabel exLabel = new JLabel("Execution System");
		JComboBox<String> exSystem = new JComboBox<String>();
		exLabel.setEnabled(false);
		exSystem.setMaximumSize(new Dimension(Short.MAX_VALUE, JComboBox.HEIGHT));
		exSystem.addItem("Shell Compiler");
		exSystem.setEnabled(false);
		JButton exButton = new JButton(new AbstractAction("Open Execution Preview") {
			private static final long serialVersionUID = 3626621817499179974L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Map<Pair<Job, Integer>, Integer> priorities = new HashMap<Pair<Job, Integer>, Integer>();
				for (Integer i : priorityMap.keySet()) {
					for (Job j : mwf.getChildren()) {
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
				.addComponent(tableScrollPane).addGroup(buttonsHori).addGroup(executionSystemHorizontal));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(executionEnvironmentVertical)
				.addComponent(tableScrollPane).addGroup(buttonsVert).addGroup(executionSystemVertical));

	}

	private static class DatabaseValueChecker {
		private static class JobTraverser implements JobVisitor {
			private boolean allLitsConnected = true;
			private final List<Literal> literals;
			private final List<Job> workingSet;
			private final List<Job> visited;
			private final MutableWorkflow mwf;
			
			public JobTraverser(List<Literal> literals, MutableWorkflow mwf, List<Job> workingSet, List<Job> visited) {
				this.literals = literals;
				this.mwf = mwf;
				this.workingSet = workingSet;
				this.visited = visited;
			}
			
			@Override
			public void visitTool(Job j, Tool t) {
				for (Port ip : j.getInputPorts()) {
					Location l = j.bindings.get(ip);
					ConnectionKey src = mwf.getVariableSource(l);
					if (src != null && ! visited.contains(src.target))
						workingSet.add(src.target);
					else 
						allLitsConnected = false;
				}
			}

			@Override
			public void visitLiteral(Job j, Literal l) {
				literals.add(l);
			}
			
			public boolean allLiteralsConnected() {
				return allLitsConnected;
			}
		}
		
		

		public static boolean checkDatabseRow(MutableWorkflow mwf, final HashMap<String, String> row) {
			boolean b = true;
			try {
				for (Literal l : detectConnectedLiterals(mwf)) {
					if (row.get(l.getKey()) == null || row.get(l.getKey()).equals(":"))
						b = false;
				}
			} catch (MissingInputsException e) {
				b = false;
			}
			return b;
		}

		public static List<Literal> detectConnectedLiterals(final MutableWorkflow mwf) throws MissingInputsException {
			final List<Literal> literals = new ArrayList<Literal>();
			final List<Job> workingSet = new ArrayList<Job>();
			final List<Job> visited = new ArrayList<Job> ();
			// add sink tools
			for (Job j : mwf.getChildren()) {
				j.visit(new JobVisitor() {

					@Override
					public void visitTool(Job j, Tool t) {
						if (t.getId().equals("SinkTool")) {
							workingSet.add(j);
						}
					}

					@Override
					public void visitLiteral(Job j, Literal l) {
						// do nothing
					}
				});
			}
			JobTraverser jv = new JobTraverser(literals, mwf, workingSet, visited); 
			
			while (!workingSet.isEmpty()) {
				Job j = workingSet.remove(workingSet.size() - 1);
				visited.add(j);
				j.visit(jv);
			}
			
			if (jv.allLiteralsConnected())
				return literals;
			else 
				throw new MissingInputsException();
		}
	}

	private static class MissingInputsException extends Exception {

		private static final long serialVersionUID = -964570440435994657L;

	}
}
