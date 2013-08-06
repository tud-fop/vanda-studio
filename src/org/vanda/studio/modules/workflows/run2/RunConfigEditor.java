package org.vanda.studio.modules.workflows.run2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.SpinnerNumberModel;

import org.vanda.workflows.data.Database;

/**
 * Dialogue to select Run-Directory, Assignments, Run-System  
 * @author kgebhardt
 *
 */
public class RunConfigEditor {
	public interface ExecutionLogic {
		public void evokeExecution(List<Integer> assignmentSelection, String filePath);
	}
	
	private JPanel pan;
	private JTextField tFolder;
	private JLabel lFolder;
	public File dir;
	List<Integer> assignmentSelection;

	public JComponent getComponent() {
		return pan;
	}

	public RunConfigEditor(Database db, String path, final ExecutionLogic el) {
		// Panel and basic Layout
		pan = new JPanel();
		GroupLayout layout = new GroupLayout(pan);
		pan.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		
		// Execution Environment Folder Selection
		// TODO offer nice default path and remember last path
		dir = new File(path);
		lFolder = new JLabel("Execution Environment");
		tFolder = new JTextField(dir.getAbsolutePath());
		tFolder.setPreferredSize(new Dimension(300, 20));
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
		
		SequentialGroup exexutionEnvironmentHorizontal = layout.createSequentialGroup()
				.addComponent(lFolder)
				.addComponent(tFolder)
				.addComponent(bFolder); 
		ParallelGroup executionEnvironmentVertical = layout.createParallelGroup()
				.addComponent(lFolder)
				.addComponent(tFolder)
				.addComponent(bFolder);
				
		// AssignmentSelectionTable
		assignmentSelection = new ArrayList<Integer>();

		
		ParallelGroup leftColumn = layout.createParallelGroup();
		ParallelGroup rightColumn = layout.createParallelGroup();
		SequentialGroup tableRows = layout.createSequentialGroup();
		
		// Table Head	
		JLabel headerLeft = new JLabel("Assignment");
		JLabel headerRight = new JLabel("Priority");
		leftColumn.addComponent(headerLeft);
		rightColumn.addComponent(headerRight);
		tableRows.addGroup(
				layout.createParallelGroup()
					.addComponent(headerLeft)
					.addComponent(headerRight)
				);
		
		// Table Content
		// TODO  remember previous selections and priorities
		for ( int i = 0; i < db.getSize(); ++i ) {
			final Integer a_i = new Integer(i);
			JCheckBox assignment = new JCheckBox(new AbstractAction("Assignment " + a_i) {
				private static final long serialVersionUID = 1827258959703699422L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (assignmentSelection.contains(a_i))
						assignmentSelection.remove(a_i);
					else 
						assignmentSelection.add(a_i);
				}
			});
			JSpinner  priority   = new JSpinner(new SpinnerNumberModel(i, 0, 1000, 1));
			ParallelGroup row = layout.createParallelGroup()
					.addComponent(assignment)
					.addComponent(priority);
			leftColumn.addComponent(assignment);
			rightColumn.addComponent(priority);
			tableRows.addGroup(row);
		}
		SequentialGroup tableColumns = layout.createSequentialGroup()
				.addGroup(leftColumn)
				.addGroup(rightColumn);
		
		
		
		// ExecutionSystem Selection
		// TODO read out available Systems from somewhere
		JLabel exLabel = new JLabel("Execution System");
		JComboBox<String> exSystem = new JComboBox<String>();
		exSystem.addItem("Shell Compiler");
		JButton exButton = new JButton(new AbstractAction("Run") {
			private static final long serialVersionUID = 3626621817499179974L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				el.evokeExecution(assignmentSelection, dir.getAbsolutePath());
			}
		});
		
		SequentialGroup executionSystemHorizontal = layout.createSequentialGroup()
				.addComponent(exLabel)
				.addComponent(exSystem)
				.addComponent(exButton);
		ParallelGroup executionSystemVertical = layout.createParallelGroup()
				.addComponent(exLabel)
				.addComponent(exSystem)
				.addComponent(exButton);
				

		// Setup entire layout
		layout.setHorizontalGroup(
				layout.createParallelGroup()
					.addGroup(exexutionEnvironmentHorizontal)
					.addGroup(tableColumns)
					.addGroup(executionSystemHorizontal)
					);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(executionEnvironmentVertical)
					.addGroup(tableRows)
					.addGroup(executionSystemVertical)
					);

	}
}
