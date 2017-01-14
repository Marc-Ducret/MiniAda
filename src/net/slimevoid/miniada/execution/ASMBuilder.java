package net.slimevoid.miniada.execution;

import java.util.Stack;

public class ASMBuilder {
	
	public static final String OP_S = "q";
	
	private static final String[] labels = new String[]{"blue", "red", "green",
			"white", "black", "turtle", "clock", "fire", "water", "storm"};
	
	private static final String[] dataNames = new String[]{"train", "car", 
			"plane", "dice", "mushroom", "tree", "flower"};
	
	public static enum Register implements ASMOperand {
		RAX, RBX, RCX, RDX, RBP, RSP, RSI, RDI,
		R8, R9, R10, R11, R12, R13, R14, R15;

		@Override
		public void appendToBuilder(StringBuilder buff) {
			buff.append('%').append(this.name().toLowerCase());
		}

		@Override
		public void pre(ASMBuilder asm) {
		}

		@Override
		public void post(ASMBuilder asm) {
		}
	}
	
	private Register[] tmpRegs = new Register[]{
			Register.R8 , Register.R9 , Register.R10, Register.R11,
			Register.R12, Register.R13, Register.R14, Register.R15};
	private boolean[] tmpRegUsage = new boolean[tmpRegs.length];
	
	private int labelId = 0;
	private int dataId = 0;
	
	private StringBuilder txt;
	private StringBuilder data;
	
	private Stack<ASMRoutine> toBuild = new Stack<>();
	
	public ASMBuilder() {
		txt = new StringBuilder();
		data = new StringBuilder();
	}
	
	public void label(String label) {
		txt.append(label).append(":\n");
	}
	
	public void call(String label) {
		txt.append("\tcall ").append(label).append('\n');
	}
	
	public void jmp(String label) {
		txt.append("\tjmp ").append(label).append('\n');
	}
	
	public void jz(String label) {
		jflag(label, "z");
	}
	
	public void jflag(String label, String flag) {
		txt.append("\tj"+flag+" ").append(label).append('\n');
	}
	
	public void main(String label) {
		txt.append("\t.globl ").append(label).append('\n');
	}
	
	public void binaryInstr(String name, ASMOperand from, ASMOperand to) {
		from.pre(this);
		to.pre(this);
		txt.append('\t').append(name).append(OP_S).append(' ');
		from.appendToBuilder(txt);
		txt.append(", ");
		to.appendToBuilder(txt);
		txt.append('\n');
		from.post(this);
		to.post(this);
	}
	
	public void unaryInstr(String name, ASMOperand op) {
		op.pre(this);
		txt.append('\t').append(name).append(OP_S).append(' ');
		op.appendToBuilder(txt);
		txt.append('\n');
		op.post(this);
	}
	
	public void set(String flag, Register r) {
		txt.append('\t').append("set").append(flag).append(" %").append(r.name().toLowerCase()).append('b');
		txt.append('\n');
	}
	
	private void arglessInstr(String name) {
		txt.append('\t').append(name).append('\n');
	}
	
	public void mov(ASMOperand from, ASMOperand to) {
		binaryInstr("mov", from, to);
	}
	
	public void sub(ASMOperand from, ASMOperand to) {
		binaryInstr("sub", from, to);
	}
	
	public void add(ASMOperand from, ASMOperand to) {
		binaryInstr("add", from, to);
	}
	
	public void test(ASMOperand from, ASMOperand to) {
		binaryInstr("test", from, to);
	}
	
	public void cmp(ASMOperand opA, ASMOperand opB) {
		binaryInstr("cmp", opA, opB);
	}
	
	public void push(ASMOperand op) {
		unaryInstr("push", op);
	}
	
	public void pop(ASMOperand op) {
		unaryInstr("pop", op);
	}
	
	public void not(ASMOperand op) {
		unaryInstr("noq", op);
	}
	
	public void neg(ASMOperand op) {
		unaryInstr("neg", op);
	}
	
	public void ret() {
		arglessInstr("ret");
	}
	
	public void comment(String line) {
		if(!line.contains("\n")) txt.append("\t# ").append(line).append('\n');
	}
	
	public ASMData registerString(String str) {
		ASMData d = new ASMData(newDataName());
		data.append(d.name).append(":\n\t.string \"").append(str)
			.append("\"\n");
		return d;
	}
	
	public Register getTmpReg() {
		for(int i = 0; i < tmpRegs.length; i ++) {
			if(!tmpRegUsage[i]) {
				tmpRegUsage[i] = true;
				return tmpRegs[i];
			}
		}
		throw new RuntimeException("No tempoary register free");
	}
	
	public void freeTempRegister(Register reg) {
		for(int i = 0; i < tmpRegs.length; i++)
			if(tmpRegs[i] == reg) tmpRegUsage[i] = false;
	}
	
	public String newLabel() {
		int id = labelId++;
		int n = labels.length;
		if(id >= n) return labels[id % n] + '_' + (id / n);
		else		return labels[id % n];
	}
	
	public String newDataName() {
		int id = dataId++;
		int n = dataNames.length;
		if(id >= n) return dataNames[id % n] + '_' + (id / n);
		else		return dataNames[id % n];
	}
	
	public String builtAsm() {
		return "\t.text\n"+txt.toString()+"\n\t.data\n"+data.toString();
	}
	
	public void planBuild(ASMRoutine rout) {
		if(!rout.isPlanned()) {
			toBuild.push(rout);
			rout.setPlanned();
		}
	}
	
	public void build() {
		while(!toBuild.isEmpty()) {
			toBuild.pop().buildASM(this);
		}
	}
}
