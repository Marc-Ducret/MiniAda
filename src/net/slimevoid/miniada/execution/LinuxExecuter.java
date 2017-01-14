package net.slimevoid.miniada.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;

public class LinuxExecuter extends Executer {
	
	private final File temp;
	
	public LinuxExecuter(File temp) {
		this.temp = temp;
	}

	@Override
	public String execute(String asm) throws ExecutionException {
		temp.mkdir();
		File s = new File(temp, "asm.s");
		File o = new File(temp, "asm.o");
		File exe = new File(temp, "asm");
		try {
			s.createNewFile();
			Writer w = new FileWriter(s);
			w.write(asm.replaceAll("\r", ""));
			w.close();
			Process  as = Runtime.getRuntime().exec(
							"as "+temp.getName()+"/"+s.getName()
							+" -o "+temp.getName()+"/"+o.getName());
			if(as.waitFor() != 0) 
				throw new ExecutionException("Assembly failed ");
			Process gcc = Runtime.getRuntime().exec(
					"gcc "+temp.getName()+"/"+o.getName()
					+" -o "+temp.getName()+"/"+exe.getName());
			if(gcc.waitFor() != 0)
				throw new ExecutionException("GCC failed");
			Process p = Runtime.getRuntime().exec("./"+temp.getName()+"/"+exe.getName());
			BufferedReader r = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String ln;
			String res = "";
			while((ln = r.readLine()) != null) res += ln+"\n";
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExecutionException("IOException "+e.getMessage()); 
		} catch (InterruptedException e) {
			throw new ExecutionException("InterruptedException "+e.getMessage()); 
		}
	}
	
	public static void main(String[] args) throws IOException, ExecutionException {
		String asm = "";
		for(String l : Files.readAllLines(new File("test.s").toPath())) {
			asm += l+"\n";
		}
		System.out.println(new LinuxExecuter(new File("tmpExec")).execute(asm));
	}
}
