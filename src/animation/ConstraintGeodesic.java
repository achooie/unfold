/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

/**
 *
 * @author akitaya
 */
public class ConstraintGeodesic {

    double length;

    ConstraintGeodesic(double angle) {
        length = angle;
    }

    @Override
    public String toString() {
        return super.toString() + " " + length*180/Math.PI;
    }
}
