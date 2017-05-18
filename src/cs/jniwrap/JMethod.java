package cs.jniwrap;

import cs.ArrayUtils.ArrayOps;
import cs.ArrayUtils.StringOps;

public final class JMethod extends JMember {
	//private boolean isConstructor;
	private int argCount;
	private JArgs args;
	private JArg returnType;
	private String genericDeclaration;

	private String parseGenericDecl(JNIClass jc, String[] strs) {
		boolean inGeneric = false;

		String genericDecl = "";
		for(int i = 0; (isConstructor ? i : i + 1) < strs.length; ++i ) {
			String s  = strs[i];
			if(s.length() == 0)
				continue;
			if(isConstructor) {
				if(s.contains(jc.packageName() + "("))
					break; //reached method declaration, method not generic
			} else {
				/*
				  if the next element is the function name declaration, stop iterating
					because the current element is the return type and the return type
					might be a generic type instantiation which will be misinterpreted as the
					functions generic parameters
				  
				 */
				if(strs[i + 1].contains(getName() + "("))
					break;
			}
			if(s.charAt(0) == '<') {
				//found generic declaration
				inGeneric = true;
			}
			if(inGeneric)
				genericDecl += s;
			if( s.charAt(s.length() - 1) == '>' && inGeneric)
				break;
			if(inGeneric)
				genericDecl += " ";
		}
		return genericDecl;
	}
	public JMethod(JNIClass jc, JSig sig, String txt) {
		super(jc, sig, txt);
		
		final int indexOfCon = txt.indexOf(jc.packageName() + "(");
		isConstructor = indexOfCon != -1;
		
		//closed parentheses, 0 arguments
		if(txt.contains("()"))
			argCount = 0;
		//no commas, only one argument
		else if(!txt.contains(","))
			argCount = 1;
		//multiple arguments, count the number of commas and add one for the first argument
		else {
			argCount = 1;
			argCount += ArrayOps.reduceString(0, txt, (argc, c) -> (char)c == ',' ? argc + 1 : argc);
		}
		final int parmStartIndex = txt.indexOf('(');
		final int nameStartIndex = txt.lastIndexOf(' ', parmStartIndex) + 1;
		setName(txt.substring(nameStartIndex, parmStartIndex));
		final String genericDecl = parseGenericDecl(jc, txt.split(" "));
		
		if(!genericDecl.isEmpty()) 
			genericDeclaration = genericDecl;
		
		
		if(argCount != 0) {
			String argsStr = StringOps.splitBetween(sig.getSignature(), '(', ')');//sig.getSignature();

			//argsStr = argsStr.substring(argsStr.indexOf('(') + 1, argsStr.lastIndexOf(')'));
			
			try {
				args = new JArgs(argsStr);
			} catch (JVPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		String retStr = StringOps.splitAfterFirst(sig.getSignature(), ')');
		//retStr = retStr.substring(retStr.indexOf(')') + 1);
		returnType = new JArg();
		try {
			returnType.parse(retStr);
		} catch (JVPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public String emitDeclaration(JNIClass jc) {
		String parms = args == null ? "()" : args.toJNICall(jc);
		String retTypeStr = JType.getJNIType(returnType.getArgStr());
		String result = "";
		if(isStatic)
			result += "static ";
		result += (isConstructor ? jc.getClassname() : retTypeStr + " " + getName()) + parms + ";";
		return result;
	}
	public String emitDefinition(JNIClass jc) {
		String parms = args == null ? "()" : args.toJNICall(jc);
		String retTypeStr = JType.getJNIType(returnType.getArgStr());
		String result = "";
		if(isStatic)
			result += "static ";
		result += (isConstructor ? jc.getClassname() : retTypeStr + " " + getName()) + parms + " {";
		result += returnsVoid() ? "" : "return ";
		String uniqueName = "";
		try {
			uniqueName = getUniqueName();
		} catch (LogicFlawException e) {
			// TODO Auto-generated catch block
			System.out.println("Error occurred while emitting definition of JMethod: unique id not set.");
			e.printStackTrace();
			System.exit(1);
		}
		String invocationArgs = (args == null ? ""  : args.toJNICallInvocation(jc));
		result += String.format("%s->%s(%s, %s%s); }", jc.jniGetterName,
				JType.generateMethodCall(sigString(), isStatic), 
				(isStatic ? jc.getClassIdVarName() : "m_jobject"), uniqueName, invocationArgs);
		return result;
	}
	
	public String getUniqueName() throws LogicFlawException {
		String idVarName = "";
		if(isConstructor)
			idVarName = StringOps.splitAfterLast(getName(), '.');
		else
			idVarName = getName();
		if( isOverloaded() ) 
			idVarName += getUniqueID();
		
		return String.format("jid_method_%s", idVarName);
	}
	
	public boolean returnsVoid() {
		return returnType.isVoid();
	}
	
	public String emitIdDeclaration(JNIClass jc) {
		String idVarName = "";
		
		try {
			idVarName = getUniqueName();
		} catch (LogicFlawException e) {
			System.out.println(
					"Attempted to emit id declaration of overloaded method " + getName() + " with no unique id!"
					);
			e.printStackTrace();
			System.exit(0);
		}
		String result = "static jmethodID " + idVarName + " = nullptr;";
		return result;
	}
	
	public String emitIdInitialization(JNIClass jc) {
		String callee = isStatic ? "GetStaticMethodID" : "GetMethodID";
		
		String methodIdName = "";
		try {
			methodIdName = getUniqueName();
		} catch (LogicFlawException e) {
			System.out.println(
					"Attempted to emit id initialization of overloaded method " + getName() + " with no unique id!"
					);
			e.printStackTrace();
			System.exit(1);
		}
		
		String parmListForCallee = "(cls, \"" + (isConstructor ? "<init>" : getName()) + "\", \"" + sigString() + "\"" + ")";
		return methodIdName + " = env->" + callee + parmListForCallee + ";";

	}
	
	public String[] referencedTypes() {
		if(args == null) {
			final String result[] = new String[1];
			result[0] = JMember.resolveTypeReferenceNoPrim(returnType.getArgStr());
			return result;
		}
		final String result[] = new String[args.size() + 1];
		
		final String argResult = JMember.resolveTypeReferenceNoPrim(returnType.getArgStr());
		if(argResult != null)
			result[0] = argResult;
		args.foreachArg((arg, i) -> 
			result[ argResult == null ? i : i + 1 ] = JMember.resolveTypeReferenceNoPrim(arg.getArgStr())
		);
		return result;

	}
	public void invariants() {
		
		assert((argCount == 0 && args == null) || 
				(argCount != 0 && args != null));
		if(args != null)
			args.invariants();
		
		//boolean returnTypeValidity = returnType != null && returnType.invariants();
		assert(returnType != null);
		returnType.invariants();
		//if it is a constructor, the return type must be void
		//also, constructors may not be synchronized, static or native
		assert(!isConstructor ||
				(isConstructor && !isStatic && !isNative && !isSynchronized && returnType.isVoid()));
		assert(!isTransient && !isVolatile && getName() != null && getName().length() != 0);
		assert(getSignature() != null);
		getSignature().invariants();
	}
}
