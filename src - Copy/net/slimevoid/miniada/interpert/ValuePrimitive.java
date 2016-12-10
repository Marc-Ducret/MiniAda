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
			if(this.val instanceof Integer)
				prim.val = (int) this.val;
			else if(this.val instanceof Character)
				prim.val = (char) this.val;
			else if(this.val instanceof Boolean)
				prim.val = (boolean) this.val;
			else assert(false); //TODO just prim.val = this.val???
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
