package org.vanda.datasources;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;

public final class Element {

	private String prefix;
	private String value;
	private MultiplexObserver<Element> observable;
	
	public Element(String prefix, String value) {
		observable = new MultiplexObserver<Element>();
		this.prefix = prefix;
		this.value = value;
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

	public Observable<Element> getObservable() {
		return observable;
	}
	
	public void setElement(String prefix, String value) {
		if (!this.prefix.equals(prefix) || !this.value.equals(value)) {
			this.prefix = prefix;
			this.value = value;
			observable.notify(this);
		}
	}

	public void setPrefix(String prefix) {
		if (!this.prefix.equals(prefix)) {
			this.prefix = prefix;
			observable.notify(this);
		}
	}

	public void setValue(String value) {
		if (!this.value.equals(value)) {
			this.value = value;
			observable.notify(this);
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
