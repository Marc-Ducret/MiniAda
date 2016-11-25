package net.slimevoid.miniada.syntax;

import static java.lang.Math.max;
import static java.lang.Math.min;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.SubEnvironment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypePrimitive;
import net.slimevoid.miniada.token.Yytoken;

public class InstructionFor extends Instruction {
	
	public final Identifier var;
	public final Expression from, to;
	public final boolean reverse;
	public final InstructionBlock block;
	
	private InstructionFor(Identifier var, Expression from, Expression to,
			boolean reverse, InstructionBlock block, 
			Yytoken fst, Yytoken lst) {
		this.var = var;
		this.from = from;
		this.to = to;
		this.reverse = reverse;
		this.block = block;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static InstructionFor matchInstructionFor(TokenList toks) 
			throws MatchException {
		Keyword forr = Compiler.matchKeyword(toks, KeywordType.FOR);
		Identifier var = Compiler.matchIdent(toks);
		Compiler.matchKeyword(toks, KeywordType.IN);
		boolean reverse = toks.nextIsOcc(KeywordType.REVERSE);
		if(reverse) toks.next();
		if(!toks.gotoFirstOcc(SymbolType.DOTDOT))
			throw new MatchException(toks.cur(), "Expected \"..\"");
		Symbol dot = Compiler.matchSymbol(toks, SymbolType.DOTDOT);
		toks.prev(); toks.prev();
		toks.setBound();
		toks.revert();
		Expression from = Expression.matchExpression(toks);
		toks.resetBound();
		toks.goTo(dot); toks.next();
		if(!toks.gotoFirstOcc(KeywordType.LOOP))
			throw new MatchException(toks.next(), "Expected keyword \"loop\"");
		Keyword loop = Compiler.matchKeyword(toks, KeywordType.LOOP);
		toks.prev(); toks.prev();
		toks.setBound();
		toks.revert();
		Expression to = Expression.matchExpression(toks);
		toks.resetBound();
		toks.goTo(loop); toks.next();
		InstructionBlock block = matchInstructionBlock(toks, KeywordType.END);
		Compiler.matchKeyword(toks, KeywordType.END);
		Compiler.matchKeyword(toks, KeywordType.LOOP);
		return new InstructionFor(var, from, to, reverse, block, forr, 
				Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
	}
	
	@Override
	public String toString() {
		return "while "+from+" loop "+block+" end loop;";
	}

	@Override
	public void typeCheck(Environment env) throws TypeException {
		Type t = from.computeType(env);
		if(!t.canBeCastedInto(TypePrimitive.INTEGER))
			throw new TypeException(from, 
					"Expected type Integer while expression has type "+t);
		t = to.computeType(env);
		if(!t.canBeCastedInto(TypePrimitive.INTEGER))
			throw new TypeException(from, 
					"Expected type Integer while expression has type "+t);
		SubEnvironment localEnv = new SubEnvironment(env, env.expectedReturn);
		localEnv.registerVar(var, TypePrimitive.INTEGER);
		localEnv.restricAlteration(var);
		block.typeCheck(localEnv);
	}

	@Override
	public boolean willReturn() throws TypeException {
		return false;
	}
	
	@Override
	public boolean execute(Environment env) {
		int di = reverse ? -1 : 1;
		int i = from.valueInt(env);
		int bound = to.valueInt(env);
		int min = min(i, bound);
		int max = max(i, bound);
		while(i >= min && i <= max) {
			env.setValue(var, i);
			if(block.execute(env)) return true;
			i += di;
		}
		return false;
	}
}
