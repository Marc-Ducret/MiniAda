package net.slimevoid.miniada.token;

public class ConstChar extends Yytoken {
	
	public final char value;
	
	public ConstChar(char value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return super.toString()+"'"+value+"'";
	}
}
