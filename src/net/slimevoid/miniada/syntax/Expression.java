package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValuePrimitive;
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
	 * assumes that the TokenList is bounded to the end of the expression
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
			Symbol lpar = (Symbol) toks.nextBoundChecked();
			toks.savePos();
			toks.goToBound();
			if(!toks.nextIsOcc(SymbolType.RPAR))
				throw new MatchException(lpar, "Unmatched parenthesis");
			Symbol rpar = (Symbol) toks.nextBoundChecked();
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
		if(toks.gotoFirstOcc(SymbolType.DOT)) {
			toks.revert();
			return ExpressionAccess.matchExpressionAccess(toks);
		}
		try {
			Compiler.matchIdent(toks);
			if(toks.outOfBounds()) {
				toks.prev();
				return ExpressionAccess.matchExpressionAccess(toks);
			}
		} catch (MatchException e) {
			toks.prev();
			Expression expr = ExpressionConstant.matchExpressionConstant(toks);
			toks.checkConsumed();
			return expr;
		}
		toks.prev();
		Expression expr = ExpressionCall.matchExpressionCall(toks);
		return expr;
	}
	
	public boolean isAlterable() {
		return type.isAccess();
	}
	
	public Type getType(Environment env) throws TypeException {
		if(!computedType) type = computeType(env);
		return type;
	}
	
	public Type getComputedType() {
		return type;
	}
	
	/**
	 * This or valuePrim must be overidden by sub classes
	 */
	public Value value(Scope s) {
		return new ValuePrimitive(valuePrim(s));
	}
	
	public Object valuePrim(Scope s) {
		return ((ValuePrimitive)value(s)).getVal();
	}
	
	public boolean valueBool(Scope s) {
		return (boolean) valuePrim(s);
	}
	
	public int valueInt(Scope s) {
		return (int) valuePrim(s);
	}
}
