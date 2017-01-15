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
	private boolean token = true;;
	
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
					System.out.print("Connection established...");
					while(!token)
						try {
							Thread.sleep(50);
						} catch (InterruptedException e2) {}
					token = false;
					System.out.println("OK-go");
					try {
						DataOutputStream out = new DataOutputStream(
							new BufferedOutputStream(sok.getOutputStream()));
						DataInputStream in = new DataInputStream(
							new BufferedInputStream(sok.getInputStream()));
						try {
							int asmSize = in.readInt();
							byte[] b = new byte[asmSize];
							int read = 0;
							while(read < asmSize) {
								read += in.read(b, read, asmSize-read);
							}
							String asm = new String(b);
							System.out.println("Executing");
							String res = exec.execute(asm);
							System.out.println("Execution complete");
							out.writeBoolean(true);
							out.writeInt(res.length());
							out.writeBytes(res);
						} catch (ExecutionException e) {
							out.writeBoolean(false);
							out.writeInt(e.message.length());
							out.writeBytes(e.message);
							System.out.println("Execution failed : "+e.message);
						}
						out.flush();
					} catch (IOException e) {
						try {
							sok.close();
						} catch (IOException e1) {}
					}
					token = true;
				}
			}).start();
		}
	}
	
	public static void main(String[] args) throws IOException {
		new Server(new LinuxExecuter(new File("tmpExec")), 1337).run();
	}
}
