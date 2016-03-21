package mn.dbtk.programobjects;

public class StoredObjectParseException extends Exception {
	private static final long serialVersionUID = 154869519378730000L;

	public StoredObjectParseException(String string) {
		super(string);
	}

	public StoredObjectParseException(String string, Exception e) {
		super(string, e);
	}

}
