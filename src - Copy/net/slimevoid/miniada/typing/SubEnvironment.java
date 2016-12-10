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
		if(getNameSpace(id) != NameSpace.VAR) 
			throw new TypeException(id, id+" doesn't refer to a variable");
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
	public Type getType(Identifier id) throws TypeException {
		if(getNameSpace(id) != NameSpace.TYPE) 
			throw new TypeException(id, id+" doesn't refer to a type");
		try {
			return super.getType(id);
		} catch(TypeException e) {
			return parent.getType(id);
		}
	}
	
	@Override
	public DeclarationFunction getFunction(Identifier id) throws TypeException {
		if(getNameSpace(id) != NameSpace.FUNC) 
			throw new TypeException(id, id+" doesn't refer to a function");
		try {
			return super.getFunction(id);
		} catch(TypeException e) {
			return parent.getFunction(id);
		}
	}
	
	@Override
	public DeclarationProcedure getProcedure(Identifier id)throws TypeException{
		if(getNameSpace(id) != NameSpace.PROC) 
			throw new TypeException(id, id+" doesn't refer to a procedure");
		try {
			return super.getProcedure(id);
		} catch(TypeException e) {
			return parent.getProcedure(id);
		}
	}
	
	@Override
	public NameSpace getNameSpace(Identifier id) {
		NameSpace ns = super.getNameSpace(id);
		if(ns != null) return ns;
		return parent.getNameSpace(id);
	}
	
	@Override
	public boolean isAlterable(Identifier id) {
		try {
			super.getVarType(id);
			return super.isAlterable(id);
		} catch(TypeException e) {
			return parent.isAlterable(id);
		}
	}
}
