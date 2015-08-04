package io.lp0onfire.ssi.model;

/**
 * An immutable representation of a point in 3-space.
 * Vector operations assume a right-hand-rule coordinate system.
 */
public class Vector {

  private final int x;
  private final int y;
  private final int z;
  
  public int getX() {
    return x;
  }
  public int getY() {
    return y;
  }
  public int getZ() {
    return z;
  }
  
  public Vector(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  @Override
  public boolean equals(Object aThat) {
    if (aThat == null) return false;
    if (this == aThat) return true;
    if (!(aThat instanceof Vector)) return false;
    Vector that = (Vector)aThat;
    return this.getX() == that.getX() && this.getY() == that.getY() && this.getZ() == that.getZ();
  }
  
  @Override
  public int hashCode() {
    return 11 * getX() + 31 * getY() + getZ();
  }
  
  @Override
  public String toString() {
    return "(" + x + ", " + y + ", " + z + ")";
  }
  
  public Vector add(Vector that) {
    return new Vector(this.getX() + that.getX(), this.getY() + that.getY(), this.getZ() + that.getZ());
  }
  
  public Vector subtract(Vector that) {
    return this.add(that.negate());
  }
  
  public Vector negate() {
    return new Vector(-this.getX(), -this.getY(), -this.getZ());
  }
  
}
