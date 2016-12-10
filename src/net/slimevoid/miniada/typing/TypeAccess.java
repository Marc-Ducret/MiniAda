package net.slimevoid.miniada.typing;

import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValueAccess;

public class TypeAccess extends Type {

	public final Type type;
	
	protected TypeAccess(Type type) {
		super("access@"+type.name);
		this.type = type;
	}
	
	@Override
	public boolean canBeCastedInto(Type t) {
		if(t instanceof TypeAccess)
			return type.canBeCastedInto(((TypeAccess) t).type);
		if(t instanceof TypeDefined)
			if(((TypeDefined) t).getDefinition() instanceof TypeAccess) {
				TypeAccess def = (TypeAccess) 
						((TypeDefined) t).getDefinition();
				return type.canBeCastedInto(def.type);
			}
		return false;
	}

	@Override
	public Value defaultValue() {
		return new ValueAccess(null);
	}
}
