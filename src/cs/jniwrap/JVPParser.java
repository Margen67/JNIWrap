package cs.jniwrap;

import java.util.Vector;

public final class JVPParser {
	/*
	 * output of the javap command 
	 */
	private Vector<String> m_cmd_output;
	
	public JVPParser(Vector<String> cmd_output) throws JVPException {
		if(cmd_output == null)
			throw new JVPException("Parser received null command output!");
		
		setCmdOutput(cmd_output);
	}
	private Vector<String> getCmdOutput() {
		return m_cmd_output;
	}
	private void setCmdOutput(Vector<String> m_cmd_output) throws JVPException {
		if(getCmdOutput() != null) {
			throw new JVPException("Attempted to set already initialized command output.");
		}
		this.m_cmd_output = m_cmd_output;
	}
	/*
	 * returns the full text of the C++ jni wrapper for writing to a file
	 */
	public JNIClass parse(String fileName) {
		JNIClass clz = new JNIClass(getCmdOutput(), fileName);
		
		
		return clz;
	}
}
