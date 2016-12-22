package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.typing.Environment;
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
		//TODO impl
		assert(false);
	}
}
