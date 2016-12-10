package net.slimevoid.miniada.typing;

import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValueRecord;

public class TypeRecord extends Type {

	public static class Member {
		public final String name;
		public final Type type;
		
		public Member(String name, Type type) {
			this.name = name;
			this.type = type;
		}
	}
	
	public final Member[] mems;

	public TypeRecord(Member[] mems) {
		super("record@");
		this.mems = mems;
	}
	
	public boolean hasMember(String id) {
		for(Member mem : mems) {
			if(mem.name.equalsIgnoreCase(id)) return true;
		}
		return false;
	}
	
	public Type getMemberType(String id) {
		for(Member mem : mems) {
			if(mem.name.equalsIgnoreCase(id)) return mem.type;
		}
		return null;
	}
	
	public int getMemberNumber(String id) {
		for(int i = 0; i < mems.length; i ++) {
			if(mems[i].name.equalsIgnoreCase(id)) return i;
		}
		return -1;
	}

	@Override
	public Value defaultValue() {
		Value[] vals = new Value[mems.length];
		for(int i = 0; i < mems.length; i++)
			vals[i] = mems[i].type.defaultValue();
		return new ValueRecord(vals);,
	}
}
