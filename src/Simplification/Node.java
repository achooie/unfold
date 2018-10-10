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
public class Node {

    static int i = 0;
    int id;
    ArrayList<Crease> creases;
    double x, y;
    int localComplexity;

    public Node(double x, double y) {
        this.x = x;
        this.y = y;
        creases = new ArrayList<>();
        id = ++i;
    }

    public void addCrease(Crease c) {
        if (c.getType() == Crease.AUX) {
            return;
        }
        for (int j = 0; j < creases.size(); j++) {
            Crease crease = creases.get(j);
            if (c.getAngle(this) < crease.getAngle(this)) {
                creases.add(j, c);
                return;
            }
        }
        creases.add(creases.size(), c);
    }

    public void deleteCrease(Crease c) {
        creases.remove(c);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public ArrayList<Crease> getCreases() {
        return creases;
    }

    public int getId() {
        return id;
    }

    public boolean isEdge() {
        if (Math.abs(Math.abs(x) - 200) < Crease.TOLERANCE || Math.abs(Math.abs(y) - 200) < Crease.TOLERANCE) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        Node other = (Node) obj;
        if ((Math.abs(this.x - other.x) < Crease.TOLERANCE
                && Math.abs(this.y - other.y) < Crease.TOLERANCE)) {
            return true;
        }
        return false;
    }

    public double distanceFrom(Node n) {
        return Math.sqrt(Math.pow(x - n.x, 2) + Math.pow(y - n.y, 2));
    }

    @Override
    public String toString() {
        return "n(" + x + "," + y + ")";
    }

    public int getMVBalance() {
        int mvBalance = 0;
        for (int j = 0; j < creases.size(); j++) {
            Crease crease = creases.get(j);
            if (crease.type == Crease.MOUNTAIN) {
                mvBalance++;
            } else if (crease.type == Crease.VALLEY) {
                mvBalance--;
            }
        }
        return mvBalance;
    }
}
