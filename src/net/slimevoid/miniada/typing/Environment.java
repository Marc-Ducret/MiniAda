package net.slimevoid.miniada.typing;

import java.util.HashMap;
import java.util.Map;

import net.slimevoid.miniada.syntax.DeclarationFunction;
import net.slimevoid.miniada.syntax.DeclarationProcedure;
import net.slimevoid.miniada.syntax.NativeFunction;
import net.slimevoid.miniada.syntax.NativeProcedure;
import net.slimevoid.miniada.syntax.SyntaxNode;
import net.slimevoid.miniada.token.Identifier;

public class Environment {
	
	public static enum NameSpace {VAR, TYPE, FUNC, PROC};
	
	private int curOffset = 0;
	
	private Map<String, Type> varTypes = new HashMap<>();
	private Map<String, Integer> varOffsets = new HashMap<>();
	private Map<String, Boolean> restrictAlter = new HashMap<>();
	private Map<String, TypeDefined> types = new HashMap<>();
	private Map<String, DeclarationFunction> functions = new HashMap<>();
	private Map<String, DeclarationProcedure> procedures = new HashMap<>();
	private Map<String, NameSpace> nameSpaces = new HashMap<>();
	
	public final Type expectedReturn;
	
	public Environment(Type expectedReturn) {
		this.expectedReturn = expectedReturn;
	}
	
	public Type getVarType(Identifier id) throws TypeException {
		if(!nameSpaces.containsKey(id.name.toLowerCase())) 
			throw new TypeException(id, "Unknown variable "+id);
		if(!isVarInit(id)) {
			throw new TypeException(id, "Variable "+id+" cannot be used here");
		}
		return varTypes.get(id.name.toLowerCase());
	}
	
	public void offset(int off) {
		curOffset += off;
	}
	
	public Integer getVarOffset(Identifier id) {
		return varOffsets.get(id.name.toLowerCase());
	}
	
	public int getVarAnteriority(Identifier id) {
		return 0;
	}
	
	public Type getType(Identifier id) throws TypeException {
		return getTypeFromTable(id);
	}
	
	private TypeDefined getTypeFromTable(Identifier id) throws TypeException {
		if(!types.containsKey(id.name.toLowerCase())) {
			throw new TypeException(id, "Unknown type "+id.name);
		}
		return types.get(id.name.toLowerCase());
	}
	
	public boolean isTypeDeclared(Identifier id) {
		return isTypeDeclaredLocally(id);
	}
	
	public boolean isTypeDeclaredLocally(Identifier id) {
		return types.containsKey(id.name.toLowerCase());
	}
	
	public boolean isTypeDefined(Identifier id) {
		TypeDefined t = types.get(id.name.toLowerCase());
		return t != null && t.isDefined();
	}
	
	public void declareType(Identifier id) throws TypeException {
		if(isTypeDeclaredLocally(id)) 
			throw new TypeException(id, "type "+id+" is already declared");
		useName(id, NameSpace.TYPE);
		types.put(id.name.toLowerCase(), new TypeDefined(id.name));
	}
	
	public void defineType(Identifier id, Type def) throws TypeException {
		if(isTypeDefined(id))
			throw new TypeException(id, "type "+id+" is already defined");
		getTypeFromTable(id).define(def);
	}
	
	public void registerVar(Identifier name) 
			throws TypeException {
		useName(name, NameSpace.VAR);
	}
	
	public void setVarType(Identifier name, Type type) {
		varTypes.put(name.name.toLowerCase(), type);
		varOffsets.put(name.name.toLowerCase(), curOffset);
		curOffset += type.size();
	}
	
	public void registerProcedure(DeclarationProcedure proc)
			throws TypeException {
		useName(proc.name, NameSpace.PROC);
		procedures.put(proc.name.name.toLowerCase(), proc);
	}
	
	public void registerFunction(DeclarationFunction func) 
			throws TypeException {
		useName(func.name, NameSpace.FUNC);
		functions.put(func.name.name.toLowerCase(), func);
	}
	
	public void registerNativeProcedure(NativeProcedure proc)
			throws TypeException {
		nameSpaces.put(proc.name.toLowerCase(), NameSpace.PROC);
		procedures.put(proc.name.toLowerCase(), proc);
	}
	
	public void registerNativeFunction(NativeFunction func) 
			throws TypeException {
		nameSpaces.put(func.name.toLowerCase(), NameSpace.FUNC);
		functions.put(func.name.toLowerCase(), func);
	}
	
	public DeclarationFunction getFunction(Identifier id) throws TypeException {
		if(!functions.containsKey(id.name.toLowerCase()))
			throw new TypeException(id, "Unknown function \""+id+"\"");
		return functions.get(id.name.toLowerCase());
	}
	
	public DeclarationProcedure getProcedure(Identifier id)throws TypeException{
		if(!procedures.containsKey(id.name.toLowerCase()))
			throw new TypeException(id, "Unknown procedure \""+id+"\"");
		return procedures.get(id.name.toLowerCase());
	}
	
	public Type getAccessForType(Identifier typ) throws TypeException {
		if(!isTypeDeclared(typ))
			throw new TypeException(typ, "type "+typ.name+" isn't declared");
		if(isTypeDefined(typ) && !getType(typ).isRecord())
			throw new TypeException(typ, "type "+typ.name+" isn't a record");
		return new TypeAccess(getType(typ));
	}
	
	public void useName(Identifier id, NameSpace space) throws TypeException {
		if(nameSpaces.containsKey(id.name.toLowerCase()))
			throw new TypeException(id, "identifier "+id+" is already used");
		nameSpaces.put(id.name.toLowerCase(), space);
	}
	
	public void checkDefinitions(SyntaxNode loc) throws TypeException {
		for(TypeDefined t : types.values()) {
			if(!t.isDefined())
				throw new TypeException(loc, "Type "+t+" remains undefined");
			if(t.isAccess() && !((TypeAccess)t.getDefinition()).type.isRecord())
				throw new TypeException(loc, 
						"Type "+t+" is an access to non record type");
		}
	}
	
	public boolean isVarInit(Identifier id) {
		return !nameSpaces.containsKey(id.name.toLowerCase()) ||
				varTypes.containsKey(id.name.toLowerCase());
	}
	
	public boolean isAlterable(Identifier id) {
		return !restrictAlter.containsKey(id.name.toLowerCase());
	}
	
	public void restricAlteration(Identifier id) {
		restrictAlter.put(id.name.toLowerCase(), true);
	}
	
	public NameSpace getNameSpace(Identifier id) {
		return nameSpaces.get(id.name.toLowerCase());
	}
}
