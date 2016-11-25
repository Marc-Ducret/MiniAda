package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypePrimitive;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Yytoken;

public class Operator extends SyntaxNode {
	
	public static enum OperatorType {EQ, NEQ, GT, GE, LT, LE, PLUS, MINUS, 
		TIMES, DIVIDE, REM, AND, AND_THEN, OR, OR_ELSE, NOT}
	
	public final OperatorType type;
	
	private Operator(OperatorType type, Yytoken fst, Yytoken lst) {
		this.type = type;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	
	public static Operator matchOperator(TokenList toks) 
			throws MatchException {
		Yytoken tok = toks.next();
		if(tok instanceof Symbol) {
			for(OperatorType ot : OperatorType.values()) {
				if(ot.name().equalsIgnoreCase(((Symbol) tok).type.name())) {
					return new Operator(ot, tok, tok);
				}
			}
			throw new MatchException(tok, "Expected operator");
		} else {
			toks.prev();
			try {
				Keyword k = Compiler.matchKeyword(toks, KeywordType.AND,
						KeywordType.OR,
						KeywordType.REM,
						KeywordType.NOT);
				if(k.type == KeywordType.NOT)
					return new Operator(OperatorType.NOT, k, k);
				if(k.type == KeywordType.AND) {
					toks.savePos();
					try {
						Keyword then = Compiler.matchKeyword(toks, 
															KeywordType.THEN);
						toks.dropSave();
						return new Operator(OperatorType.AND_THEN, k, then);
					} catch(MatchException e) {
						toks.revert();
						return new Operator(OperatorType.AND, k, k);
					}
				} 
				if(k.type == KeywordType.OR) {
					toks.savePos();
					try {
						Keyword els = Compiler.matchKeyword(toks, 
															KeywordType.ELSE);
						toks.dropSave();
						return new Operator(OperatorType.OR_ELSE, k, els);
					} catch(MatchException e) {
						toks.revert();
						return new Operator(OperatorType.OR, k, k);
					}
				}
				return new Operator(OperatorType.REM, k, k);
			} catch(MatchException e) {
				throw new MatchException(tok, "Expected operator");
			}
		}
	}
	
	/**
	 * @return null if type is whatever
	 */
	public Type getOperandType() {
		switch(type) {
		case EQ: case NEQ:
			return null;
		case GT: case GE: case LT: case LE:
		case PLUS: case MINUS: case TIMES: case DIVIDE: case REM:
			return TypePrimitive.INTEGER;
		case AND: case AND_THEN: case OR: case OR_ELSE: case NOT:
			return TypePrimitive.BOOLEAN;
		default:
			return null;
		}
	}
	
	public Type getResultingType() {
		switch(type) {
		case EQ: case NEQ:
		case GT: case GE: case LT: case LE:
			return TypePrimitive.BOOLEAN;
		case PLUS: case MINUS: case TIMES: case DIVIDE: case REM:
			return TypePrimitive.INTEGER;
		case AND: case AND_THEN: case OR: case OR_ELSE: case NOT:
			return TypePrimitive.BOOLEAN;
		default:
			return null;
		}
	}
	
	@Override
	public String toString() {
		return ""+type;
	}
}
