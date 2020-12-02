package com.github.davidmoten.jns.v2;

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

    public Mesh(int nx, int ny, double lx, double ly) {
        this.nx = nx;
        this.ny = ny;
        this.lx = lx;
        this.ly = ly;

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
    }

    public static void main(String[] args) {
        new Mesh(5, 6, 10, 20);
    }

}
