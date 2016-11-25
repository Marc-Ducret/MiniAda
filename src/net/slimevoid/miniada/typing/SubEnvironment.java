package net.slimevoid.miniada.typing;

import net.slimevoid.miniada.syntax.DeclarationFunction;
import net.slimevoid.miniada.syntax.DeclarationProcedure;
import net.slimevoid.miniada.token.Identifier;

public class SubEnvironment extends Environment {
	
	private final Environment parent;
	
	public SubEnvironment(Environment parent, Type expectedReturn) {
		super(expectedReturn);
		this.parent = parent;
	}
	
	@Override
	public Type getVarType(Identifier id) throws TypeException {
		try {
			return super.getVarType(id);
		} catch(TypeException e) {
			return parent.getVarType(id);
		}
	}
	
	@Override
	public boolean isTypeDeclared(Identifier id) {
		return super.isTypeDeclared(id) || parent.isTypeDeclared(id);
	}
	
	@Override
	protected TypeDefined getTypeFromTable(Identifier id) throws TypeException {
		try {
			return super.getTypeFromTable(id);
		} catch(TypeException e) {
			return parent.getTypeFromTable(id);
		}
	}
	
	@Override
	public DeclarationFunction getFunction(Identifier id) throws TypeException {
		try {
			return super.getFunction(id);
		} catch(TypeException e) {
			return parent.getFunction(id);
		}
	}
	
	@Override
	public DeclarationProcedure getProcedure(Identifier id)throws TypeException{
		try {
			return super.getProcedure(id);
		} catch(TypeException e) {
			return parent.getProcedure(id);
		}
	}
	
	@Override
	public boolean isAlterable(Identifier id) {
		try {
			getVarType(id);
			return super.isAlterable(id);
		} catch(TypeException e) {
			return parent.isAlterable(id);
		}
	}
}
