package net.slimevoid.miniada;

import net.slimevoid.miniada.syntax.NativeFunction;
import net.slimevoid.miniada.syntax.NativeProcedure;
import net.slimevoid.miniada.typing.TypePrimitive;

public class Librairy {
	
	public static final NativeProcedure PUT = buildPut();
	public static final NativeProcedure NEW_LINE = buildNewLine();
	public static final NativeFunction CHARACTER_VAL = buildCharVal();
	
	private static NativeProcedure buildPut() {
		NativeProcedure put = new NativeProcedure("Put", 
				                      TypePrimitive.CHARACTER) {
			@Override
			public void execute(Object...args) {
				System.out.print((char) args[0]);
			}
		};
		return put;
	}

	private static NativeProcedure buildNewLine() {
		NativeProcedure newline = new NativeProcedure("New_Line") {
			
			@Override
			public void execute(Object...args) {
				System.out.println();
			}
		};
		return newline;
	}
	
	private static NativeFunction buildCharVal() {
		return new NativeFunction("Character'Val", TypePrimitive.CHARACTER,
				TypePrimitive.INTEGER) {
			
			@Override
			public Object execute(Object[] args) {
				return (char) (int) args[0];
			}
		};
	}
}
