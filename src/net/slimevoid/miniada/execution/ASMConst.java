package net.slimevoid.miniada.execution;

public class ASMConst implements ASMOperand {

	private int val;
	
	public ASMConst(int i) {
		this.val = i;
	}

	@Override
	public void appendToBuilder(StringBuilder buff) {
		buff.append('$').append(val);
	}
}
