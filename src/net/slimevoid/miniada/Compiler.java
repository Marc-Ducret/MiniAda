package net.slimevoid.miniada;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import net.slimevoid.miniada.typing.TypeException;


public class Compiler {
	
	public final static int PASS_LEX = 0x0,
							PASS_STX = 0x1,
							PASS_TYP = 0x2,
							PASS_EXE = 0x3;
	
	public static void main(String[] args) throws IOException {
		Compiler comp = new Compiler();
//		comp.compile(new File("input"), 0x3, 2);
		comp.test();
	}
	
	public void test() throws IOException {
		long startTime = System.nanoTime();
		runTests(new File("tests/syntax/"), 1);
		runTests(new File("tests/typing/"), 2);
		runTests(new File("tests/exec/"), 2);
		runTests(new File("tests/exec-fail/"), 2);
		long elapsed = (System.nanoTime() - startTime)/1000000;
		System.out.println("Testing ended in "+elapsed+" ms");
	}
	
	public boolean compile(File file, int maxPass, int verbose) 
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
						if(maxPass >= PASS_EXE) {
							System.out.println("== EXEC ==");
							src.dproc.execute();
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
				// TODO print the chars
				if(debug) e.printStackTrace();
			}
			return false;
		}
		long elapsed = (System.nanoTime() - startTime)/1000000;
		if(!silent)
			System.out.println("Successful compilation in "+elapsed+" ms");
		return true;
	}
	
	public void runTests(File testFolder, int pass) throws IOException {
		System.out.println("Testing "+testFolder.getName()+" [0x"+pass+"]");
		int ct = 0;
		int ok = 0;
		for(File f : testFolder.listFiles()) { 
			if(!f.getName().endsWith(".adb")) continue;
			ct ++;
			if(compile(f, pass, 0)) ok ++;
			else {
				System.out.println("Failed test "+f.getName());
				compile(f, pass, 1);
			}
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
	
	public TokenList lexicalAnalysis(File file, boolean debug)
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
	
	public SourceFile syntaxAnalysis(TokenList toks, boolean debug)
			throws MatchException {
		if(debug) System.out.println("== SYNTAX ==");
		SourceFile src = SourceFile.matchSourceFile(toks);
		toks.checkStackSize();
		if(debug) System.out.println(src);
		return src;
	}
	
	public void typeAnalysis(SourceFile src, boolean debug)
			throws TypeException {
		if(debug) System.out.println("== TYPE ==");
		Environment env = new Environment(null);
		env.registerNativeProcedure(Librairy.PUT);
		env.registerNativeProcedure(Librairy.NEW_LINE);
		env.registerNativeFunction(Librairy.CHARACTER_VAL);
		src.dproc.typeDeclaration(env);
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
