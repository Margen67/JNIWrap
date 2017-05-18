package cs.jniwrap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;

public abstract class JMember {
	private JSig signature;
	private String name;
	protected boolean isStatic, 
	isFinal, 
	isNative, 
	isVolatile, 
	isSynchronized, 
	isTransient, 
	mayThrow,
	isGeneric,
	isConstructor, 
	isPrimitive;
	private boolean isOverloaded;
	private boolean isUniqueInitialized;
	
	
	private int uniqueID = -1;
	public JMember(JNIClass jniClass, JSig sig, String txt) {
		isStatic = txt.contains(" static ");
		isFinal = txt.contains(" final ");
		isNative = txt.contains(" native ");
		isVolatile = txt.contains(" volatile ");
		isSynchronized = txt.contains("synchronized");
		isTransient = txt.contains("transient");
		mayThrow = txt.contains(" throws ");
		
		setSignature(sig);
	}
	public JSig getSignature() {
		return signature;
	}
	private void setSignature(JSig signature) {
		this.signature = signature;
	}
	protected String sigString() {
		return getSignature().getSignature();
	}
	
	public void setUniqueID(int uniqueID) throws LogicFlawException {
		if(isUniqueInitialized)
			throw new LogicFlawException(
					"setUniqueID called on a member with an already initialized unique id! Member = " + this.toString()
					);
		
		this.uniqueID = uniqueID;
		isUniqueInitialized = true;
	}
	
	public int getUniqueID() throws LogicFlawException {
		if(!isUniqueInitialized)
			throw new LogicFlawException("Attempted to read unique id of a member with an uninitialized unique id!");
		
		return uniqueID;
	}
	
	public abstract String emitDeclaration(JNIClass jc);
	public abstract String emitDefinition(JNIClass jc);
	public abstract String emitIdDeclaration(JNIClass jc);
	public abstract String emitIdInitialization(JNIClass jc);
	public abstract void invariants();
	/*@Override
	public int hashCode() {
		return signature.hashCode() ^ getName().hashCode();
	}*/
	

	protected boolean isOverloaded() {
		return isOverloaded;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isFinal ? 1231 : 1237);
		result = prime * result + (isGeneric ? 1231 : 1237);
		result = prime * result + (isNative ? 1231 : 1237);
		result = prime * result + (isOverloaded ? 1231 : 1237);
		result = prime * result + (isStatic ? 1231 : 1237);
		result = prime * result + (isSynchronized ? 1231 : 1237);
		result = prime * result + (isTransient ? 1231 : 1237);
		result = prime * result + (isUniqueInitialized ? 1231 : 1237);
		result = prime * result + (isVolatile ? 1231 : 1237);
		result = prime * result + (mayThrow ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		result = prime * result + uniqueID;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JMember other = (JMember) obj;
		if (isFinal != other.isFinal)
			return false;
		if (isGeneric != other.isGeneric)
			return false;
		if (isNative != other.isNative)
			return false;
		if (isOverloaded != other.isOverloaded)
			return false;
		if (isStatic != other.isStatic)
			return false;
		if (isSynchronized != other.isSynchronized)
			return false;
		if (isTransient != other.isTransient)
			return false;
		if (isUniqueInitialized != other.isUniqueInitialized)
			return false;
		if (isVolatile != other.isVolatile)
			return false;
		if (mayThrow != other.mayThrow)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		if (uniqueID != other.uniqueID)
			return false;
		return true;
	}
	protected void setOverloaded(boolean isOverloaded) {
		this.isOverloaded = isOverloaded;
	}
	public String getName() {
		return name;
	}
	protected void setName(String name) {
		this.name = name;
	}
	public final boolean isField() {
		return this.getClass().equals(JField.class);
	}
	
	public final boolean isMethod() {
		return this.getClass().equals(JMethod.class);
	}
	
	public abstract String[] referencedTypes();
	protected static String resolveTypeReference(String s) {
		if(s.charAt(0) == '[') 
			return JType.resolveMultidimArrayElementType(s);
		else
			return JType.getJNIType(s);
	}
	protected static String resolveTypeReferenceNoPrim(String s) {
		char c0 = s.charAt(0);
		if(JType.isPrimitive(c0) || (c0 == '[' && JType.isMultiDimArrayPrim(s)) )
			return null;
		if(c0 == '[') 
			return JType.resolveMultidimArrayElementType(s);
		else
			return JType.getJNIType(s);
	}
}
