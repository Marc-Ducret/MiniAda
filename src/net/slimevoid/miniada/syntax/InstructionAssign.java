package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

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
	public boolean execute(Environment env) {
		env.setValue(access.id, expr.value(env)); //TODO records
		return false;
	}
}
