package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

public class ExpressionBinOperator extends Expression {

	public final Operator op;
	public final Expression eL, eR;
	
	private ExpressionBinOperator(Operator op, Expression eL, Expression eR) {
		this.op = op;
		this.eL = eL;
		this.eR = eR;
		setFirstToken(eL.firstTok);
		setLastToken (eR.lastTok);
	}
	
	public static ExpressionBinOperator matchExpressionBinOperator(
			TokenList toks,
			Operator op) throws MatchException {
		Expression eR = Expression.matchExpression(toks);
		toks.goTo(op.firstTok);
		toks.prev();
		toks.setBound();
		toks.revert();
		Expression eL = Expression.matchExpression(toks);
		return new ExpressionBinOperator(op, eL, eR);
	}
	
	
	@Override
	public Type computeType(Environment env) throws TypeException {
		Type left = eL.getType(env);
		Type right = eR.getType(env);
		Type opType = op.getOperandType();
		if(opType != null && !left.equals(opType))
			throw new TypeException(eL, "Expected type "+opType
					+" while expression has type "+left);
		if(!left.canBeCastedInto(right) && !right.canBeCastedInto(left)) 
			throw new TypeException(eL, "Expected type "+left
					+" while expression has type "+right);
		return op.getResultingType();
	}
	
	@Override
	public String toString() {
		return "("+eL+" "+op+" "+eR+")";
	}

	@Override
	public Object valuePrim(Environment env) {
		switch(op.type) {
		case AND:
			return eL.valueBool(env) && eR.valueBool(env);
		case AND_THEN:
			return eL.valueBool(env) & eR.valueBool(env);
		case DIVIDE:
			return eL.valueInt(env) / eR.valueInt(env);
		case EQ:
			return eL.value(env).eq(eR.value(env));
		case GE:
			return eL.valueInt(env) >= eR.valueInt(env);
		case GT:
			return eL.valueInt(env) >  eR.valueInt(env);
		case LE:
			return eL.valueInt(env) <= eR.valueInt(env);
		case LT:
			return eL.valueInt(env) <  eR.valueInt(env);
		case MINUS:
			return eL.valueInt(env) - eR.valueInt(env);
		case NEQ:
			return !eL.value(env).eq(eR.value(env));
		case OR:
			return eL.valueBool(env) || eR.valueBool(env);
		case OR_ELSE:
			return eL.valueBool(env) |  eR.valueBool(env);
		case PLUS:
			return eL.valueInt(env) + eR.valueInt(env);
		case REM:
			return eL.valueInt(env) % eR.valueInt(env);
		case TIMES:
			return eL.valueInt(env) * eR.valueInt(env);
		default:
			return null;
		}
	}
}
