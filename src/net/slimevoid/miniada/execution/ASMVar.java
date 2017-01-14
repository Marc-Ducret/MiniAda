package net.slimevoid.miniada.execution;

import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.typing.Environment;

public class ASMVar extends ASMMem {
	
	private final int anteriority;
	
	public ASMVar(int off, int ant) {
		super(off, null);
		this.anteriority = ant;
	}
	
	public ASMVar(Identifier id, Environment env) {
		this(env.getVarOffset(id), env.getVarAnteriority(id));
	}

	@Override
	public void pre(ASMBuilder asm) {
		if(anteriority > 0) {
			ref = asm.getTmpReg();
			asm.mov(Register.RBP, ref);
			for(int i = 0; i < anteriority; i ++)
				asm.mov(new ASMMem(0, ref), ref);
		} else {
			ref = Register.RBP;
		}
	}

	@Override
	public void post(ASMBuilder asm) {
		if(anteriority > 0)
			asm.freeTempRegister(ref);
	}
}
