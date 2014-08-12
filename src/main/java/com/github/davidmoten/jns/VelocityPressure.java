package com.github.davidmoten.jns;

public class VelocityPressure {

    private final Vector velocity;
    private final double pressure;

    public VelocityPressure(Vector velocity, double pressure) {
        this.velocity = velocity;
        this.pressure = pressure;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public double getPressure() {
        return pressure;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VelocityPressure [velocity=");
        builder.append(velocity);
        builder.append(", pressure=");
        builder.append(pressure);
        builder.append("]");
        return builder.toString();
    }

}
