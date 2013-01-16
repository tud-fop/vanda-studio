import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;


public class DictViewTest {

	
	public static void invocationPoint(String fileName) {
		try {
			LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
			boolean nimbusPresent = false;
			for (LookAndFeelInfo info : infos) {
				if("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					nimbusPresent = true;
					break;
				}
			}
			if (!nimbusPresent)
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {}
		
		
		JFrame frame = new JFrame(fileName);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			frame.setContentPane(new DictView(fileName, '\t', false));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		frame.setSize(1000,1000);
		frame.setVisible(true);
	}
	
	private static String fileName;
	
	public static void main(String[] args) {
		System.out.println(args[0]);
		fileName = args[0];
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				invocationPoint(fileName);
			}
		});
	}
}
