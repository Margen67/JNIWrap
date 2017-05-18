package cs.jniwrap.stats;
import java.sql.*;
import java.util.HashMap;
import static cs.jniwrap.utils.ErrorLogger.log;
import static cs.jniwrap.utils.FatalError.fatal;
public class StatsDB {

	private static final String jdbcDriver = "com.mysql.jdbc.Driver";
	private static final String dbURL = "jdbc:mysql://localhost/";
	private static final String dbUsername = StatsDB.class.getSimpleName();
	private static final String dbPassword = "unprotected";
	private static final String dbName = "JNISTATS";
	private Connection conn = null;
	private Statement stmt = null;
	private boolean isOpen = false;
	
	private static HashMap<Class, String> typeTranslator;
	
	static {
		typeTranslator = new HashMap<>();
		typeTranslator.put(Byte.class, "TINYINT");
		typeTranslator.put(Short.class, "SMALLINT");
		typeTranslator.put(Integer.class, "INT");
		typeTranslator.put(Boolean.class, "BOOLEAN");
		typeTranslator.put(Float.class, "FLOAT");
		typeTranslator.put(Double.class, "DOUBLE");
		typeTranslator.put(String.class, "TEXT");
	}
	
	public StatsDB() {
	}
	@Override 
	protected void finalize() {
		if(isOpen || conn != null || stmt != null) 
			fatal("finalizer for StatsDB was called, but DB is still open or not properly cleaned up. %s", this.toString());
		
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}
	public boolean open() {
		if(isOpen) {
			log("open was called for a StatsDB that is already open, returning true.");
			return true;
		}
		try {
			/*
			 * We don't actually use the class object, this just causes the class
			 * to be loaded and its static initializers invoked
			 */
			Class jdbcDriverClass = Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE DATABASE " + dbName);
			isOpen = true;
		} catch(Exception e) {
			e.printStackTrace();
			isOpen = false;
		} finally {
			
		}
		return isOpen;
	}
	
	public boolean close() {
		if(!isOpen){
			log("close was called on a StatsDB that is not open, returning false.");
			return true;
		}
		boolean closedStmt = true, closedConn = true;
		if(stmt != null)
			try {
				stmt.close();
				stmt = null;
			} catch (SQLException e) {
				e.printStackTrace();
				closedStmt = false;
			}
		if(conn != null)
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				e.printStackTrace();
				closedConn = false;
			}
		isOpen = !(closedStmt && closedConn);
		return !isOpen;
	}
	
	private static final String getSQLDType(Class cls) {
		return typeTranslator.get(cls);
	}
	
	public boolean createTable(String tableName, TableColumnDescr primaryKey, 
			TableColumnDescr... dataTypes) {
		String tableCreationStatement = String.format("CREATE TABLE %s(", tableName);
		String primaryKeyType = getSQLDType(primaryKey.getTypeId());
		String primaryKeyStr = String.format("%s %s PRIMARY KEY", primaryKey, primaryKeyType);
		tableCreationStatement += primaryKeyStr;
		if(dataTypes.length != 0) 
			for(TableColumnDescr descr : dataTypes)
				tableCreationStatement += String.format(", %s %s", descr, getSQLDType(descr.getTypeId()));
		try {
			return stmt.execute(tableCreationStatement  + ");");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public String toString() {
		return "StatsDB [conn=" + conn + ", stmt=" + stmt + ", isOpen=" + isOpen + "]";
	}

}
