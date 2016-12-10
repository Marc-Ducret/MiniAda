package net.slimevoid.miniada.interpert;

public abstract class Value {
	
	public abstract void copyTo(Value dest);
	public abstract boolean eq(Value v);
	
	public int toInt() {
		return (int) ((ValuePrimitive)this).getVal();
	}
	
	public char toChar() {
		return (char) ((ValuePrimitive)this).getVal();
	}
	
	public boolean toBool() {
		return (boolean) ((ValuePrimitive)this).getVal();
	}

	public ValueRecord toRecord() {
		return (ValueRecord) this;
	}
}
