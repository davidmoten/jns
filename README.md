jns
===

3D Navier-stokes solver for incompressible fluids using Java 8 for regions including obstacles and surface.

Interested in this generally having seen water movement models in use at Australian Maritime Safety Authority to predict the location of people that have fallen in the ocean for example.

Would like to document in java 8 (enjoying the conciseness of lambdas) a basic implementation of a [Navier-Stokes equations](http://en.wikipedia.org/wiki/Navier%E2%80%93Stokes_equations) solver for an incompressible fluid in 3D using the [pressure-correction method](http://en.wikipedia.org/wiki/Pressure-correction_method). See Architecture below for more specific aims.

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

Note that for the term **f** above in the case of gravity this is a vector with z coordinate equal 
to the force exerted by gravity on a cubic metre of fluid. This is thus 

&nbsp;&nbsp;&nbsp;&nbsp;f<sub>z</sub> = ma = density * volume * accelerationGravity <br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;= 1025kg/m<sup>3</sup> * 1m<sup>3</sup> * -9.81m/s<sup>2</sup> using an approximation for the density of salt water<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;= -10055.25kgm/s<sup>2</sup>

So in this case **f** = (0,0,-10055.25) assuming gravity is the only extraneous force.

The governing equation for pressure computation (derived via *conservation of mass*) is:

&nbsp;&nbsp;&nbsp;&nbsp;&nabla;<sup>2</sup>p = -&nabla; &sdot; (**v** &sdot; &nabla;)**v**

Derivatives
-------------
Numerical approximations for the derivatives (first and second) of a function f are given by:

&nbsp;&nbsp;Suppose the point x has two close neighbours a,b (a<x<b)

&nbsp;&nbsp;Then

&nbsp;&nbsp;&nbsp;&nbsp;f'(x) &#8776; (f(b) - f(a))/(b - a)

&nbsp;&nbsp;&nbsp;&nbsp;f''(x) &#8776; (f(b) + f(a) - 2f(x))/(b-a)<sup>2</sup>

Architecture
--------------
Aims:

* decouple the algorithm, the mesh, derivative methods, root solver so that the program can be altered with ease.
* the algorithm should be clearly visible in the code
* minimize pollution of code with data structure choices (array iterations for example)
* accept performance degradation arising from the decoupling but seek to later leverage concurrency possibly in a distributed fashion.
* consider using lazy computation


Unit tests will be created for regular grid meshes.

The [Solver](src%2Fmain%2Fjava%2Fcom%2Fgithub%2Fdavidmoten%2Fjns%2FSolver.java) class is the main entry point.


Lazy computation
-------------------

What would be nice is the ability to only calculate what we need for the output. For instance in a 10x10x10 grid
if I want to know the value of velocity at (5,5,5) after 2 time steps then clearly the whole grid does not have
to be computed for the two time steps. 

When the full grid is computed, rather than map-reduce (which might be the best bet for distributed processing) seek to enable [Rx](http://github.com/Netflix/RxJava) to improve performance. 
