package com.github.davidmoten.jns.v2;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

import com.github.davidmoten.guavamini.Preconditions;

/**
 *
 * <p>
 * The governing equations are solved on a computational mesh. The mesh used in
 * this document is uniform with mesh cells of width ∆x and height, ∆y. The grid
 * divides the domain in to nx × ny cells where nx and ny are the number of
 * cells in the x and y directions, respectively.
 * <p>
 * The grid cells are referred to using their index. The i index referes to the
 * cells x direction and the j index referes to cells in the y direction.
 * <p>
 * A staggered grid is used to store the variables where the pressure is stored
 * at the cell center and the velocities are stored at the cell faces. This,
 * possibly odd, choice is made since it allows for the solution to have a tight
 * coupling between pressure and the velocity and has been found to be the
 * preferred methodology.
 * <p>
 * Arrays are created to refer to the locations important for each cell. x(i)
 * stores the location of the ith cells left face. y(j) stores the location of
 * the jth cells bottom face. The location of the middle of the cell is stored
 * in the xm(i) and the ym(j) arrays.
 *
 */
public class Mesh {

    private final int nx;
    private final int ny;
    private final double lx; // length of mesh in x direction
    private final double ly; // length of mesh in y direction
    private final int imin;
    private final int imax;
    private final int jmin;
    private final int jmax;

    private final double[] x;
    private final double[] y;

    private final double[] xm; // middles of x positions
    private final double[] ym; // middles of y positions
    private final double dx;
    private final double dy;
    private final double dxi;
    private final double dyi;
    private final double nu;
    private final double rho;
    private final double[][] u;
    private final double[][] v;

//    % Index extents
//    imin =2; imax=imin+nx−1;
//    jmin =2; jmax=jmin+ny−1;
//    % Create mesh
//    x ( imin : imax+1)=l inspace ( 0 , Lx , nx +1);
//    y ( jmin : jmax+1)=l inspace ( 0 , Ly , ny +1);
//    xm( imin : imax )= 0. 5∗( x ( imin : imax)+x ( imin +1:imax + 1 ) );
//    ym( jmin : jmax )= 0. 5∗( y ( jmin : jmax)+y ( jmin +1: jmax + 1 ) );
//    % Create mesh sizes
//    dx=x ( imin+1)−x ( imin ) ;
//    dy=y ( jmin+1)−y ( jmin ) ;
//    d xi=1/dx ;
//    d yi=1/dy ;

//    A few notes on this code:
//        • nx=nx and ny=ny
//        • Lx and Ly are the lengths of the domain in the x and y directions, respectively.
//        • The index extents, imin, imax, jmin, and jmax, provide a quick way to access the first and last computational cells. The index extents do not start at 1 because we need to add cells outside the domain
//        to enforce boundary conditions (more on this later).
//        • The mesh sizes are precomputed to save computational cost. Additionally dxi = 1/dx and dyi = 1/dy
//        are also precomputed since divisions are significantly more computationally expensive than multiplications.

    public Mesh(int nx, int ny, double lx, double ly, double nu, double rho) {
        this.nx = nx;
        this.ny = ny;
        this.lx = lx;
        this.ly = ly;
        this.nu = nu;
        this.rho = rho;

        this.imin = 1;
        this.imax = imin + nx - 1;
        this.jmin = 1;
        this.jmax = jmin + ny - 1;

        // make arrays one cell bigger for boundary stuff
        this.x = new double[nx + 2];
        this.y = new double[ny + 2];

        for (int i = imin; i <= imax + 1; i++) {
            x[i] = (i - 1) * (lx / nx);
        }

        for (int j = jmin; j <= jmax + 1; j++) {
            y[j] = (j - 1) * (ly / ny);
        }

        xm = new double[nx + 1];
        ym = new double[ny + 1];

        for (int i = imin; i <= imax; i++) {
            xm[i] = (x[i] + x[i + 1]) / 2;
        }

        for (int j = jmin; j <= jmax; j++) {
            ym[j] = (y[j] + y[j + 1]) / 2;
        }

        dx = x[imin + 1] - x[imin];
        dy = y[jmin + 1] - y[jmin];
        dxi = 1 / dx;
        dyi = 1 / dy;
        
     // components of velocity
        u = new double[nx + 2][ny + 2];
        v = new double[nx + 2][ny + 2];
    }

    public void run(double[] uTop, double[] uBottom, double[] vLeft, double[] vRight, double dt) {

        Preconditions.checkArgument(uTop.length == lx);
        Preconditions.checkArgument(uBottom.length == lx);
        Preconditions.checkArgument(vLeft.length == ly);
        Preconditions.checkArgument(vRight.length == ly);


        // ustar is intermediate u till pressure correction happens
        double[][] us = new double[nx + 2][ny + 2];

        // vstart is intermediate v till pressure correction happens
        double[][] vs = new double[nx + 2][ny + 2];

        // apply boundary conditions

        for (int i = imin - 1; i <= imax + 1; i++) {
            u[i][jmin - 1] = u[i][jmin] - 2 * (u[i][jmin] - uBottom[i]);
            u[i][jmax + 1] = u[i][jmax] - 2 * (u[i][jmax] - uTop[i]);
        }

        for (int j = jmin - 1; j <= jmax + 1; j++) {
            v[imin - 1][j] = v[imin][j] - 2 * (v[imin][j] - vLeft[j]);
            v[imax + 1][j] = v[imax][j] - 2 * (v[imax][j] - vRight[j]);
        }

        // The convective and viscous terms in Eq. 4 are discretized using finite
        // differences which approximate the derivatives using neighboring values.

        for (int j = jmin; j <= jmax; j++) {
            for (int i = imin + 1; i <= imax; i++) {
                double vmiddle = 0.25 * (v[i - 1][j] + v[i - 1][j + 1] + v[i][j] + v[i][j + 1]);
                double d2udx2 = (u[i - 1][j] - 2 * u[i][j] + u[i + 1][j]) / dx / dx;
                double d2udy2 = (u[i][j - 1] - 2 * u[i][j] + u[i][j + 1]) / dy / dy;
                double dudx = (u[i + 1][j] - u[i - 1][j]) / 2 / dx;
                double dudy = (u[i][j + 1] - u[i][j - 1]) / 2 / dy;
                us[i][j] = u[i][j]
                        + dt * (nu * (d2udx2 + d2udy2) - (u[i][j] * dudx + vmiddle * dudy));
            }
        }

        for (int j = jmin + 1; j <= jmax; j++) {
            for (int i = imin; i <= imax; i++) {
                double umiddle = 0.25 * (u[i - 1][j] + u[i - 1][j + 1] + u[i][j] + u[i][j + 1]);
                double d2vdx2 = (v[i - 1][j] - 2 * v[i][j] + v[i + 1][j]) / dx / dx;
                double d2vdy2 = (v[i][j - 1] - 2 * v[i][j] + v[i][j + 1]) / dy / dy;
                double dvdx = (v[i + 1][j] - v[i - 1][j]) / 2 / dx;
                double dvdy = (v[i][j + 1] - v[i][j - 1]) / 2 / dy;
                vs[i][j] = v[i][j]
                        + dt * (nu * (d2vdx2 + d2vdy2) - (u[i][j] * dvdx + umiddle * dvdy));
            }
        }

        ///////////////////////
        // Poisson Equation //
        ///////////////////////

        // Solve Lp = R where L is the Laplacian operator

        // Use EJML library
        DMatrixSparseCSC laplacian = new DMatrixSparseCSC(nx * ny, nx * ny);

        for (int j = 1; j <= ny; j++) {
            for (int i = 1; i <= nx; i++) {
                laplacian.set( //
                        i + (j - 1) * nx - 1, //
                        i + (j - 1) * nx - 1, 2 * dxi * dxi + 2 * dyi * dyi);
                for (int ii = i - 1; ii <= i + 1; ii += 2) {
                    if (ii > 0 && ii < nx) {
                        laplacian.set( //
                                i + (j - 1) * nx - 1, //
                                ii + (j - 1) * nx - 1, //
                                -dxi * dxi);
                    } else { // Neuman conditions on the boundary
                        laplacian.set( //
                                i + (j - 1) * nx - 1, //
                                i + (j - 1) * nx - 1, //
                                laplacian.get( //
                                        i + (j - 1) * nx - 1, //
                                        i + (j - 1) * nx - 1) //
                                        - dxi * dxi);
                    }
                }

                for (int jj = j - 1; jj <= j + 1; jj += 2) {
                    if (jj > 0 && jj < ny) {
                        laplacian.set( //
                                i + (j - 1) * nx - 1, //
                                i + (jj - 1) * nx - 1, //
                                -dyi * dyi);
                    } else { // Neuman conditions on the boundary
                        laplacian.set( //
                                i + (j - 1) * nx - 1, //
                                i + (j - 1) * nx - 1, //
                                laplacian.get( //
                                        i + (j - 1) * nx - 1, //
                                        i + (j - 1) * nx - 1) //
                                        - dyi * dyi);
                    }
                }
            }
        }

        DMatrixRMaj pv = new DMatrixRMaj(nx * ny, 1);
        {
            double[] r = new double[nx * ny];
            int n = 0;
            for (int j = jmin; j <= jmax; j++) {
                for (int i = imin; i <= imax; i++) {
                    n++;
                    r[n - 1] = -rho / dt * ( //
                    (us[i + 1][j] - us[i][j]) * dxi //
                            + (vs[i][j + 1] - vs[i][j]) * dyi);
                }
            }
            CommonOps_DSCC.solve(laplacian, new DMatrixRMaj(r), pv);
        }

        // convert the pressure vector to a matrix
        int n = 0;
        double[][] p = new double[nx][ny];
        for (int i = imin; i <= imax; i++) {
            for (int j = jmin; j <= jmax; j++) {
                n++;
                p[i][j] = pv.get(n - 1);
            }
        }

        // do the corrector step

        for (int j = jmin; j <= jmax; j++) {
            for (int i = imin + 1; i <= imax; i++) {
                u[i][j] = us[i][j] - dt / rho * (p[i][j] - p[i - 1][j]) * dxi;
            }
        }
        for (int j = jmin + 1; j <= jmax; j++) {
            for (int i = imin; i <= imax; i++) {
                v[i][j] = vs[i][j] - dt / rho * (p[i][j] - p[i][j - 1]) * dyi;
            }
        }

    }

    public static void main(String[] args) {
    }

}
