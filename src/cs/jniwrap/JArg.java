package cs.jniwrap;

public class JArg {
	private String argStr;
	
	public JArg() {
		setArgStr(null);
	}
	private int parseObject(String s, int start) throws JVPException {
		if(s.charAt(start) != 'L')
			throw new JVPException("parseObject's starting index must point to the beginning of the object sig.");
		return s.indexOf(';', start);
	}
	private int parseArray(String s, int start) throws JVPException {
		if(s.charAt(start) != '[')
			throw new JVPException("parseArray's starting index needs to be at the beginning of the array sig.");
		if(JType.isPrimitive(s.charAt(start + 1)))
			return start + 1;
		else {
			if(s.charAt(start + 1) == '[')
				return parseArray(s, start + 1);
			else {
				return parseObject(s, start+1);
			}
		}
	}
	public boolean isVoid() {
		return argStr.charAt(0) == 'V';
	}
	public String parse(String argPos) throws JVPException {
		int argumentEndIndex = 0;
		char c = argPos.charAt(0);
		if(JType.isPrimitive(c))
			argumentEndIndex = 0;
		else {
			if(c == '[')
				argumentEndIndex = parseArray(argPos, 0);
			else
				argumentEndIndex = parseObject(argPos, 0);
		}
		argumentEndIndex = (argumentEndIndex + 1 < argPos.length()) ? argumentEndIndex + 1 : argPos.length();
		setArgStr(argPos.substring(0, argumentEndIndex));
		
		return argPos.substring(argumentEndIndex);
	}
	public String getArgStr() {
		return argStr;
	}
	private void setArgStr(String argStr) {
		this.argStr = argStr;
	}
	
	public void invariants() {
		assert(argStr != null);
		assert(argStr.length() != 0);
	}
}
