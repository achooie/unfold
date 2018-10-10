/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package origamid;

import Simplification.CP;
import diagram.FaceArea;
import diagram.FaceRegion;
import java.awt.Component;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import oripa.Doc;
import oripa.Folder;
import oripa.ORIPA;
import oripa.geom.*;
import translations.FromToOripa;

/**
 *
 * @author akitaya
 */
public class GeomUtilD extends GeomUtil {

    public static boolean CCWcheck(Vector2d p0, Vector2d p1, Vector2d q) {
        double dx1, dx2, dy1, dy2;

        dx1 = p1.x - p0.x;
        dy1 = p1.y - p0.y;
        dx2 = q.x - p0.x;
        dy2 = q.y - p0.y;

        if (dx1 * dy2 - dy1 * dx2 >= -1e-3) {
            return true;
        }
        return false;
    }

    public static boolean isOnFace(OriVertex v, OriFace face) {

        int heNum = face.halfedges.size();

        OriHalfedge baseHe = face.halfedges.get(0);
        boolean baseFlg = CCWcheck(baseHe.vertex.preP, baseHe.next.vertex.preP, v.preP);
        for (int i = 1; i < heNum; i++) {
            OriHalfedge he = face.halfedges.get(i);
            if (CCWcheck(he.vertex.preP, he.next.vertex.preP, v.preP) != baseFlg) {
                return false;
            }
        }

        return true;
    }

    public static boolean isOnFace(Vector2d v, OriFace face) {

        int heNum = face.halfedges.size();

        OriHalfedge baseHe = face.halfedges.get(0);
        boolean baseFlg = CCWcheck(baseHe.vertex.preP, baseHe.next.vertex.preP, v);
        for (int i = 1; i < heNum; i++) {
            OriHalfedge he = face.halfedges.get(i);
            if (CCWcheck(he.vertex.preP, he.next.vertex.preP, v) != baseFlg) {
                return false;
            }
        }

        return true;
    }

    public static boolean isContainPointFace(OriFace face, Vector2d v, double eps) {

        int heNum = face.halfedges.size();

        // If its on the faces edge, return false
        for (int i = 0; i < heNum; i++) {
            OriHalfedge he = face.halfedges.get(i);
            if (oripa.geom.GeomUtil.DistancePointToSegment(v, he.vertex.preP, he.next.vertex.preP) < eps) {
                return false;
            }
        }

        OriHalfedge baseHe = face.halfedges.get(0);
        boolean baseFlg = oripa.geom.GeomUtil.CCWcheck(baseHe.vertex.preP, baseHe.next.vertex.preP, v);

        for (int i = 1; i < heNum; i++) {
            OriHalfedge he = face.halfedges.get(i);
            if (oripa.geom.GeomUtil.CCWcheck(he.vertex.preP, he.next.vertex.preP, v) != baseFlg) {
                return false;
            }
        }

        return true;
    }

    public static boolean isLineSegmentContained(Vector2d s0, Vector2d e0, Vector2d s1, Vector2d e1) {
        // Whether or not is parallel
        Vector2d dir0 = new Vector2d(e0);
        dir0.sub(s0);
        Vector2d dir1 = new Vector2d(e1);
        dir1.sub(s1);

        if (!GeomUtil.isParallel(dir0, dir1)) {
            return false;
        }

        int cnt = 0;
        if (GeomUtil.DistancePointToSegment(s0, s1, e1) < EPS) {
            cnt++;
        }
        if (GeomUtil.DistancePointToSegment(e0, s1, e1) < EPS) {
            cnt++;
        }

        if (cnt >= 2) {
            return true;
        }
        return false;

    }

    public static Vector2d getMidPoint(Vector2d p1, Vector2d p2) {
        return new Vector2d((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    public static OriFace getFaceContainingPoint(Vector2d p, Doc doc) {
        for (OriFace oriFace : doc.faces) {
            if (origamid.GeomUtilD.isOnFace(p, oriFace)) {
                return oriFace;
            }
        }
        return null;
    }

    public static Vector2d getPositionFolded(OriVertex oriVertex, Doc doc) {
        for (OriFace oriFace : doc.faces) {
            if (origamid.GeomUtilD.isOnFace(oriVertex, oriFace)) {
                Vector2d originBefore = oriFace.halfedges.get(0).next.vertex.preP;
                Vector2d vecBefore1 = new Vector2d();
                vecBefore1.sub(oriFace.halfedges.get(0).vertex.preP, originBefore);
                Vector2d vecBefore2 = new Vector2d();
                vecBefore2.sub(oriFace.halfedges.get(0).next.next.vertex.preP, originBefore);
                Vector2d originAfter = oriFace.halfedges.get(0).next.vertex.p;
                Vector2d vecAfter1 = new Vector2d();
                vecAfter1.sub(oriFace.halfedges.get(0).vertex.p, originAfter);
                Vector2d vecAfter2 = new Vector2d();
                vecAfter2.sub(oriFace.halfedges.get(0).next.next.vertex.p, originAfter);
                Vector2d pointBefore = new Vector2d();
                pointBefore.sub(oriVertex.preP, originBefore);
                double a = 0, b = 0;
                if ((vecBefore1.x * vecBefore2.y - vecBefore2.x * vecBefore1.y) != 0) {
                    b = (pointBefore.y * vecBefore1.x - pointBefore.x * vecBefore1.y)
                            / (vecBefore1.x * vecBefore2.y - vecBefore2.x * vecBefore1.y);
                    a = (pointBefore.y * vecBefore2.x - pointBefore.x * vecBefore2.y)
                            / (vecBefore2.x * vecBefore1.y - vecBefore1.x * vecBefore2.y);
                }
                Vector2d pointAfter = new Vector2d();
                pointAfter.x = originAfter.x + a * vecAfter1.x + b * vecAfter2.x;
                pointAfter.y = originAfter.y + a * vecAfter1.y + b * vecAfter2.y;

                return pointAfter;
            }
        }
        return null;
    }

    public static ArrayList<OriFace> getSortedFaces(Doc doc) throws Exception {
        ArrayList<OriFace> sortedFaces = new ArrayList<>();
        boolean[] isSorted = new boolean[doc.faces.size()];
        for (int i = 0; i < doc.faces.size(); i++) {
            for (int j = 0; j < doc.overlapRelation.length; j++) {
                int numberOf2 = 0;
                if (!isSorted[j]) {
                    for (int k = 0; k < isSorted.length; k++) {
                        if ((!isSorted[k]) && doc.overlapRelation[j][k] == 2) {
                            numberOf2++;
                        }
                    }
                    if (numberOf2 == 0) {
                        OriFace face = doc.faces.get(j);
                        isSorted[j] = true;
                        for (int k = sortedFaces.size() - 1; k >= 0; k--) {
                            OriFace oriFace = sortedFaces.get(k);
                            if (isFaceOverlap(oriFace, face, EPS)) {
                                face.z_order = oriFace.z_order + 1;
                                break;
                            }
                        }
                        sortedFaces.add(face);
//                        System.out.println(j + " z ind.." + face.z_order + " face outline " + face.outline);
                        for (OriHalfedge he : face.halfedges) {
                            he.vertex.tmpInt++;
                            he.vertex.tmpVec.add(new Point2d(face.z_order * 5, face.z_order * 3));
//                            System.out.println("     tempint.." + he.vertex.tmpInt + " " + he.vertex.tmpVec);
                        }
                        break;
                    }
                }
            }
//            if(sortedFaces.size()<i+1){
//                System.out.println("safdddddddd");
//                System.exit(i);
//            }
        }
        if (doc.faces.size() != sortedFaces.size()) {
//            ExporterORmat.export(doc, "ormat");
//            
//            System.out.println("size "+doc.faces.size() +" " + sortedFaces.size() +" " +doc.overlapRelation.length);
//            System.exit(i);
//            throw new Exception("ORIPA couldnt find the folded form");
        }
        return sortedFaces;
    }

    public static Doc foldCP(CP cp) throws Exception {

        ORIPA.doc = FromToOripa.getDoc(cp);
        ORIPA.doc.sortedFaces.clear();
        ORIPA.doc.buildOrigami3(false);
        (new Folder(ORIPA.doc)).fold();
//        ORIPA.doc.sortedFaces = getSortedFaces(ORIPA.doc);
        return ORIPA.doc;
    }

    public static boolean isSameSegment(Vector2d p0, Vector2d p1, Vector2d p2, Vector2d p3) {
        if (Math.abs(p0.x - p2.x) < EPS && Math.abs(p0.y - p2.y) < EPS) {
            return Math.abs(p1.x - p3.x) < EPS && Math.abs(p1.y - p3.y) < EPS;
        }
        if (Math.abs(p0.x - p3.x) < EPS && Math.abs(p0.y - p3.y) < EPS) {
            return Math.abs(p1.x - p2.x) < EPS && Math.abs(p1.y - p2.y) < EPS;
        }
        return false;
    }

    public static boolean isSamePoint(Vector2d p0, Vector2d p1) {
        return Math.abs(p1.x - p0.x) < EPS && Math.abs(p1.y - p0.y) < EPS;
    }

    public static OriEdge subtract(OriEdge localEdge, OriEdge oriEdge) {
        if (isSamePoint(localEdge.sv.preP, oriEdge.sv.preP)) {
            return new OriEdge(localEdge.ev, oriEdge.ev, localEdge.type);
        }
        if (isSamePoint(localEdge.sv.preP, oriEdge.ev.preP)) {
            return new OriEdge(localEdge.ev, oriEdge.sv, localEdge.type);
        }
        if (isSamePoint(localEdge.ev.preP, oriEdge.sv.preP)) {
            return new OriEdge(localEdge.sv, oriEdge.ev, localEdge.type);
        }
        if (isSamePoint(localEdge.ev.preP, oriEdge.ev.preP)) {
            return new OriEdge(localEdge.sv, oriEdge.sv, localEdge.type);
        }
        return null;
    }

//    public static ArrayList<OriEdge> subtract(Doc doc1, Doc doc2) {
//        ArrayList<OriEdge> subtraction = new ArrayList<>();
//        subtraction.addAll(doc1.edges);
//        for (OriEdge oriEdge1 : subtraction) {
//            if (oriEdge1.type == Crease.AUX || oriEdge1.type == Crease.PAPER_EDGE) {
//                subtraction.remove(oriEdge1);
//                continue;
//            }
//        }
//        for (OriEdge oriEdge2 : doc2.edges) {
//            if (oriEdge2.type == Crease.AUX || oriEdge2.type == Crease.PAPER_EDGE) {
//                continue;
//            }
//            for (int i = 0; i < subtraction.size(); i++) {
//                OriEdge oriEdge1 = subtraction.get(i);
//                if (isSameSegment(oriEdge1.sv.preP, oriEdge1.ev.preP,
//                        oriEdge2.sv.preP, oriEdge2.ev.preP)) {
//                    if (oriEdge1.type == oriEdge2.type) {
//                        subtraction.remove(oriEdge1);
//                    }
//                    break;
//                }
//                if (isLineSegmentsOverlap(oriEdge1.sv.preP, oriEdge1.ev.preP,
//                        oriEdge2.sv.preP, oriEdge2.ev.preP)) {
//                    subtraction.remove(oriEdge1);
//                    Vector2d[] p = subtractSegments(oriEdge1.sv.preP, oriEdge1.ev.preP,
//                            oriEdge2.sv.preP, oriEdge2.ev.preP);
//                    if (p[0] != null) {
//                        subtraction.add(new OriEdge(new OriVertex(p[0]), new OriVertex(p[1]), oriEdge1.type));
//                    }
//                    if (p[2] != null) {
//                        subtraction.add(new OriEdge(new OriVertex(p[2]), new OriVertex(p[3]), oriEdge1.type));
//                    }
//                    break;
//                }
//            }
//        }
//        return subtraction;
//    }
//
//    private static Vector2d[] subtractSegments(Vector2d s0, Vector2d e0, Vector2d s1, Vector2d e1) {
//
//        int cnt = 0;
//        if (DistancePointToSegment(s0, s1, e1) < EPS) {
//            cnt++;
//        }
//        if (DistancePointToSegment(e0, s1, e1) < EPS) {
//            cnt++;
//        }
//        if (DistancePointToSegment(s1, s0, e0) < EPS) {
//            cnt++;
//        }
//        if (DistancePointToSegment(e1, s0, e0) < EPS) {
//            cnt++;
//        }
//
//        if (cnt >= 2) {
//            return true;
//        }
//        return false;
//
//    }
    public static boolean isSameFace(OriFace oriFace1, OriFace oriFace2) {
        if (oriFace1.halfedges.size() != oriFace2.halfedges.size()) {
            return false;
        }
        for (OriHalfedge he1 : oriFace1.halfedges) {
            boolean hasPair = false;
            for (OriHalfedge he2 : oriFace2.halfedges) {
                if (isSamePoint(he1.vertex.preP, he2.vertex.preP)) {
                    hasPair = true;
                    break;
                }
            }
            if (!hasPair) {
                return false;
            }
        }
        return true;
    }

    public static Component[] reverse(Component[] array) {
        Component[] rev = new Component[array.length];
        for (int i = 0; i < array.length; i++) {
            rev[array.length - i - 1] = array[i];
        }
        return rev;
    }

    public static boolean isBetween(int z_order, int face1, int face2) {
        return (z_order > face1 && z_order < face2) || (z_order < face1 && z_order > face2);
    }

    public static OriVertex getOriVertexAt(Vector2d p, OriFace face) {
        for (OriHalfedge he : face.halfedges) {
            if (DistanceSquared(p, he.vertex.preP) < EPS) {
                return he.vertex;
            }
            if (isOnSegment(p, he.vertex.preP, he.next.vertex.preP)) {
                double d1 = GeomUtilD.Distance(he.vertex.preP, he.next.vertex.preP);
                double d2 = GeomUtilD.Distance(he.vertex.preP, p);
                //new heigth should be a poundering mean of heights
                double newHeight = (d2 * he.next.vertex.tmpVec.x + (d1 - d2) * he.vertex.tmpVec.x) / d1;
                //so should be the folded position 
                Vector2d newPosition = new Vector2d(
                        (d2 * he.next.vertex.p.x + (d1 - d2) * he.vertex.p.x) / d1,
                        (d2 * he.next.vertex.p.y + (d1 - d2) * he.vertex.p.y) / d1);
                OriVertex newV = new OriVertex(p);
                newV.tmpVec.x = newHeight;
                newV.p = newPosition;
//                System.out.println("isonSegment!!! " + p + " "+newV.tmpVec.x+" " + he.vertex.preP + " "+he.vertex.tmpVec.x + " " + he.next.vertex.preP + " "+he.next.vertex.tmpVec.x);
                return newV;
            }
        }
        OriVertex newV = new OriVertex(p);
        newV.tmpVec.x = 0;
        newV.p = getPositionFolded(p, face);
        return newV;
    }

    private static boolean isOnSegment(double px, double py, double s1x, double s1y, double s2x, double s2y) {
        if (isBetween((int) px, (int) s1x, (int) s2x)
                || isBetween((int) py, (int) s1y, (int) s2y)) {
            double d1 = s2x - s1x;
            if (Math.abs(d1) < EPS) {
                return Math.abs(px - s1x) < EPS;
            }
            double d2 = s2y - s1y;
            if (Math.abs(d2) < EPS) {
                return Math.abs(py - s1y) < EPS;
            }
            return Math.abs((px - s1x) / d1 - (py - s1y) / d2) < EPS;
        }
        return false;
    }

    public static boolean isOnSegment(Vector2d p, Vector2d s1, Vector2d s2) {
        return isOnSegment(p.x, p.y, s1.x, s1.y, s2.x, s2.y);
    }

    public static boolean isOnSegment(Point2D p, Point2D s1, Point2D s2) {
        return isOnSegment(p.getX(), p.getY(), s1.getX(), s1.getY(), s2.getX(), s2.getY());
    }

    public static FaceRegion getFaceRegionContainingPoint(Vector2d p, ArrayList<ArrayList<FaceRegion>> faceLayers) {
//        System.out.println("debug getFaceRegionContainingPoint "+p);
        for (ArrayList<FaceRegion> layer : faceLayers) {
            for (FaceRegion faceRegion : layer) {
                if (origamid.GeomUtilD.isOnFace(p, faceRegion.face)) {
                    return faceRegion;
                }
            }
        }
        return null;
    }

    public static Vector2d getPositionFolded(Vector2d p, OriFace oriFace) {
        Vector2d originBefore = oriFace.halfedges.get(0).next.vertex.preP;
        Vector2d vecBefore1 = new Vector2d();
        vecBefore1.sub(oriFace.halfedges.get(0).vertex.preP, originBefore);
        Vector2d vecBefore2 = new Vector2d();
        vecBefore2.sub(oriFace.halfedges.get(0).next.next.vertex.preP, originBefore);
        Vector2d originAfter = oriFace.halfedges.get(0).next.vertex.p;
        Vector2d vecAfter1 = new Vector2d();
        vecAfter1.sub(oriFace.halfedges.get(0).vertex.p, originAfter);
        Vector2d vecAfter2 = new Vector2d();
        vecAfter2.sub(oriFace.halfedges.get(0).next.next.vertex.p, originAfter);
        Vector2d pointBefore = new Vector2d();
        pointBefore.sub(p, originBefore);
        double a = 0, b = 0;
        if ((vecBefore1.x * vecBefore2.y - vecBefore2.x * vecBefore1.y) != 0) {
            b = (pointBefore.y * vecBefore1.x - pointBefore.x * vecBefore1.y)
                    / (vecBefore1.x * vecBefore2.y - vecBefore2.x * vecBefore1.y);
            a = (pointBefore.y * vecBefore2.x - pointBefore.x * vecBefore2.y)
                    / (vecBefore2.x * vecBefore1.y - vecBefore1.x * vecBefore2.y);
        }
        Vector2d pointAfter = new Vector2d();
        pointAfter.x = originAfter.x + a * vecAfter1.x + b * vecAfter2.x;
        pointAfter.y = originAfter.y + a * vecAfter1.y + b * vecAfter2.y;

        return pointAfter;
    }

    public static FaceArea getFaceAreaContainingPoint(Vector2d p, ArrayList<ArrayList<FaceArea>> faceLayers) {
//        System.out.println("debug getFaceRegionContainingPoint "+p);
        for (ArrayList<FaceArea> layer : faceLayers) {
            for (FaceArea faceArea : layer) {
                if (origamid.GeomUtilD.isOnFace(p, faceArea.oriFace)) {
                    return faceArea;
                }
            }
        }
        return null;
    }
}
