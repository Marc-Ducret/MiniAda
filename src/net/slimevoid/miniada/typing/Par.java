package net.slimevoid.miniada.typing;

import net.slimevoid.miniada.token.Identifier;

public class Par {
	
	public final Identifier name;
	public final Type type;
	public final boolean out;
	
	public Par(Identifier name, Type type) {
		this(name, type, false);
	}
	
	public Par(Identifier name, Type type, boolean out) {
		this.name = name;
		this.type = type;
		this.out = out;
	}
}
