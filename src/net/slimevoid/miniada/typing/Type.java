package net.slimevoid.miniada.typing;

import net.slimevoid.miniada.interpert.Value;

public abstract class Type {

	protected String name;

	protected Type(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public boolean canBeCastedInto(Type t) {
		return t == this;
	}
	
	public abstract Value defaultValue();

	public boolean isAccess() {
		return this instanceof TypeAccess ||
				(this instanceof TypeDefined && 
				((TypeDefined)this).getDefinition() instanceof TypeAccess);
	}

	public boolean isRecord() {
		return this instanceof TypeRecord ||
				(this instanceof TypeDefined && 
				((TypeDefined)this).getDefinition().isRecord());
	}
	
	public boolean isDefined() {
		return true;
	}
}