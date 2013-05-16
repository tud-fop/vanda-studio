package org.vanda.studio.modules.previews;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.util.ExceptionMessage;

public class ScoresPreviewFactory implements PreviewFactory {

	private Application app;
	
	public ScoresPreviewFactory(Application app) {
		super();
		this.app = app;
	}
	
	@Override
	public JComponent createPreview(String absolutePath) {
		File scores = new File(absolutePath);
		File meta = new File(absolutePath + ".meta");
		Scanner sScores = null, sMeta = null, sSentences = null;
		try {
			// read stuff from file
			sScores = new Scanner(scores);
			sMeta = new Scanner(meta);
			File sentences = new File(app.findFile(sMeta.nextLine()));
			sMeta.close();
			sSentences = new Scanner(sentences);
			List<String> left = new ArrayList<String>();
			List<String> right = new ArrayList<String>();
			while (sScores.hasNextLine() && sSentences.hasNextLine()) {
				left.add(sSentences.nextLine());
				right.add(sScores.nextLine());
			}
			sScores.close();
			sSentences.close();
			
			// write to array
			String[][] data = new String[left.size()][2];
			for (int i = 0; i < left.size(); i++) {
				data[i][0] = left.get(i);
				data[i][1] = right.get(i);
			}
			// build GUI
			JTable jTable = new JTable(data, new String[] {"element", "score"});
			JComponent result = new JScrollPane(jTable);
			return result;
		} catch (FileNotFoundException e) {
			app.sendMessage(new ExceptionMessage(e));
			return app.getPreviewFactory(null).createPreview(absolutePath);
		} finally {
			if (sScores != null)
				sScores.close();
			if (sMeta != null)
				sMeta.close();
			if (sSentences != null)
				sSentences.close();
		}
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		return createPreview(absolutePath);
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Runtime.getRuntime().exec("xdg-open " + value);
				} catch (IOException e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
		});

		t.start();
	}

}
