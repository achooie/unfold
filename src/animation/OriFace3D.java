/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

import java.awt.Color;
import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import oripa.geom.OriFace;

/**
 *
 * @author Hugo
 */
public class OriFace3D {

    OriFace faceBefore;
    OriFace faceAfter;
    OriFace faceSimulation;
    BranchGroup branchGroup;
    TransformGroup objTrans;
    Transform3D initialTransf;
    public Transform3D transf;
    static Appearance mainAppearanceFront;
    static Appearance mainAppearanceBack;
    public OriHalfedge3D[] halfEdges;
    static Color3f faceColorFront = new Color3f(0.86f, 0.86f, 0.86f);
    static Color3f faceColorBack = new Color3f(0.46f, 0.53f, 1.0f);
    Shape3D shapeFront;
    Shape3D shapeBack;
    Shape3D[] outline;
    boolean isFixed;
    boolean updated;
    int zdiff;
    double zPosition;

    private OriFace3D() {
        objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        branchGroup = new BranchGroup();
        branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        transf = new Transform3D();
    }

    static void init() {
        PolygonAttributes pa = new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
                PolygonAttributes.CULL_BACK, // Types of culling
                0.1f); // Polygon offset 
        mainAppearanceFront = new Appearance();
        mainAppearanceFront.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        mainAppearanceFront.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        mainAppearanceFront.setPolygonAttributes(pa);
        mainAppearanceBack = new Appearance();
        mainAppearanceBack.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        mainAppearanceBack.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        mainAppearanceBack.setPolygonAttributes(pa);
        Material mat1 = new Material();
        mat1.setAmbientColor(faceColorBack);
	mat1.setDiffuseColor(new Color3f(0f,0f,0f));
	mat1.setSpecularColor(new Color3f(0.1f,0.1f,0.1f));
        mat1.setShininess(1f);
        mainAppearanceBack.setMaterial(mat1);
        Material mat2 = new Material();
        mat2.setAmbientColor(faceColorFront);
	mat2.setDiffuseColor(new Color3f(0f,0f,0f));
	mat2.setSpecularColor(new Color3f(0.1f,0.1f,0.1f));
        mat2.setShininess(1f);
        mainAppearanceFront.setMaterial(mat2);

        ColoringAttributes ca = new ColoringAttributes();
        ca.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
        mainAppearanceFront.setColoringAttributes(ca);
        mainAppearanceBack.setColoringAttributes(ca);
    }

//    public static OriFace3D getUnfoldedSquare() {
//        OriFace3D face3D = new OriFace3D();
//        face3D.halfEdges = new OriHalfedge3D[4];
//        PolygonAttributes pa = new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
//                PolygonAttributes.CULL_BACK, // Types of culling
//                0.1f); // Polygon offset 
//        mainAppearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
//        mainAppearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
//        mainAppearance.setPolygonAttributes(pa);
//        face3D.setPoints(new Point3d[]{new Point3d(2, 2, 0), new Point3d(-2, 2, 0),
//                    new Point3d(-2, -2, 0), new Point3d(2, -2, 0)});
//        return face3D;
//    }
    public OriFace3D(Transform3D initialTransform, OriFace oriFace) {
        objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        initialTransf = initialTransform;
        transf = new Transform3D(initialTransform);
        this.faceSimulation = oriFace;
        branchGroup = new BranchGroup();
        branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
    }

    public void updatePoint(Point3d p, Point3d old) {

        for (int i = 0; i < halfEdges.length; i++) {
            if (halfEdges[i].v.distanceSquared(old) == 0) {
                GeometryArray geom = (GeometryArray) outline[i].getGeometry();
                Point3d aux = new Point3d();
                geom.getCoordinate(0, aux);
//                System.out.println("update point1 " + old.x + ", " + old.y + " " + aux.x + ", " + aux.y);
                geom.setCoordinate(0, p);
                geom = (GeometryArray) outline[(i + outline.length - 1) % outline.length].getGeometry();
                geom.getCoordinate(1, aux);
//                System.out.println("update point2 " + old.x + ", " + old.y + " " + aux.x + ", " + aux.y);
                geom.setCoordinate(1, p);

                geom = (GeometryArray) shapeFront.getGeometry();
                GeometryArray geom2 = (GeometryArray) shapeBack.getGeometry();
                if (i == 0) {
                    for (int j = 0; j < halfEdges.length - 2; j++) {
                        geom.setCoordinate(j * 3, p);
                        geom2.setCoordinate(j * 3 + 2, p);
                    }
                } else {
                    int j = i * 3 - 2;
                    if (j < (halfEdges.length - 2) * 3) {
                        geom.setCoordinate(j, p);
                        geom2.setCoordinate(j, p);
                    }
                    j += -2;
                    if (j > 0) {
                        geom.setCoordinate(j, p);
                        geom2.setCoordinate(j - 2, p);
                    }
                }




//                for (int j = 0; j < (halfEdges.length - 2) * 3; j++) {
//                    int rest = j % 3;
//                    int quo = j / 3;
//                    switch (rest) {
//                        case 0:
//                            if (i == 0) {
//                                geom.setCoordinate(j, p);
//                                geom2.setCoordinate(j + 2, p);
//                            }
//                            break;
//                        case 1:
//                            if (quo == 1 + i) {
//                            }
//                            break;
//                        case 2:
//                            break;
//                    }
//                }

                return;
            }
        }





//
//
////        double minDist = 1000;
////        ArrayList<Shape3D> shapes = new ArrayList<>();
////        ArrayList<ArrayList<Double>> distsArray = new ArrayList<>();
//
//
//
//
//        for (int i = 0; i < objTrans.numChildren(); i++) {
//            Node n = objTrans.getChild(i);
//            if (n.getClass().equals(Shape3D.class)) {
//                Shape3D shape = (Shape3D) n;
//                GeometryArray geom = (GeometryArray) shape.getGeometry();
////                ArrayList<Double> dists = new ArrayList<>();
//                for (int j = 0; j < geom.getVertexCount(); j++) {
//                    Point3d pj = new Point3d();
//                    geom.getCoordinate(j, pj);
//                    double dist = pj.distanceSquared(old);
////                    dists.add(dist);
////                    if (minDist > dist) {
////                        minDist = dist;
////                    }
//                    if (dist == 0) {
//                        geom.setCoordinate(j, p);
////                        shape.setGeometry(geom);
//                        break;
//                    }
//                }
////                distsArray.add(dists);
////                shapes.add(shape);
////                if (minDist < 1E-3) {
////                    geom.setCoordinate(nearestIndex, p);
////                    shape.setGeometry(geom);
////                    System.out.println(this + " " + p + " " + nearest + " " + minDist);
////                }
//            }
//        }
////        for (int i = 0; i < shapes.size(); i++) {
////            Shape3D shape = shapes.get(i);
////            for (int j = 0; j < distsArray.get(i).size(); j++) {
////                double dist = distsArray.get(i).get(j);
////                if (dist == minDist) {
////                    GeometryArray geom = (GeometryArray) shape.getGeometry();
////                    geom.setCoordinate(j, p);
////                    shape.setGeometry(geom);
//////                    System.out.println(this + " " + p + " " + minDist);
////                }
////            }
////        }
    }

    public void build() {


        Point3d[] vrtx = new Point3d[halfEdges.length];
        outline = new Shape3D[vrtx.length];
        for (int i = 0; i < vrtx.length; i++) {
//            System.out.println(this + " " + halfEdges[i].v.x + ", " + halfEdges[i].v.y + ", " + halfEdges[i].edge.changes + ", " + halfEdges[i].edge.desiredAngle + ", " + halfEdges[i].edge.foldingAngle + ", " + this.isFixed);
            vrtx[i] = halfEdges[i].v;
            // Outline
            LineArray lineGeometry = new LineArray(2, GeometryArray.COORDINATES
                    | GeometryArray.COLOR_3);
            lineGeometry.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
            lineGeometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
            lineGeometry.setCoordinate(0, vrtx[i]);
            lineGeometry.setCoordinate(1, halfEdges[(i + 1) % vrtx.length].v);
            Appearance lineAppearance = new Appearance();
            lineAppearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
            LineAttributes lineAttributes =
                    new LineAttributes(3.0f, // Line thickness
                    LineAttributes.PATTERN_SOLID, // Line type
                    true); // Whether to handle anti-aliasing
            lineAppearance.setLineAttributes(lineAttributes);
            lineAppearance.setRenderingAttributes(new RenderingAttributes(true, true, 0.1f, RenderingAttributes.ALWAYS));
            lineGeometry.setColor(0, new Color3f(Color.BLACK));
            lineGeometry.setColor(1, new Color3f(Color.BLACK));
            outline[i] = new Shape3D(lineGeometry, lineAppearance);
            outline[i].setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
            objTrans.addChild(outline[i]);
        }
//        System.out.println(vrtx);
        setPoints(vrtx);
    }

    public void setPoints(Point3d[] vrtx) {
        Point3d[] p = new Point3d[3];
        p[0] = new Point3d(vrtx[0]);
        TriangleArray faceGeometryFront = new TriangleArray(3 * (vrtx.length - 2),
                GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR_3);
        TriangleArray faceGeometryBack = new TriangleArray(3 * (vrtx.length - 2),
                GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR_3);
        faceGeometryBack.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        faceGeometryBack.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
        faceGeometryFront.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        faceGeometryFront.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
        for (int i = 1; i < vrtx.length - 1; i++) {

            p[1] = new Point3d(vrtx[i]);
            p[2] = new Point3d(vrtx[i + 1]);


            for (int j = 0; j < 3; j++) {
                int k = (i - 1) * 3 + j;
                faceGeometryFront.setCoordinate(k, p[j]);
                faceGeometryBack.setCoordinate(k, p[2 - j]);
                Vector3f polygonNormal = new Vector3f(0f, 0f, 1f);
                Vector3f polygonNormalBack = new Vector3f(0f, 0f, -1f);
                faceGeometryFront.setNormal(k, polygonNormal);
//                faceGeometryFront.setColor(k, faceColorFront);
                faceGeometryBack.setNormal(k, polygonNormalBack);
//                faceGeometryBack.setColor(k, faceColorBack);
            }
        }
        faceGeometryFront.setCapability(Geometry.ALLOW_INTERSECT);
        faceGeometryBack.setCapability(Geometry.ALLOW_INTERSECT);
        faceGeometryFront.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
        faceGeometryBack.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
        faceGeometryFront.setCapability(GeometryArray.ALLOW_COUNT_READ);
        faceGeometryBack.setCapability(GeometryArray.ALLOW_COUNT_READ);

        shapeFront = new Shape3D(faceGeometryFront, mainAppearanceFront);
        shapeBack = new Shape3D(faceGeometryBack, mainAppearanceBack);
        shapeFront.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        shapeBack.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        shapeFront.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        shapeBack.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        objTrans.addChild(shapeFront);
        objTrans.addChild(shapeBack);
        branchGroup.addChild(objTrans);
    }

    void updateTransform() {
//        Transform3D transf = new Transform3D();
//        objTrans.getTransform(transf);

        zPosition += zdiff / 64.0;
        if (Math.abs(zPosition - faceAfter.z_order) < .1) {
            zPosition = faceAfter.z_order;
        }
        Transform3D transl = new Transform3D();
//        Point3d p1 = new Point3d(0, 0, 0);
        transl.setTranslation(new Vector3d(0, 0, zPosition * .006));
        transl.mul(transf);
//        transf.transform(p1);
        objTrans.setTransform(transl);
//        Point3d p = new Point3d(0, 0, 0);
//        transl.transform(p);
//        System.out.println("z update " + this + " " + zPosition + " " + (faceBefore != null ? faceBefore.z_order : "") + " " + zdiff + " " + faceAfter.z_order + " " + p.z + " " + isFixed + " " + p1.z);
    }
}
