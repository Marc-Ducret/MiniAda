package net.slimevoid.miniada;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.syntax.MatchException;
import net.slimevoid.miniada.syntax.SourceFile;
import net.slimevoid.miniada.token.EOF;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Environment.NameSpace;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypePrimitive;


public class Compiler {
	
	public final static int PASS_LEX = 0x0,
							PASS_STX = 0x1,
							PASS_TYP = 0x2,
							PASS_ASM = 0x3,
							PASS_EXE = 0x4;
	
	public static void main(String[] args) {
		try {
			Compiler comp = new Compiler();
			if(args.length > 0) {
				String a = args[0];
				int pass = PASS_ASM;
				String source = "";
				if(a.startsWith("-")) {
					if(a.equalsIgnoreCase("--parse-only")) {
						pass = PASS_STX;
					} else if(a.equalsIgnoreCase("--type-only")) {
						pass = PASS_TYP;
					} else {
						System.err.println("Unknown parameter \""+a+"\"");
						System.exit(-1);
					}
					if(args.length > 1) source = args[1];
					else {
						System.err.println("No source file specified");
						System.exit(-1);
					}
				} else {
					source = args[0];
				}
				try {
					if(!source.endsWith(".abd")) {
						System.err.println("Source file must end with \".adb\"");
						System.exit(-1);
					}
					if(comp.compile(new File(source), pass, 1)) {
						
					} else {
						
					}
 				} catch(IOException e) {
 					System.err.println("IO Exception: "+e.toString()+" "+
 							(e.getMessage() == null ? "" : e.getMessage()));
 					System.exit(-1);
 				}
			} else {
				System.err.println("No source file specified");
				System.exit(-1);
			}
		} catch(Exception e) {
			System.err.println("Unhandeled exception:");
			e.printStackTrace();
			System.exit(2);
		}
		
//        comp.test("C:\\Users\\Marc\\Documents\\GitHub\\Maison-close\\"); 
		//TODO change
	}
	
	public void test(String tests) throws IOException {
		long startTime = System.nanoTime();
		runTests(new File(tests+"syntax/"), 1);
		runTests(new File(tests+"typing/"), 2);
		runTests(new File(tests+"exec/"), 3);
		runTests(new File(tests+"exec-fail/"), 2);
		long elapsed = (System.nanoTime() - startTime)/1000000;
		System.out.println("Testing ended in "+elapsed+" ms");
	}
	
	public boolean compile(File file, int maxPass, int verbose) 
			throws IOException {
		return compile(file, maxPass, verbose, System.out);
	}
	
	public boolean compile(File file, int maxPass, int verbose, PrintStream out) 
			throws IOException {
		boolean debug = verbose > 1;
		boolean silent = verbose < 1;
		long startTime = System.nanoTime();
		try {
			if(maxPass >= PASS_LEX) {
				TokenList toks = lexicalAnalysis(file, debug);
				if(maxPass >= PASS_STX) {
					SourceFile src = syntaxAnalysis(toks, debug);
					if(maxPass >= PASS_TYP) {
						typeAnalysis(src, debug);
						if(maxPass >= PASS_ASM) {
							out.print(buildAsm(src, debug));
						}
					}
				}
			}
		} catch (LocalizedException e) {
			if(!silent) {
				System.err.println(
				 "File \""+file.getName()+"\", line "+e.getLine()
				+", characters "+e.getColStart()+"-"+e.getColEnd()+":\n"
				+e.phase+" error\n"
				+e.message);
				if(debug) e.printStackTrace();
			}
			return false;
		}
		long elapsed = (System.nanoTime() - startTime)/1000000;
		if(!silent)
			System.out.println("Successful compilation in "+elapsed+" ms");
		return true;
	}
	
	private void runTests(File testFolder, int pass) throws IOException {
		if(!testFolder.exists()) {
			System.out.println("No folder "+testFolder.getAbsolutePath());
			return;
		}
		System.out.println("Testing "+testFolder.getName()+" [0x"+pass+"]");
		int ct = 0;
		int ok = 0;
		for(File f : testFolder.listFiles()) { 
			if(!f.getName().endsWith(".adb")) continue;
			ct ++;
			ByteArrayOutputStream buff = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(buff);
			if(compile(f, pass, 0, out)) {
				boolean success = true;
				String res = new String(buff.toByteArray(), 
						StandardCharsets.UTF_8);
				res = res.replaceAll("\r\n", "\n");
				String expected = "";
				if(pass >= PASS_EXE) {
					String n = f.getName();
					FileReader read = new FileReader(
							new File(testFolder, 
									n.substring(0, n.length()-3)+"out"));
					int r;
					int i = 0;
					while((r = read.read()) >= 0) {
						if((char) r == '\r') continue;
						expected += (char) r;
						if(i >= res.length())
							success = false;
						else if(res.charAt(i) != (char)r)
							success = false;
						i++;
					}
					read.close();
				}
				if(success) ok++;
				else {
					System.out.println("Failed execution "+f.getName());
					System.out.println("Print:");
					System.out.println(res);
					System.out.println("Expected:");
					System.out.println(expected);
				}
			} else {
				System.out.println("Failed compilation "+f.getName());
				compile(f, pass, 1, out);
			}
			buff.close();
		}
		File good = new File(testFolder, "good");
		if(good.exists())
			for(File f : good.listFiles()) {
				ct ++;
				if(compile(f, pass, 0)) ok ++;
				else {
					System.out.println("Failed test "+f.getName());
					compile(f, pass, 1);
				}
			}
		File bad = new File(testFolder, "bad");
		if(bad.exists())
			for(File f : bad.listFiles()) {
				ct ++;
				if(!compile(f, pass, 0)) ok ++;
				else {
					System.out.println("Failed test "+f.getName());
				}
			}
		System.out.println("score: "+ok+"/"+ct);
	}
	
	private TokenList lexicalAnalysis(File file, boolean debug)
			throws IOException {
		if(debug) System.out.println("== LEXING ==");
		Lexer lex = new Lexer(new FileReader(file));
		List<Yytoken> list = new ArrayList<>();
		Yytoken t;
		while((t = lex.yylex()) != null) {
			list.add(t);
		}
		list.add(new EOF());
		TokenList toks = new TokenList(list.toArray(new Yytoken[list.size()]));
		if(debug) {
			toks.savePos();
			try {
				int i = 0;
				while(true) System.out.println((i++)+"\t:"+toks.next());
			} catch(Exception e) {}
			toks.revert();
		}
		return toks;
	}
	
	private SourceFile syntaxAnalysis(TokenList toks, boolean debug)
			throws MatchException {
		if(debug) System.out.println("== SYNTAX ==");
		SourceFile src = SourceFile.matchSourceFile(toks);
		toks.checkStackSize();
		if(debug) System.out.println(src);
		return src;
	}
	
	private void typeAnalysis(SourceFile src, boolean debug)
			throws TypeException {
		if(debug) System.out.println("== TYPE ==");
		Environment env = new Environment(null) {
			@Override
			public Type getType(Identifier id) throws TypeException {
				try {
					return super.getType(id);
				} catch(TypeException e) {
					TypePrimitive prim = TypePrimitive.getPrimitive(id.name);
					if(prim == null) throw e; 
					return prim;
				}
			}
		};
		env.registerNativeProcedure(Library.PUT);
		env.registerNativeProcedure(Library.NEW_LINE);
		env.registerNativeFunction(Library.CHARACTER_VAL);
		for(TypePrimitive t : TypePrimitive.primitives)
			env.useName(new Identifier(t.getName()), NameSpace.TYPE);
		src.dproc.typeDeclaration(env);
	}
	
	private String buildAsm(SourceFile src, boolean debug) {
		if(debug) System.out.println("== ASM ==");
		ASMBuilder asm = new ASMBuilder();
		asm.main(src.dproc.getLabel(asm));
		asm.planBuild(src.dproc);
		asm.build();
		return asm.builtAsm();
	}
	
	public static void errorMatch(Yytoken loc, String message) 
			throws MatchException {
		throw new MatchException(loc, message);
	}
	
	public static Keyword matchKeyword(TokenList toks, KeywordType...types) 
			throws MatchException {
		Yytoken tok = toks.next();
		String strTypes = "";
		for(KeywordType kt : types) {
			strTypes += kt.name()+", ";
		}
		strTypes = strTypes.substring(0, strTypes.length()-2);
		if(tok instanceof Keyword) {
			Keyword k = (Keyword) tok;
			for(KeywordType kt : types)
				if(kt == k.type) return k;
		}
		Compiler.errorMatch(tok, "Expected keyword ("+strTypes+")");
		return null;
	}
	
	public static Symbol matchSymbol(TokenList toks, SymbolType...types) 
			throws MatchException {
		Yytoken tok = toks.next();
		String strTypes = "";
		for(SymbolType kt : types) {
			strTypes += kt.name()+", ";
		}
		strTypes = strTypes.substring(0, strTypes.length()-2);
		if(tok instanceof Symbol) {
			Symbol s = (Symbol) tok;
			for(SymbolType st : types)
				if(st == s.type) return s;
		}
		Compiler.errorMatch(tok, "Expected symbol ("+strTypes+")");
		return null;
	}
	
	public static Identifier matchIdent(TokenList toks) 
			throws MatchException {
		Yytoken tok = toks.next();
		if(tok instanceof Identifier) {
			return (Identifier) tok;
		}
		Compiler.errorMatch(tok, "Expected identifier");
		return null;
	}
	
	public static EOF matchEOF(TokenList toks) 
			throws MatchException {
		Yytoken tok = toks.next();
		if(tok instanceof EOF) {
			return (EOF) tok;
		}
		Compiler.errorMatch(tok, "Expected EOF");
		return null;
	}
}
