package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.execution.ASMMem;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.syntax.Operator.OperatorType;
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
		eL.buildAsm(asm, env);
		if(op.type == OperatorType.AND_THEN || op.type == OperatorType.OR_ELSE) {
			throw new RuntimeException("not impl"); //TODO impl 
		} else {
			eR.buildAsm(asm, env);
			if(op.type == OperatorType.EQ || op.type == OperatorType.NEQ) {
				boolean eq = op.type == OperatorType.EQ;
				Register rA = asm.getTmpReg();
				Register rB = asm.getTmpReg();
				Register res = asm.getTmpReg();
				asm.mov(new ASMConst(eq ? 1 : 0), res);
				int size = eL.getComputedType().size();
				ASMMem memA = new ASMMem(0, Register.RSP);
				ASMMem memB = new ASMMem(-size, Register.RSP);
				for(int i = 0; i < size/Compiler.WORD; i++) {
					asm.mov(memA, rA);
					asm.mov(memB, rB);
					asm.cmp(rA, rB);
					asm.mov(new ASMConst(0), rA);
					asm.set(eq ? "e" : "ne", rA);
					asm.binaryInstr(eq ? "and" : "or", rA, res);
					memA.offset(-Compiler.WORD);
					memB.offset(-Compiler.WORD);
				}
				asm.add(new ASMConst(size*2), Register.RSP);
				asm.push(res);
				asm.freeTempRegister(rA);
				asm.freeTempRegister(rB);
				asm.freeTempRegister(res);
			} else if(op.type == OperatorType.DIVIDE || op.type == OperatorType.REM){
				Register r = asm.getTmpReg();
				asm.pop(r);
				asm.pop(Register.RAX);
				asm.mov(new ASMConst(0), Register.RDX);
				asm.unaryInstr("div", r);
				asm.push(op.type == OperatorType.REM ? Register.RDX : Register.RAX);
				asm.freeTempRegister(r);
			} else {
				String instr = null;
				switch(op.type) {
				case AND: 		instr = "and"; 	break;
				case OR:		instr = "or"; 	break;
				case GE:		instr = "cmp";	break;
				case GT:		instr = "cmp";	break;
				case LE:		instr = "cmp";	break;
				case LT:		instr = "cmp";	break;
				case MINUS:		instr = "sub"; 	break;
				case PLUS:		instr = "add"; 	break;
				case TIMES:		instr = "imul";	break;
				default:
					break;
				}
				Register rL = asm.getTmpReg();
				Register rR = asm.getTmpReg();
				asm.pop(rR);
				asm.pop(rL);
				asm.binaryInstr(instr, rR, rL);
				String flag;
				switch(op.type) {
				case GE:		flag = "ge";	break;
				case GT:		flag = "g" ;	break;
				case LE:		flag = "le";	break;
				case LT:		flag = "l" ;	break;
				default: 		flag = null; 	break;
				}
				if(flag != null) {
					asm.mov(new ASMConst(0), rL);
					asm.set(flag, rL);
				}
				asm.push(rL);
				asm.freeTempRegister(rL);
				asm.freeTempRegister(rR);
			}
		}
	}
}
