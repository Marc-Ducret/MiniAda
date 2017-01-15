package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.execution.ASMMem;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Par;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

public class ExpressionCall extends Expression {
	
	public final MethodCall call;
	private DeclarationFunction func;
	
	private ExpressionCall(MethodCall call) {
		this.call = call;
		setFirstToken(call.firstTok);
		setLastToken(call.lastTok);
	}
	
	public static ExpressionCall matchExpressionCall(TokenList toks) 
			throws MatchException {
		ExpressionCall call = 
				new ExpressionCall(MethodCall.matchMethodCall(toks));
		return call;
	}
	
	@Override
	public String toString() {
		return call.toString();
	}

	@Override
	public Type computeType(Environment env) throws TypeException {
		func = env.getFunction(call.id);
		int nPars = func.pars.length;
		if(call.exprs.length != nPars) 
			throw new TypeException(this, "Function "+func.name
				+" expects "+nPars+" arguments, was given "+call.exprs.length);
		for(int i = 0; i < nPars; i ++) {
			Type tE = call.exprs[i].getType(env);
			Type tP = func.pars[i].type;
			if(!tE.canBeCastedInto(tP))
				throw new TypeException(call.exprs[i], "type "+tE
						+" is incompatible with expected type "+tP);
			if(func.pars[i].out && !call.exprs[i].isAlterable())
				throw new TypeException(call.exprs[i], 
					"Paramater is out so this expression must be alterable");
		}
		return func.retType;
	}

	@Override
	public Value value(Scope s) {
		Value[] args = new Value[call.exprs.length];
		for(int i = 0; i < args.length; i++)
			args[i] = call.exprs[i].value(s);
		return func.execute(s, args);
	}

	@Override
	public void buildAsm(ASMBuilder asm, Environment env) {
		asm.sub(new ASMConst(func.retType.size()), Register.RSP);
		int size = 0;
		for(Expression e : call.exprs) {
			e.buildAsm(asm, env);
			size += e.getComputedType().size();
		}
		asm.push(Register.RBP);
		asm.mov(Register.RSP, Register.RBP);
		asm.call(func.getLabel(asm));
		asm.pop(Register.RBP);
		asm.planBuild(func);
		if(size > 0) asm.add(new ASMConst(size), Register.RSP);
		int off = 0;
		for(int i = 0; i < func.pars.length; i ++) {
			Par p = func.pars[i];
			if(p.out) {
				Access a = ((ExpressionAccess)call.exprs[i]).access;
				ASMMem stack = new ASMMem(off, Register.RSP);
				stack.offset(off+Compiler.WORD);
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
