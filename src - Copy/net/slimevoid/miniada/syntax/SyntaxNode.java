package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.token.Yytoken;

public abstract class SyntaxNode {
	
	public static int uniqueID = 0;
	
	protected Yytoken firstTok, lastTok;
	
	protected SyntaxNode() {
	}
	
	protected void setFirstToken(Yytoken tok) {
		this.firstTok = tok;
	}
	
	protected void setLastToken(Yytoken tok) {
		this.lastTok = tok;
	}
	
	public Yytoken getFirstTok() {
		return firstTok;
	}
	
	public Yytoken getLastTok() {
		return lastTok;
	}
}
