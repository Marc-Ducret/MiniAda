package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

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
	public Object value(Environment env) {
		if(access.from == null) {
			return env.getValue(access.id);
		} else {
			//TODO do records
		}
		return null;
	}
}
