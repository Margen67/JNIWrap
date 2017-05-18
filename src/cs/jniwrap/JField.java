package cs.jniwrap;

public final class JField extends JMember {
	
	public JField(JNIClass jniClass, JSig sig, String txt) {
		super(jniClass, sig, txt);
		int lastSpace = txt.lastIndexOf(' ');
		setName(txt.substring(lastSpace + 1, txt.length() - 1));
	}
	private String getTypename() {
		return JType.getJNIType(sigString());
	}
	private String generateDummyClassForStatic() {
		String result = "struct _" + getName() + "{ \n\t" + "operator " + getTypename() + "();\n";
		result += "\t" + getTypename() + " operator =(" + getTypename() + ");\n};\n";
		result += "static _" + getName() + " " + getName() + ";";
		return result;
	}

	private String generatePutDeclaration() {
		String typename = getTypename();
		return "void __put" + getName() + "(" + typename + ");";
	}

	private String generateGetDeclaration() {
		String typename = getTypename();
		return typename + " " + "__get" + getName() + "();";
	}

	private String generatePropertyDeclaration() {
		String typename = getTypename();
		String propname = getName();
		String result = String.format(
				"__declspec(property(get = __get%s, put = __put%s)) %s %s;",
					propname, propname, typename, propname
				);
		return result;
	}
	
	private String generatePutDefinition(JNIClass jc) {
		String putterDefStart = String.format("void %s::__put%s(%s arg) {", 
				jc.getClassNamespace(), getName(), getTypename() );
		String fieldIDName = "jid_field_" + getName();
		String putterReturn = String.format("%s->%s(m_jobject, %s, arg);}", jc.jniGetterName, 
				JType.generateFieldSetter(sigString(), false), fieldIDName);
		return putterDefStart + putterReturn;
				
	}
	private String generateGetDefinition(JNIClass jc) {
		String getterDefStart = String.format("%s %s::__get%s() {", getTypename(),
				jc.getClassNamespace(), getName());
		String fieldIDName = "jid_field_" + getName();
		String getterReturn = String.format("return %s->%s(m_jobject, %s);}", jc.jniGetterName, 
				JType.generateFieldGetter(sigString(), false), fieldIDName);
		return getterDefStart + getterReturn;
				
		
	}
	private String generateFieldProperty() {
		String putter = generatePutDeclaration();
		String getter = generateGetDeclaration();
		String prop = generatePropertyDeclaration();
		return String.format("%s\n%s\n%s\n", putter, getter, prop);
	}
	public String emitDeclaration(JNIClass jc) {
		String type = getTypename();
		if(isStatic) {
			return generateDummyClassForStatic();//"static " + type + " " + getName() + ";";
		}
		return generateFieldProperty();//type + " " + getName() + ";";
	}
	public String emitDefinition(JNIClass jc) {
		if(!isStatic) {
			return generatePutDefinition(jc) + "\n" + generateGetDefinition(jc);
		}
		String result = jc.getClassNamespace() + "::_" + getName() + "::operator " + getTypename() + "() {\n";
		result += "return " + JNIClass.jniGetterName + "->" + 
		JType.generateFieldGetter(sigString(), true) 
		+ "(" + jc.getClassIdVarName() + ", " + "jid_field_" + getName() + ");\n}";
		result += getTypename() +" " + jc.getClassNamespace() + 
				"::_" + getName() + "::operator=(" + getTypename() + " arg) { \n";
		result += JNIClass.jniGetterName + "->" + 
				JType.generateFieldSetter(sigString(), true) 
		+ "(" + jc.getClassIdVarName() + ", " + "jid_field_" + getName() + ", arg);\nreturn static_cast<" + getTypename() + ">(*this);\n}\n";
		result += jc.getClassNamespace() + "::_" + getName() + " " + jc.getClassNamespace() + "::" + getName() + ";";
		return result;
	}
	public String emitIdDeclaration(JNIClass jc) {
		String result = "static jfieldID jid_field_" + getName() + " = nullptr;";
		return result;
	}
	public String emitIdInitialization(JNIClass jc) {
		String callee = isStatic ? "GetStaticFieldID" : "GetFieldID";
		String fieldIDName = "jid_field_" + getName();
		String parmListForCallee = "(cls, \"" + getName() + "\", \"" + sigString() + "\"" + ")";
		return fieldIDName + " = env->" + callee + parmListForCallee + ";";
	}
	public String[] referencedTypes() {
		String resolved = JMember.resolveTypeReferenceNoPrim(sigString());
		if(resolved == null)
			return null;
		String result[] = new String[1];
		result[0] = resolved;
		return result;
	}
	
	public void invariants() {
		assert(!isNative);
		assert(!mayThrow);
		assert(!isOverloaded());
		assert(getName() != null);
		assert(getSignature() != null);
		getSignature().invariants();
		/*!isNative && !mayThrow && !isOverloaded() && getName() != null && getSignature() != null
				&& getSignature().invariants();*/
	}

}
