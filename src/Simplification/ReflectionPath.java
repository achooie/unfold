/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simplification;

import java.util.ArrayList;

/**
 *
 * @author akitaya
 */
public class ReflectionPath {

    public ArrayList<Crease> creases;
    public Node[] ends;

    public ReflectionPath() {
        creases = new ArrayList<>();
    }

    public void add(Crease c) {
        c.reflPath = this;
        creases.add(c);
    }

    public void add(int i, Crease reflected) {
        reflected.reflPath = this;
        creases.add(i, reflected);
    }

    public void calculateEnds() {
        if (creases.size() < 2) {
            ends = creases.get(0).nodes;
            return;
        }
        ends = new Node[2];
        if (creases.get(1).hasNode(creases.get(0).nodes[0])) {
            ends[0] = creases.get(0).nodes[1];
        } else {
            ends[0] = creases.get(0).nodes[0];
        }
        if (creases.get(creases.size() - 2).hasNode(creases.get(creases.size() - 1).nodes[0])) {
            ends[1] = creases.get(creases.size() - 1).nodes[1];
        } else {
            ends[1] = creases.get(creases.size() - 1).nodes[0];
        }

    }

    public boolean isComplete() {
//            System.out.println("reflection" + reflectionPaths.size());
        if (creases.size() < 2) {
            // check if the path is a single fold throught the paper
            Node[] n = creases.get(0).getNodes();
            if (n[0].isEdge() && n[1].isEdge()) {
                return true;
            }
            return false;
        }
        return (ends[0].isEdge() && ends[1].isEdge())
                || ends[0].equals(ends[1]);
    }

    @Override
    public String toString() {
        String s = super.toString() + " n0=" + ends[0] + " n1=" + ends[1];
        for (Crease crease : creases) {
            s += " " + crease.toString();
        }
        return s;
    }

    public boolean changeDirection(CP cp) {
//        if (creases.size() < 2) {
//            return false;
//        }
        Node center;
        int creaseIndex;
        int type;
        if (ends[0].isEdge() && !ends[1].isEdge()) {
            center = cp.getNodeAt(ends[1]);
            creaseIndex = center.creases.indexOf(creases.get(creases.size() - 1));
            type = creases.get(creases.size() - 1).type;
        } else if (ends[1].isEdge() && !ends[0].isEdge()) {
            center = cp.getNodeAt(ends[0]);
            creaseIndex = center.creases.indexOf(creases.get(0));
            type = creases.get(0).type;
        } else {
            return false;
        }

        int mvBalance = center.getMVBalance();
        if (!(mvBalance > 0 && type == Crease.VALLEY
                || mvBalance < 0 && type == Crease.MOUNTAIN)) {
            return false;
        }

        double[] angles = new double[center.creases.size()];
        for (int i = 0; i < center.creases.size(); i++) {
            angles[i] = center.creases.get((i + creaseIndex + 1) % center.creases.size()).angleBetween(
                    center.creases.get((i + creaseIndex) % center.creases.size()), center);
//            System.out.println(" Angle " + n.creases.get(i).getAngle(n) * 180 / Math.PI + " " + angles[i]);
        }
        double angle_ = angles[0];
        for (int i = 1; i < angles.length; i++) {
            angle_ += Math.pow(-1, i) * angles[i];
            if (angle_ < -Crease.TOLERANCE) {
                //calculate angle of the new crease
                double newAngle = center.creases.get((i + creaseIndex) % center.creases.size()).getAngle(center)
                        + angles[i] + angle_;
                if (newAngle > Math.PI) {
                    newAngle -= 2 * Math.PI;
                }
                //create new crease
                Crease newCrease = addNewCrease(cp, center, newAngle, type);
                Node newNode = newCrease.nodes[0];
                for (int j = 0; j < creases.size(); j++) {
                    cp.removeCrease(creases.get(j));
                }
                if (!newNode.isEdge()) {
                    return propagateNewCreasesRecursive(newCrease, newNode, cp,
                            (type == Crease.MOUNTAIN ? Crease.VALLEY : Crease.MOUNTAIN));
                }
                return true;
            } else if (Math.abs(angle_) < Crease.TOLERANCE) {
                Crease removeCandidateCrease = center.creases.get((i + creaseIndex + 1) % center.creases.size());
                ReflectionPath removeCandidate = removeCandidateCrease.reflPath;
//                System.out.println("oioioio " + removeCandidate);
                if ((removeCandidate.ends[0].equals(center) || removeCandidate.ends[1].equals(center))
                        && removeCandidateCrease.type != type && removeCandidate.isSemiComplete()) {
//                    System.out.println("oioioioioioioio");
                    for (int j = 0; j < creases.size(); j++) {
                        cp.removeCrease(creases.get(j));
                    }
                    for (int j = 0; j < removeCandidate.creases.size(); j++) {
                        cp.removeCrease(removeCandidate.creases.get(j));
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isSemiComplete() {
        return ends[0].isEdge() || ends[1].isEdge();
    }

    static boolean propagateNewCreasesRecursive(Crease c, Node n, CP cp, int type) {
//        if(Unfolder.i==12){
//        cp.exportResult("teste" + Unfolder.i + "" + System.currentTimeMillis());
//        System.out.println("asdf " + c);
//        }
        int creaseIndex = n.creases.indexOf(c);

        double[] angles = new double[n.creases.size()];
        double[] angles_ = new double[n.creases.size()];
        for (int i = 0; i < n.creases.size(); i++) {
            angles[i] = n.creases.get((i + creaseIndex + 1) % n.creases.size()).angleBetween(
                    n.creases.get((i + creaseIndex) % n.creases.size()), n);
//            System.out.println(" Angle " + n.creases.get(i).getAngle(n) * 180 / Math.PI + " " + angles[i]);
        }
        angles_[0] = angles[0];
        for (int i = 1; i < angles.length; i++) {
            angles_[i] = angles_[i - 1] + Math.pow(-1, i) * angles[i];
        }
        for (int i = 0; i < angles_.length - 1; i++) {
            Crease removeCandidate = n.creases.get((i + creaseIndex + 1) % n.creases.size());
            if (Math.abs(angles_[i]) < Crease.TOLERANCE) {
                ReflectionPath reflPath = removeCandidate.reflPath;
//                System.out.println("oioioio " + removeCandidate);
                if (reflPath == null) {
//                    cp.exportResult("teste");
//                    System.out.println("oioioi "+c+" "+n+" "+removeCandidate);
//                    System.exit(type);
                    return false;
                }
                int balance = n.getMVBalance();
                if (balance > 0 && removeCandidate.type == Crease.MOUNTAIN
                        || balance < 0 && removeCandidate.type == Crease.VALLEY) {
                    continue;
                }
                if ((reflPath.ends[0].equals(n) || reflPath.ends[1].equals(n))
                        && removeCandidate.type != type && reflPath.isSemiComplete()) {
                    for (int j = 0; j < reflPath.creases.size(); j++) {
                        cp.removeCrease(reflPath.creases.get(j));
                    }
                    return true;
                }
            }
        }
        for (int i = 0; i < angles_.length; i++) {
            if (angles_[i] < -Crease.TOLERANCE) {
//                //calculate angle of the new crease
                double newAngle = n.creases.get((i + creaseIndex) % n.creases.size()).getAngle(n)
                        + angles[i] + angles_[i];
                if (newAngle > Math.PI) {
                    newAngle -= 2 * Math.PI;
                }
                //create new crease
                Crease newCrease = addNewCrease(cp, n, newAngle, type);
                Node newNode = newCrease.nodes[0];
                if (!newNode.isEdge()) {
                    return propagateNewCreasesRecursive(newCrease, newNode, cp,
                            (type == Crease.MOUNTAIN ? Crease.VALLEY : Crease.MOUNTAIN));
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    static Crease propagateNewCreases(Crease c, Node n, CP cp, int type) {
//        if(Unfolder.i==12){
//        cp.exportResult("teste" + Unfolder.i + "" + System.currentTimeMillis());
//        System.out.println("asdf " + c);
//        }
        int creaseIndex = n.creases.indexOf(c);

        double[] angles = new double[n.creases.size()];
        double[] angles_ = new double[n.creases.size()];
        for (int i = 0; i < n.creases.size(); i++) {
            angles[i] = n.creases.get((i + creaseIndex + 1) % n.creases.size()).angleBetween(
                    n.creases.get((i + creaseIndex) % n.creases.size()), n);
//            System.out.println(" Angle " + n.creases.get(i).getAngle(n) * 180 / Math.PI + " " + angles[i]);
        }
        angles_[0] = angles[0];
        for (int i = 1; i < angles.length; i++) {
            angles_[i] = angles_[i - 1] + Math.pow(-1, i) * angles[i];
            if (angles_[i] < -Crease.TOLERANCE) {
//                //calculate angle of the new crease
                double newAngle = n.creases.get((i + creaseIndex) % n.creases.size()).getAngle(n)
                        + angles[i] + angles_[i];
                if (newAngle > Math.PI) {
                    newAngle -= 2 * Math.PI;
                }
                //create new crease
                return addNewCrease(cp, n, newAngle, type);
            }
        }
        return null;
    }

    static Crease propagateNewCreasesClockwise(Crease c, Node n, CP cp, int type) {
        int creaseIndex = n.creases.indexOf(c);

        double[] angles = new double[n.creases.size()];
        double[] angles_ = new double[n.creases.size()];
        for (int i = 0; i < n.creases.size(); i++) {
            angles[i] = n.creases.get((-i + creaseIndex - 1 + n.creases.size()) % n.creases.size()).angleBetween(
                    n.creases.get((-i + creaseIndex + n.creases.size()) % n.creases.size()), n);
        }
        angles_[0] = angles[0];
        for (int i = 1; i < angles.length; i++) {
            angles_[i] = angles_[i - 1] + Math.pow(-1, i) * angles[i];
            if (angles_[i] < -Crease.TOLERANCE) {
//                //calculate angle of the new crease
                double newAngle = n.creases.get((-i + creaseIndex + n.creases.size()) % n.creases.size()).getAngle(n)
                        - angles[i] - angles_[i];
                if (newAngle < -Math.PI) {
                    newAngle += 2 * Math.PI;
                }
                //create new crease
                return addNewCrease(cp, n, newAngle, type);
            }
        }
        return null;
    }

    public static Crease addNewCrease(CP cp, Node n, double newAngle, int type) {
        Crease newCrease = new Crease(new Node(n.x + Math.cos(newAngle) * 1000, n.y
                + Math.sin(newAngle) * 1000), cp.getNodeAt(n), type);
//                System.out.println("new crease " + newCrease.nodes[0].x + " " + newCrease.nodes[0].y + " " + newCrease.nodes[1].x + " " + newCrease.nodes[1].y);

        Node newNode = null;
        Crease intercepted = null;
        double minDistance = 1000;
        for (Crease crease : cp.creases) {
            if (crease.type == Crease.AUX) {
                continue;
            }
            Node newNodeCandidate = crease.getCrossPoint(newCrease);
            if (newNodeCandidate != null && !n.equals(newNodeCandidate)) {
//                        System.out.println("new node candidate " + newNodeCandidate.x + ", " + newNodeCandidate.y + " " + newNodeCandidate.equals(center));
                double dist = n.distanceFrom(newNodeCandidate);
                if (dist < minDistance) {
                    newNode = newNodeCandidate;
                    intercepted = crease;
                    minDistance = dist;
                }
            }
        }

//                System.out.println("new node " + newNode.x + ", " + newNode.y + " " + type);
        if (newNode == null) {
            System.out.println("ffffff " + newCrease);
        }
        cp.splitCrease(intercepted, newNode);
        newCrease.nodes[0] = newNode;
        cp.addNode(newNode);
        newNode.addCrease(newCrease);
        n.addCrease(newCrease);
        cp.addCrease(newCrease);
        return newCrease;
    }

    //splits the reflection path in two changing the direction.]
    //flap movement as the hinge of a petal fold
    public boolean split(CP cp) {
        if (ends[0].isEdge() || ends[1].isEdge()) {
            return false;
        }
        int mvBalance0 = ends[0].getMVBalance();
        int mvBalance1 = ends[1].getMVBalance();
        if (!(mvBalance0 > 0 && creases.get(0).type == Crease.VALLEY
                || mvBalance0 < 0 && creases.get(0).type == Crease.MOUNTAIN)) {
            return false;
        }
        if (!(mvBalance1 > 0 && creases.get(creases.size() - 1).type == Crease.VALLEY
                || mvBalance1 < 0 && creases.get(creases.size() - 1).type == Crease.MOUNTAIN)) {
            return false;
        }
        System.out.println("split " + this);
        Crease newCrease = propagateNewCreases(creases.get(0), cp.getNodeAt(ends[0]), cp, creases.get(0).type);
        System.out.println("debug split " + newCrease);
        if (newCrease == null) {
            return false;
        }
        Node newNode = newCrease.nodes[0];
        boolean firstPropagation = true;
        while (true) {
            if (newNode.equals(ends[1])) {
                break;
            }
            if (newNode.isEdge()) {
                if (firstPropagation) {
                    firstPropagation = false;
                    newCrease = propagateNewCreasesClockwise(creases.get(creases.size() - 1), cp.getNodeAt(ends[1]), cp, creases.get(creases.size() - 1).type);
                    if (newCrease == null) {
                        return false;
                    }
                    newNode = newCrease.nodes[0];
                    continue;
                }
                break;
            }
            newCrease = propagateNewCreases(newCrease, newNode, cp,
                    newCrease.type == Crease.MOUNTAIN ? Crease.VALLEY : Crease.MOUNTAIN);
            newNode = newCrease.nodes[0];
        }
        for (int i = 0; i < creases.size(); i++) {
            cp.removeCrease(creases.get(i));
        }
        return true;
    }
}
