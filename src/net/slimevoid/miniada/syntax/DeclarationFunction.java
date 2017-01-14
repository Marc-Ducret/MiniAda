package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.execution.ASMMem;
import net.slimevoid.miniada.execution.ASMRoutine;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.SubScope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.syntax.Instruction.InstructionBlock;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Par;
import net.slimevoid.miniada.typing.SubEnvironment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

public class DeclarationFunction extends Declaration implements ASMRoutine {
	
	public final Identifier name;
	private final Params params;
	private final Declaration[] decls;
	private final InstructionBlock instrs;
	private final TypeNode ret;
	
	public Par[] pars;
	public Type retType;
	public Environment localEnv;
	
	private String label;
	private boolean planned = false;
	
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
				localEnv.offset(-t.size()*p.ids.size());
			}
			localEnv.returnLoc = new ASMMem(localEnv.getOffset()-retType.size(), Register.RBP);
			for(Param p : params.ps) {
				for(Identifier id : p.ids)
					localEnv.registerVar(id);
				Type t = p.type.computeType(localEnv);
				for(Identifier id : p.ids) {
					pars.add(new Par(id, t, p.mode.isOut));
					localEnv.setVarType(id, t);
					if(!p.mode.isOut) localEnv.restricAlteration(id);
				}
			}
		}
		this.pars = pars.toArray(new Par[pars.size()]);
		env.registerFunction(this);
		localEnv.offset(Compiler.WORD*2);
		for(Declaration decl : decls) decl.typeDeclaration(localEnv);
		if(decls.length > 0) localEnv.checkDefinitions(decls[decls.length-1]);
		instrs.typeCheck(localEnv);
		if(!instrs.willReturn())
			throw new TypeException(this, "Missing return");
	}
	
	public Value execute(Scope s, Value...args) {
		SubScope localS = new SubScope(s);
		for(int i = 0; i < pars.length; i++) {
			localS.setValue(pars[i].name, args[i]);
		}
		for(Declaration decl : decls) decl.init(localS);
		instrs.execute(localS);
		return localS.ret;
	}
	
	@Override
	public void buildASM(ASMBuilder asm) {
		asm.label(getLabel(asm));
		asm.sub(new ASMConst(localEnv.getOffset()-Compiler.WORD*2), Register.RSP);
		instrs.buildAsm(asm, localEnv);
		asm.add(new ASMConst(localEnv.getOffset()-Compiler.WORD*2), Register.RSP);
		asm.ret();
	}
	
	@Override
	public String getLabel(ASMBuilder asm) {
		if(label == null) label = asm.newLabel();
		return label;
	}
	
	@Override
	public boolean isPlanned() {
		return planned;
	}
	
	@Override
	public void setPlanned() {
		this.planned = true;
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
