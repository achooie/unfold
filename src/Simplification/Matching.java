/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simplification;

import java.util.ArrayList;

/**
 *
 * @author Hugo
 */
public class Matching {

    Node[] nodeMapping;
    Crease[][] creaseMapping;
    boolean mountainIsMinus;
    ArrayList<ReflectionPath> removableReflections;
    ArrayList<Crease> terminalCreases;
    ArrayList<Node> terminalNodes;
    //a matching may have a reflection
    Matching reflectionMatching;
    boolean isReflection;
    boolean[] checkedTCreases;

    public Matching(int nodesNumber, Node pivot) {
        nodeMapping = new Node[nodesNumber];
        creaseMapping = new Crease[nodesNumber][];
        terminalCreases = new ArrayList<>();
        terminalNodes = new ArrayList<>();
        nodeMapping[0] = pivot;
        isReflection = false;
    }

    private Matching(Matching m) {
        this.creaseMapping = m.creaseMapping.clone();
        this.mountainIsMinus = m.mountainIsMinus;
        this.nodeMapping = m.nodeMapping.clone();
        this.terminalCreases = (ArrayList<Crease>) m.terminalCreases.clone();
        this.terminalNodes = (ArrayList<Node>) m.terminalNodes.clone();
        this.reflectionMatching = m.reflectionMatching;
        this.isReflection = m.isReflection;
    }

    @Override
    public Matching clone() {
        return new Matching(this);
    }

    public boolean validateMatch(ArrayList<ReflectionPath> reflectionPaths, ArrayList<Matching> matchings) {
        boolean checkedTerminal = false;
        removableReflections = new ArrayList<>();
        checkedTCreases = new boolean[terminalCreases.size()];
        int reflectionIndex[] = new int[terminalCreases.size()];
//        System.out.println("terminal creases " + terminalCreases.size());
        // Check if terminal creases ends on the edge of the paper
        for (int j = 0; j < terminalCreases.size(); j++) {
            checkedTerminal = false;
            Crease tCrease = terminalCreases.get(j);
            Node tNode = terminalNodes.get(j);
            Node opositeToTNode = tNode.equals(tCrease.nodes[0]) ? tCrease.nodes[1] : tCrease.nodes[0];
            //if isreflection and already checked skip
            if (isReflection && reflectionMatching.checkedTCreases[j]) {
                continue;
            }


//            System.out.println("tcrease " + tCrease.nodes[0].x + ", " + tCrease.nodes[0].y + "  " + tCrease.nodes[1].x + ", " + tCrease.nodes[1].y + ", " + isReflection);
            ReflectionPath reflection = tCrease.reflPath;
//            for (ReflectionPath reflection : reflectionPaths) {
            if (!(reflection.ends[0].equals(opositeToTNode) || reflection.ends[1].equals(opositeToTNode))) {
//                    continue;
                return false;
            }
            reflectionIndex[j] = reflection.creases.indexOf(tCrease);
//            System.out.println("reflection index: " + reflectionIndex[j] + " ref size: " + reflection.creases.size() + " " + reflection.creases.get(reflection.creases.size() - 1));
//                if (Unfolder.i == 287) {
//                    System.out.println("reflection index: " + reflectionIndex + " ref size: " + reflection.size() + " " + reflection.get(reflection.size() - 1)
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases
//                            + "\n " + reflection.get(reflection.size() - 1).nodes[1].creases.get(0).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases.get(1).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases.get(2).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases.get(3).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI);
////                    System.exit(-1);
//                }
            if (reflectionIndex[j] == 0 && !tNode.equals(reflection.ends[0])) {
//                System.out.println(reflection + " " + j + " " + reflectionIndex[j]);
                boolean hasEdgeNode = reflection.ends[1].isEdge();
                if ((hasEdgeNode || checkReflections(matchings, reflection, j, reflectionIndex[j]))) {
//                        checkedTerminal = true;
                    if (!isReflection || hasEdgeNode) {
                        removableReflections.add(reflection);
                    }
//                    System.out.println(this + "validate 1 " + hasEdgeNode + " " + isReflection);
                    continue;
                }
//                    else {
////                        checkedTerminal = false;
//                        continue;
//                    }
            } else if (reflectionIndex[j] == reflection.creases.size() - 1 && !tNode.equals(reflection.ends[1])) {
//                System.out.println(reflection + " " + j + " " + reflectionIndex[j]);
                boolean hasEdgeNode = reflection.ends[0].isEdge();
                if ((hasEdgeNode || checkReflections(matchings, reflection, j, reflectionIndex[j]))) {
//                        checkedTerminal = true;
                    if (!isReflection || hasEdgeNode) {
                        removableReflections.add(reflection);
                    }
//                    System.out.println(this + "validate 2 " + hasEdgeNode + " " + isReflection);
                    continue;
                }
//                    else {
////                        checkedTerminal = false;
//                        continue;
//                    }
            }
//                checkedTerminal = false;
//            }
            if (!checkedTerminal) {
                break;
            }
        }
//        if (checkedTerminal && reflectionMatching != null) {
//            if (reflectionMatching.validateMatch(reflectionPaths, matchings)) {
//                matchings.remove(reflectionMatching);
//                return true;
//            }
//            return false;
//        }
        return checkedTerminal;
    }

    public boolean validateMatch1(ArrayList<ReflectionPath> reflectionPaths, ArrayList<Matching> matchings) {
        boolean checkedTerminal = true;
        removableReflections = new ArrayList<>();
        int reflectionIndex[] = new int[terminalCreases.size()];
//        System.out.println("terminal creases " + terminalCreases.size());
        if (!isReflection) {
            checkedTCreases = new boolean[terminalCreases.size()];
        }
        // Check if terminal creases ends on the edge of the paper
        for (int j = 0; j < terminalCreases.size(); j++) {
            Crease tCrease = terminalCreases.get(j);
            Node tNode = terminalNodes.get(j);
            Node opositeToTNode = tNode.equals(tCrease.nodes[0]) ? tCrease.nodes[1] : tCrease.nodes[0];


//            System.out.println("tcrease " + tCrease.nodes[0].x + ", " + tCrease.nodes[0].y + "  " + tCrease.nodes[1].x + ", " + tCrease.nodes[1].y + ", " + isReflection);
            ReflectionPath reflection = tCrease.reflPath;
//            for (ReflectionPath reflection : reflectionPaths) {
            if (!(reflection.ends[0].equals(opositeToTNode) || reflection.ends[1].equals(opositeToTNode))) {
//                    continue;
                return false;
            }

            //if isreflection and already checked skip
            if (isReflection && checkedTCreases[j]) {
                continue;
            }
            reflectionIndex[j] = reflection.creases.indexOf(tCrease);
//            System.out.println("reflection index: " + reflectionIndex[j] + " ref size: " + reflection.creases.size() + " " + reflection.creases.get(reflection.creases.size() - 1));
//                if (Unfolder.i == 287) {
//                    System.out.println("reflection index: " + reflectionIndex + " ref size: " + reflection.size() + " " + reflection.get(reflection.size() - 1)
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases
//                            + "\n " + reflection.get(reflection.size() - 1).nodes[1].creases.get(0).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases.get(1).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases.get(2).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI
//                            + " " + reflection.get(reflection.size() - 1).nodes[1].creases.get(3).getAngle(reflection.get(reflection.size() - 1).nodes[1])*180/Math.PI);
////                    System.exit(-1);
//                }
            if (reflectionIndex[j] == 0 && !tNode.equals(reflection.ends[0])) {
//                System.out.println(reflection + " " + j + " " + reflectionIndex[j]);
                boolean hasEdgeNode = reflection.ends[1].isEdge();
                if (hasEdgeNode) {
                    removableReflections.add(reflection);
                    checkedTCreases[j] = true;
//                    System.out.println(this + "validate 1 " + hasEdgeNode + " " + isReflection);
                    continue;
                } else {
                    checkedTerminal = false;
//                        continue;
                }
            } else if (reflectionIndex[j] == reflection.creases.size() - 1 && !tNode.equals(reflection.ends[1])) {
//                System.out.println(reflection + " " + j + " " + reflectionIndex[j]);
                boolean hasEdgeNode = reflection.ends[0].isEdge();
                if (hasEdgeNode) {
                    removableReflections.add(reflection);
                    checkedTCreases[j] = true;
//                    System.out.println(this + "validate 2 " + hasEdgeNode + " " + isReflection);
                    continue;
                } else {
                    checkedTerminal = false;
//                        continue;
                }
            } else {
                return false;
            }
//                checkedTerminal = false;
//            }
//            if (!checkedTerminal) {
//            break;
//            }
        }

        if (checkedTerminal) {
            return true;
        }

//      check for reflections!

        for (int i = matchings.indexOf(this) + 1; i < matchings.size(); i++) {
            Matching matching = matchings.get(i);
            checkedTerminal = true;
            if (matching.reflectionMatching != null) {
                continue;
            }
            for (int j = 0; j < matching.terminalCreases.size(); j++) {
                if (checkedTCreases[j]) {
                    continue;
                }
                Crease tCrease = matching.terminalCreases.get(j);
                if (!(tCrease.reflPath.equals(terminalCreases.get(j).reflPath))
                        || tCrease.equals(terminalCreases.get(j))) {
                    checkedTerminal = false;
                    break;
                }
            }
            if (checkedTerminal) {
                matching.reflectionMatching = this;
                reflectionMatching = matching;
                reflectionMatching.isReflection = true;
                matching.checkedTCreases = new boolean[terminalCreases.size()];
                for (int j = 0; j < terminalCreases.size(); j++) {
                    if (!checkedTCreases[j]) {
                        removableReflections.add(terminalCreases.get(j).reflPath);
                        checkedTCreases[j] = true;
                        matching.checkedTCreases[j] = true;
                    }
                }
//                System.out.println("Found reflection matching!");
                return true;
            }

        }

//        if (checkedTerminal && reflectionMatching != null) {
//            if (reflectionMatching.validateMatch(reflectionPaths, matchings)) {
//                matchings.remove(reflectionMatching);
//                return true;
//            }
//            return false;
//        }
        return false;
    }

    private boolean checkReflections(ArrayList<Matching> matchings, ReflectionPath reflection,
            int terminalCreaseIndex, int reflectionIndex) {
        if (reflection.creases.size() < 2) {
            return false;
        }
        Crease other = reflectionIndex == 0 ? reflection.creases.get(reflection.creases.size() - 1) : reflection.creases.get(0);
        if (reflectionMatching != null) {
            checkedTCreases[terminalCreaseIndex] = true;
//            System.out.println("checkrefl " + reflectionMatching.terminalCreases.get(terminalCreaseIndex) + " " + other);
            return reflectionMatching.terminalCreases.get(terminalCreaseIndex).equals(other);
        }

        //see if any of the registered matchings is the reflection
        for (int i = matchings.indexOf(this) + 1; i < matchings.size(); i++) {
            Matching matching = matchings.get(i);
            if (matching.reflectionMatching != null) {
                continue;
            }
            Crease tCrease = matching.terminalCreases.get(terminalCreaseIndex);
            Node tNode = matching.terminalNodes.get(terminalCreaseIndex);
            Node opositeToTNode = tNode.equals(tCrease.nodes[0]) ? tCrease.nodes[1] : tCrease.nodes[0];
            if (!(reflection.ends[0].equals(opositeToTNode) || reflection.ends[1].equals(opositeToTNode))) {
                continue;
            }
            if (tCrease.equals(other)) {
                matching.reflectionMatching = this;
                reflectionMatching = matching;
                reflectionMatching.isReflection = true;
                checkedTCreases[terminalCreaseIndex] = true;
//                System.out.println("deu!!! " + terminalCreases.size() + " " + other.nodes[0].x + ", " + other.nodes[0].y + " " + other.nodes[1].x + ", " + other.nodes[1].y);
//                System.exit(-1);
                return true;
            }
        }
        return false;
    }

    public boolean isReflection() {
        return isReflection;
    }
}
