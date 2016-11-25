package net.slimevoid.miniada.typing;

import net.slimevoid.miniada.LocalizedException;
import net.slimevoid.miniada.syntax.SyntaxNode;
import net.slimevoid.miniada.token.Yytoken;

public class TypeException extends LocalizedException {

	private static final long serialVersionUID = 1L;
	
	public final Yytoken firstTok, lastTok;
	
	public TypeException(Yytoken firstTok, Yytoken lastTok, String message) {
		super("Typing", message);
		this.firstTok = firstTok;
		this.lastTok = lastTok;
	}
	
	public TypeException(SyntaxNode loc, String message) {
		this(loc.getFirstTok(), loc.getLastTok(), message);
	}

	public TypeException(Yytoken tok, String message) {
		this(tok, tok, message);
	}

	@Override
	public int getLine() {
		return firstTok.line;
	}

	@Override
	public int getColStart() {
		return firstTok.colS;
	}

	@Override
	public int getColEnd() {
		return lastTok.colE;
	}
}
