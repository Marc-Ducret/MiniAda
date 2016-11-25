package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.syntax.Instruction.InstructionBlock;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Par;
import net.slimevoid.miniada.typing.SubEnvironment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

public class DeclarationFunction extends Declaration {
	
	public final Identifier name;
	private final Params params;
	private final Declaration[] decls;
	private final InstructionBlock instrs;
	private final TypeNode ret;
	
	public Par[] pars;
	public Type retType;
	public Environment localEnv;
	
	protected DeclarationFunction(Identifier name, Params pars, 
			Declaration[] decls, InstructionBlock instrs, TypeNode ret) {
		this.name = name;
		this.params = pars;
		this.decls = decls;
		this.instrs = instrs;
		this.ret = ret;
	}
	
	public static DeclarationFunction matchDeclarationFunction(
			TokenList toks) throws MatchException {
		Compiler.matchKeyword(toks, KeywordType.FUNCTION);
		Identifier name = Compiler.matchIdent(toks);
		Params pars;
		if(toks.nextIsOcc(KeywordType.RETURN)) pars = null;
		else pars = Params.matchParams(toks);
		Compiler.matchKeyword(toks, KeywordType.RETURN);
		TypeNode ret = TypeNode.matchType(toks);
		Compiler.matchKeyword(toks, KeywordType.IS);
		List<Declaration> decls = new ArrayList<>();
		while(!toks.nextIsOcc(KeywordType.BEGIN)) {
			decls.add(Declaration.matchDeclaration(toks));
		}
		Compiler.matchKeyword(toks, KeywordType.BEGIN);
		DeclarationFunction dfunc = new DeclarationFunction(name, pars, 
				decls.toArray(new Declaration[decls.size()]), 
				Instruction.matchInstructionBlock(toks, KeywordType.END), ret);
		Compiler.matchKeyword(toks, KeywordType.END);
		if(!toks.nextIsOcc(SymbolType.SEMICOLON)) {
			Identifier id = Compiler.matchIdent(toks);
			if(!id.name.equals(name.name)) throw new MatchException(id, 
					"Expected identifier \""+name.name+"\"");
		}
		dfunc.setFirstToken(name);
		dfunc.setLastToken(Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
		return dfunc;
	}
	
	@Override
	public void typeDeclaration(Environment env) throws TypeException {
		env.checkDefinitions(this);
		List<Par> pars = new ArrayList<>();
		retType = ret.computeType(env);
		localEnv = new SubEnvironment(env, retType);
		if(params != null) {
			for(Param p : params.ps) {
				Type t = p.type.computeType(localEnv);
				for(Identifier id : p.ids) {
					pars.add(new Par(id, t, p.mode.isOut));
					localEnv.registerVar(id, t);
					if(!p.mode.isOut) localEnv.restricAlteration(id);
				}
			}
		}
		this.pars = pars.toArray(new Par[pars.size()]);
		env.registerFunction(this);
		for(Declaration decl : decls) decl.typeDeclaration(localEnv);
		if(decls.length > 0) localEnv.checkDefinitions(decls[decls.length-1]);
		instrs.typeCheck(localEnv);
		if(!instrs.willReturn())
			throw new TypeException(this, "Missing return");
	}
	
	public Object execute(Object[] args) {
		for(int i = 0; i < pars.length; i++) {
			localEnv.setValue(pars[i].name, args[i]);
		}
		for(Declaration decl : decls) decl.init(localEnv);
		instrs.execute(localEnv);
		return localEnv.ret;
	}
	
	@Override
	public String toString() {
		String str = "function "+name+" return "+ret+" is ";
		for(Declaration dcl : decls) {
			str+= "\n"+dcl;
		}
		str += "\nbegin"+instrs+" end;";
		return str;
	}
}
