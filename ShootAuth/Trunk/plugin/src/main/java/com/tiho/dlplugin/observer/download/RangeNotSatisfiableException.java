package com.tiho.dlplugin.observer.download;

public class RangeNotSatisfiableException extends Exception {

	private static final long serialVersionUID = 6801752102963140665L;

	public RangeNotSatisfiableException() {
		super();
	}

	public RangeNotSatisfiableException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RangeNotSatisfiableException(String detailMessage) {
		super(detailMessage);
	}

	public RangeNotSatisfiableException(Throwable throwable) {
		super(throwable);
	}

	
	
}
