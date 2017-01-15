package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.execution.ASMVar;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.SubEnvironment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypePrimitive;

public class InstructionFor extends Instruction {
	
	public final Identifier var;
	public final Expression from, to;
	public final boolean reverse;
	public final InstructionBlock block;
	
	private SubEnvironment localEnv;
	
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
		if(reverse) toks.nextBoundChecked();
		if(!toks.gotoFirstOcc(SymbolType.DOTDOT))
			throw new MatchException(toks.cur(), "Expected \"..\"");
		Symbol dot = Compiler.matchSymbol(toks, SymbolType.DOTDOT);
		toks.prev(); toks.prev();
		toks.setBound();
		toks.revert();
		Expression from = Expression.matchExpression(toks);
		toks.resetBound();
		toks.goTo(dot); toks.nextBoundChecked();
		if(!toks.gotoFirstOcc(KeywordType.LOOP))
			throw new MatchException(toks.cur(), "Expected keyword \"loop\"");
		Keyword loop = Compiler.matchKeyword(toks, KeywordType.LOOP);
		toks.prev(); toks.prev();
		toks.setBound();
		toks.revert();
		Expression to = Expression.matchExpression(toks);
		toks.resetBound();
		toks.goTo(loop); toks.nextBoundChecked();
		InstructionBlock block = matchInstructionBlock(toks, KeywordType.END);
		Compiler.matchKeyword(toks, KeywordType.END);
		Compiler.matchKeyword(toks, KeywordType.LOOP);
		return new InstructionFor(var, from, to, reverse, block, forr, 
				Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
	}
	
	@Override
	public String toString() {
		return "for "+var+" in "+from+".."+to+" loop "+block+" end loop;";
	}

	@Override
	public void typeCheck(Environment env) throws TypeException {//TODO check if changes altered typing
		localEnv = new SubEnvironment(env, env.expectedReturn);
		localEnv.returnLoc = env.returnLoc;
		Type t = from.getType(env);
		if(!t.canBeCastedInto(TypePrimitive.INTEGER))
			throw new TypeException(from, 
					"Expected type Integer while expression has type "+t);
		t = to.getType(env);
		if(!t.canBeCastedInto(TypePrimitive.INTEGER))
			throw new TypeException(from, 
					"Expected type Integer while expression has type "+t);
		localEnv.offset(Compiler.WORD);
		localEnv.registerVar(var);
		localEnv.setVarType(var, TypePrimitive.INTEGER);
		localEnv.restricAlteration(var); 
		block.typeCheck(localEnv);
	}

	@Override
	public boolean willReturn() throws TypeException {
		return false;
	}
	
	@Override
	public boolean execute(Scope s) {
		int di = reverse ? -1 : 1;
		int min = from.valueInt(s);
		int max =   to.valueInt(s);
		int i = reverse ? max : min;
		while(i >= min && i <= max) {
			s.setValuePrim(var, i);
			if(block.execute(s)) return true;
			i += di;
		}
		return false;
	}

	@Override
	public void buildAsm(ASMBuilder asm, Environment env) {
		String start = asm.newLabel();
		String end = asm.newLabel();
		(reverse ? to : from).buildAsm(asm, env);
		(reverse ? from : to).buildAsm(asm, env);
		Register rInit = asm.getTmpReg();
		Register rBound = asm.getTmpReg();
		asm.pop(rBound);
		asm.pop(rInit);
		asm.push(Register.RBP);
		asm.mov(Register.RSP, Register.RBP);
		asm.sub(new ASMConst(localEnv.getOffset()-Compiler.WORD), Register.RSP);
		asm.push(rBound);
		ASMVar i = new ASMVar(var, localEnv);
		asm.mov(rInit, i);
		asm.freeTempRegister(rInit);
		asm.freeTempRegister(rBound);
		asm.label(start);
		Register r = asm.getTmpReg();
		asm.pop(r);
		asm.push(r);
		asm.cmp(r, i);
		asm.freeTempRegister(r);
		asm.jflag(end, reverse ? "l" : "g");
		block.buildAsm(asm, localEnv);
		asm.unaryInstr(reverse ? "dec" : "inc", i);
		asm.jmp(start);
		asm.label(end);
		asm.add(new ASMConst(localEnv.getOffset()-Compiler.WORD+Compiler.WORD), Register.RSP);
		asm.pop(Register.RBP);
	}
}
