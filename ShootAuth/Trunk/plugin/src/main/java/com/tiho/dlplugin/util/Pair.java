package com.tiho.dlplugin.util;

import java.io.Serializable;

public final class Pair<FIRST, SECOND> implements Serializable{
	
	private static final long serialVersionUID = -7580226520510051490L;
	
	public final FIRST first;
	public final SECOND second;

	private Pair(FIRST first, SECOND second) {
		this.first = first;

		this.second = second;
	}

	public FIRST getFirst() {
		return this.first;
	}

	public SECOND getSecond() {
		return this.second;
	}

	public static <FIRST, SECOND> Pair<FIRST, SECOND> of(FIRST first,
			SECOND second) {
		return new Pair(first, second);
	}

	public String toString() {
		return String.format("Pair[%s,%s]", new Object[] { this.first,
				this.second });
	}
}
