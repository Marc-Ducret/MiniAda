package net.slimevoid.miniada;

import java.io.File;
import java.io.IOException;

public class CompilerTest {
	
	public static void main(String[] args) throws IOException {
		Compiler comp = new Compiler();
		comp.compile(new File("input"), Compiler.PASS_TYP, 2);
//		comp.test("C:\\Users\\Marc\\Documents\\GitHub\\Maison-close\\"); 
	}
}
