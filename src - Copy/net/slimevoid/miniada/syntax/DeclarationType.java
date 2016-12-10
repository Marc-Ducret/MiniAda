package net.slimevoid.miniada.syntax;


import java.util.ArrayList;
import java.util.List;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypeRecord;
import net.slimevoid.miniada.typing.TypeRecord.Member;

public class DeclarationType extends Declaration {

	public final Identifier name;
	public final TypeDefinition def;
	
	private DeclarationType(Identifier name, TypeDefinition def) {
		this.name = name;
		this.def = def;
	}
	
	public static DeclarationType matchDeclarationType(TokenList toks) 
			throws MatchException {
		Keyword type = Compiler.matchKeyword(toks, KeywordType.TYPE);
		Identifier name = Compiler.matchIdent(toks);
		TypeDefinition def = null;
		if(toks.nextIsOcc(KeywordType.IS)) {
			toks.next();
			Keyword k = Compiler.matchKeyword(toks, KeywordType.ACCESS, 
													KeywordType.RECORD);
			if(k.type == KeywordType.ACCESS) {
				def = new TypeDefinitionAccess(Compiler.matchIdent(toks));
			} else {
				List<Fields> fields = new ArrayList<>();
				while(!toks.nextIsOcc(KeywordType.END)) {
					fields.add(Fields.matchFields(toks));
				}
				if(fields.size() == 0) throw new MatchException(toks.cur(),
											"Expected at least one field");
				def = new TypeDefinitionRecord(
						fields.toArray(new Fields[fields.size()]));
				Compiler.matchKeyword(toks, KeywordType.END);
				Compiler.matchKeyword(toks, KeywordType.RECORD);
			}
		}
		DeclarationType decl = new DeclarationType(name, def);
		decl.setFirstToken(type);
		decl.setLastToken(Compiler.matchSymbol(toks, SymbolType.SEMICOLON));
		return decl;
	}
	
	@Override
	public void typeDeclaration(Environment env) throws TypeException {
		if(def == null) {
			env.declareType(name);
		} else {
			if(!env.isTypeDeclaredLocally(name))
				env.declareType(name);
			env.defineType(name, def.buildDef(env, env.getType(name)));
		}
	}
	
	@Override
	public String toString() {
		return "type "+name+(def == null ? "" : " is "+def);
	}
	
	public static abstract class TypeDefinition {
		
		public abstract Type buildDef(Environment env, Type type)
				throws TypeException;
	}
	
	public static class TypeDefinitionAccess extends TypeDefinition {
		public final Identifier id;
		
		private TypeDefinitionAccess(Identifier id) {
			this.id = id;
		}
		
		@Override
		public Type buildDef(Environment env, Type type) 
				throws TypeException {
			return env.getAccessForType(id);
		}
		
		@Override
		public String toString() {
			return "access "+id;
		}
	}
	
	public static class TypeDefinitionRecord extends TypeDefinition {
		public Fields[] fields;
		
		private TypeDefinitionRecord(Fields[] fields) {
			this.fields = fields;
		}
		
		@Override
		public Type buildDef(Environment env, Type type)
				throws TypeException {
			List<Member> mems = new ArrayList<>();
			for(Fields f : fields)
				for(Identifier id : f.ids) {
					for(Member m : mems)
						if(m.name.equalsIgnoreCase(id.name))
							throw new TypeException(id, 
									"A field in this record is already "+
									"nammed "+id);
					Type t = f.type.computeType(env);
					if(t == type) throw new TypeException(id,
							"Field can't be of type "+type+" without access "+
							"modifier");
					if(!t.isDefined()) throw new TypeException(id,
							"Type "+t+" is undefined");
					mems.add(new Member(id.name, t));
				}
			return new TypeRecord(mems.toArray(new Member[mems.size()]));
		}
		
		@Override
		public String toString() {
			return "record "+fields+" end record;";
		}
	}
}
