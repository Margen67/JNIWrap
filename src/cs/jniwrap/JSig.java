package cs.jniwrap;

public class JSig {
	private String signature;
	private static final String sigStartMarker = "Signature: ";
	public JSig(String sig) {
		int markerStart = sig.indexOf(sigStartMarker);
		setSignature(sig.substring(markerStart + sigStartMarker.length(), sig.length()));
	}
	public String getSignature() {
		return signature;
	}
	private void setSignature(String signature) {
		this.signature = signature;
	}
	
	public boolean isField() {
		return !getSignature().contains("(");
	}
	public boolean isMethod() {
		return !isField();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
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
		JSig other = (JSig) obj;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}
	public void invariants() {
		assert(signature != null);
		assert(signature.length() != 0);
	}
}
