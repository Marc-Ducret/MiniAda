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

public class DeclarationProcedure extends Declaration {
	
	public final Identifier name;
	public final Params params;
	public final Declaration[] decls;
	public final InstructionBlock instrs;
	
	public Par[] pars;
	public Environment localEnv;
	
	protected DeclarationProcedure(Identifier name, Params pars, 
			Declaration[] decls, InstructionBlock instrs) {
		this.name = name;
		this.params = pars;
		this.decls = decls;
		this.instrs = instrs;
	}
	
	public static DeclarationProcedure matchDeclarationProcedure(
			TokenList toks) throws MatchException {
		Compiler.matchKeyword(toks, KeywordType.PROCEDURE);
		Identifier name = Compiler.matchIdent(toks);
		Params pars;
		if(toks.nextIsOcc(KeywordType.IS)) pars = null;
		else pars = Params.matchParams(toks);
		Compiler.matchKeyword(toks, KeywordType.IS);
		List<Declaration> decls = new ArrayList<>();
		while(!toks.nextIsOcc(KeywordType.BEGIN)) {
			decls.add(Declaration.matchDeclaration(toks));
		}
		Compiler.matchKeyword(toks, KeywordType.BEGIN);
		DeclarationProcedure dproc = new DeclarationProcedure(name, pars, 
				decls.toArray(new Declaration[decls.size()]), 
				Instruction.matchInstructionBlock(toks, KeywordType.END));
		Compiler.matchKeyword(toks, KeywordType.END);
		if(!toks.nextIsOcc(SymbolType.SEMICOLON)) {
			Identifier id = Compiler.matchIdent(toks);
			if(!id.name.equalsIgnoreCase(name.name)) 
				throw new MatchException(id, 
					"Expected identifier \""+name.name+"\"");
		}
		dproc.setFirstToken(name);
		dproc.setLastToken(Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
		return dproc;
	}
	
	@Override
	public void typeDeclaration(Environment env) throws TypeException {
		env.checkDefinitions(this);
		List<Par> pars = new ArrayList<>();
		localEnv = new SubEnvironment(env, null);
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
		env.registerProcedure(this);
		for(Declaration decl : decls) decl.typeDeclaration(localEnv);
		if(decls.length > 0) localEnv.checkDefinitions(decls[decls.length-1]); //TODO notlikethis #testtype6
		instrs.typeCheck(localEnv);
	}
	
	public void execute(Object...args) {
		for(int i = 0; i < pars.length; i++) {
			localEnv.setValue(pars[i].name, args[i]);
		}
		for(Declaration decl : decls) decl.init(localEnv);
		instrs.execute(localEnv);
	}
	
	@Override
	public String toString() {
		String str = "procedure "+name+" is ";
		for(Declaration dcl : decls) {
			str+= "\n"+dcl;
		}
		str += "\nbegin "+instrs+" end;";
		return str;
	}
}