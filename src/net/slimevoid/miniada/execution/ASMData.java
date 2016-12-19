package net.slimevoid.miniada.execution;

public class ASMData implements ASMOperand {
	
	public final String name;
	
	protected ASMData(String name) {
		this.name = name;
	}

	@Override
	public void appendToBuilder(StringBuilder buff) {
		buff.append('$').append(name);
	}
}
