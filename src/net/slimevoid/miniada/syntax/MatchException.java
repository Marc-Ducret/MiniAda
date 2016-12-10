package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.LocalizedException;
import net.slimevoid.miniada.token.Yytoken;

public class MatchException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	private final Yytoken loc;
	
	public MatchException(Yytoken loc, String message) {
		super("Syntax", "at token "+loc+"\n"+message);
		this.loc = loc;
	}

	@Override
	public int getLine() {
		return loc.line;
	}

	@Override
	public int getColStart() {
		return loc.colS;
	}

	@Override
	public int getColEnd() {
		return loc.colE;
	}
}
