package net.slimevoid.miniada.execution;

public interface ASMOperand {
	
	public void pre(ASMBuilder asm);
	
	public void appendToBuilder(StringBuilder buff);

	public void post(ASMBuilder asm);
}
