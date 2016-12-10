package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeAccess;
import net.slimevoid.miniada.typing.TypeDefined;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypeRecord;
import net.slimevoid.miniada.typing.Typeable;

public class Access extends SyntaxNode implements Typeable {
	
	public final Identifier id;
	public final Expression from;
	
	public DeclarationFunction func;
	
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
			try {
				func = env.getFunction(id);
				alterable = func.retType.isAccess();
				return func.retType;
			} catch(TypeException e) {}
			alterable = env.isAlterable(id) || env.getVarType(id).isAccess();
			return env.getVarType(id);
		}
		Type t = from.getType(env);
		while(true)
			if(t instanceof TypeDefined)
				t = ((TypeDefined) t).getDefinition();
			else if(t instanceof TypeAccess)
				t = ((TypeAccess) t).type;
			else break;
		if(!(t instanceof TypeRecord))
			throw new TypeException(from, 
					"Expected record or access to record");
		TypeRecord rec = (TypeRecord) t;
		alterable = from.isAlterable() || rec.getMemberType(id.name).isAccess();
		if(!rec.hasMember(id.name))
			throw new TypeException(id, t.getName()+" has no field "+id);
		return rec.getMemberType(id.name);
	}
}
