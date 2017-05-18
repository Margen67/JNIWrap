package cs.ArrayUtils;

public final class StringOps {
	public static final String splitAfterFirst(String s, char c) {
		return s.substring(s.indexOf(c) + 1);
	}
	public static final String splitAfterLast(String s, char c) {
		return s.substring(s.lastIndexOf(c) + 1);
	}
	public static final String splitBetween(String s, char start, char end) {
		return s.substring(s.indexOf(start) + 1, s.lastIndexOf(end));
	}
}
