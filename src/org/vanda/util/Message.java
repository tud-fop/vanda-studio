package org.vanda.util;

import java.util.Date;


public interface Message extends HasActions, Selectable {

	String getHeadline();
	
	String getMessage();
	
	Date getDate();
	
}
