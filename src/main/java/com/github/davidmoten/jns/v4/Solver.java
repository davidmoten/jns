package com.github.davidmoten.jns.v4;

/**
 * Navier Stokes solver for incompressible fluid using Chorin's method.
 *
 */
public class Solver {
    private static final double dt = 0.01; // time step
    private double dx; // grid spacing in x-direction
    private double dy; // grid spacing in y-direction
    private double[] dz; // grid spacing in z-direction

    // TODO use viscosity coefficient
    // private static final double viscosity = 0.1;

    private double[][][] u; // x-velocity component
    private double[][][] v; // y-velocity component
    private double[][][] w; // z-velocity component
    private double[][][] p; // pressure

    private int nx; // grid size in x-direction
    private int ny; // grid size in y-direction
    private int nz; // grid size in z-direction

    // intermediate variables declared here for reuse to save allocations
    private double[][][] div;
    private double[][][] uNext; // x-velocity component
    private double[][][] vNext; // y-velocity component
    private double[][][] wNext; // z-velocity component
    private double[][][] pNext; // pressure

    public Solver(int nx, int ny, int nz, double dx, double dy, double[] dz) {
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;

        u = new double[nx][ny][nz];
        v = new double[nx][ny][nz];
        w = new double[nx][ny][nz];
        p = new double[nx][ny][nz];

        uNext = new double[nx][ny][nz];
        vNext = new double[nx][ny][nz];
        wNext = new double[nx][ny][nz];
        pNext = new double[nx][ny][nz];

        div = new double[nx][ny][nz];
    }

    public void solve() {
        // Perform velocity advection and store in next
        advect(u, uNext);
        advect(v, vNext);
        advect(w, wNext);

        // Calculate the divergence of the velocity field and store in div
        computeDivergence(uNext, vNext, wNext, div);

        // Perform pressure projection and store in pNext
        project(p, div, pNext);

        // swap p and pNext
        double[][][] temp = p;
        p = pNext;
        pNext = temp;

        // Subtract the pressure gradient and store in u, v, w
        subtractPressureGradient(uNext, p, u);
        subtractPressureGradient(vNext, p, v);
        subtractPressureGradient(wNext, p, w);
    }

    private void advect(double[][][] field, double[][][] result) {
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    double x = i - dt * u[i][j][k] / dx;
                    double y = j - dt * v[i][j][k] / dy;
                    double z = k - dt * w[i][j][k] / dz[k];

                    result[i][j][k] = trilinearInterpolate(field, x, y, z);
                }
            }
        }
    }

    private double trilinearInterpolate(double[][][] field, double x, double y, double z) {
        int i = (int) Math.floor(x);
        int j = (int) Math.floor(y);
        int k = (int) Math.floor(z);

        double dx1 = x - i;
        double dx0 = 1.0 - dx1;
        double dy1 = y - j;
        double dy0 = 1.0 - dy1;
        double dz1 = z - k;
        double dz0 = 1.0 - dz1;

        return dx0
                * (dy0 * (dz0 * field[i][j][k] + dz1 * field[i][j][k + 1])
                        + dy1 * (dz0 * field[i][j + 1][k] + dz1 * field[i][j + 1][k + 1]))
                + dx1 * (dy0 * (dz0 * field[i + 1][j][k] + dz1 * field[i + 1][j][k + 1])
                        + dy1 * (dz0 * field[i + 1][j + 1][k] + dz1 * field[i + 1][j + 1][k + 1]));
    }

    private double[][][] computeDivergence(double[][][] u, double[][][] v, double[][][] w, double[][][] div) {

        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    div[i][j][k] = (u[i + 1][j][k] - u[i - 1][j][k] + v[i][j + 1][k] - v[i][j - 1][k] + w[i][j][k + 1]
                            - w[i][j][k - 1]) / (2 * dx);
                }
            }
        }

        return div;
    }

    private void project(double[][][] p, double[][][] div, double[][][] result) {
        for (int iter = 0; iter < 20; iter++) {
            for (int i = 1; i < nx - 1; i++) {
                for (int j = 1; j < ny - 1; j++) {
                    for (int k = 1; k < nz - 1; k++) {
                        result[i][j][k] = ((p[i + 1][j][k] + p[i - 1][j][k] + p[i][j + 1][k] + p[i][j - 1][k]
                                + p[i][j][k + 1] + p[i][j][k - 1]) - div[i][j][k] * dx * dx) / 6.0;
                    }
                }
            }
        }
    }

    private void subtractPressureGradient(double[][][] field, double[][][] p, double[][][] result) {
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    result[i][j][k] = field[i][j][k] - 0.5 * dt * (p[i + 1][j][k] - p[i - 1][j][k]) / dx
                            - 0.5 * dt * (p[i][j + 1][k] - p[i][j - 1][k]) / dy
                            - 0.5 * dt * (p[i][j][k + 1] - p[i][j][k - 1]) / dz[k];
                }
            }
        }
    }

    public static void main(String[] args) {
        int nx = 10; // number of grid points in x-direction
        int ny = 10; // number of grid points in y-direction
        int nz = 10; // number of grid points in z-direction

        // Define the grid spacing in each direction
        double dx = 0.1; // grid spacing in x-direction
        double dy = 0.1; // grid spacing in y-direction
        double[] dz = new double[nz]; // grid spacing in z-direction
        // Initialize the variable depth spacing
        for (int k = 0; k < nz; k++) {
            dz[k] = 0.1 * (k + 1); // assuming linearly increasing spacing
        }

        Solver solver = new Solver(nx, ny, nz, dx, dy, dz);
        solver.solve();
    }
}