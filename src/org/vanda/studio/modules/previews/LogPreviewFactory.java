package org.vanda.studio.modules.previews;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vanda.studio.app.PreviewFactory;

public class LogPreviewFactory implements PreviewFactory {

	@Override
	public JComponent createPreview(String value) {
		List<LogEntry> entries = new ArrayList<LogEntry>();
		try {
			BufferedReader input = new BufferedReader(new FileReader(value));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					if (line.startsWith("Checking: "))
						entries.add(new LogEntry());
					else
						entries.get(entries.size() - 1).appendLine(line);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			return null;
		}
		if (entries.isEmpty())
			return null;
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
		private String date = null;
		private int exitCode = -1;
		private String text = "";
		
		public void appendLine(String line) {
			text += line;
			text += System.getProperty("line.separator");
			SimpleDateFormat sdf = new SimpleDateFormat("EE d. MMM HH:mm:ss zzzz yyyy");
			if (date == null) {
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
