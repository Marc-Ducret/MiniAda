package net.slimevoid.miniada;

import net.slimevoid.miniada.token.*;
import static net.slimevoid.miniada.token.Keyword.KeywordType;
import static net.slimevoid.miniada.token.Symbol.SymbolType.*;

%%
%public
%class Lexer
%unicode
%line
%column
%ignorecase

%{
  public Yytoken localizedToken(Yytoken t) throws LexingException {
  	t.specifyLocation(yyline+1, yycolumn+1, yycolumn + yytext().length());
  	return t;
  }
  
%}
digit = [0-9]
alpha = [a-z] | [A-Z]
integer = 0 | [1-9] {digit}*
char = ' [^] '
ident = ( {alpha} ({alpha} | {digit} | _)* ) | Character'Val
endline = \r|\n|\r\n
whitespace = {endline} | [ \t\f]
comment = - - [^\r\n]*

%state STRING

%%

/* keywords */
<YYINITIAL> {
	{comment}        	{ }
	{integer}			{ return localizedToken(new ConstInt(yytext()));}
	{char}				{ return localizedToken(new ConstChar(yytext().
										charAt(1)));					}
	{ident} {
		for(KeywordType t : KeywordType.values()) {
			if(yytext().equalsIgnoreCase(t.name()))
				return localizedToken(new Keyword(t));
		}
		return localizedToken(new Identifier(yytext()));				 
	}
	".."				{ return localizedToken(new Symbol(DOTDOT));	}
	":="				{ return localizedToken(new Symbol(COLONEQ));	}
	"/="				{ return localizedToken(new Symbol(NEQ));		}
	">="				{ return localizedToken(new Symbol(GE)); 		}
	"<="				{ return localizedToken(new Symbol(LE)); 		}
	"="					{ return localizedToken(new Symbol(EQ)); 		}
	">"					{ return localizedToken(new Symbol(GT)); 		}
	"<"					{ return localizedToken(new Symbol(LT)); 		}
	"+"					{ return localizedToken(new Symbol(PLUS));		}
	"-"					{ return localizedToken(new Symbol(MINUS));		}
	"*"					{ return localizedToken(new Symbol(TIMES)); 	}
	"/"					{ return localizedToken(new Symbol(DIVIDE)); 	}
	"."					{ return localizedToken(new Symbol(DOT)); 		}
	";"					{ return localizedToken(new Symbol(SEMICOLON)); }
	","					{ return localizedToken(new Symbol(COMMA)); 	}
	":"					{ return localizedToken(new Symbol(COLON)); 	}
	"("					{ return localizedToken(new Symbol(LPAR)); 		}
	")"					{ return localizedToken(new Symbol(RPAR));		}
	{whitespace}      	{  }
	
}
/* error fallback */
[^]                              { throw new LexingException(yyline+1, 
															 yycolumn+1,
									"Illegal character <"+yytext()+">"); }