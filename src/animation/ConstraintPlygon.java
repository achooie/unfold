/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

import java.util.ArrayList;

/**
 *
 * @author akitaya
 */
public class ConstraintPlygon {

    public ArrayList<ConstraintGeodesic> geodesics;
    public ArrayList<Double> desiredDehedral;
    public ArrayList<Double> dihedral;
    public ArrayList<ConstraintTriangle> triangles;

    public ConstraintPlygon(ArrayList<ConstraintGeodesic> geodesics,
            ArrayList<Double> desiredDehedral, ArrayList<Double> dihedral) {
        this.geodesics = geodesics;
        this.desiredDehedral = desiredDehedral;
        this.dihedral = dihedral;
    }
}
