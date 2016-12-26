package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypePrimitive;

public class InstructionWhile extends Instruction {
	
	public final Expression cond;
	public final InstructionBlock block;
	
	private InstructionWhile(Expression cond, InstructionBlock block, 
			Yytoken fst, Yytoken lst) {
		this.cond = cond;
		this.block = block;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static InstructionWhile matchInstructionWhile(TokenList toks) 
			throws MatchException {
		Keyword whilee = Compiler.matchKeyword(toks, KeywordType.WHILE);
		if(!toks.gotoFirstOcc(KeywordType.LOOP))
			throw new MatchException(toks.cur(), "Expected keyword \"loop\"");
		Keyword loop = Compiler.matchKeyword(toks, KeywordType.LOOP);
		toks.prev(); toks.prev();
		toks.setBound();
		toks.revert();
		Expression cond = Expression.matchExpression(toks);
		toks.resetBound();
		toks.goTo(loop); toks.nextBoundChecked();
		InstructionBlock block = matchInstructionBlock(toks, KeywordType.END);
		Compiler.matchKeyword(toks, KeywordType.END);
		Compiler.matchKeyword(toks, KeywordType.LOOP);
		return new InstructionWhile(cond, block, whilee, 
				Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
	}
	
	@Override
	public String toString() {
		return "while "+cond+" loop "+block+" end loop;";
	}

	@Override
	public void typeCheck(Environment env) throws TypeException {
		Type t = cond.computeType(env);
		if(!t.canBeCastedInto(TypePrimitive.BOOLEAN))
			throw new TypeException(cond, 
					"Expected type Boolean while expression has type "+t);
		block.typeCheck(env);
	}

	@Override
	public boolean willReturn() throws TypeException {
		return false;
	}

	@Override
	public boolean execute(Scope s) {
		while(cond.valueBool(s)) {
			if(block.execute(s)) return true;
		}
		return false;
	}

	@Override
	public void buildAsm(ASMBuilder build, Environment env) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not impl");
	}
}
