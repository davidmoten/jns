jns
===

3D Navier-stokes solver for incompressible fluids using Java 8 for regions including obstacles and surface.

Interested in this generally having seen water movement models in use at Australian Maritime Safety Authority to predict the location of people that have fallen in the ocean for example.

Would like to document in java 8 (enjoying the conciseness of lambdas) a basic implementation of a [Navier-Stokes equations](http://en.wikipedia.org/wiki/Navier%E2%80%93Stokes_equations) solver for an incompressible fluid in 3D using the [pressure-correction method](http://en.wikipedia.org/wiki/Pressure-correction_method).

In terms of reference material I've found very detailed mathematical discussions of the pressure-correction method or high level descriptions and nothing in between. Ideally I'd like some pseudo code describing the algorithm with enough detail to code from. Still looking and in the meantime am fleshing out my best guesses.

*Update*: This looks good [Fractional step methods for the Navier Stokes equations on non-staggered grids](http://journal.austms.org.au/ojs/index.php/ANZIAMJ/article/download/593/461) by Armfield and Street. I might run with this shortly.

Architecture
--------------
Aim is to provide an implementation that is as much as possible independent of the chosen mesh (regular or irregular) and thus to defer calculations of say first and second derivatives to be particular to the mesh type.

Unit tests will be created for regular grid meshes.
