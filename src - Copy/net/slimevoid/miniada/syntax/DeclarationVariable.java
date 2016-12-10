package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

public class DeclarationVariable extends Declaration {
	
	public final Identifier[] ids;
	public final TypeNode type;
	public final Expression init;
	
	private DeclarationVariable(Identifier[] ids, TypeNode type, Expression init) {
		this.ids = ids;
		this.type = type;
		this.init = init;
	}

	public static DeclarationVariable matchDeclarationVariable(TokenList toks) 
			throws MatchException {
		List<Identifier> ids = new ArrayList<>();
		ids.add(Compiler.matchIdent(toks));
		while(toks.nextIsOcc(SymbolType.COMMA)) {
			toks.next();
			ids.add(Compiler.matchIdent(toks));
		}
		Compiler.matchSymbol(toks, SymbolType.COLON);
		TypeNode type = TypeNode.matchType(toks);
		Expression init = null;
		if(toks.nextIsOcc(SymbolType.COLONEQ)) {
			toks.next();
			if(toks.gotoFirstOcc(SymbolType.SEMICOLON)) {
				Symbol scol = Compiler.matchSymbol(toks, SymbolType.SEMICOLON);
				toks.prev(); toks.prev(); toks.setBound();
				toks.revert();
				init = Expression.matchExpression(toks);
				toks.goTo(scol);
				toks.resetBound();
			}
		}
		DeclarationVariable dvar = new DeclarationVariable(
				ids.toArray(new Identifier[ids.size()]), type, init);
		dvar.setFirstToken(ids.get(0));
		dvar.setLastToken(Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
		return dvar;
	}
	
	@Override
	public void typeDeclaration(Environment env) throws TypeException {
		for(Identifier id : ids)
			env.registerVar(id);
		Type t = type.computeType(env);
		if(init != null) {
			Type tI = init.computeType(env);
			if(!tI.canBeCastedInto(t))
				throw new TypeException(init, "Expected type "+t
										+" while expression has type "+tI);
		}
		for(Identifier id : ids)
			env.setVarType(id, t);
	}
	
	@Override
	public void init(Scope s) {
		for(Identifier id : ids)
			s.setValue(id, type.type.defaultValue());
		if(init != null)
			for(Identifier id : ids)
				s.updateValue(id, init.value(s));
	}
	
	@Override
	public String toString() {
		String str = "";
		for(Identifier id : ids) {
			str += id+", ";
		}
		str = str.substring(0, str.length()-2)+" : "+type;
		if(init != null) str += " := "+init;
		return str+";";
	}
}
