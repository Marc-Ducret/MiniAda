package net.slimevoid.miniada.typing;

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
}