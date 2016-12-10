package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
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
		System.out.println("ACCESS");//TODO RM
		toks.printState(); //TODO RM
		ExpressionAccess e = new ExpressionAccess(Access.matchAccess(toks));
		toks.checkConsumed();
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
}
