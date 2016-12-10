package net.slimevoid.miniada.interpert;

import java.util.HashMap;
import java.util.Map;

import net.slimevoid.miniada.token.Identifier;

public class Scope {
	
	private Map<String, Value> vars = new HashMap<>();
	public Value ret;
	
	public void setValue(Identifier id, Value value) {
		System.out.println("SET "+id+" to "+value+" IN "+this);
		vars.put(id.name.toLowerCase(), value);
	}
	
	public void updateValue(Identifier id, Value value) {
		value.copyTo(vars.get(id.name.toLowerCase()));
	}
	
	public Value getValue(Identifier id) {
		return vars.get(id.name.toLowerCase());
	}
	
	public int getValueInt(Identifier id) {
		return getValue(id).toInt();
	}
	
	public char getValueChar(Identifier id) {
		return getValue(id).toChar();
	}
	
	public boolean getValueBool(Identifier id) {
		return getValue(id).toBool();
	}

	public void setReturn(Value value) {
		this.ret = value;
	}

	public void setValuePrim(Identifier var, Object val) {
		setValue(var, new ValuePrimitive(val));
	}
	
	public boolean knowsVal(Identifier id) {
		return vars.containsKey(id.name.toLowerCase());
	}
}
