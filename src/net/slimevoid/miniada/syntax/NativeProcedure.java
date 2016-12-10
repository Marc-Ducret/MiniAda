package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.typing.Par;
import net.slimevoid.miniada.typing.TypePrimitive;

public class NativeProcedure extends DeclarationProcedure {
	
	public final String name;
	
	public NativeProcedure(String name, TypePrimitive...pars) {
		super(null, null, null, null);
		this.name = name;
		this.pars = new Par[pars.length];
		for(int i = 0; i < pars.length; i ++) 
			this.pars[i] = new Par(null, pars[i]);
	}
}
