package com.github.davidmoten.jns;

public class Util {
	public static <T> T unexpected() {
		return unexpected("unexpected");
	}

	public static <T> T unexpected(String msg) {
		throw new RuntimeException(msg);
	}
}
