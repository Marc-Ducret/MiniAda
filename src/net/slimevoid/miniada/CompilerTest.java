package net.slimevoid.miniada;

import java.io.IOException;

public class CompilerTest {
	
	public static void main(String[] args) throws IOException {
		Compiler comp = new Compiler();
//		comp.compile(new File("input.abd"), Compiler.PASS_TYP, 2);
		comp.test("tests/"); 
	}
}
