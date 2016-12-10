package net.slimevoid.miniada.token;

import net.slimevoid.miniada.LocalizedException;

public class LexingException extends LocalizedException {

	private static final long serialVersionUID = 1L;
	
	public final int line;
	public final int col;
	
	public LexingException(int line, int col, String message) {
		super("Lexing", message);
		this.line = line;
		this.col = col;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getColStart() {
		return col;
	}

	@Override
	public int getColEnd() {
		return col;
	}
}
