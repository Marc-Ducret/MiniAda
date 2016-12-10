package net.slimevoid.miniada;

import java.io.PrintStream;

import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.execution.ASMData;
import net.slimevoid.miniada.execution.ASMBuilder.Registers;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValuePrimitive;
import net.slimevoid.miniada.syntax.NativeFunction;
import net.slimevoid.miniada.syntax.NativeProcedure;
import net.slimevoid.miniada.typing.TypePrimitive;

public class Library {
	
	public static final NativeProcedure PUT = buildPut();
	public static final NativeProcedure NEW_LINE = buildNewLine();
	public static final NativeFunction CHARACTER_VAL = buildCharVal();
	
	private static PrintStream out = System.out;
	
	private static NativeProcedure buildPut() {
		NativeProcedure put = new NativeProcedure("Put", 
				                      TypePrimitive.CHARACTER) {
			@Override
			public void execute(Scope s, Value...args) {
				out.print(args[0].toChar());
			}
			
			@Override
			public void buildASM(ASMBuilder asm) {
				super.buildASM(asm);
				String msg = asm.newDataName();
				asm.registerString(msg, "%c");
				asm.mov(new ASMData(msg), Registers.RDI);
				asm.mov(new ASMConst(0), Registers.RAX);
				asm.call("printf");
				asm.ret();
			}
		};
		return put;
	}

	private static NativeProcedure buildNewLine() {
		NativeProcedure newline = new NativeProcedure("New_Line") {
			
			@Override
			public void execute(Scope s, Value...args) {
				out.println();
			}
		};
		return newline;
	}
	
	private static NativeFunction buildCharVal() {
		return new NativeFunction("Character'Val", TypePrimitive.CHARACTER,
				TypePrimitive.INTEGER) {
			
			@Override
			public Value execute(Scope s, Value...args) {
				return new ValuePrimitive((char) args[0].toInt());
			}
		};
	}
	
	protected static void setOutput(PrintStream out) {
		Library.out = out;
	}
}