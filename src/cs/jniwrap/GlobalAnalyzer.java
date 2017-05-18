package cs.jniwrap;

import java.util.HashMap;
import java.util.Vector;

import cs.jniwrap.stats.StatsDB;
import cs.jniwrap.stats.TableColumnDescr;
import cs.jniwrap.utils.Bitcompress;

import static cs.jniwrap.utils.ErrorLogger.log;
import static cs.jniwrap.utils.FatalError.fatal;
public class GlobalAnalyzer {
	private JNIClass[] m_classes;
	private HashMap<Integer, JNIClass> m_classmap;
	private StatsDB statsDB;
	private static final float loadFactor = 0.75f;
	
	public GlobalAnalyzer(JNIClass[] classes) {
		this.setClasses(classes);
		m_classmap = new HashMap<>(getClasses().length, loadFactor);
		
		for(int i = 0; i < getClasses().length; ++i)
			m_classmap.put(new Integer(classes[i].hashCode()), classes[i]);
		/*
		statsDB = new StatsDB();
		if(!statsDB.open()) {
			log("Failed to open SQL database connection!");
			if(!statsDB.close()) //nothing we can do here
				fatal("Failed to clean up failed SQL database connection attempt, exiting.");
			statsDB = null;
		}
		else if(!createFunctionsTable()) {
			log("Failed to create SQL function table!");
			if(!statsDB.close())
				fatal("Failed to clean up SQL database connection after createFunctionsTable failed, exiting.");
			statsDB = null;
			
		}*/
		Bitcompress comp = new Bitcompress(makeMassiveTyperefArray());
	}
	
	private boolean createFunctionsTable(){
		final TableColumnDescr primaryKey = new TableColumnDescr(Integer.class, "id");
		
		return statsDB.createTable("FUNCTIONS", primaryKey, new TableColumnDescr(String.class, "Name"));
	}
	
	private JNIClass[] getClasses() {
		return m_classes;
	}
	private void setClasses(JNIClass[] m_classes) {
		this.m_classes = m_classes;
	}
	
	private JNIClass findClassByHash(int hash) {
		return m_classmap.get(new Integer(hash));
	}
	
	public void invariants() {
		assert(getClasses() != null);
		for(JNIClass jc : getClasses())
			jc.invariants();
	}
	
	public int[] makeMassiveTyperefArray() {
		Vector<Integer> refArr = new Vector<>();
		for(JNIClass jc : getClasses()) {
			for(int ref: jc.getTyperefs())
				refArr.add(ref);
		}
		int[] result = new int[refArr.size()];
		int i = 0;
		for(int value : refArr)
			result[i++] = value;
		return result;
	}
	
	public void initializeReferenceCounts() {
		/*
		for(JNIClass jc : getClasses()) {
			int refs[] = jc.getTyperefs();
			int index = 0;
			for(int ref : refs) {
				JNIClass referenced = findClassByHash(ref);
				if(referenced == null) {
					String badRef = jc.gatherTyperefsS()[index];
					//assert(false);
				}
				//assert(referenced != null);
				referenced.incrementRefCount();
				index++;
			}
		}*/
	}
	
	public void performAnalysis() {
		initializeReferenceCounts();
		
	}
}
