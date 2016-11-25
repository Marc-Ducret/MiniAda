package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;

public class Param extends SyntaxNode {
	
	public final List<Identifier> ids;
	public final Mode mode;
	public final TypeNode type;
	
	private Param(List<Identifier> ids, Mode mode, TypeNode type, 
			Yytoken fst, Yytoken lst) {
		this.ids = ids;
		this.mode = mode;
		this.type = type;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static Param matchParam(TokenList toks)
			throws MatchException {
		List<Identifier> ids = new ArrayList<>();
		while(true) {
			Identifier id = Compiler.matchIdent(toks);
			ids.add(id);
			toks.savePos();
			try {
				Compiler.matchSymbol(toks, SymbolType.COMMA);
				toks.dropSave();
			} catch(MatchException e) {
				toks.revert();
				break;
			}
		}
		Compiler.matchSymbol(toks, SymbolType.COLON);
		toks.savePos();
		Mode m;
		try {
			m = Mode.matchMode(toks);
			toks.dropSave();
		} catch(MatchException e) {
			toks.revert();
			m = Mode.DEFAULT;
		}
		TypeNode t = TypeNode.matchType(toks);
		return new Param(ids, m, t, ids.get(0), t.lastTok);
	}
}
