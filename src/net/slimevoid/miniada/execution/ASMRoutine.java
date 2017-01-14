package net.slimevoid.miniada.execution;

/**
 * Execution scheme:
 * parameters: stack
 * return value on stack
 * computations with %r(8-15) and stack
 */
public interface ASMRoutine {

	public void buildASM(ASMBuilder asm);
	public String getLabel(ASMBuilder asm);
	public boolean isPlanned();
	public void setPlanned();
}
