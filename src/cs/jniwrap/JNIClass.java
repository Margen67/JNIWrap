package cs.jniwrap;
import sun.misc.Unsafe;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import java.util.function.Consumer;

import cs.ArrayUtils.ArrayOps;

import static cs.jniwrap.utils.ErrorLogger.*;
import static cs.jniwrap.utils.FatalError.*;
public final class JNIClass {
	private JMember[] m_members;//Vector<JMember> members;
	private String classname;
	private String classpath;
	private String[] interfaces;
	private String extended;
	private boolean isInterface;
	private boolean isGeneric;
	private int m_hashcode;
	private boolean extendsObject;
	public static final String jniGetterName = "GETJNIENV()";
	
	private int m_typerefs[];
	
	private int m_refs_to;
	private String m_filename;
	private Vector<String> debugStrs = null;
	public JNIClass(Vector<String> strs, String filename) {
		m_filename = filename;
		debugStrs = strs;
		setClassname(
				filename.substring(filename.lastIndexOf(File.separatorChar) + 1)
				.replace(".class", "")
				.replace('$', '.')
				);
		String classpath_ = strs.elementAt(1);
		isInterface = classpath_.indexOf(" interface ") != -1;
		int indexOfClassname = classpath_.indexOf("." + getClassname().replace('.', '$'));
		if(indexOfClassname != -1) {
			int previousSpace = classpath_.lastIndexOf(" ", indexOfClassname);
			setClasspath(classpath_.substring(previousSpace + 1, indexOfClassname));
		}
		else {
			setClasspath("");
		}
		isGeneric = classpath_.indexOf(packageName() + "<") != -1;
		
		/*
		 * used later when analyzing typerefs globally
		 */
		m_hashcode = getClassNamespace().hashCode();
		
		extended = parseExtended(classpath_);
		interfaces = parseInterfaces(classpath_);
		Vector<JMember> members = new Vector<>();
		for(int i = 2; i < strs.size() - 1; i += 2) {
			JSig sig = new JSig(strs.get(i + 1));
			JMember member = null;
			if(sig.isField())
				member = new JField(this, sig, strs.get(i));
			else
				member = new JMethod(this, sig, strs.get(i));
			members.addElement(member);
		}
		setMembers(members.toArray(new JMember[members.size()]));
		
		try {
			initializeUniqueIds();
		} catch (LogicFlawException e) {
			System.out.println("Logic flaw occurred while initializing unique member ID's.\n\t\t***JNICLASS DUMP***");
			dumpClass();
			e.printStackTrace();
			System.exit(-1);
		}
		findOverloads();
		setTyperefs(gatherTyperefsI());
		setRefsTo(0);
	//	if(m_filename.contains("$") && members.isEmpty())
		//	log("break here.");
		/*System.out.println(generateHeader());*/
		//System.out.println(emitIdDeclarations());
		/*System.out.println(emitInitializeIds());
		System.out.println(emitDefinitions());
		printTyperefs();*/
	}
	
	private void forEachMember(Consumer<JMember> op) {
		for(JMember memb : getMembers())
			op.accept(memb);
	}
	
	public String generateInheritanceList() {
		String result = ": public " + extended;
		if(interfaces == null)
			return result;
		for(String iface : interfaces) 
			result += ", public " + iface;
		return result;
	}
	
	public int memberCount() {
		return getMembers().length;
	}
	public JMember getMember(int i) {
		return getMembers()[i];
	}
	public String generateHeader() {
		String result = "#pragma once\nnamespace " + getClasspath().replace(".", "::") 
				+ " {\n\tclass " + getClassname() + " " + generateInheritanceList() + " {\n\t\tjobject m_jobject;\n\tpublic:\n";
		
		for(JMember mem : getMembers()) {
			result += "\t\t" + mem.emitDeclaration(this) + "\n";
		}
		result += "\t\t" + "static void initializeIds(JNIEnv env);\n";
		return result + "\t};\n}\n";
	}
	private void initializeUniqueIds() throws LogicFlawException {
		int[] uniques = ArrayOps.mapToInt(getMembers(), member -> member.hashCode());
		int sortedUniques[] = uniques.clone();
		Arrays.sort(sortedUniques);
		
		/*
		 * make sure we have no duplicates
		 */
		int last = 0;
		boolean first = true;
		int uniqueIndex = 0;
		
		for(final int id : sortedUniques) {
			if(!first && last == id)
				throw new LogicFlawException("JMember's hashCode method does not produce unique results!");
			last = id;
			if(first)
				first = false;
			final int i = ArrayOps.indexOfMatch(uniques, value -> value == id);
			if(i < uniques.length)
				getMember(i).setUniqueID(uniqueIndex++);
		}
		/*
		 * verify that all id's are unique
		 */
		for(JMember outerMemb : getMembers()) {
			int memberID = outerMemb.getUniqueID();
			for(JMember innerMemb : getMembers()) {
				if(innerMemb.getUniqueID() == memberID && innerMemb != outerMemb) {
					throw new LogicFlawException(
							"Found two JMembers with matching unique ID's! inner = " + 
									innerMemb.toString() + ", outer = " + outerMemb.toString()
					);
				}
			}
		}
	}
	public String emitInitializeIds() {
		String result = "static void " + getClassNamespace() + "::initializeIds(JNIEnv* env) {\n";
		String classpathForFindclass = getClasspath().replace('.', '/') + "/" + getClassname();
		String findClassInvocation = "jclass cls = env->FindClass(\"" + classpathForFindclass + "\");";
		result += "\t" + findClassInvocation + "\n";
		for(JMember memb: getMembers()) {
			result += "\t" + memb.emitIdInitialization(this) + "\n";
		}
		result += "\tcls_" + getClassname() + " = cls;\n";
		return result + "}";
	}
	private static final String extendsTxt = " extends ";
	private static final String implementsTxt = " implements ";
	private String parseExtended(String s) {
		int extendsIndex = s.indexOf(extendsTxt);
		if(extendsIndex == -1) {
			extendsObject = true;
			return "java::lang::Object";
		}
		extendsIndex += extendsTxt.length();
		String sub = s.substring(extendsIndex, s.indexOf(' ', extendsIndex));
		return sub.replace(".", "::");
	}
	
	private String[] parseInterfaces(String s) {

		int implementsIndex = s.indexOf(implementsTxt);
		if(implementsIndex == -1)
			return null;
		Vector<String> result = new Vector<>();
		implementsIndex += implementsTxt.length();
		String ifaces[] = s.substring(implementsIndex, s.indexOf(" {")).split(",");
		for( String iface : ifaces) 
			result.add(iface.replace(" ", "").replace(".", "::"));
		
		
		return result.toArray(new String[result.size()]);
	}
	
	public String getClassIdVarName() {
		return "cls_" + getClassname();
	}
	
	public String emitIdDeclarations() {
		String result = "static jclass " + getClassIdVarName() + " = nullptr;\n";
		for(JMember memb : getMembers())
			result += memb.emitIdDeclaration(this) + "\n";
		return result;
	}
	
	public String emitDefinitions() {
		String result = "";
		for(JMember memb : getMembers())
			result += memb.emitDefinition(this) + "\n";
		return result;
	}
	
	public String[] gatherTyperefsS() {
		final Vector<String> result = new Vector<>();
		forEachMember(memb -> {
			ArrayOps.forEach(memb.referencedTypes(), s -> { if(s != null) result.add(s); });
		});
		
		result.add(JType.removeTemplateArgs(extended));
		ArrayOps.forEach(interfaces, iface -> result.add(JType.removeTemplateArgs((String) iface)));
	
		return result.toArray(new String[result.size()]);
	}

	private int[] gatherTyperefsI() {

		String[] sRefs = gatherTyperefsS();

		
		int resultArr[] = new int[sRefs.length];
		for(int i = 0; i < resultArr.length; ++i)
			resultArr[i] = sRefs[i].hashCode();
		
		return resultArr;
	}

	public void printTyperefs() {
		String refs[] = gatherTyperefsS();
		Arrays.sort(refs);
		for(String s : refs)
			System.out.println(s);
	}
	
	private void findOverloads() {
		forEachMember(outerMember -> {
			if(outerMember.isField() || outerMember.isOverloaded())
				return;
			final String outerName = outerMember.getName();
			forEachMember(innerMember -> {
				if(innerMember.isField() || outerMember == innerMember)
					return;
				if(outerName.equals(innerMember.getName())) {
					outerMember.setOverloaded(true);
					innerMember.setOverloaded(true);
					return;
				}
			});
		});

	}
	
	public void invariants() {
		assert(getMembers() != null);
		assert(classname != null);
		assert(getTyperefs() != null);
		forEachMember(memb -> memb.invariants());
	}
	
	public boolean isEmptyClass() {
		return memberCount() == 0;
	}
	
	private void dumpClass() {
		forEachMember(memb -> System.out.println(memb.toString()));	
	}
	
	
	@Override public int
	hashCode() {
		return m_hashcode;
	}
	public String getClassname() {
		return classname;
	}

	private void setClassname(String classname) {
		this.classname = classname;
	}

	public String getClasspath() {
		return classpath;
	}

	private void setClasspath(String classpath) {
		this.classpath = classpath;
	}
	
	public String packageName() {
		String cpath = getClasspath();
		return cpath.length() == 0 ? getClassname() : cpath + "." + getClassname();
	}
	
	public String getClassNamespace() {
		return packageName().replace(".", "::");
	}

	public int[] getTyperefs() {
		return m_typerefs;
	}

	private void setTyperefs(int m_typerefs[]) {
		this.m_typerefs = m_typerefs;
	}

	public int getRefsTo() {
		return m_refs_to;
	}
	
	public void incrementRefCount() {
		m_refs_to++;
	}

	private void setRefsTo(int m_refs_to) {
		this.m_refs_to = m_refs_to;
	}

	public JMember[] getMembers() {
		return m_members;
	}

	public void setMembers(JMember[] m_members) {
		this.m_members = m_members;
	}

}
