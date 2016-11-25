package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.Typeable;

public abstract class Expression extends SyntaxNode implements Typeable {
	
	private Type type;
	private boolean computedType;	

	/**
	 * Warning: no guarantee on the TokenList resulting position
	 */
	public static Expression matchExpression(TokenList toks) 
			throws MatchException {
		if(toks.outOfBounds())
			throw new MatchException(toks.nextBypassBound(), 
									"Expected expression");
		if(toks.nextIsOcc(SymbolType.MINUS))
			return ExpressionUnOperator.matchExpressionUnOperator(toks);
		if(toks.nextIsOcc(KeywordType.NOT))
			return ExpressionUnOperator.matchExpressionUnOperator(toks);
		if(toks.gotoLastOcc(KeywordType.OR))
			return ExpressionBinOperator.matchExpressionBinOperator(toks, 
					Operator.matchOperator(toks));
		if(toks.gotoLastOcc(KeywordType.AND))
			return ExpressionBinOperator.matchExpressionBinOperator(toks, 
					Operator.matchOperator(toks));
		if(toks.gotoLastOcc(SymbolType.EQ, SymbolType.NEQ))
			return ExpressionBinOperator.matchExpressionBinOperator(toks, 
					Operator.matchOperator(toks));
		if(toks.gotoLastOcc(SymbolType.GT, SymbolType.GE,
				SymbolType.LT, SymbolType.LE))
			return ExpressionBinOperator.matchExpressionBinOperator(toks, 
					Operator.matchOperator(toks));
		if(toks.gotoLastOcc(SymbolType.PLUS, SymbolType.MINUS))
			return ExpressionBinOperator.matchExpressionBinOperator(toks, 
					Operator.matchOperator(toks));
		if(toks.gotoLastOcc(KeywordType.REM, 
				SymbolType.TIMES, 
				SymbolType.DIVIDE))
			return ExpressionBinOperator.matchExpressionBinOperator(toks, 
					Operator.matchOperator(toks));
		if(toks.nextIsOcc(SymbolType.LPAR)) {
			Symbol lpar = (Symbol) toks.next();
			toks.savePos();
			toks.goToBound();
			if(!toks.nextIsOcc(SymbolType.RPAR))
				throw new MatchException(lpar, "Unmatched parenthesis");
			Symbol rpar = (Symbol) toks.next();
			toks.prev(); toks.prev(); toks.setBound();
			toks.revert();
			Expression expr = matchExpression(toks);
			expr.setFirstToken(lpar);
			expr.setLastToken(rpar);
			return expr;
		}
		if(toks.nextIsOcc(KeywordType.NEW)) {
			return ExpressionNew.matchExpressionNew(toks);
		}
		try {
			Compiler.matchIdent(toks);
			toks.prev(); toks.savePos();
			try {
				Expression e = ExpressionAccess.matchExpressionAccess(toks);
				toks.dropSave();
				return e;
			} catch(MatchException e) {}
			toks.revert();
			return ExpressionCall.matchExpressionCall(toks);
		} catch (MatchException e) {}
		toks.prev();
		Expression expr = ExpressionConstant.matchExpressionConstant(toks);
		toks.checkConsumed();
		return expr;
	}
	
	public boolean isAlterable() {
		return false;
	}
	
	public Type getType(Environment env) throws TypeException {
		if(!computedType) type = computeType(env);
		return type;
	}
	
	public abstract Object value(Environment env);
	
	public boolean valueBool(Environment env) {
		return (boolean) value(env);
	}
	
	public int valueInt(Environment env) {
		return (int) value(env);
	}
}
