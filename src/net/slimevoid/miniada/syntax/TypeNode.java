package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.Typeable;
import net.slimevoid.miniada.token.Yytoken;

public class TypeNode extends SyntaxNode implements Typeable {
	
	public final Identifier id;
	public final boolean isAccess;
	
	public Type type;
	
	private TypeNode(Identifier id, boolean isAccess, Yytoken fst, Yytoken lst){
		this.id = id;
		this.isAccess = isAccess;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static TypeNode matchType(TokenList toks) 
			throws MatchException {
		Keyword access;
		try {
			access = Compiler.matchKeyword(toks, KeywordType.ACCESS);
		} catch(MatchException e) {
			toks.prev();
			Identifier id = Compiler.matchIdent(toks);
			return new TypeNode(id, false, id, id);
		}
		Identifier id = Compiler.matchIdent(toks);
		return new TypeNode(id, true, access, id);
	}
	
	private Type buildType(Environment env) throws TypeException {
		if(isAccess) {
			return env.getAccessForType(id);
		}
		Type t = env.getType(id);
		if(!t.isDefined())
			throw new TypeException(id, "Type "+id+" is not defined");
		return t;
	}
	
	@Override
	public Type getType(Environment env) throws TypeException {
		if(type == null) type = buildType(env);
		return type;
	}
	
	@Override
	public String toString() {
		return (isAccess ? "access " : "") + id;
	}

}
