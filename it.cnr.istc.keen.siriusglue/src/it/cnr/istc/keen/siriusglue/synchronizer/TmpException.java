package it.cnr.istc.keen.siriusglue.synchronizer;

//FIXME: Implement a proper exception
public class TmpException extends RuntimeException {
	private static final long serialVersionUID = 1705424031620209744L;

	public TmpException(String msg) {
		super(msg);
	}

	public TmpException(Throwable t) {
		super(t);
	}

}