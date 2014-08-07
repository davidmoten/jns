jns
===

3D Navier-stokes solver for incompressible fluids using Java 8 for regions including obstacles and surface.

Interested in this generally having seen water movement models in use at Australian Maritime Safety Authority to predict the location of people that have fallen in the ocean for example.

Would like to document in java 8 (enjoying the conciseness of lambdas) a basic implementation of a [Navier-Stokes equations](http://en.wikipedia.org/wiki/Navier%E2%80%93Stokes_equations) solver for an incompressible fluid in 3D using the [pressure-correction method](http://en.wikipedia.org/wiki/Pressure-correction_method).

In terms of reference material I've found very detailed mathematical discussions of the pressure-correction method or high level descriptions and nothing in between. Ideally I'd like some pseudo code describing the algorithm with enough detail to code from. Still looking and in the meantime am fleshing out my best guesses.

*Update*: These references look useful:

* [Fractional step methods for the Navier Stokes equations on non-staggered grids](http://journal.austms.org.au/ojs/index.php/ANZIAMJ/article/download/593/461) by Armfield and Street. I might run with this shortly.
* [Numerical Fluid Mechanics MIT lectures](http://ocw.mit.edu/courses/mechanical-engineering/2-29-numerical-fluid-mechanics-fall-2011/lecture-notes/MIT2_29F11_lect_24.pdf)
* [Computational Fluid Dynamics lectures Uni Michigan](http://www.fem.unicamp.br/~phoenics/SITE_PHOENICS/Apostilas/CFD-1_U%20Michigan_Hong/Lecture13.pdf) might have sufficent explanation of the SIMPLER algorithm to run with also.
* [Computational Methods for Fluid Dynamics](https://docs.google.com/file/d/0B7WvmGcRs5CzanBEeDlDaEk3dEU/edit) by Ferziger and Peric
* [2D solver in mathematica](http://blog.wolfram.com/2013/07/09/using-mathematica-to-simulate-and-visualize-fluid-flow-in-a-box/)

Navier-Stokes Equations
-------------------------
The momentum equation is:

&nbsp;&nbsp;&nbsp;&nbsp;&rho;( &delta;**v**/&delta;t + (**v** &sdot; &nabla;)**v** ) = -&nabla;p + &mu;&nabla;<sup>2</sup>**v** + **f**

where 

&nbsp;&nbsp;&nbsp;&nbsp;&rho; = fluid density

&nbsp;&nbsp;&nbsp;&nbsp;**v** = velocity vector

&nbsp;&nbsp;&nbsp;&nbsp;p = pressure

&nbsp;&nbsp;&nbsp;&nbsp;t = time

&nbsp;&nbsp;&nbsp;&nbsp;**f** = other forces vector (for example gravity)

&nbsp;&nbsp;&nbsp;&nbsp;&mu; = viscosity (1 / Reynolds number)

The governing equation for pressure computation (derived via *conservation of mass*) is:

&nbsp;&nbsp;&nbsp;&nbsp;&nabla;<sup>2</sup>p = -&nabla; &sdot; (**v** &sdot; &nabla;)**v**

Note that for the term **f** above in the case of gravity this is a vector with z coordinate equal 
to the force exerted by gravity on a cubic metre of fluid. This is thus 

&nbsp;&nbsp;&nbsp;&nbsp;ACCELERATION * MASS = ACCELERATION * DENSITY * VOLUME = 9.81m/s<sup>2</sup> * 1025kg/m<sup>3</sup> * 1m<sup>3</sup> using an approximation for the density of salt water. 

Architecture
--------------
Aim is to provide an implementation that is as much as possible independent of the chosen mesh (regular or irregular) and thus to defer calculations of say first and second derivatives to be particular to the mesh type.

Unit tests will be created for regular grid meshes.

The [Solver](src%2Fmain%2Fjava%2Fcom%2Fgithub%2Fdavidmoten%2Fjns%2FSolver.java) class is the main entry point.
