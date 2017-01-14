package net.slimevoid.miniada.interpert;

public class ValuePrimitive extends Value {
	
	private Object val;
	
	public ValuePrimitive(Object val) {
		this.val = val;
	}

	@Override
	public void copyTo(Value dest) {
		if(this.val == null)
			((ValueAccess)dest).var = null;
		else {
			ValuePrimitive prim = ((ValuePrimitive)dest);
			prim.val = this.val;
		}
	}

	@Override
	public boolean eq(Value v) {
		if(!(v instanceof ValuePrimitive)) return false;
		ValuePrimitive vp = (ValuePrimitive) v;
		return vp.val == null && val == null || vp.val.equals(val);
	}

	public Object getVal() {
		return val;
	}

}