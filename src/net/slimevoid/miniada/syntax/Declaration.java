package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.TypeException;

public abstract class Declaration extends SyntaxNode {
	
	public static Declaration matchDeclaration(TokenList toks) 
			throws MatchException {
 		if(toks.nextIsOcc(KeywordType.TYPE))
 			return DeclarationType.matchDeclarationType(toks);
 		if(toks.nextIsOcc(KeywordType.PROCEDURE))
 			return DeclarationProcedure.matchDeclarationProcedure(toks);
 		if(toks.nextIsOcc(KeywordType.FUNCTION))
 			return DeclarationFunction.matchDeclarationFunction(toks);
		return DeclarationVariable.matchDeclarationVariable(toks);
	}
	
	public abstract void typeDeclaration(Environment env) throws TypeException;

	public void init(Environment env) {}
}
