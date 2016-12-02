package net.slimevoid.miniada.execution;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private ServerSocket serv;
	private final Executer exec;
	
	public Server(Executer exec, int port) throws IOException {
		serv = new ServerSocket(port);
		System.out.println("Listening at "+serv.getLocalSocketAddress());
		this.exec = exec;
	}
	
	public void run() throws IOException {
		while(true) {
			Socket sok = serv.accept();
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("Connection established");
					try {
						DataOutputStream out = new DataOutputStream(
							new BufferedOutputStream(sok.getOutputStream()));
						DataInputStream in = new DataInputStream(
							new BufferedInputStream(sok.getInputStream()));
						try {
							String asm = in.readUTF();
							String res = exec.execute(asm);
							out.writeBoolean(true);
							out.writeUTF(res);
						} catch (ExecutionException e) {
							out.writeBoolean(false);
							out.writeUTF(e.message);
						}
						out.flush();
					} catch (IOException e) {
						try {
							sok.close();
						} catch (IOException e1) {}
					}
				}
			}).start();
		}
	}
	
	public static void main(String[] args) throws IOException {
		new Server(new LinuxExecuter(new File("tmpExec")), 1337).run();
	}
}
