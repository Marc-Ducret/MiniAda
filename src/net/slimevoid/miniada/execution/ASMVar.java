package net.slimevoid.miniada.execution;

import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.typing.Environment;

public class ASMVar implements ASMOperand {
	
	private final int offset;
	private final int anteriority;
	private Register ref;
	
	public ASMVar(int off, int ant) {
		this.offset = off;
		this.anteriority = ant;
	}

	public ASMVar(Identifier id, Environment env) {
		this(env.getVarOffset(id), env.getVarAnteriority(id));
		System.out.println("VAR "+id+" off: "+offset+", ant: "+anteriority);//TODO rm
	}

	@Override
	public void appendToBuilder(StringBuilder buff) {
		buff.append(-offset).append("(%").append(ref.name().toLowerCase()).append(')');
	}

	@Override
	public void pre(ASMBuilder asm) {
		assert(anteriority == 0);
		ref = asm.getTmpReg();
//		for(int i = 0; i < anteriority; i ++) TODO
//			asm.mov(from, to);
		asm.mov(Register.RBP, ref);
	}

	@Override
	public void post(ASMBuilder asm) {
		asm.freeTempRegister(ref);
	}
}
