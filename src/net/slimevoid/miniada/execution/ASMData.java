package net.slimevoid.miniada.execution;

public class ASMData implements ASMOperand {
	
	public final String name;
	
	public ASMData(String name) {
		this.name = name;
	}

	@Override
	public void appendToBuilder(StringBuilder buff) {
		// TODO
	}
}
