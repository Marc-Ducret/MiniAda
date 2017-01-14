package net.slimevoid.miniada.execution;

import net.slimevoid.miniada.execution.ASMBuilder.Register;

public class ASMMem implements ASMOperand {
	
	protected int offset;
	protected Register ref;
	
	public ASMMem(int off, Register ref) {
		this.offset = off;
		this.ref = ref;
	}

	@Override
	public void appendToBuilder(StringBuilder buff) {
		buff.append(-offset).append("(%").append(ref.name().toLowerCase().replace('e',  'r')).append(')');
	}

	@Override
	public void pre(ASMBuilder asm) {
	}

	@Override
	public void post(ASMBuilder asm) {
	}
	
	public void offset(int off) {
		offset += off;
	}
	
	public void freeRegister(ASMBuilder asm) {
		asm.freeTempRegister(ref);
	}
}
