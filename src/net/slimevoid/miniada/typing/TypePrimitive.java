package net.slimevoid.miniada.typing;

import java.util.HashMap;
import java.util.Map;

public class TypePrimitive extends Type {
	
	private static final Map<String, TypePrimitive> prims = new HashMap<>();

	public static final TypePrimitive INTEGER = new TypePrimitive("Integer"),
									  BOOLEAN = new TypePrimitive("Boolean"),
									CHARACTER = new TypePrimitive("Character");
	
	public static final TypePrimitive NULL = new TypePrimitive("@typenull") {
		public boolean canBeCastedInto(Type t) {
			if(t == this) return true;
			if(t instanceof TypeAccess) return true;
			if(t instanceof TypeDefined &&
					((TypeDefined) t).getDefinition() instanceof TypeDefAccess)
				return true;
			return false;
		}
	};
	
	private TypePrimitive(String name) {
		super(name);
		prims.put(name.toLowerCase(), this);
	}
	
	public static TypePrimitive getPrimitive(String name) {
		return prims.get(name.toLowerCase());
	}
}
