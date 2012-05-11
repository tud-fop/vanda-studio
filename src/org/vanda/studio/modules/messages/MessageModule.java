package org.vanda.studio.modules.messages;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.Message;
import org.vanda.studio.util.Observer;

public class MessageModule implements Module {

	@Override
	public String getName() {
		return "Messages Module for Vanda Studio";
	}

	@Override
	public Object createInstance(Application a) {
		return new Messages(a);
	}
	
	private static final class Messages implements Observer<Message> {
		private final Application app;
		private final JList messageList;
		private final DefaultListModel listModel;
		
		@SuppressWarnings("serial")
		public Messages(Application a) {
			app = a;
			listModel = new DefaultListModel() {
				@Override
				public Object getElementAt(int index) {
					return ((Message) super.getElementAt(index)).getHeadline();
				}
				
			};
			messageList = new JList(listModel);
			messageList.setName("Messages");
			app.getWindowSystem().addToolWindow(null, "messages", "Messages", null, messageList);
			app.getMessageObservable().addObserver(this);
			app.sendMessage(new Message() {

				@Override
				public void appendActions(List<Action> as) {
					
				}

				@Override
				public String getHeadline() {
					return "Welcome to Vanda Studio!";
				}

				@Override
				public String getMessage() {
					return null;
				}
				
			});
		}

		@Override
		public void notify(Message event) {
			listModel.add(0, event);
		}
		
	}

}
