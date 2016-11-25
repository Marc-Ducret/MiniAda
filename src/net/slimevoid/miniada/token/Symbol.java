package net.slimevoid.miniada.token;

public class Symbol extends Yytoken {

	public static enum SymbolType {EQ, NEQ, GT, GE, LT, LE, PLUS, MINUS, 
									TIMES, DIVIDE, DOT, SEMICOLON, COMMA,
									COLON, LPAR, RPAR, COLONEQ, DOTDOT}
	
	public final SymbolType type;
	
	public Symbol(SymbolType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return super.toString()+type;
	}
}
