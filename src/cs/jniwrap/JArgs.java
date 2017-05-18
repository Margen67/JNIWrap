package cs.jniwrap;

import java.util.Vector;
import java.util.function.ObjIntConsumer;

import cs.ArrayUtils.ArrayOps;

public class JArgs {
	private JArg[] m_args;
	public JArgs(String s) throws JVPException {
		Vector<JArg> args = new Vector<>();
		while(!s.isEmpty())
		{
			JArg arg = new JArg();
			s = arg.parse(s);
			args.add(arg);
		}
		m_args = args.toArray(new JArg[args.size()]);
	}
	
	public String toJNICall(JNIClass jc) {
		int argIndex = 0;
		String parmList = "(";
		
		for(JArg arg : m_args) {
			String typeStr = JType.getJNIType(arg.getArgStr());
			parmList += ((parmList.length() == 1) ? "" : ", ") + typeStr + " arg" + argIndex++;
		}
		return parmList + ")";
	}
	public String toJNICallInvocation(JNIClass jc) {
		int argIndex = 0;
		String parmList = "";
		for(JArg arg : m_args) 
			parmList += (parmList.length() == 1 ? "" : ", ") + "arg" + argIndex++;
		
		return parmList;
	}
	public int size() {
		return m_args.length;
	}
	
	public JArg get(int index) {
		return m_args[index]; 
	}
	public void invariants() {
		assert(m_args != null);
		assert(size() != 0);
		//no arguments may be void
		foreachArg((arg, i) -> {arg.invariants(); assert(!arg.isVoid());});

	}
	
	public void foreachArg(int startIndex, ObjIntConsumer<JArg> op) {
		for(int i = startIndex; i < m_args.length; ++i) {
			op.accept(m_args[i], i);
		}
	}
	public void foreachArg(ObjIntConsumer<JArg> op) {
		foreachArg(0, op);
	}
}
