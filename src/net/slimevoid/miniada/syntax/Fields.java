package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;

public class Fields extends SyntaxNode {
	
	public final List<Identifier> ids;
	public final TypeNode type;
	
	private Fields(List<Identifier> ids, TypeNode type, 
			Yytoken fst, Yytoken lst) {
		this.ids = ids;
		this.type = type;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static Fields matchFields(TokenList toks)
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
		TypeNode t = TypeNode.matchType(toks);
		return new Fields(ids, t, ids.get(0),
				Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
	}
}
