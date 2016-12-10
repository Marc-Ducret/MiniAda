package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;

public class Params extends SyntaxNode {
	
	public final List<Param> ps;
	
	private Params(List<Param> ps, Yytoken fst, Yytoken lst) {
		this.ps = ps;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static Params matchParams(TokenList toks)
			throws MatchException {
		List<Param> ps = new ArrayList<>();
		Symbol lpar = Compiler.matchSymbol(toks, SymbolType.LPAR);
		while(true) {
			Param p = Param.matchParam(toks);
			ps.add(p);
			toks.savePos();
			try {
				Compiler.matchSymbol(toks, SymbolType.SEMICOLON);
				toks.dropSave();
			} catch(MatchException e) {
				toks.revert();
				break;
			}
		}
		return new Params(ps, lpar, 
				Compiler.matchSymbol(toks, SymbolType.RPAR));
	}
}
