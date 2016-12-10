package net.slimevoid.miniada;

import java.io.IOException;

public abstract class LocalizedException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public abstract int getLine();
	public abstract int getColStart();
	public abstract int getColEnd();
	
	public final String phase;
	public final String message;
	
	public LocalizedException(String phase, String message) {
		this.phase = phase;
		this.message = message;
	}
}
