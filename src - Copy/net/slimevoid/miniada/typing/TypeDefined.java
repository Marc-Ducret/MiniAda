package net.slimevoid.miniada.typing;

import net.slimevoid.miniada.interpert.Value;

public class TypeDefined extends Type {
	
	private Type def;
	
	protected TypeDefined(String name) {
		super(name);
	}
	
	public Type getDefinition() {
		assert(isDefined());
		return def;
	}
	
	public boolean isDefined() {
		return def != null;
	}
	
	protected void define(Type def) {
		assert(!isDefined());
		this.def = def;
	}

	@Override
	public Value defaultValue() {
		return def.defaultValue();
	}
}
