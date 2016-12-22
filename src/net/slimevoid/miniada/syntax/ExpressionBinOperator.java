package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.interpert.Scope;
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
	public Object valuePrim(Scope s) {
		switch(op.type) {
		case AND:
			return eL.valueBool(s) && eR.valueBool(s);
		case AND_THEN:
			return eL.valueBool(s) & eR.valueBool(s);
		case DIVIDE:
			return eL.valueInt(s) / eR.valueInt(s);
		case EQ:
			return eL.value(s).eq(eR.value(s));
		case GE:
			return eL.valueInt(s) >= eR.valueInt(s);
		case GT:
			return eL.valueInt(s) >  eR.valueInt(s);
		case LE:
			return eL.valueInt(s) <= eR.valueInt(s);
		case LT:
			return eL.valueInt(s) <  eR.valueInt(s);
		case MINUS:
			return eL.valueInt(s) - eR.valueInt(s);
		case NEQ:
			return !eL.value(s).eq(eR.value(s));
		case OR:
			return eL.valueBool(s) || eR.valueBool(s);
		case OR_ELSE:
			return eL.valueBool(s) |  eR.valueBool(s);
		case PLUS:
			return eL.valueInt(s) + eR.valueInt(s);
		case REM:
			return eL.valueInt(s) % eR.valueInt(s);
		case TIMES:
			return eL.valueInt(s) * eR.valueInt(s);
		default:
			return null;
		}
	}

	@Override
	public void buildAsm(ASMBuilder asm, Environment env) {
		//TODO impl
		assert(false);
	}
}
