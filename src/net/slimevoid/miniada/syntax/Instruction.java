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
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.TypeException;

public abstract class Instruction extends SyntaxNode {
	
	public abstract void typeCheck(Environment env) throws TypeException;
	public abstract void buildAsm(ASMBuilder build);
	
	public static Instruction matchInstruction(TokenList toks) 
			throws MatchException {
		if(toks.nextIsOcc(KeywordType.RETURN))
			return InstructionReturn.matchInstructionReturn(toks);
		if(toks.nextIsOcc(KeywordType.BEGIN)) {
			Keyword begin = Compiler.matchKeyword(toks, KeywordType.BEGIN);
			InstructionBlock block = matchInstructionBlock(toks, 
															KeywordType.END);
			block.setFirstToken(begin);
			Compiler.matchKeyword(toks, KeywordType.END);
			block.setLastToken(Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
			return block;
		}
		if(toks.nextIsOcc(KeywordType.IF)) {
			return InstructionIf.matchInstructionIf(toks);
		}
		if(toks.nextIsOcc(KeywordType.FOR)) {
			return InstructionFor.matchInstructionFor(toks);
		}
		if(toks.nextIsOcc(KeywordType.WHILE)) {
			return InstructionWhile.matchInstructionWhile(toks);
		}
		if(toks.gotoFirstOcc(SymbolType.SEMICOLON, SymbolType.COLONEQ)) {
			if(toks.nextIsOcc(SymbolType.COLONEQ)) {
				return InstructionAssign.matchInstructionAssign(toks);
			}
			toks.revert();
			return InstructionCall.matchInstructionCall(toks);
		}
		throw new MatchException(toks.cur(), "Expected instruction");
	}
	
	public abstract boolean willReturn() throws TypeException;
	
	/**
	 * @param env
	 * @return true if instruction returned
	 */
	public abstract boolean execute(Scope s);
	
	public static InstructionBlock matchInstructionBlock(TokenList toks, 
			KeywordType...stopWords) 
			throws MatchException {
		List<Instruction> instrs = new ArrayList<>();
		boolean run = true;
		while(run) {
			instrs.add(matchInstruction(toks));
			for(KeywordType k : stopWords)
				if(toks.nextIsOcc(k))
					run = false;
		}
		return new InstructionBlock(
				instrs.toArray(new Instruction[instrs.size()]));
	}
	
	public static class InstructionBlock extends Instruction {
		
		public final Instruction[] instrs;
		
		private InstructionBlock(Instruction[] instrs) {
			this.instrs = instrs;
			setFirstToken(instrs[0].firstTok);
			setLastToken(instrs[instrs.length-1].lastTok);
		}
		
		@Override
		public String toString() {
			String str = "";
			for(Instruction i : instrs) str += i;
			return str;
		}

		@Override
		public void typeCheck(Environment env) throws TypeException {
			for(Instruction instr : instrs) {
				instr.typeCheck(env);
			}
		}

		@Override
		public boolean willReturn() throws TypeException {
			boolean wret = false;
			for(Instruction instr : instrs) {
				if(wret) throw new TypeException(instr, "Dead code");
				if(instr.willReturn()) wret = true;
			}
			return wret;
		}

		@Override
		public boolean execute(Scope localS) {
			for(Instruction i : instrs) {
				if(i.execute(localS)) return true;
			}
			return false;
		}

		@Override
		public void buildAsm(ASMBuilder asm) {
			for(Instruction instr : instrs) instr.buildAsm(asm);
		}
	}
}
