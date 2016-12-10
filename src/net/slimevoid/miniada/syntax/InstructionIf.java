package net.slimevoid.miniada.syntax;

import java.util.ArrayList;
import java.util.List;

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

public class InstructionIf extends Instruction {
	
	public final Expression[] conds;
	public final InstructionBlock[] blocks;
	
	private InstructionIf(Expression[] conds, InstructionBlock[] blocks, 
			Yytoken fst, Yytoken lst) {
		this.conds = conds;
		this.blocks = blocks;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	private static boolean goToTheThen(TokenList toks) throws MatchException {
		if(!toks.gotoFirstOcc(KeywordType.THEN))
			return false;
		// Check AND THEN issue
		toks.prev();
		while(toks.nextIsOcc(KeywordType.AND)) {
			toks.next(); toks.next();
			if(!toks.gotoFirstOcc(KeywordType.THEN))
				return false;
			toks.dropSave(); toks.prev();
		}
		toks.next();
		// end check
		return true;
	}
	
	public static InstructionIf matchInstructionIf(TokenList toks) 
			throws MatchException {
		Keyword iff = Compiler.matchKeyword(toks, KeywordType.IF);
		if(!goToTheThen(toks))
			throw new MatchException(toks.next(), "Expected keyword \"then\"");
		Keyword then = Compiler.matchKeyword(toks, KeywordType.THEN);
		List<Expression> conds = new ArrayList<>();
		List<InstructionBlock> blocks = new ArrayList<>();
		toks.prev(); toks.prev();
		toks.setBound();
		toks.revert();
		conds.add(Expression.matchExpression(toks));
		toks.resetBound();
		toks.goTo(then); toks.next();
		blocks.add(matchInstructionBlock(toks, 	KeywordType.END,
				 								KeywordType.ELSE,
				 								KeywordType.ELSIF));
		while(toks.nextIsOcc(KeywordType.ELSIF)) {
			toks.next();
			if(!goToTheThen(toks))
				throw new MatchException(toks.next(), 
						"Expected keyword \"then\"");
			then = Compiler.matchKeyword(toks, KeywordType.THEN);
			toks.prev(); toks.prev();
			toks.setBound();
			toks.revert();
			conds.add(Expression.matchExpression(toks));
			toks.resetBound();
			toks.goTo(then); toks.next();
			blocks.add(matchInstructionBlock(toks, 	KeywordType.END,
													KeywordType.ELSE,
													KeywordType.ELSIF));
		}
		if(toks.nextIsOcc(KeywordType.ELSE)) {
			toks.next();
			conds.add(null);
			blocks.add(matchInstructionBlock(toks, 	KeywordType.END,
													KeywordType.ELSE,
													KeywordType.ELSIF));
		}
		Compiler.matchKeyword(toks, KeywordType.END);
		Compiler.matchKeyword(toks, KeywordType.IF);
		return new InstructionIf(conds.toArray(new Expression[conds.size()]),
						blocks.toArray(new InstructionBlock[blocks.size()]),
						iff, Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
	}
	
	@Override
	public String toString() {
		String str = "IF("+conds[0]+") THEN "+blocks[0];
		for(int i = 1; i < conds.length; i ++)
			if(conds[i] != null)
				str += "\n"+"ELSIF("+conds[i]+") THEN "+blocks[i];
			else
				str += "\n"+"ELSE "+blocks[i];
		return str+" end if;";
	}

	@Override
	public void typeCheck(Environment env) throws TypeException {
		for(Expression cond : conds) {
			if(cond == null) break;
			Type t = cond.computeType(env);
			if(!t.canBeCastedInto(TypePrimitive.BOOLEAN))
				throw new TypeException(cond, 
						"Expected type Boolean while expression has type "+t);
		}
		for(InstructionBlock block : blocks)
			block.typeCheck(env);
	}

	@Override
	public boolean willReturn() throws TypeException {
		if(conds.length == 0 || conds[conds.length-1] != null) return false;
		for(InstructionBlock block : blocks) {
			if(!block.willReturn()) return false;
		}
		return true;
	}

	@Override
	public boolean execute(Scope s) {
		for(int i = 0; i < conds.length; i ++) {
			Expression cond = conds[i];
			InstructionBlock block = blocks[i];
			if(cond == null || cond.valueBool(s))
				return block.execute(s);
		}
		return false;
	}

	@Override
	public void buildAsm(ASMBuilder build) {
		// TODO Auto-generated method stub
	}
}
