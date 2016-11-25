package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeDefRecord;
import net.slimevoid.miniada.typing.TypeDefined;
import net.slimevoid.miniada.typing.TypeException;

public class ExpressionNew extends Expression {

	public final Identifier type;
	
	private ExpressionNew(Identifier type) {
		this.type = type;
		setLastToken(type);
	}
	
	public static ExpressionNew matchExpressionNew(TokenList toks) 
			throws MatchException {
		Keyword k = Compiler.matchKeyword(toks, KeywordType.NEW);
		ExpressionNew expr = new ExpressionNew(Compiler.matchIdent(toks));
		expr.setFirstToken(k);
		return expr;
	}
	
	@Override
	public String toString() {
		return "new "+type;
	}

	@Override
	public Type computeType(Environment env) throws TypeException {
		Type t = env.getType(type);
		if(!(t instanceof TypeDefined))
			throw new TypeException(type, "Expected type record");
		if(!(((TypeDefined)t).getDefinition() instanceof TypeDefRecord))
			throw new TypeException(type, "Expected type record");
		return env.getAccessForType(type);
	}

	@Override
	public Object value(Environment env) {
		return null; //TODO record
	}
}
