package net.slimevoid.miniada.typing;

public class TypeDefRecord extends TypeDef {

	public static class Member {
		public final String name;
		public final Type type;
		
		public Member(String name, Type type) {
			this.name = name;
			this.type = type;
		}
	}
	
	public final Member[] mems;

	public TypeDefRecord(Member[] mems) {
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
}
