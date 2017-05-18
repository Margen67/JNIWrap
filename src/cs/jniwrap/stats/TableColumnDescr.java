package cs.jniwrap.stats;

public class TableColumnDescr {
	private Class typeId;
	private String name;
	
	public TableColumnDescr(Class typeId, String name) {
		setTypeId(typeId);
		setName(name);
	}

	public Class getTypeId() {
		return typeId;
	}

	private void setTypeId(Class typeId) {
		this.typeId = typeId;
	}

	private String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return getName();
	}
}
