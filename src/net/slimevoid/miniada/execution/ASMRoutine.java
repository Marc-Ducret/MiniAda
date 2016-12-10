package net.slimevoid.miniada.execution;

/**
 * Execution scheme:
 * parameters: %rsi, %rdi, stack
 * return value in %rbx
 * computations with %r(8-15) and stack
 */
public interface ASMRoutine {

	public void buildASM(ASMBuilder asm);
	public String getLabel(ASMBuilder asm);
	public boolean isPlanned();
	public void setPlanned();
}
