package net.slimevoid.miniada.typing;

public interface Typeable {
	public Type computeType(Environment env) throws TypeException;
}
