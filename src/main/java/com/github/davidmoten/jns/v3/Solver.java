package com.github.davidmoten.jns.v3;

import com.github.davidmoten.guavamini.Preconditions;

public class Solver {

    public static final double FLUID_DENSITY = 1.025; // density of sea water

    // All variables are in SI units
    private int gridSizeX; // Size of the grid in the X-direction
    private int gridSizeY; // Size of the grid in the Y-direction
    private int gridSizeZ; // Size of the grid in the Z-direction
    private double[][][] u; // Velocity grid in the X-direction
    private double[][][] v; // Velocity grid in the Y-direction
    private double[][][] w; // Velocity grid in the Z-direction
    private double[][][] p; // Pressure grid
    private boolean[][][] obstacle; // Obstacle grid
    private double[] depth; // Depth values at each grid point
    private double deltaX;
    private double deltaY;
    private Forcing tidalForcingX;
    private Forcing tidalForcingY;
    private double timeStep;
    private double fluidDensity;

    private double[][][] uNext;
    private double[][][] vNext;
    private double[][][] wNext;
    private double[][][] pNext;

    // Constructor
    public Solver(int gridSizeX, int gridSizeY, int gridSizeZ, int deltaX, int deltaY, double[] depths,
            Forcing tidalForcingX, Forcing tidalForcingY, double timeStep, double fluidDensity) {
        Preconditions.checkArgument(depths.length == gridSizeZ);
        this.gridSizeX = gridSizeX;
        this.gridSizeY = gridSizeY;
        this.gridSizeZ = gridSizeZ;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.tidalForcingX = tidalForcingX;
        this.tidalForcingY = tidalForcingY;
        this.fluidDensity = fluidDensity;

        u = new double[gridSizeX][gridSizeY][gridSizeZ];
        v = new double[gridSizeX][gridSizeY][gridSizeZ];
        w = new double[gridSizeX][gridSizeY][gridSizeZ];
        p = new double[gridSizeX][gridSizeY][gridSizeZ];

        uNext = new double[gridSizeX][gridSizeY][gridSizeZ];
        vNext = new double[gridSizeX][gridSizeY][gridSizeZ];
        wNext = new double[gridSizeX][gridSizeY][gridSizeZ];
        pNext = new double[gridSizeX][gridSizeY][gridSizeZ];

        obstacle = new boolean[gridSizeX][gridSizeY][gridSizeZ];
        depth = depths;
    }

    // Set the obstacle at a given grid position
    public void setObstacle(int i, int j, int k) {
        obstacle[i][j][k] = true;
    }

    // Set the depth values for the grid
    public void setDepth(double[] depthValues) {
        depth = depthValues;
    }

    // Calculate the next-step velocity using the lid-driven cavity problem
    // conditions and tidal forcing
    public void calculateNextStepVelocity(double currentTime) {
        // Update the velocity components for the entire grid
        for (int i = 1; i < gridSizeX - 1; i++) {
            for (int j = 1; j < gridSizeY - 1; j++) {
                for (int k = 1; k < gridSizeZ - 1; k++) {
                    if (!obstacle[i][j][k]) {
                        calculateNextStepVelocityCell(i, j, k, currentTime);
                        calculateNextStepPressureCell(i, j, k);
                    }
                }
            }
        }
        u = uNext;
        v = vNext;
        w = wNext;
        p = pNext;
        applyPressureCorrection();
    }

    // Calculate the next-step velocity of a single cell
    private void calculateNextStepVelocityCell(int i, int j, int k, double currentTime) {

        // Retrieve the velocity components and depth of the cell
        double ui = u[i][j][k];
        double vi = v[i][j][k];
        double wi = w[i][j][k];
        double currentDepth = depth[k];

        // Calculate the grid
        // spacing in the depth dimension
        double deltaZPlus = (depth[k + 1] - currentDepth) / 2.0;
        double deltaZMinus = (currentDepth - depth[k - 1]) / 2.0;

        // Compute the gradients of velocity in each direction using central difference
        // scheme
        double du_dx = (u[i + 1][j][k] - u[i - 1][j][k]) / 2.0;
        double dv_dy = (v[i][j + 1][k] - v[i][j - 1][k]) / 2.0;
        double dw_dz = (w[i][j][k + 1] - w[i][j][k - 1]) / (deltaZPlus + deltaZMinus);

        // Compute the Laplacian of velocity in each direction using central difference
        // scheme
        double d2u_dx2 = (u[i + 1][j][k] - 2 * ui + u[i - 1][j][k]) / Math.pow((deltaX), 2);
        double d2v_dy2 = (v[i][j + 1][k] - 2 * vi + v[i][j - 1][k]) / Math.pow((deltaY), 2);
        double d2w_dz2 = (w[i][j][k + 1] - 2 * wi + w[i][j][k - 1])
                / (Math.pow(deltaZPlus, 2) + Math.pow(deltaZMinus, 2));

        // Retrieve the tidal forcing values at the current time step
        double tidalForcingX = this.tidalForcingX.get(i, j, k, currentTime);
        double tidalForcingY = this.tidalForcingY.get(i, j, k, currentTime);

        // Compute the next-step velocities using the Navier-Stokes equations with tidal
        // forcing
        double next_u = ui
                - (timeStep * (ui * du_dx + vi * dv_dy + wi * dw_dz) - (timeStep / fluidDensity) * du_dx * d2u_dx2)
                + tidalForcingX;
        double next_v = vi
                - (timeStep * (ui * du_dx + vi * dv_dy + wi * dw_dz) - (timeStep / fluidDensity) * dv_dy * d2v_dy2)
                + tidalForcingY;
        double next_w = wi
                - (timeStep * (ui * du_dx + vi * dv_dy + wi * dw_dz) - (timeStep / fluidDensity) * dw_dz * d2w_dz2);

        // Update the velocity components of the cell
        uNext[i][j][k] = next_u;
        vNext[i][j][k] = next_v;
        wNext[i][j][k] = next_w;
    }

    // Calculate the next-step pressure of a single cell
    private void calculateNextStepPressureCell(int i, int j, int k) {
        // Retrieve the velocity components and pressure of the neighboring cells
        double uEast = u[i + 1][j][k];
        double uWest = u[i - 1][j][k];
        double vNorth = v[i][j + 1][k];
        double vSouth = v[i][j - 1][k];
        double wUp = w[i][j][k + 1];
        double wDown = w[i][j][k - 1];
        // double pCenter = p[i][j][k]; // not used
        double pEast = p[i + 1][j][k];
        double pWest = p[i - 1][j][k];
        double pNorth = p[i][j + 1][k];
        double pSouth = p[i][j - 1][k];
        double pUp = p[i][j][k + 1];
        double pDown = p[i][j][k - 1];

        double currentDepth = depth[k];
        double deltaZPlus = (depth[k + 1] - currentDepth) / 2.0;
        double deltaZMinus = (currentDepth - depth[k - 1]) / 2.0;
        double deltaZ = deltaZPlus + deltaZMinus;

        // Compute the next-step pressure using the Poisson equation
        double next_p = ((pEast + pWest) / Math.pow(deltaX, 2) + (pNorth + pSouth) / Math.pow(deltaY, 2)
                + (pUp + pDown) / Math.pow(deltaZ, 2)
                - ((uEast - uWest) / deltaX + (vNorth - vSouth) / deltaY + (wUp - wDown) / deltaZ) / timeStep)
                / (2 / (Math.pow(deltaX, 2)) + 2 / (Math.pow(deltaY, 2)) + 2 / (Math.pow(deltaZ, 2)));

        // Update the pressure of the cell
        pNext[i][j][k] = next_p;
    }

    // Calculate the pressure correction using the pressure correction method
    public void applyPressureCorrection() {
        // Compute the pressure correction for the entire grid
        for (int i = 1; i < gridSizeX - 1; i++) {
            for (int j = 1; j < gridSizeY - 1; j++) {
                for (int k = 1; k < gridSizeZ - 1; k++) {
                    if (!obstacle[i][j][k]) {
                        applyPressureCorrection(i, j, k);
                    }
                }
            }
        }
    }

    private void applyPressureCorrection(int i, int j, int k) {
        // Retrieve the pressure of the neighboring cells
        double pEast = p[i + 1][j][k];
        double pWest = p[i - 1][j][k];
        double pNorth = p[i][j + 1][k];
        double pSouth = p[i][j - 1][k];
        double pUp = p[i][j][k + 1];
        double pDown = p[i][j][k - 1];
        double deltaZ = (depth[k + 1] - depth[k - 1]) / 2;

        // Compute the pressure correction using the pressure correction equation
        double pressureCorrection = (pEast - pWest) / (2 * deltaX) + (pNorth - pSouth) / (2 * deltaY)
                + (pUp - pDown) / (2 * deltaZ);

        // Apply the pressure correction to the velocity field
        u[i][j][k] -= (pressureCorrection * timeStep) / fluidDensity;
        v[i][j][k] -= (pressureCorrection * timeStep) / fluidDensity;
        w[i][j][k] -= (pressureCorrection * timeStep) / fluidDensity;
    }
}
