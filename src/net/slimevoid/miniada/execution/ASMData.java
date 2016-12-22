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

	@Override
	public void pre(ASMBuilder asm) {
	}

	@Override
	public void post(ASMBuilder asm) {
	}
}
