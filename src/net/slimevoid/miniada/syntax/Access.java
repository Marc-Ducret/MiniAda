package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeAccess;
import net.slimevoid.miniada.typing.TypeDef;
import net.slimevoid.miniada.typing.TypeDefAccess;
import net.slimevoid.miniada.typing.TypeDefRecord;
import net.slimevoid.miniada.typing.TypeDefined;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.Typeable;
import net.slimevoid.miniada.token.Yytoken;

public class Access extends SyntaxNode implements Typeable {
	
	public final Identifier id;
	public final Expression from;
	
	public boolean alterable;
	
	private Access(Identifier id, Yytoken fst, Yytoken lst) {
		this(id, null, fst, lst);
	}

	private Access(Identifier id, Expression from, Yytoken fst, Yytoken lst) {
		this.id = id;
		this.from = from;
		setFirstToken(fst);
		setLastToken(lst);
	}

	public static Access matchAccess(TokenList toks) 
			throws MatchException {
		if(toks.gotoLastOcc(SymbolType.DOT)) {
			Compiler.matchSymbol(toks, SymbolType.DOT);
			toks.savePos();
			Identifier id = Compiler.matchIdent(toks);
			toks.revert();
			toks.prev(); toks.prev(); toks.setBound();
			toks.revert();
			Expression from = Expression.matchExpression(toks);
			return new Access(id, from, from.firstTok, id);
		}
		Identifier id = Compiler.matchIdent(toks);
		return new Access(id, id, id);
	}
	
	@Override
	public String toString() {
		if(from == null) return id.name;
		else return from+"."+id.name;
	}

	@Override
	public Type computeType(Environment env) throws TypeException {
		alterable = false;
		if(from == null) {
			alterable = env.isAlterable(id);
			try {
				return env.getFunction(id).retType;
			} catch(TypeException e) {}
			return env.getVarType(id);
		}
		Type t = from.getType(env);
		if(t instanceof TypeAccess) { //TODO refactor access?? 
			t = ((TypeAccess) t).type;
			alterable = true;
		}
		if(!(t instanceof TypeDefined)) 
			throw new TypeException(from, 
					"Expected record or access to type record");
		TypeDef def = (TypeDef) ((TypeDefined)t).getDefinition();
		if(def instanceof TypeDefAccess) {
			alterable = true;
			TypeDefAccess acc = (TypeDefAccess) def;
			if(acc.type instanceof TypeDefined)
				def = ((TypeDefined)acc.type).getDefinition();
		}
		if(!(def instanceof TypeDefRecord))
			throw new TypeException(from, 
					"Expected record or access to type record");
		TypeDefRecord rec = (TypeDefRecord) def;
		if(!rec.hasMember(id.name))
			throw new TypeException(id, t.getName()+" has no field "+id);
		return rec.getMemberType(id.name);
	}
}
