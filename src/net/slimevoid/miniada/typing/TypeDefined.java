package net.slimevoid.miniada.typing;

public class TypeDefined extends Type {
	
	private TypeDef def;
	
	protected TypeDefined(String name) {
		super(name);
	}
	
	public TypeDef getDefinition() {
		assert(isDefined());
		return def;
	}
	
	public boolean isDefined() {
		return def != null;
	}
	
	protected void define(TypeDef def) {
		assert(!isDefined());
		this.def = def;
	}
}
