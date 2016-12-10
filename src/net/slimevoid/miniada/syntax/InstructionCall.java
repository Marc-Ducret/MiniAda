package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
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
		InstructionCall ic = new InstructionCall(
									MethodCall.matchMethodCall(toks));
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
}
