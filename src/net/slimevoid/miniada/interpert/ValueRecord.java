package net.slimevoid.miniada.interpert;

public class ValueRecord extends Value {
	
	private final Value[] vals;

	public ValueRecord(Value[] vals) {
		this.vals = vals;
	}
	
	@Override
	public void copyTo(Value dest) {
		ValueRecord r = ((ValueRecord)dest);
		for(int i = 0; i < vals.length; i ++)
			this.vals[i].copyTo(r.vals[i]);
	}

	@Override
	public boolean eq(Value v) {
		ValueRecord r = ((ValueRecord)v);
		for(int i = 0; i < vals.length; i ++)
			if(!this.vals[i].eq(r.vals[i]))
				return false;
		return true;
	}
	
	public Value getVal(int i) {
		return vals[i];
	}
	
	public void setVal(int i, Value val) {
		vals[i] = val;
	}
}
