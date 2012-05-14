package org.vanda.studio.modules.messages;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.types.CompositeType;
import org.vanda.studio.model.types.Equation;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.TypeVariable;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.Message;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Token;

public class MessageModule implements Module {

	@Override
	public String getName() {
		return "Messages Module for Vanda Studio";
	}

	@Override
	public Object createInstance(Application a) {
		/*
		Type t1 = new TypeVariable(Token.getToken(0));
		Type t2 = new TypeVariable(Token.getToken(1));
		Type t3 = new TypeVariable(Token.getToken(2));
		Type t4 = new TypeVariable(Token.getToken(3));
		Type t5 = new TypeVariable(Token.getToken(4));
		Type t6 = new TypeVariable(Token.getToken(5));
		ArrayList<Type> u1 = new ArrayList<Type>(1);
		u1.add(new TypeVariable(Token.getToken(6)));
		Type tau1 = new CompositeType("String");
		Type tau2 = new CompositeType("[]", u1);
		Type tau3 = u1.get(0);
		ArrayList<Type> u2 = new ArrayList<Type>(1);
		u2.add(new TypeVariable(Token.getToken(7)));
		Type tau4 = new CompositeType("[]", u2);
		Type tau5 = u2.get(0);
		Set<Equation> eqs = new HashSet<Equation>();
		eqs.add(new Equation(t1, tau1));
		eqs.add(new Equation(t2, tau2));
		eqs.add(new Equation(t3, tau3));
		eqs.add(new Equation(t1, tau4));
		eqs.add(new Equation(t4, tau5));
		try {
			Types.unify(eqs);
			System.out.println(eqs);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		return new Messages(a);
	}
	
	private static final class Messages implements Observer<Message> {
		private final Application app;
		private final JList messageList;
		private final JScrollPane scrollPane;
		private final DefaultListModel listModel;
		
		@SuppressWarnings("serial")
		public Messages(Application a) {
			app = a;
			listModel = new DefaultListModel() {
				@Override
				public Object getElementAt(int index) {
					Message m = (Message) super.getElementAt(index);
					return "[" + DateFormat.getTimeInstance().format(m.getDate()) + "] " + m.getHeadline();
				}
				
			};
			messageList = new JList(listModel);
			scrollPane = new JScrollPane(messageList);
			scrollPane.setName("Messages");
			app.getWindowSystem().addToolWindow(null, "messages", "Messages", null, scrollPane);
			app.getMessageObservable().addObserver(this);
			app.sendMessage(new Message() {
				
				final Date d = new Date();

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

				@Override
				public Date getDate() {
					return d;
				}
				
			});
		}

		@Override
		public void notify(Message event) {
			listModel.add(0, event);
			messageList.setSelectedIndex(0);
			app.getWindowSystem().focusToolWindow(scrollPane);
		}
		
	}

}
