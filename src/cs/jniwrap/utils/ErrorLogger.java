package cs.jniwrap.utils;

public class ErrorLogger {
	
	public static final void log(String fmt, Object...objects) {
		System.out.printf(fmt + "\n", objects);
	}
}
