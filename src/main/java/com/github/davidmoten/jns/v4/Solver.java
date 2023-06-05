package com.github.davidmoten.jns.v4;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Navier Stokes solver for incompressible fluid using Chorin's method. Created
 * via conversation with ChatGPT 3 so take with large grain of salt!
 *
 */
public class Solver {

    private static final int NUM_PRESSURE_PROJECTION_ITERATIONS = 20;
    private static final double seawaterDensity = 1025.0; // kg/m³
    private static final double viscosity = 1.02;
    private static final double gravity = 9.81; // m/s²

    private static final double dt = 0.0001; // time step
    private double dx; // grid spacing in x-direction
    private double dy; // grid spacing in y-direction

    private double[][][] u; // x-velocity component
    private double[][][] v; // y-velocity component
    private double[][][] w; // z-velocity component
    private double[][][] p; // pressure
    private boolean[][][] obstacle; // obstacles

    private int nx; // grid size in x-direction
    private int ny; // grid size in y-direction
    private int nz; // grid size in z-direction

    // intermediate variables declared here for reuse to save allocations
    private double[][][] div;
    private double[][][] uNext; // x-velocity component
    private double[][][] vNext; // y-velocity component
    private double[][][] wNext; // z-velocity component
    private double[][][] pNext; // pressure
    private double[] depth;

    public Solver(int nx, int ny, int nz, double dx, double dy, double[] dz, boolean[][][] obstacle) {
        Preconditions.checkArgument(dz.length == nz - 1);
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.dx = dx;
        this.dy = dy;
        this.obstacle = obstacle;

        u = new double[nx][ny][nz];
        v = new double[nx][ny][nz];
        w = new double[nx][ny][nz];
        p = new double[nx][ny][nz];

        uNext = new double[nx][ny][nz];
        vNext = new double[nx][ny][nz];
        wNext = new double[nx][ny][nz];
        pNext = new double[nx][ny][nz];

        div = new double[nx][ny][nz];

        depth = new double[nz];
        double sum = 0;
        for (int i = 0; i < depth.length; i++) {
            depth[i] = sum;
            if (i < dz.length) {
                sum += dz[i];
            }
        }

        // uses pressure due to depth only
        initializePressure();
    }

    public void setLidDrivenCavityBoundary(double speed) {
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                u[i][j][0] = 1;
            }
        }
        for (int i = 0; i < nx; i += 1) {
            for (int j = 0; j < ny; j++) {
                for (int k = 1; k < nz; k++) {
                    if (i == 0 || i == nx - 1 || j == 0 || j == ny - 1 || k == nz - 1) {
                        obstacle[i][j][k] = true;
                    }
                }
            }
        }
    }

    private void initializePressure() {
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    p[i][j][k] = seawaterDensity * gravity * depth[k];
                }
            }
        }
    }

    public void solve() {
        // Chorin's Projection method

        setObstaclePressureToAverageOfNeighbours();

        // Perform velocity advection and store in *next
        advect(u, uNext, false);
        advect(v, vNext, false);
        advect(w, wNext, true);

        // Calculate the divergence of the velocity field and store in div
        computeDivergence(uNext, vNext, wNext, div);

        // Perform pressure projection and store in pNext
        projectPressure(p, div, pNext);

        // swap p and pNext
        swapPressures();

        // Subtract the pressure gradient and store in u, v, w
        subtractPressureGradient(uNext, p, false, u);
        subtractPressureGradient(vNext, p, false, v);
        subtractPressureGradient(wNext, p, true, w);

    }

    private void swapPressures() {
        // swap p and pNext
        double[][][] temp = p;
        p = pNext;
        pNext = temp;
    }

    private void setObstaclePressureToAverageOfNeighbours() {
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    if (obstacle[i][j][k]) {
                        p[i][j][k] = averageOfNeighboringPressure(i, j, k);
                    }
                }
            }
        }
    }

    private void advect(double[][][] field, double[][][] result, boolean includeGravity) {
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    if (!obstacle[i][j][k]) {
                        double deltaZ = (depth[k + 1] - depth[k - 1]) / 2;

                        double x = i - dt * u[i][j][k] / dx;
                        double y = j - dt * v[i][j][k] / dy;
                        double z = k - dt * w[i][j][k] / deltaZ;

                        double interpolatedValue = trilinearInterpolate(field, x, y, z);

                        // Calculate the second-order derivatives for viscosity
                        double d2udx2 = (u[i + 1][j][k] - 2 * u[i][j][k] + u[i - 1][j][k]) / (dx * dx);
                        double d2udy2 = (u[i][j + 1][k] - 2 * u[i][j][k] + u[i][j - 1][k]) / (dy * dy);
                        double d2udz2 = (u[i][j][k + 1] - 2 * u[i][j][k] + u[i][j][k - 1]) / (deltaZ * deltaZ);

                        double d2vdx2 = (v[i + 1][j][k] - 2 * v[i][j][k] + v[i - 1][j][k]) / (dx * dx);
                        double d2vdy2 = (v[i][j + 1][k] - 2 * v[i][j][k] + v[i][j - 1][k]) / (dy * dy);
                        double d2vdz2 = (v[i][j][k + 1] - 2 * v[i][j][k] + v[i][j][k - 1]) / (deltaZ * deltaZ);

                        double d2wdx2 = (w[i + 1][j][k] - 2 * w[i][j][k] + w[i - 1][j][k]) / (dx * dx);
                        double d2wdy2 = (w[i][j + 1][k] - 2 * w[i][j][k] + w[i][j - 1][k]) / (dy * dy);
                        double d2wdz2 = (w[i][j][k + 1] - 2 * w[i][j][k] + w[i][j][k - 1]) / (deltaZ * deltaZ);

                        // Apply advection with viscosity
                        result[i][j][k] = interpolatedValue
                                - dt * (u[i][j][k] * (interpolatedValue - trilinearInterpolate(u, x, y, z)) / dx
                                        + v[i][j][k] * (interpolatedValue - trilinearInterpolate(v, x, y, z)) / dy
                                        + w[i][j][k] * (interpolatedValue - trilinearInterpolate(w, x, y, z)) / deltaZ)
                                + dt * viscosity * (d2udx2 + d2udy2 + d2udz2 + d2vdx2 + d2vdy2 + d2vdz2 + d2wdx2
                                        + d2wdy2 + d2wdz2)
                                - (includeGravity ? dt * gravity : 0);
                    }
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

    private void computeDivergence(double[][][] u, double[][][] v, double[][][] w, double[][][] div) {
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    div[i][j][k] = (u[i + 1][j][k] - u[i - 1][j][k] + v[i][j + 1][k] - v[i][j - 1][k] + w[i][j][k + 1]
                            - w[i][j][k - 1]) / (2 * dx) - dt * gravity;
                }
            }
        }
    }

    private void projectPressure(double[][][] p, double[][][] div, double[][][] result) {
        for (int iter = 0; iter < NUM_PRESSURE_PROJECTION_ITERATIONS; iter++) {
            for (int i = 1; i < nx - 1; i++) {
                for (int j = 1; j < ny - 1; j++) {
                    for (int k = 1; k < nz - 1; k++) {
                        if (!obstacle[i][j][k]) {
                            result[i][j][k] = ((p[i + 1][j][k] + p[i - 1][j][k] + p[i][j + 1][k] + p[i][j - 1][k]
                                    + p[i][j][k + 1] + p[i][j][k - 1]) - div[i][j][k] * dx * dx) / 6.0;
                        }
                    }
                }
            }
        }
    }

    private void subtractPressureGradient(double[][][] field, double[][][] p, boolean includeGravity,
            double[][][] result) {
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    if (!obstacle[i][j][k]) {
                        double deltaZ = (depth[k + 1] - depth[k - 1]) / 2;
                        result[i][j][k] = field[i][j][k] - 0.5 * dt * (p[i + 1][j][k] - p[i - 1][j][k]) / dx
                                - 0.5 * dt * (p[i][j + 1][k] - p[i][j - 1][k]) / dy
                                - 0.5 * dt * (p[i][j][k + 1] - p[i][j][k - 1]) / deltaZ
                                - (includeGravity ? dt * gravity : 0);
                    }
                }
            }
        }
    }

    // Get the pressure value from neighboring obstacle cells
    private double averageOfNeighboringPressure(int i, int j, int k) {
        double pressureSum = 0.0;
        int count = 0;

        // Check the neighboring cells in the x, y, and z directions
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                for (int dk = -1; dk <= 1; dk++) {
                    // Exclude the current cell
                    if (di == 0 && dj == 0 && dk == 0) {
                        continue;
                    }

                    // Check if the neighboring cell is an obstacle
                    int neighborI = i + di;
                    int neighborJ = j + dj;
                    int neighborK = k + dk;
                    if (!isObstacle(neighborI, neighborJ, neighborK)) {
                        pressureSum += p[neighborI][neighborJ][neighborK];
                        count++;
                    }
                }
            }
        }

        // Return the average pressure of neighboring non-obstacle cells
        if (count > 0) {
            return pressureSum / count;
        } else {
            return seawaterDensity * gravity * depth[k]; // If no neighboring non-obstacle cells, set to the desired
                                                         // initial
                                                         // pressure value
        }
    }

    // Check if a cell is an obstacle
    private boolean isObstacle(int i, int j, int k) {
        // Perform boundary checks to avoid accessing out-of-bounds cells
        if (i < 0 || i >= nx || j < 0 || j >= ny || k < 0 || k >= nz) {
            return false; // Return false for out-of-bounds cells
        }

        // Return true if the cell is an obstacle
        return obstacle[i][j][k];
    }

    public static void main(String[] args) {
        int nx = 10; // number of grid points in x-direction
        int ny = 10; // number of grid points in y-direction
        int nz = 10; // number of grid points in z-direction

        // Define the grid spacing in each direction
        double dx = 0.1; // grid spacing in x-direction
        double dy = 0.1; // grid spacing in y-direction
        double[] dz = new double[nz - 1]; // grid spacing in z-direction

        // Initialize the variable depth spacing
        for (int k = 0; k < dz.length; k++) {
            dz[k] = 0.1;
        }

        Solver solver = new Solver(nx, ny, nz, dx, dy, dz, new boolean[nx][ny][nz]);
        solver.setLidDrivenCavityBoundary(1);
        for (int i = 0; i < 1; i++) {
            solver.solve();
        }
        for (int i = 1; i < nx - 1; i++) {
            for (int j = 1; j < ny - 1; j++) {
                for (int k = 1; k < nz - 1; k++) {
                    System.out.println(i + ", " + j + ", " + k + ": " + solver.u[i][j][k]);
                }
            }
        }
    }
}