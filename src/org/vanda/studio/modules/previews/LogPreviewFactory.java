package org.vanda.studio.modules.previews;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vanda.studio.app.PreviewFactory;

public class LogPreviewFactory implements PreviewFactory {

	@Override
	public JComponent createPreview(String value) {
		List<LogEntry> entries = new ArrayList<LogEntry>();
		try {
			File file = new File(value);
			FileInputStream fis = new FileInputStream(file);
			fis.skip(Math.max(0, file.length() - 524288));
			BufferedReader input = new BufferedReader(new InputStreamReader(fis));
			try {
				String line = null;
				LogEntry entry = new LogEntry();
				entries.add(entry);
				while ((line = input.readLine()) != null) {
					if (line.startsWith("Checking: ")) {
						if (!"".equals(entry.text)) {
							entry = new LogEntry();
							entries.add(entry);
						}
					} else
						entry.appendLine(line);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			return new JLabel("Log does not exist.");
		}
		if (entries.isEmpty())
			return new JLabel("Log is empty.");
		Collections.reverse(entries);
		final JComboBox le = new JComboBox(entries.toArray());
		final JTextArea ta = new JTextArea(entries.get(0).getText());
		le.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ta.setText(((LogEntry) le.getSelectedItem()).getText());
			}
		});
		JPanel pan = new JPanel(new BorderLayout());
		pan.add(le, BorderLayout.NORTH);
		pan.add(new JScrollPane(ta), BorderLayout.CENTER);
		return pan;
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Runtime.getRuntime().exec("xdg-open " + value);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		t.start();
	}
	
	private class LogEntry {
		private String date = "<incomplete>";
		private int exitCode = -1;
		private String text = "";
		
		public void appendLine(String line) {
			text += line;
			text += System.getProperty("line.separator");
			String pattern = "[A-Z][a-z] \\d+\\. [A-Z][a-z][a-z] \\d\\d:\\d\\d:\\d\\d [A-Z]* \\d\\d\\d\\d";
			if (date.equals("<incomplete>") && line.matches(pattern)) {
				date = line;
			} else {
				if (line.startsWith("Skipping: ")) {
					exitCode = 0;
				} else if (line.startsWith("Returned: ")) {
					exitCode = Integer.parseInt(line.substring(10));
				}
			}
		};
		
		public String getText() {
			return text;
		}
		
		@Override
		public String toString() {
			return date;
		}
		
		public int getExitCode() {
			return exitCode;
		}
		
	}

}
