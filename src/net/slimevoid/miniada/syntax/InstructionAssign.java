package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValueAccess;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeAccess;
import net.slimevoid.miniada.typing.TypeDefined;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypeRecord;

public class InstructionAssign extends Instruction {
	
	public final Access access;
	public final Expression expr;
	
	@Override
	public void typeCheck(Environment env) throws TypeException {
		Type tLeft = access.computeType(env);
		Type tRight = expr.computeType(env);
		if(!access.alterable)
			throw new TypeException(access, "Not alterable");
		if(!tRight.canBeCastedInto(tLeft))
			throw new TypeException(access, 
					"Type "+tRight+" is incompatible with "+tLeft);
	}
	
	private InstructionAssign(Expression expr, Access access) {
		this.expr = expr;
		this.access = access;
		setFirstToken(expr.firstTok);
		setLastToken(access.lastTok);
	}
	
	public static InstructionAssign matchInstructionAssign(TokenList toks) 
			throws MatchException {
		Symbol colonEq = Compiler.matchSymbol(toks, SymbolType.COLONEQ);
		toks.gotoFirstOcc(SymbolType.SEMICOLON);
		Symbol semicol = Compiler.matchSymbol(toks, SymbolType.SEMICOLON);
		toks.prev(); toks.prev();
		toks.setBound();
		toks.revert();
		Expression expr = Expression.matchExpression(toks);
		toks.resetBound();
		toks.goTo(colonEq); toks.prev();
		toks.setBound();
		toks.revert();
		Access acc = Access.matchAccess(toks);
		toks.resetBound();
		toks.goTo(semicol); toks.next();
		return new InstructionAssign(expr, acc);
	}

	@Override
	public String toString() {
		return access+" := "+expr+" ;";
	}

	@Override
	public boolean willReturn() throws TypeException {
		return false;
	}
	
	@Override
	public boolean execute(Scope s) {
		if(access.from == null)
			s.updateValue(access.id, expr.value(s));
		else {
			TypeDefined t = (TypeDefined) access.from.getComputedType();
			if(t.getDefinition() instanceof TypeAccess) {
				t = (TypeDefined) ((TypeAccess)t.getDefinition()).type;
			}
			TypeRecord r = ((TypeRecord)t.getDefinition());
			Value v = access.from.value(s);
			if(v instanceof ValueAccess)
				v = ((ValueAccess) v).getVar();
			v.toRecord().setVal(r.getMemberNumber(access.id.name), 
								expr.value(s));
		}
		return false;
	}
}
