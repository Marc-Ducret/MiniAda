package net.slimevoid.miniada.interpert;

import net.slimevoid.miniada.token.Identifier;

public class SubScope extends Scope {

	private final Scope parent;
	
	public SubScope(Scope parent) {
		this.parent = parent;
	}
	
	@Override
	public Value getValue(Identifier id) {
		if(super.knowsVal(id))
			return super.getValue(id);
		else
			return parent.getValue(id);
	}
	
	@Override
	public void updateValue(Identifier id, Value value) {
		if(super.knowsVal(id))
			super.updateValue(id, value);
		else
			parent.updateValue(id, value);
	}
}
