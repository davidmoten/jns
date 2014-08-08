package com.github.davidmoten.jns;

public class Util {

	public static final double GRAVITY_M_PER_S2 = 9.80665;
	public static final int SEA_LEVEL_PRESSURE_PASCALS = 101325;
	public static final double DENSITY_KG_PER_M3 = 1025;
	public final static Vector GRAVITY = Vector.create(0, 0, -9.80665);

	public static <T> T unexpected() {
		return unexpected("unexpected");
	}

	public static <T> T unexpected(String msg) {
		throw new RuntimeException(msg);
	}

	public static double pressureAtDepth(double depthMetres) {
		return SEA_LEVEL_PRESSURE_PASCALS + DENSITY_KG_PER_M3 * depthMetres
				* GRAVITY_M_PER_S2;
	}

	public static Vector gravityForce(Cell cell) {
		return GRAVITY.times(cell.density());
	}

}
