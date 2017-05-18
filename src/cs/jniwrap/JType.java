package cs.jniwrap;

import java.util.HashMap;

import cs.ArrayUtils.CollectionUtils;

public class JType {
	private static final char primitiveTypes[] = {
			'Z',
			'B',
			'C',
			'S',
			'I',
			'J',
			'F',
			'D',
			'V'
	};
	
	private static final HashMap<Integer, String> typenameMap = CollectionUtils.constructConstMap(
				new Integer('Z'), "boolean",
				new Integer('B'), "byte",
				new Integer('C'), "char",
				new Integer('S'), "short",
				new Integer('I'), "int",
				new Integer('J'), "long",
				new Integer('F'), "float",
				new Integer('D'), "double"
			);

	public static String getJNIType(String sig) {
		char lead = sig.charAt(0);
		if(lead == 'V')
			return "void";
		else if(isPrimitive(lead))
			return "j" + typenameMap.get(new Integer(lead));
		else if(lead == 'L') {
			String sigParse = sig.substring(1, sig.length() - 1);
			
			return sigParse.replace("/", "::").replace("$", "::");//"jobject";
		}
		else if(lead == '[') {
			if(sig.charAt(1) == '[' || sig.charAt(1) == 'L')
				return "jobjectArray";
			else
				return "j" + typenameMap.get(new Integer(sig.charAt(1))) + "Array";
		}
		//ruh roh
		return null;
	}
	
	public static String generateFieldGetter(String sig, boolean isStatic) {
		String fieldType = "Object";
		char char0 = sig.charAt(0);
		if(isPrimitive(char0)) {
			if(char0 == 'V')
				fieldType = "Void";
			else {
				String type = typenameMap.get(new Integer(sig.charAt(0)));
				type = type.substring(0, 1).toUpperCase() + type.substring(1);
				fieldType = type;
			}
		}
		return "Get" + (isStatic ? "Static" : "") + fieldType + "Field";
	}
	
	public static String generateFieldSetter(String sig, boolean isStatic) {
		return generateFieldGetter(sig, isStatic).replace("Get", "Set");
	}
	
	public static boolean isMultiDimArrayPrim(String sig) {
		int i;
		for(i = 0; sig.charAt(i) == '['; ++i)
			;
		return isPrimitive(sig.charAt(i));
	}
	public static String resolveMultidimArrayElementType(String sig) {
		int i;
		for(i = 0; sig.charAt(i) == '['; ++i)
			;
		return JType.getJNIType(sig.substring(i));
	}
	
	public static String generateMethodCall(String sig, boolean isStatic) {
		String sub = sig.substring(sig.indexOf(')') + 1, sig.length());
		String getter = generateFieldGetter(sub, isStatic);
		return getter.replace("Get", "Call").replace("Field", "Method");
	}
	
	public static final boolean isPrimitive(char c) {
		for(char prim : primitiveTypes)
			if(prim == c)
				return true;
		return false;
	}
	public JType() {
		
	}
	
	public static final String removeTemplateArgs(String s) {
		return s.contains("<") ? s.substring(0, s.indexOf('<')) : s;
	}

}
