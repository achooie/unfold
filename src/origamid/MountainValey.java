/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package origamid;

import Simplification.CP;
import Simplification.Crease;
import Simplification.Node;
import Simplification.ReflectionPath;
import java.util.ArrayList;

/**
 *
 * @author akitaya
 */
public class MountainValey {

    public static Crease getReflectionCrease(Crease c, Node n) {
        ArrayList<Crease> creases = n.getCreases();
        int index = creases.indexOf(c);
        Crease reflection = creases.get((index + 2) % creases.size());
        Crease axis = creases.get((index + 1) % creases.size());
        for (int i = 0; i < 2; i++) {
            if (c.getType() != reflection.getType()) {
//                    System.out.println("---> axis " + axis.getAngle(n) * 180 / Math.PI);
//                    System.out.println("---> reflexion " + reflection.getAngle(n) * 180 / Math.PI);
//                    System.out.println(Math.abs(2 * axis.getAngle(n) - c.getAngle(n) - reflection.getAngle(n)));
                if (Math.abs(c.angleBetween(axis, n) - reflection.angleBetween(axis, n)) < Crease.TOLERANCE) {
//                        System.out.println("returned " + reflection.getId());
                    return reflection;
                }
            }
            reflection = creases.get((index - 2 + creases.size()) % creases.size());
            axis = creases.get((index - 1 + creases.size()) % creases.size());
        }
//        System.out.println("returned null");
        return null;
    }

    //return the arraylist of reflection paths. Each reflection path is an array 
    // list of ordered creases.If it is a simple valley/mountain, the first and the last
    // crease must have an edge node or a loop
    public static ArrayList<ReflectionPath> getReflectionPaths(CP cp) {
//        System.out.println("getReflectionPaths");
        ArrayList<Crease> creases = new ArrayList<>();
        ArrayList<ReflectionPath> paths = new ArrayList<>();
        creases.addAll(cp.getCreases());
        while (creases.size() > 0) {
            Crease c = creases.get(0);
            creases.remove(0);
            if (c.getType() == Crease.PAPER_EDGE || c.getType() == Crease.AUX) {
                continue;
            }
            ReflectionPath reflectionPath = new ReflectionPath();
            Node n = c.getNodes()[0];
//            System.out.println("crease " + c.getId() + " type " + c.getType() + " angle " + c.getAngle(n) * 180 / Math.PI);
            Crease reflected = c;
            reflectionPath.add(c);
            do {
                if (n.isEdge()) {
                    break;
                }
                reflected = getReflectionCrease(reflected, n);
//                System.out.println("getReflectionCrease" + reflected);
                if (reflected == null) {
                    break;
                }
                if (reflected.equals(c)) {
                    break;
                }
                int index = reflectionPath.creases.indexOf(reflected);
                if (index != -1) {
//                    System.out.println("merda!!!");
                    ReflectionPath reflectionCicle = new ReflectionPath();
                    for (int i = reflectionPath.creases.size() - 1; i >= index; i--) {
                        Crease cicle = reflectionPath.creases.remove(i);
                        reflectionCicle.add(cicle);
                    }
                    reflectionCicle.calculateEnds();
                    paths.add(reflectionCicle);
                    break;
                }
                reflectionPath.add(reflected);
                creases.remove(reflected);
                n = reflected.getNodes()[0].equals(n)
                        ? reflected.getNodes()[1]
                        : reflected.getNodes()[0];
            } while (!reflected.equals(c));
            reflectionPath.calculateEnds();
            if (reflectionPath.ends[0].equals(reflectionPath.ends[1])) {
                paths.add(reflectionPath);
//                System.out.println("ciclo!!!" + reflectionPath.creases);
                continue;
            }
            n = c.getNodes()[1];
            reflected = c;
            do {
                if (n.isEdge()) {
                    break;
                }
                reflected = getReflectionCrease(reflected, n);
//                System.out.println("getReflectionCrease1" + reflected);
                if (reflected == null) {
                    break;
                }
                int index = reflectionPath.creases.indexOf(reflected);
                if (index != -1) {
//                    System.out.println(index+" merda1!!! "+reflectionPath);
                    ReflectionPath reflectionCicle = new ReflectionPath();
                    for (int i = 0; i <= index; i++) {
                        Crease cicle = reflectionPath.creases.remove(0);
                        reflectionCicle.add(cicle);
                    }
                    reflectionCicle.calculateEnds();
                    paths.add(reflectionCicle);
                    break;
                }
                reflectionPath.add(0, reflected);
                creases.remove(reflected);
                n = reflected.getNodes()[0].equals(n)
                        ? reflected.getNodes()[1]
                        : reflected.getNodes()[0];
            } while (!reflected.equals(c));
//            creases.remove(c);
            reflectionPath.calculateEnds();
            paths.add(reflectionPath);
        }
        return paths;
    }

    public static boolean removeFold(CP cp) {
        System.out.println("begining removeFold");
        ArrayList<Crease> creases = new ArrayList<>();
        creases.addAll(cp.getCreases());
        while (creases.size() > 0) {
            Crease c = creases.get(0);
            creases.remove(0);
            if (c.getType() == Crease.PAPER_EDGE || c.getType() == Crease.AUX) {
                continue;
            }
            ArrayList<Crease> removable = new ArrayList<>();
            Node n = c.getNodes()[0];
            boolean isUnfoldable = false;
            for (int i = 0; i < 2; i++) {
                System.out.println("crease " + c.getId() + " type " + c.getType() + " angle " + c.getAngle(n) * 180 / Math.PI);
                Crease reflected = c;
                do {
                    if (n.isEdge()) {
                        break;
                    }
                    reflected = getReflectionCrease(reflected, n);
                    if (reflected == null) {
                        isUnfoldable = true;
                        break;
                    }
                    if (reflected.equals(c)) {
                        break;
                    }
                    removable.add(reflected);
                    creases.remove(reflected);
                    n = reflected.getNodes()[0].equals(n)
                            ? reflected.getNodes()[1]
                            : reflected.getNodes()[0];
                } while (!reflected.equals(c));
                n = c.getNodes()[1];
            }
            removable.add(c);
            if (isUnfoldable) {
                continue;
            }
            for (Crease crease : removable) {
                cp.removeCrease(crease);
            }
            cp.removeRedundantNodes();
            return true;
        }
        return false;
    }
}
