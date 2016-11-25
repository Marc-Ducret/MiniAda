package net.slimevoid.miniada.token;

public abstract class Yytoken {
	
	public int line = -1;
	public int colS = -1;
	public int colE = -1;
	public int posInList = -1;
	
	public void specifyLocation(int line, int colS, int colE) 
			throws LexingException {
		this.line = line;
		this.colS = colS;
		this.colE = colE;
	}
	
	@Override
	public String toString() {
		return "";
	}
}
