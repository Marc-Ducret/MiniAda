package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;

public class SourceFile extends SyntaxNode {
	
	public final DeclarationProcedure dproc;
	
	private SourceFile(DeclarationProcedure dproc, Yytoken fst, Yytoken lst) {
		this.dproc = dproc;
		setFirstToken(fst);
		setLastToken(lst);
	}
	
	public static SourceFile matchSourceFile(TokenList toks) 
			throws MatchException {
		Keyword with = Compiler.matchKeyword(toks, KeywordType.WITH);
		Identifier ada = Compiler.matchIdent(toks);
		if(!ada.name.equalsIgnoreCase("ADA"))
			throw new MatchException(ada, "Expected identifier Ada");
		Compiler.matchSymbol(toks, SymbolType.DOT);
		Identifier text_io = Compiler.matchIdent(toks);
		if(!text_io.name.equalsIgnoreCase("TEXT_IO"))
			throw new MatchException(text_io, "Expected identifier Text_IO");
		Compiler.matchSymbol(toks, SymbolType.SEMICOLON);
		Compiler.matchKeyword(toks, KeywordType.USE);
		Identifier ada2 = Compiler.matchIdent(toks);
		if(!ada2.name.equalsIgnoreCase("ADA"))
			throw new MatchException(ada2, "Expected identifier Ada");
		Compiler.matchSymbol(toks, SymbolType.DOT);
		Identifier text_io2 = Compiler.matchIdent(toks);
		if(!text_io2.name.equalsIgnoreCase("TEXT_IO"))
			throw new MatchException(text_io2, "Expected identifier Text_IO");
		Compiler.matchSymbol(toks, SymbolType.SEMICOLON);
		DeclarationProcedure dproc = 
				DeclarationProcedure.matchDeclarationProcedure(toks);
		Compiler.matchEOF(toks);
		return new SourceFile(dproc, with, dproc.lastTok);
	}
	
	@Override
	public String toString() {
		return "with Ada.Text_IO; use Ada.Text_IO;\n"+dproc;
	}
}
