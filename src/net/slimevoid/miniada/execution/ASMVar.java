package net.slimevoid.miniada.execution;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.typing.Environment;

public class ASMVar extends ASMMem {
	
	private final int stackID;
	private final boolean isLocal;
	
	private ASMVar(int off, int stackID, boolean isLocal) {
		super(off, null);
		this.stackID = stackID;
		this.isLocal = isLocal;
	}
	
	public ASMVar(Identifier id, Environment env) {
		this(env.getVarOffset(id), env.getVarFrameID(id), env.isVarLocal(id));
	}

	@Override
	public void pre(ASMBuilder asm) {
		if(isLocal) ref = Register.RBP;
		else {
			ref = asm.getTmpReg();
			String found = asm.newLabel();
			String look = asm.newLabel();
			asm.mov(Register.RBP, ref);
			asm.label(look);
			asm.cmp(new ASMConst(stackID), new ASMMem(2*Compiler.WORD, ref));
			asm.jflag(found, "e");
			asm.mov(new ASMMem(0, ref), ref);
			asm.jmp(look);
			asm.label(found);
		}
	}

	@Override
	public void post(ASMBuilder asm) {
		if(!isLocal) asm.freeTempRegister(ref);
	}
}
