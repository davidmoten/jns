/////////////////////////////////////////////////////////////////////
// Navier Stokes Solver                                                                        
/////////////////////////////////////////////////////////////////////
/**
 * Provides a clear and concise solver for the
 * Navier Stokes equations over a rectangular
 * grid domain (regularly gridded) for an
 * incompressible liquid (sea water). Concise
 * and clear code is considered more important
 * than performance because concurrent/
 * distributed running of the routines will be
 *  used to provide performance scalability.
 */
package org.moten.david.ns

/////////////////////////////////////////////////////////////////////

/**
 * Logs to System.out with a timestamp.
 */
object Logger {
  import java.util.Date
  import java.text.SimpleDateFormat
  val df = new SimpleDateFormat("HH:mm:ss.SSS")
  var infoEnabled = true
  var debugEnabled = true
  def info(msg: => AnyRef) = if (infoEnabled)
    println(df.format(new Date()) + " " + msg)
  def debug(msg: => AnyRef) = if (debugEnabled)
    println(df.format(new Date()) + " " + msg)
}
import Logger._

/**
 * Useful exceptions.
 */
object Throwing {
  def unexpected =
    throw new RuntimeException(
      "program should not get to this point")

  def unexpected(message: String) =
    throw new RuntimeException(message)

  def todo =
    throw new RuntimeException("not implemented, TODO")
}

import Throwing._

/////////////////////////////////////////////////////////////////////
// Direction                                                                        
/////////////////////////////////////////////////////////////////////
/**
 * X,Y horizontal coordinates (arbitrary coordinate system).
 * Z is height above sea level in m (all calculations
 * assume SI units).
 *
 */
object Direction extends Enumeration {
  type Direction = Value
  val X, Y, Z = Value
  def directions = List(X, Y, Z)
}
import Direction._

/////////////////////////////////////////////////////////////////////
// Derivative
/////////////////////////////////////////////////////////////////////
/**
 * The derivative type.
 */
object DerivativeType extends Enumeration {
  type Derivative = Value
  val FirstDerivative, SecondDerivative = Value
}
import DerivativeType._

/////////////////////////////////////////////////////////////////////
// Vector                                
/////////////////////////////////////////////////////////////////////

/**
 * A mathematical vector in X,Y,Z space.
 *
 * @param x
 * @param y
 * @param z
 */
case class Vector(x: Double, y: Double, z: Double) {
  def this(list: List[Double]) {
    this(list(0), list(1), list(2))
  }
  def this(t: Tuple3[Double, Double, Double]) {
    this(t._1, t._2, t._3)
  }
  def get(direction: Direction): Double = {
    direction match {
      case X => x
      case Y => y
      case Z => z
    }
  }
  def *(v: Vector) = x * v.x + y * v.y + z * v.z
  def *(d: Double) = Vector(x * d, y * d, z * d)
  def minus(v: Vector) = Vector(x - v.x, y - v.y, z - v.z)
  def -(v: Vector) = minus(v)
  def +(v: Vector) = add(v)
  def add(v: Vector) = Vector(x + v.x, y + v.y, z + v.z)
  def /(d: Double) = Vector(x / d, y / d, z / d)
  def /(v: Vector) = Vector(x / v.x, y / v.y, z / v.z)
  def sum = x + y + z
  def modify(direction: Direction, d: Double) = {
    Vector(if (direction equals X) d else x,
      if (direction equals Y) d else y,
      if (direction equals Z) d else z)
  }
  def ===(v: Vector) = this equals v
  def list = List(x, y, z)
  def add(direction: Direction, d: Double) =
    modify(direction, get(direction) + d)
}

/**
 * Companion object for Vector.
 */
object Vector {
  import Double._
  def zero = Vector(0, 0, 0)
}

import Vector._

/**
 * An ordering to help with readable String
 * representations of Vector collections.
 */
object HasPositionOrdering extends Ordering[HasPosition] {
  override def compare(a: HasPosition, b: HasPosition): Int =
    if (a.position.z != b.position.z)
      return a.position.z compare b.position.z
    else if (a.position.y != b.position.y)
      return a.position.y compare b.position.y
    else
      return a.position.x compare b.position.x
}

/**
 * A 3 dimensional matrix.
 */
case class Matrix(row1: Vector, row2: Vector, row3: Vector) {
  def *(v: Vector) =
    Vector(row1 * v, row2 * v, row3 * v)
}

/////////////////////////////////////////////////////////////////////
// Value                                                                        
/////////////////////////////////////////////////////////////////////

/**
 * Has a position in 3D space.
 */
trait HasPosition {
  def position: Vector
}

/**
 * Has a position in 3D space and a value (for example temperature).
 */
trait HasValue extends HasPosition {
  def value: Value
}

/**
 * A concrete implementation of HasValue.
 */
case class Point(value: Value)
  extends HasValue {
  val position = value.position
}

/**
 * Is a candidate for being an edge of a set of positions. 
 */
trait EdgeCandidate

/**
 * Is an obstacle to water movement.
 */
case class Obstacle(position: Vector)
  extends HasPosition with EdgeCandidate

/**
 * Is either water or obstacle with unknown value. 
 */
case class Empty(position: Vector) extends HasPosition
  with EdgeCandidate {
  def this() {
    this(Vector.zero)
  }
}

/**
 * Measures the velocity and pressure field and water
 * properties for a given position.
 */
case class Value(position: Vector,
  velocity: Vector, pressure: Double,
  density: Double, viscosity: Double)
  extends HasPosition with HasValue {

  val value = this

  /**
   * Returns a copy of this with pressure modified.
   * @param p
   * @return
   */
  def modifyPressure(p: Double) =
    new Value(position, velocity, p, density, viscosity)

  /**
   * Returns a copy of this with velocity modified.
   * @param vel
   * @return
   */
  def modifyVelocity(vel: Vector) =
    new Value(position, vel, pressure, density, viscosity)

  def modifyPosition(pos: Vector) =
    new Value(pos, velocity, pressure, density, viscosity)

}

/**
 * Companion object.
 */
object Value {
  import scala.language.implicitConversions

  implicit def toValue(v: HasValue) = v.value
  implicit def toPosition(p: HasPosition) = p.position
}

/////////////////////////////////////////////////////////////////////
// Solver                      
/////////////////////////////////////////////////////////////////////

/**
 * Companion object for `Solver`.
 */
object Solver {
  /**
   * Acceleration due to gravity. Note that this vector
   * determines the meaning of the Z direction (positive Z
   * direction is decrease in depth).
   */
  val gravity = Vector(0, 0, -9.8)

  /**
   * Returns the value of a function of interest on the
   *  Position/Value field
   */
  type ValueFunction = HasValue => Double

}

/**
 * Positions, values and methods for the numerical Navier
 * Stokes equation solver.
 */
trait Solver {
  import Solver._
  import Value._

  /**
   * ************************************************
   * Implement these
   * ************************************************
   */

  /**
   * Returns all positions.
   * @return
   */
  def getPositions: Set[HasPosition]

  /**
   * Returns the gradient of the function f with respect to direction at the
   * given position.
   *
   * @param position
   * @param direction
   * @param f
   * @param derivativeType
   * @return
   */
  def getGradient(position: HasPosition, direction: Direction,
    f: ValueFunction,
    derivativeType: Derivative, overrideValue: Option[HasValue]): Double

  /**
   * Returns calculated `Solver` after timestep seconds.
   * @param timestep
   * @return
   */
  def step(timestep: Double): Solver

  /**
   * ************************************************
   * Implemented for you
   * ************************************************
   */

  /**
   * Returns the Laplacian of the velocity vector in the given direction.
   * @param position
   * @param direction
   * @return
   */
  private def getVelocityLaplacian(position: HasValue, direction: Direction) =
    getVelocityGradient2nd(position, direction).sum

  /**
   * Returns the Laplacian of the velocity vector as a vector.
   * @param position
   * @return
   */
  private def getVelocityLaplacian(position: HasValue): Vector =
    Vector(getVelocityLaplacian(position, X),
      getVelocityLaplacian(position, Y),
      getVelocityLaplacian(position, Z))

  /**
   * Returns the Jacobian of velocity at a position.
   * @param position
   * @return
   */
  private def getVelocityJacobian(position: HasValue) =
    Matrix(getVelocityGradient(position, X),
      getVelocityGradient(position, Y),
      getVelocityGradient(position, Z))

  /**
   * Returns the derivative of velocity over time using this
   * {http://en.wikipedia.org/wiki/Navier%E2%80%93Stokes_equations#Cartesian_coordinates
   *  formula}.
   * @param position
   * @return
   */
  private def dvdt(position: HasValue) = {
    val value = position.value
    val velocityLaplacian: Vector = getVelocityLaplacian(position)
    val pressureGradient: Vector = getPressureGradient(position)
    val velocityJacobian: Matrix = getVelocityJacobian(position)
    val divergenceOfStress =
      velocityLaplacian * value.value.viscosity minus pressureGradient
    debug("velocityLaplacian" + velocityLaplacian)
    debug("pressureGradient=" + pressureGradient)
    debug("velocityJacobian=" + velocityJacobian)
    debug("divergenceOfStress=" + divergenceOfStress)

    val result = ((divergenceOfStress) / value.density)
      .add(gravity)
      .minus(velocityJacobian * value.velocity)
    debug("dvdt=" + result)
    result
  }

  /** 
   * Returns the Laplacian of pressure at position which in 3D is:
   * dp2/d2x + dp2/d2y + dp2/d2z.
   * @param position
   * @return
   */
  private def getPressureLaplacian(position: HasValue, overrideValue: HasValue) =
    getPressureGradient2nd(position, overrideValue).sum

  /**
   * Returns the velocity vector after time timeDelta seconds.
   * @param position
   * @param timeStep
   * @return
   */
  private def getVelocityAfterTime(position: HasValue, timeDelta: Double) =
    position.value.velocity.add(dvdt(position) * timeDelta)

  /**
   * Returns the Conservation of Mass (Continuity) Equation described by the
   * Navier-Stokes equations.
   */
  private def getPressureCorrection(position: HasValue, v1: Vector,
    timeDelta: Double)(pressure: Double): Double = {
    val v = position.value
    //assume not obstacle or boundary
    val overrideValue = v.modifyPressure(pressure)
    getPressureCorrection(position, overrideValue)
  }

  /**
   * Returns the value of the pressure correction function at position.
   * @param position
   * @return
   */
  private def getPressureCorrection(position: HasValue, overrideValue: HasValue): Double = {
    val pressureLaplacian = getPressureLaplacian(position, overrideValue)
    return pressureLaplacian +
      directions.map(d => overrideValue.velocity.get(d) * getGradient(position, d,
        gradientDot(d),
        FirstDerivative, Some(overrideValue))).sum
  }

  /**
   * Returns the value of Del(Del dot v) for a given position.
   * @param direction
   * @param v
   * @return
   */
  def gradientDot(direction: Direction)(position: HasValue): Double =
    getVelocityGradient(position, direction) * position.velocity

  /**
   * Returns the` Value` at the given position after `timeDelta` in seconds
   * by solving
   * <a href="http://en.wikipedia.org/wiki/Navier%E2%80%93Stokes_equations#Cartesian_coordinates">
   * a 3D formulation of the Navier-Stokes equations</a>.  After the velocity
   *  calculation a pressure correction is performed according to this
   * <a href="http://en.wikipedia.org/wiki/Pressure-correction_method">method</a>.
   */
  def getValueAfterTime(position: HasPosition, timeDelta: Double): HasPosition = {
    position match {
      case v: HasValue => getValueAfterTime(v, timeDelta)
      case _ => position
    }
  }

  /**
   * Returns the` Value` at the given position after `timeDelta` in seconds
   * by solving
   * <a href="http://en.wikipedia.org/wiki/Navier%E2%80%93Stokes_equations#Cartesian_coordinates">
   * a 3D formulation of the Navier-Stokes equations</a>.  After the velocity
   *  calculation a pressure correction is performed according to this
   * <a href="http://en.wikipedia.org/wiki/Pressure-correction_method">method</a>.
   */
  private def getValueAfterTime(position: HasValue, timeDelta: Double): HasValue = {
    debug("getting value after time at " + position)
    val value = position.value
    debug("value=" + value)

    val v1 = getVelocityAfterTime(position, timeDelta)
    debug("v1=" + v1)
    val f = getPressureCorrection(position, v1, timeDelta)(_)
    //TODO what values for h,precision?
    val h = 1
    val precision = 0.000001
    val maxIterations = 15
    val newPressure = NewtonsMethod.solve(f, value.pressure, h,
      precision, maxIterations) match {
        case None => value.pressure
        case Some(a) => if (a < 0) value.pressure else a
      }
    debug("newPressure=" + newPressure + "old=" + value.pressure)
    return value.value.modifyPressure(newPressure).modifyVelocity(v1)
  }

  /**
   * Returns the pressure gradient vector at position.
   * @param position
   * @return
   */
  private def getPressureGradient(position: HasValue): Vector =
    new Vector(directions.map(getPressureGradient(position, _)))

  /**
   * Returns the pressure gradient at position in a given direction.
   * @param position
   * @param direction
   * @return
   */
  private def getPressureGradient(position: HasValue,
    direction: Direction): Double = {
    val value = position.value;
    getGradient(position, direction,
      (p: HasValue) => p.pressure,
      FirstDerivative, None)
  }

  /**
   * Returns the second derivative pressure gradient at position.
   * @param position
   * @return
   */
  private def getPressureGradient2nd(position: HasValue, overrideValue: HasValue): Vector =
    new Vector(directions.map(d =>
      getGradient(position, d,
        (p: HasValue) => p.pressure,
        SecondDerivative, Some(overrideValue))))

  /**
   * Returns the gradient of the velocity vector at position in the given
   * direction.
   * @param position
   * @param direction
   * @return
   */
  private def getVelocityGradient(position: HasValue, direction: Direction): Vector = {
    def velocity(d: Direction) = (p: HasValue) => p.value.velocity.get(d)
    new Vector(directions.map(
      d => getGradient(position, direction, velocity(d), FirstDerivative, None)))
  }

  /**
   * Returns the gradient of the pressure gradient at position in the
   * given direction.
   * @param position
   * @param direction
   * @return
   */
  private def getVelocityGradient2nd(position: HasValue,
    direction: Direction): Vector = {
    def velocity(d: Direction) = (p: HasValue) => p.velocity.get(d)
    new Vector(directions.map(d =>
      getGradient(position, direction, velocity(d),
        SecondDerivative, None)))
  }

  /**
   * Returns a new immutable Solver object representing the
   * state of the system after `timestep` seconds.
   * @param solver
   * @param timestep
   * @param numSteps
   * @return
   */
  private def step(solver: Solver, timestep: Double, numSteps: Int): Solver =
    if (numSteps == 0) return solver
    else return step(solver.step(timestep), timestep, numSteps - 1)

  /**
   * Returns a new immutable Solver object after repeating the
   *  timestep `numSteps` times.
   * @param timestep
   * @param numSteps
   * @return
   */
  def step(timestep: Double, numSteps: Int): Solver =
    step(this, timestep, numSteps)

  /**
   * Returns a readable view of the positions and their values.
   * @return
   */
  override def toString = getPositions.toList.sorted(HasPositionOrdering)
    .map(v => v.toString + "\n").toString
}

/////////////////////////////////////////////////////////////////////
// Sign                      
/////////////////////////////////////////////////////////////////////

trait NonZeroSign
case class PositiveSign() extends NonZeroSign
case class NegativeSign() extends NonZeroSign

object Sign {
  val Positive = PositiveSign()
  val Negative = NegativeSign()
  val nonZeroSigns = List(Positive, Negative)
}

import Sign._

/////////////////////////////////////////////////////////////////////
// Grid                      
/////////////////////////////////////////////////////////////////////

/**
 * Utility methods for a Grid of 3D points.
 */
object RegularGrid {

  type DirectionalNeighbours = Map[(Direction, NonZeroSign, Vector), HasPosition];

  def getDirectionalNeighbours(positions: Set[HasPosition]): DirectionalNeighbours = {

    // get a map of (vector, direction) 
    //                -> List of positions sorted by direction ordinate
    // which is for each direction say x a map of ((0,y,z), X) -> ((0.8,y,z),(1.2,y,z))
    // so that for a given point on a regular grid we can decide what are the 
    // neighbouring points
    val map: Map[(Vector, Direction), List[HasPosition]] =
      getPositionsByTwoOrdinatesAndDirection(positions)
    val list =
      for (d <- directions; sign <- nonZeroSigns; p <- positions) // use map above to return closest position for the direction and sign
      yield {
        val closest = closestNeighbour(map, d, sign, p.position)
        ((d, sign, p.position), closest)
      }
    list.toMap
  }

  def getPositionsByTwoOrdinatesAndDirection(positions: Set[HasPosition]): Map[(Vector, Direction), List[HasPosition]] =
    positions.toList.map(
      p =>
        directions.map(
          d => ((p.position.modify(d, 0), d), p))).flatten
      .groupBy(_._1)
      .toList
      .map(x => (x._1,
        x._2.map(y => y._2)
        .sortBy(y => y.position.get(x._1._2))))
      .toMap

  private def closestNeighbour(map: Map[(Vector, Direction), List[HasPosition]],
    d: Direction, sign: NonZeroSign, p: Vector): HasPosition = {
    val plist = map.getOrElse((p.modify(d, 0), d), unexpected)

    //validate boundaries
    if (!isEdge(plist.head))
      unexpected("edge (obstacle or empty) not found at start of " + plist)
    if (!isEdge(plist.last))
      unexpected("edge (obstacle or empty) not found at end of " + plist)

    closestNeighbour(plist, d, sign, p)
  }

  private def isEdge(p: HasPosition) =
    p.isInstanceOf[EdgeCandidate]

  def closestNeighbour(list: List[HasPosition], d: Direction, sign: NonZeroSign, p: Vector): HasPosition = {
    //can assumes list is sorted by increasing value in the Direction ordinate
    if (list.size == 1)
      Empty(p)
    else {
      val index = list.map(_.position).indexOf(p);
      if (sign == Positive) {
        if (index == list.size - 1)
          Empty(p)
        else
          list(index + 1)
      } else {
        if (index == 0)
          Empty(p)
        else
          list(index - 1)
      }
    }
  }

}

/**
 * Regular or irregular grid of 3D points (vectors).
 */
case class RegularGrid(positions: Set[HasPosition]) {
  private val map = RegularGrid.getDirectionalNeighbours(positions)

  def neighbours(direction: Direction, sign: NonZeroSign, p: Vector) =
    map.getOrElse((direction, sign, p), unexpected("no entry for " + (direction, sign, p) + "\n" + map))
}

/////////////////////////////////////////////////////////////////////
// RegularGridSolver                                                                        
/////////////////////////////////////////////////////////////////////

object RegularGridSolver {
  import scala.math.signum
  import Solver._
  import Value._

  type O = Obstacle
  type A = HasPosition //A for Any
  type E = Empty
  type V = HasValue
  type Positions = Tuple3[HasPosition, HasPosition, HasPosition]

  private def getNeighbours(grid: RegularGrid, position: HasPosition,
    direction: Direction): Positions =
    (
      grid.neighbours(direction, Negative, position),
      position,
      grid.neighbours(direction, Positive, position))

  def overrideValue(t: HasPosition, overrideValue: Value): HasPosition = {
    if (t.position.equals(overrideValue.position))
      overrideValue
    else
      t
  }

  def overrideValue(t: Positions, v: HasValue): Positions =
    (overrideValue(t._1, v),
      overrideValue(t._2, v),
      overrideValue(t._3, v))

  def overrideValue(t: Positions, v: Option[HasValue]): Positions =
    v match {
      case Some(value: HasValue) => overrideValue(t, value)
      case None => t
    }

  def getGradient(grid: RegularGrid, position: HasPosition, direction: Direction,
    f: ValueFunction, derivativeType: Derivative,
    overridden: Option[HasValue]): Double = {
    val n = overrideValue(
      getNeighbours(grid, position, direction), overridden)
    getGradient(f, n._1, n._2, n._3, direction, derivativeType)
  }

  def getGradient(f: ValueFunction,
    v1: HasPosition, v2: HasPosition, v3: HasPosition, direction: Direction,
    derivativeType: Derivative): Double = {

    if (v2.isInstanceOf[Obstacle])
      unexpected("why ask for gradient at obstacle? " + v2)
    else {

      val t = transform((v1, v2, v3))

      //cannot use match statement because of type erasure
      if (is[V, V, V](t))
        getGradient(f, toV(t._1), toV(t._2), toV(t._3), direction, derivativeType)
      else if (is[V, V, E](t))
        getGradient(f, toV(t._1), toV(t._2), direction, derivativeType)
      else
        unexpected
    }
  }

  /**
   * Returns either (V,V,V) or (V,V,E).
   *
   * @param t
   * @return
   */
  private def transform(
    v: (HasPosition, HasPosition, HasPosition)): (HasPosition, HasPosition, HasPosition) = {

    //would like to use a match statement like
    //t match {
    //  case v: (V, V, V, Sign) => (v._1, v._2, v._3)
    // ...
    // but type erasure means we cannot

    if (is[V, V, V](v))
      (v._1, v._2, v._3)
    else if (is[V, V, E](v))
      (v._1, v._2, v._3)
    else if (v._2.isInstanceOf[Obstacle])
      (v._1, v._2, v._3)
    else if (is[E, V, V](v))
      (v._2, v._3, v._1)
    else if (is[A, V, O](v))
      transform((v._1, v._2, obstacleToHasValue(toO(v._3), toV(v._2))))
    else if (is[O, V, A](v))
      transform((obstacleToHasValue(toO(v._1), toV(v._2)), v._2, v._3))
    else unexpected("not handled " + v)
  }

  private def toO(p: HasPosition) = p.asInstanceOf[O]

  private def toV(p: HasPosition) = p.asInstanceOf[V]

  private def is[T1 <: HasPosition, T2 <: HasPosition, T3 <: HasPosition](v: (HasPosition, HasPosition, HasPosition))(implicit man1: Manifest[T1], man2: Manifest[T2], man3: Manifest[T3]) =
    man1.runtimeClass.isAssignableFrom(v._1.getClass) &&
      man2.runtimeClass.isAssignableFrom(v._2.getClass) &&
      man3.runtimeClass.isAssignableFrom(v._3.getClass)

  private def obstacleToHasValue(o: Obstacle, point: HasValue): HasValue =
    return Point(point.value.modifyVelocity(Vector.zero).modifyPosition(o.position))

  private def getGradient(f: HasValue => Double,
    p1: HasValue, p2: HasValue, p3: HasValue,
    direction: Direction,
    derivativeType: Derivative): Double = {
    derivativeType match {
      case FirstDerivative =>
        (f(p3) - f(p1)) / (p3.position.get(direction) - p1.position.get(direction))
      case SecondDerivative =>
        (f(p3) + f(p1) - 2 * f(p2)) / sqr(p3.position.get(direction) - p1.position.get(direction))
      case _ => unexpected
    }
  }

  private def sqr(d: Double) = d * d

  private def getGradient(f: HasValue => Double,
    p1: HasValue, p2: HasValue,
    direction: Direction,
    derivativeType: Derivative): Double = {
    derivativeType match {
      case FirstDerivative =>
        (f(p2) - f(p1)) / (p2.position.get(direction) - p1.position.get(direction))
      case SecondDerivative =>
        0
      case _ => unexpected
    }
  }

}

/**
 * Implements gradient calculation for a regular grid. Every positionA,
 * on the grid has nominated neighbours to be used in gradient
 * calculations (both first and second derivatives).
 */
class RegularGridSolver(positions: Set[HasPosition], validate: Boolean) extends Solver {
  import Solver._
  import RegularGrid._
  import scala.math._
  import RegularGridSolver._

  private final val grid = new RegularGrid(positions)

  if (validate)
    info("validated")

  def this(positions: Set[HasPosition]) =
    this(positions, true);

  override def getPositions = grid.positions

  override def getGradient(position: HasPosition, direction: Direction,
    f: ValueFunction,
    derivativeType: Derivative, overrideValue: Option[HasValue]): Double =
    return RegularGridSolver.getGradient(grid, position, direction,
      f, derivativeType, overrideValue);

  override def step(timestep: Double): Solver = {
    info("creating parallel collection")
    val collection = grid.positions //.par
    info("solving timestep")
    val stepped = collection.map(getValueAfterTime(_, timestep))
    info("converting to sequential collection")
    val seq = stepped.seq

    info("creating new Solver")
    return new RegularGridSolver(
      seq, false)
  }
}

/////////////////////////////////////////////////////////////////////
// Newtons Method                                                                        
/////////////////////////////////////////////////////////////////////

/**
 * Newton's Method solver for one dimensional equations in
 *  the real numbers.
 *
 */
object NewtonsMethod {
  import scala.math._
  import scala.annotation._

  /**
   * Uses Newton's Method to solve f(x) = 0 for x. Returns `None`
   * if no solution found within maxIterations. This method uses
   * tail recursion optimisation so a large number of maxIterations
   * will not cause a stack overflow.
   *
   * @param f function to find roots of (where f(x)=0)
   * @param x initial guess at the solution.
   * @param h the delta for calculation of derivative
   * @param precision the desired maximum absolute value of f(x) at an
   *        acceptable solution
   * @param maxIterations the maximum number of iterations to perform.
   *        If maxIterations is reached then returns `None`
   * @return optional solution
   */
  @tailrec
  def solve(f: Double => Double, x: Double, h: Double,
    precision: Double, maxIterations: Long): Option[Double] = {
    val fx = f(x)
    if (abs(fx) <= precision) Some(x)
    else if (maxIterations == 0) None
    else {
      val gradient = (f(x + h) - fx) / h
      if (gradient == 0) None
      else solve(f, x - fx / gradient, h, precision, maxIterations - 1)
    }
  }
}