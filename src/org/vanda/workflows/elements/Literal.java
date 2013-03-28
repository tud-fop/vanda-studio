package org.vanda.workflows.elements;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.workflows.elements.Elements.*;

public final class Literal {
	
	private Type type;
	private String value;
	private final MultiplexObserver<ElementEvent<Literal>> observable;
	
	public Literal(Type type, String value) {
		this.type = type;
		this.value = value;
		observable = new MultiplexObserver<ElementEvent<Literal>>();
	}
	
	public Observable<ElementEvent<Literal>> getObservable() {
		return observable;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setType(Type type) {
		if (!type.equals(this.type)) {
			this.type = type;
			observable.notify(new PropertyChangeEvent<Literal>(this));
		}
	}
	
	public void setValue(String value) {
		if (!value.equals(this.value)) {
			this.value = value;
			observable.notify(new PropertyChangeEvent<Literal>(this));
		}
	}
	
	
}
