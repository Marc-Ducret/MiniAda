package net.slimevoid.miniada.token;

public class Identifier extends Yytoken {
	public final String name;
	
	public Identifier(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return super.toString()+name;
	}
}
