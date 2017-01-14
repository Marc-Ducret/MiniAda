package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.execution.ASMMem;
import net.slimevoid.miniada.execution.ASMVar;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValueAccess;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeAccess;
import net.slimevoid.miniada.typing.TypeDefined;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypeRecord;

public class ExpressionAccess extends Expression {
	
	public final Access access;
	
	private ExpressionAccess(Access acc) {
		this.access = acc;
		setFirstToken(acc.firstTok);
		setLastToken(acc.lastTok);
	}
	
	public static ExpressionAccess matchExpressionAccess(TokenList toks) 
			throws MatchException {
		ExpressionAccess e = new ExpressionAccess(Access.matchAccess(toks));
		return e;
	}

	@Override
	public Type computeType(Environment env) throws TypeException {
		return access.computeType(env);
	}
	
	@Override
	public String toString() {
		return access.toString();
	}
	
	@Override
	public boolean isAlterable() {
		return access.alterable;
	}

	@Override
	public Value value(Scope s) {
		if(access.from == null) {
			if(access.func != null) return access.func.execute(s);
			return s.getValue(access.id);
		} else {
			TypeDefined t = (TypeDefined) access.from.getComputedType();
			if(t.getDefinition() instanceof TypeAccess) {
				t = (TypeDefined) ((TypeAccess)t.getDefinition()).type;
			}
			TypeRecord r = ((TypeRecord)t.getDefinition());
			Value v = access.from.value(s);
			if(v instanceof ValueAccess)
				v = ((ValueAccess) v).getVar();
			return v.toRecord().getVal(
					r.getMemberNumber(access.id.name));
		}
	}

	@Override
	public void buildAsm(ASMBuilder asm, Environment env) {
		if(access.from != null) {
			access.from.buildAsm(asm, env);
			if(access.from.getComputedType().isAccess()) {
				Register r = asm.getTmpReg();
				asm.pop(r);
				int sF = this.getComputedType().size();
				ASMMem from = new ASMMem(access.offset, r);
				for(int i = 0; i < sF/8; i ++) {
					asm.push(from);
					from.offset(8);
				}
				asm.freeTempRegister(r);
			} else {
				int sR = access.from.getComputedType().size();
				int sF = this.getComputedType().size();
				asm.add(new ASMConst(sR), Register.RSP);
				ASMMem from = new ASMMem(access.offset+8, Register.RSP);
				for(int i = 0; i < sF/8; i ++) {
					asm.push(from);
				}
			}
		} else {
			assert(access.func == null);
			ASMVar from = new ASMVar(access.id, env);
			for(int i = 0; i < this.getComputedType().size()/8; i ++) {
				asm.push(from);
				from.offset(8);
			}
		}
	}
}
