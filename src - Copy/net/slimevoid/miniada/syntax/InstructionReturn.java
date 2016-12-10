package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.TypeException;

public class InstructionReturn extends Instruction {
	
	public final Expression ret;
	
	private InstructionReturn(Expression ret, Yytoken fst, Yytoken lst) {
		this.ret = ret;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static InstructionReturn matchInstructionReturn(TokenList toks) 
			throws MatchException {
		Keyword retrn = Compiler.matchKeyword(toks, KeywordType.RETURN);
		Expression ret = null;
		if(!toks.nextIsOcc(SymbolType.SEMICOLON)) {
			if(!toks.gotoFirstOcc(SymbolType.SEMICOLON))
				throw new MatchException(toks.next(), "Expected ';'");
			Symbol semicol = (Symbol) toks.next();
			toks.prev(); toks.prev();
			toks.setBound();
			toks.revert();
			ret = Expression.matchExpression(toks);
			toks.resetBound();
			toks.goTo(semicol);
		}
		return new InstructionReturn(ret, retrn, 
				Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
	}
	
	@Override
	public void typeCheck(Environment env) throws TypeException {
		if(ret == null && env.expectedReturn != null)
			throw new TypeException(this, 
					"Return was expected of type "+env.expectedReturn);
		if(ret != null && !ret.getType(env).canBeCastedInto(env.expectedReturn))
			throw new TypeException(this, 
					"Return was expected of type "+env.expectedReturn+
					" while a type "+ret.getType(env)+" was given");
	}
	
	@Override
	public boolean willReturn() throws TypeException {
		return true;
	}
	
	@Override
	public String toString() {
		return "RETURN "+ret+" ;";
	}

	@Override
	public boolean execute(Scope s) {
		if(ret != null) s.setReturn(ret.value(s));
		return true;
	}
}
