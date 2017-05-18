package cs.jniwrap.utils;

public class FatalError {
	public static void fatal(String fmt, Object...objects) {
		ErrorLogger.log(fmt, objects);
		System.exit(0);
	}
}
