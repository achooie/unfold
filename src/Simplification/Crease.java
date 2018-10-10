/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simplification;

/**
 *
 * @author akitaya
 */
public class Crease {
    
    public static final int MOUNTAIN = 2;
    public static final int VALLEY = 3;
    public static final int PAPER_EDGE = 1;
    public static final int AUX = 0;
    public static final double TOLERANCE = 1.0e-2;
    static int i = 0;
    int id;
    Node[] nodes;
    public ReflectionPath reflPath;
    double angle;
    int type;
    
    public Crease(Node n1, Node n2, int type) {
        this.nodes = new Node[2];
        this.nodes[0] = n1;
        this.nodes[1] = n2;
        this.angle = Math.atan2(n2.getY() - n1.getY(), n2.getX() - n1.getX());
        this.type = type;
        id = ++i;
//        if (id == 463) {
//            System.out.println(angle+" "+n1+" "+n2);
//            System.exit(-1);
//        }
    }
    
    public double getAngle(Node n) {
//        if (Unfolder.i == 287) {
//            System.out.println("getAngle " + angle * 180 / Math.PI + " " + id);
//        }
        if (this.nodes[0].equals(n)) {
            return angle;
        }
        return (angle > 0) ? (angle - Math.PI) : (angle + Math.PI);
    }
    
    public int getType() {
        return type;
    }
    
    public Node[] getNodes() {
        return nodes;
    }
    
    public Node getOpposingNode(Node n) {
        if (n.equals(nodes[0])) {
            return nodes[1];
        }
        return nodes[0];
    }
    
    public int getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        Crease other = (Crease) obj;
        if ((nodes[0].equals(other.nodes[0]) && (nodes[1].equals(other.nodes[1])))
                || (nodes[0].equals(other.nodes[1]) && (nodes[1].equals(other.nodes[0])))) {
            return other.type == type;
        }
        return false;
    }
    
    public boolean hasEdgeNode() {
        if (nodes[0].isEdge() || nodes[1].isEdge()) {
            return true;
        }
        return false;
    }
    
    public boolean hasNode(Node n) {
        if (nodes[0].equals(n) || nodes[1].equals(n)) {
            return true;
        }
        return false;
    }
    
    public boolean haveCommonNode(Crease other) {
        if (nodes[0].equals(other.nodes[0]) || nodes[0].equals(other.nodes[1])
                || nodes[1].equals(other.nodes[0]) || nodes[1].equals(other.nodes[1])) {
            return true;
        }
        return false;
    }
    
    public Node getCrossPoint(Crease c) {
        double den = (nodes[0].x - nodes[1].x) * (c.nodes[0].y - c.nodes[1].y)
                - (nodes[0].y - nodes[1].y) * (c.nodes[0].x - c.nodes[1].x);
        if (Math.abs(den) < TOLERANCE) {
            // creases are paralell
            //check if they can intercept
            if (c.nodes[0].x < c.nodes[1].x) {
                if (nodes[0].x < c.nodes[0].x || nodes[0].x > c.nodes[1].x) {
                    return null;
                }
            } else {
                if (nodes[0].x > c.nodes[0].x || nodes[0].x < c.nodes[1].x) {
                    return null;
                }
            }
            if (c.nodes[0].y < c.nodes[1].y) {
                if (nodes[0].y < c.nodes[0].y || nodes[0].y > c.nodes[1].y) {
                    return null;
                }
            } else {
                if (nodes[0].y > c.nodes[0].y || nodes[0].y < c.nodes[1].y) {
                    return null;
                }
            }
            
            Node insideSquare;
            if (c.nodes[0].x > -200 && c.nodes[0].x < 200
                    && c.nodes[0].y > -200 && c.nodes[0].y < 200) {
                insideSquare = c.nodes[0];
            } else {
                insideSquare = c.nodes[1];
            }
            double dist1 = insideSquare.distanceFrom(nodes[0]);
            double dist2 = insideSquare.distanceFrom(nodes[1]);
            double dist3 = nodes[0].distanceFrom(nodes[1]);
            if (dist1 > dist2) {
                if (Math.abs(dist1 - dist2 - dist3) < TOLERANCE) {
                    //creases intercept
                    return nodes[1];
                }
            } else {
                if (Math.abs(dist2 - dist1 - dist3) < TOLERANCE) {
                    //creases intercept
                    return nodes[0];
                }
            }
            return null;
        }
        double intersectX = (nodes[0].x * nodes[1].y - nodes[0].y * nodes[1].x)
                * (c.nodes[0].x - c.nodes[1].x) - (nodes[0].x - nodes[1].x)
                * (c.nodes[0].x * c.nodes[1].y - c.nodes[1].x * c.nodes[0].y);
        intersectX /= den;
        double intersectY = (nodes[0].x * nodes[1].y - nodes[0].y * nodes[1].x)
                * (c.nodes[0].y - c.nodes[1].y) - (nodes[0].y - nodes[1].y)
                * (c.nodes[0].x * c.nodes[1].y - c.nodes[1].x * c.nodes[0].y);
        intersectY /= den;
//        System.out.println("INTERSECTION ; " + intersectX + ", " + intersectY);
        if (Math.abs(Math.abs(intersectX - nodes[1].x) + Math.abs(nodes[0].x - intersectX)
                - Math.abs(nodes[0].x - nodes[1].x)) < TOLERANCE
                && Math.abs(Math.abs(intersectX - c.nodes[1].x) + Math.abs(c.nodes[0].x - intersectX)
                - Math.abs(c.nodes[0].x - c.nodes[1].x)) < TOLERANCE
                && Math.abs(Math.abs(intersectY - nodes[1].y) + Math.abs(nodes[0].y - intersectY)
                - Math.abs(nodes[0].y - nodes[1].y)) < TOLERANCE
                && Math.abs(Math.abs(intersectY - c.nodes[1].y) + Math.abs(c.nodes[0].y - intersectY)
                - Math.abs(c.nodes[0].y - c.nodes[1].y)) < TOLERANCE) {
            Node newNode = new Node(intersectX, intersectY);
            if (newNode.equals(c.nodes[0])) {
                return c.nodes[0];
            }
            if (newNode.equals(c.nodes[1])) {
                return c.nodes[1];
            }
            if (newNode.equals(this.nodes[0])) {
                return this.nodes[0];
            }
            if (newNode.equals(this.nodes[1])) {
                return this.nodes[1];
            }
            return newNode;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "c[" + nodes[0] + "," + nodes[1] + "," + ((type==MOUNTAIN)?"m":"v") + "]";
    }
    
    public double angleBetween(Crease c, Node commonNode) {
        double angleBetween = Math.abs(c.getAngle(commonNode) - getAngle(commonNode));
        if (angleBetween > Math.PI) {
            angleBetween = Math.PI * 2 - angleBetween;
        }
        return angleBetween;
    }
}
