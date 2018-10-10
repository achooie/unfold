/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

/**
 *
 * @author akitaya
 */
public class ConstraintTriangle {

    public ConstraintGeodesic[] geodesics;
    public double[] dihedral;
    public ConstraintTriangle next;

    public ConstraintTriangle(ConstraintGeodesic g1, ConstraintGeodesic g2,
            ConstraintGeodesic g3) {
        geodesics = new ConstraintGeodesic[3];
        geodesics[0] = g1;
        geodesics[1] = g2;
        geodesics[2] = g3;
        dihedral = new double[3];

    }

    public void setNext(ConstraintTriangle next) {
        this.next = next;
    }

    void cosineRule() {
        geodesics[2].length = Math.acos(Math.cos(geodesics[0].length) * Math.cos(geodesics[1].length)
                + Math.sin(geodesics[0].length) * Math.sin(geodesics[1].length) * Math.cos(dihedral[0]));
    }

    void solveDihedral() {
        if (Math.abs(geodesics[0].length) < 1E-2) {
            dihedral[0] = Math.PI / 2;
            dihedral[2] = Math.PI / 2;
        } else if (Math.abs(geodesics[2].length) < 1E-2) {
            dihedral[1] = Math.PI / 2;
            dihedral[2] = Math.PI / 2;
        } else {
            dihedral[0] = getAngle(geodesics[2], geodesics[1], geodesics[0]);
            dihedral[1] = getAngle(geodesics[0], geodesics[1], geodesics[2]);
            dihedral[2] = getAngle(geodesics[1], geodesics[0], geodesics[2]);
        }
    }

    void solveRemainingDihedral() {
        if (Math.abs(geodesics[2].length) < 1E-2) {
            dihedral[1] = Math.PI / 2;
            dihedral[2] = Math.PI / 2;
        } else {
            dihedral[1] = getAngle(geodesics[0], geodesics[1], geodesics[2]);
            dihedral[2] = getAngle(geodesics[1], geodesics[2], geodesics[0]);;
        }
    }

    double getAngle(ConstraintGeodesic opposite, ConstraintGeodesic g1, ConstraintGeodesic g2) {
        double aux = (Math.cos(opposite.length)
                - Math.cos(g1.length) * Math.cos(g2.length))
                / (Math.sin(g1.length) * Math.sin(g2.length));
        if(Math.abs(aux)>1){
            aux = Math.signum(aux);
        }
        return Math.acos(aux);
    }

    @Override
    public String toString() {
        return super.toString() + " " + geodesics[0] + " " + geodesics[1] + " " + geodesics[2]
                + " dihedral: " + dihedral[0] * 180 / Math.PI + " " + dihedral[1] * 180 / Math.PI + " " + dihedral[2] * 180 / Math.PI;
    }
}
