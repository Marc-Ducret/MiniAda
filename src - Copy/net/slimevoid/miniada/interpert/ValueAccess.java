package net.slimevoid.miniada.interpert;

public class ValueAccess extends Value {
	
	protected Value var;

	public ValueAccess(Value var) {
		this.var = var;
	}

	@Override
	public void copyTo(Value dest) {
		System.out.println("CHANGE?? "+this.var);
		((ValueAccess)dest).var = this.var;
	}

	@Override
	public boolean eq(Value v) {
		ValueAccess acc = (ValueAccess) v;
		return acc.var == this.var;
	}
	
	public Value getVar() {
		return var;
	}
}
