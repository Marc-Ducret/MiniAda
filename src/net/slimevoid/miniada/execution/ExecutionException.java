package net.slimevoid.miniada.execution;

public class ExecutionException extends Exception {

	private static final long serialVersionUID = 1L;

	public final String message;

	public ExecutionException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
