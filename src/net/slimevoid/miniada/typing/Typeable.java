package net.slimevoid.miniada.typing;

public interface Typeable {
	public Type getType(Environment env) throws TypeException;
}
