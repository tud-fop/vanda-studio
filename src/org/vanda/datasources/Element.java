package org.vanda.datasources;

import java.util.LinkedList;

import org.vanda.datasources.Elements.ElementEvent;
import org.vanda.datasources.Elements.PrefixChangeEvent;
import org.vanda.datasources.Elements.ValueChangeEvent;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Util;

public final class Element {

	private LinkedList<ElementEvent<Element>> events;
	private int update;
	private String prefix;
	private String value;
	private MultiplexObserver<ElementEvent<Element>> observable;
	
	public Element(String prefix, String value) {
		events = new LinkedList<Elements.ElementEvent<Element>>();
		observable = new MultiplexObserver<ElementEvent<Element>>();
		this.prefix = prefix;
		this.value = value;
	}
	
	public void beginUpdate() {
		update++;
	}
	
	public void endUpdate() {
		update--;
		if (update == 0) {
			LinkedList<ElementEvent<Element>> ev = events;
			events = new LinkedList<ElementEvent<Element>>();
			Util.notifyAll(observable, ev);			
		}
	}
	
	public static Element fromString(String element) {
		int i = element.indexOf(':');
		return i == -1 ? new Element("", element) : new Element(element.substring(0, i), element.substring(i + 1));
	}

	public String getPrefix() {
		return prefix;
	}

	public String getValue() {
		return value;
	}

	public Observable<ElementEvent<Element>> getObservable() {
		return observable;
	}

	public void setPrefix(String prefix) {
		if (!this.prefix.equals(prefix)) {
			beginUpdate();
			try {
				this.prefix = prefix;
				events.add(new PrefixChangeEvent<Element>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public void setValue(String value) {
		if (!this.value.equals(value)) {
			beginUpdate();
			try {
				this.value = value;
				events.add(new ValueChangeEvent<Element>(this));
			} finally {
				endUpdate();
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(':');
		sb.append(value);
		return sb.toString();
	}

}
