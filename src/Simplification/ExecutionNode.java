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
public class ExecutionNode {
    public CP stepCp;
    public ArrayList<ExecutionNode> next;
    public ArrayList<Maneuver> maneuvers;
    public int id;

    public ExecutionNode(CP stepCp) {
        this.stepCp = stepCp;
        next = new ArrayList<>();
        maneuvers = new ArrayList<>();
    }
    
    public void addNext(ExecutionNode next, Maneuver maneuver){
        this.next.add(next);
        maneuvers.add(maneuver);
    }
}
