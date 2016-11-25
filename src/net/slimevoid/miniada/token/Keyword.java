package net.slimevoid.miniada.token;

public class Keyword extends Yytoken {

	public static enum KeywordType {ACCESS, AND, BEGIN, ELSE, ELSIF, END, FALSE,
									FOR, FUNCTION, IF, IN, IS, LOOP, NEW, NOT,
									NULL, OR, OUT, PROCEDURE, RECORD, REM,
									RETURN, REVERSE, THEN, TRUE, TYPE, USE,
									WHILE, WITH}
	
	public final KeywordType type;
	
	public Keyword(KeywordType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return super.toString()+type;
	}
}
