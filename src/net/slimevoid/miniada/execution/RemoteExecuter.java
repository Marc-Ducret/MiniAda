package net.slimevoid.miniada.execution;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

public class RemoteExecuter extends Executer {
	
	private final Socket sok;
	private final DataOutputStream out;
	private final DataInputStream in;
	
	public RemoteExecuter(String ip, int port) throws IOException {
		sok = new Socket(ip, port);
		out = new DataOutputStream(
				new BufferedOutputStream(sok.getOutputStream()));
		in = new DataInputStream(
				new BufferedInputStream(sok.getInputStream()));
	}

	@Override
	public String execute(String asm) throws ExecutionException {
		try {
			out.writeUTF(asm);
			out.flush();
			boolean success = in.readBoolean();
			String res = in.readUTF();
			sok.close();
			if(success) return res;
			else	 	throw new ExecutionException(res);
		} catch (IOException e) {
			throw new ExecutionException("Connection aborted");
		}
	}

	public static void main(String[] args) throws IOException, ExecutionException {
		String asm = "";
		for(String l : 
				Files.readAllLines(new File("../ASMtest/test.s").toPath())) {
			asm += l+"\n";
		}
		System.out.println(new RemoteExecuter("89.156.241.115", 1337)
							.execute(asm));
	}
}
