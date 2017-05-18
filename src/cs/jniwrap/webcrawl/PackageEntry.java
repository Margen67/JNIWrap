package cs.jniwrap.webcrawl;

public class PackageEntry {
	private String name, url, descr;
	public PackageEntry(String packageName, String packageURL, String packageDescription) {
		setName(packageName);
		setUrl(packageURL);
		setDescr(packageDescription);
	}
	public String getName() {
		return name;
	}
	private void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	private void setUrl(String url) {
		this.url = url;
	}
	private String getDescr() {
		return descr;
	}
	private void setDescr(String descr) {
		this.descr = descr;
	}

}
