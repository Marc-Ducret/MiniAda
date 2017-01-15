package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.execution.ASMMem;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Par;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

public class InstructionCall extends Instruction {
	
	public final MethodCall call;
	private DeclarationProcedure proc;
	
	private InstructionCall(MethodCall call) {
		this.call = call;
		setFirstToken(call.firstTok);
	}
	
	public static InstructionCall matchInstructionCall(TokenList toks) 
			throws MatchException {
		if(toks.gotoFirstOcc(SymbolType.SEMICOLON)) {
			toks.prev();
			toks.setBound();
			toks.revert();
		}
		InstructionCall ic = new InstructionCall(
				MethodCall.matchMethodCall(toks));
		toks.resetBound();
		ic.setLastToken(Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
		return ic;
	}
	
	@Override
	public String toString() {
		return call.toString()+";";
	}

	@Override
	public void typeCheck(Environment env) throws TypeException {
		proc = env.getProcedure(call.id);
		int nPars = proc.pars.length;
		if(call.exprs.length != nPars) 
			throw new TypeException(this, "Procedure "+proc.name
				+" expects "+nPars+" arguments, was given "+call.exprs.length);
		for(int i = 0; i < nPars; i ++) {
			Type tE = call.exprs[i].getType(env);
			Type tP = proc.pars[i].type;
			if(!tE.canBeCastedInto(tP))
				throw new TypeException(call.exprs[i], "type "+tE
						+" is incompatible with expected type "+tP);
			if(proc.pars[i].out && !call.exprs[i].isAlterable())
				throw new TypeException(call.exprs[i], 
					"Paramater is out so this expression must be alterable");
		}
	}

	@Override
	public boolean willReturn() throws TypeException {
		return false;
	}

	@Override
	public boolean execute(Scope s) {
		Value[] args = new Value[proc.pars.length];
		for(int i = 0; i < proc.pars.length; i ++)
			args[i] = call.exprs[i].value(s);
		proc.execute(s, args);
		return false;
	}

	@Override
	public void buildAsm(ASMBuilder asm, Environment env) {
		int size = 0;
		for(Expression e : call.exprs) {
			e.buildAsm(asm, env);
			size += e.getComputedType().size();
		}
		asm.push(Register.RBP);
		asm.mov(Register.RSP, Register.RBP);
		asm.call(proc.getLabel(asm));
		asm.pop(Register.RBP);
		asm.planBuild(proc);
		if(size > 0) asm.add(new ASMConst(size), Register.RSP);
		int off = 0;
		for(int i = 0; i < proc.pars.length; i ++) {
			Par p = proc.pars[i];
			if(p.out) {
				Access a = ((ExpressionAccess)call.exprs[i]).access;
				ASMMem stack = new ASMMem(off, Register.RSP);
				stack.offset(Compiler.WORD);
				ASMMem mem = a.getAsmOperand(asm, env);
				Register r = asm.getTmpReg();
				for(int j = 0; j < p.type.size() / Compiler.WORD; j ++) {
					asm.mov(stack, r);
					asm.mov(r, mem);
					mem.offset(Compiler.WORD);
					stack.offset(Compiler.WORD);
				}
				asm.freeTempRegister(r);
				mem.freeRegister(asm);
			}
			off += p.type.size();
		}
	}
}
