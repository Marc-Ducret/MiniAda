package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Yytoken;

public class Mode extends SyntaxNode {
	
	public static final Mode DEFAULT = new Mode(false, null, null);
	
	public final boolean isOut;
	
	private Mode(boolean isOut, Yytoken fst, Yytoken lst) {
		this.isOut = isOut;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static Mode matchMode(TokenList toks) 
			throws MatchException {
		Keyword in = Compiler.matchKeyword(toks, KeywordType.IN);
		toks.savePos();
		try {
			Keyword out = Compiler.matchKeyword(toks, KeywordType.OUT);
			toks.dropSave();
			return new Mode(true, in, out);
		} catch(MatchException e) {
			toks.revert();
			return new Mode(false, in, in);
		}
	}
}
