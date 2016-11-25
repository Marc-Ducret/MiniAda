package net.slimevoid.miniada.token;

public class ConstInt extends Yytoken {
	
	public final int value;
	public final boolean outOfBounds;
	
	public ConstInt(int value) {
		this.value = value;
		outOfBounds = false;
	}
	
	public ConstInt(String value) {
		long v;
		try {
			v = Long.parseLong(value);
		} catch(NumberFormatException e) {
			v = Long.MAX_VALUE;
		}
		outOfBounds = v > 2147483648L;
		this.value = v >= 2147483648L ? Integer.MIN_VALUE : (int) v;
	}
	
	@Override
	public void specifyLocation(int line, int colS, int colE) 
			throws LexingException {
		super.specifyLocation(line, colS, colE);
		if(outOfBounds) throw new LexingException(line, colS, 
								"integer constant is out of bounds");
	}
	
	@Override
	public String toString() {
		return super.toString()+value;
	}
}
