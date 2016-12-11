package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;

public class MethodCall extends SyntaxNode {
	
	public final Identifier id;
	public final Expression[] exprs;
	
	private MethodCall(Identifier id, Expression...exprs) {
		this.id = id;
		this.exprs = exprs;
		setFirstToken(id);
		if(exprs.length > 0) setLastToken(exprs[exprs.length-1].lastTok);
		else setLastToken(id);
	}
	
	public static MethodCall matchMethodCall(TokenList toks) 
			throws MatchException {
		Identifier id = Compiler.matchIdent(toks);
		List<Expression> exprs = new ArrayList<>();
		if(toks.nextIsOcc(SymbolType.LPAR)) {
			toks.savePos(); toks.goToBound();
			if(!toks.nextIsOcc(SymbolType.RPAR)) {
				toks.dropSave();
				throw new MatchException(toks.cur(), "Unexpected token");
			}
			toks.revert();
			toks.nextBoundChecked();
			while(true) {
				toks.gotoFirstOcc(SymbolType.RPAR, SymbolType.COMMA);
				Symbol sym = Compiler.matchSymbol(toks, SymbolType.RPAR, 
														SymbolType.COMMA);
				toks.prev(); toks.prev(); toks.setBound();
				toks.revert();
				exprs.add(Expression.matchExpression(toks));
				toks.resetBound();
				toks.goTo(sym); toks.nextBoundChecked();
				if(sym.type == SymbolType.RPAR) {
					break;
				}
			}
		} else {
			toks.checkConsumed();
		}
		return new MethodCall(id, exprs.toArray(new Expression[exprs.size()]));
	}
	
	@Override
	public String toString() {
		String str = id.toString();
		if(exprs.length > 0) {
			str += "(";
			for(Expression e : exprs) {
				str += e+", ";
			}
			str = str.substring(0, str.length()-2)+")";
		}
		return str;
	}
}
