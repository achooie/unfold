/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import java.util.ArrayList;
import org.w3c.dom.Element;

/**
 *
 * @author akitaya
 */
public class DiagramFace {
    ArrayList<FaceArea> areas;  
    ArrayList<DiagramVertex> outline;
    ArrayList<FaceArea> above;
    ArrayList<DiagramSymbol> autoSymbols;
    Element domElement;  
    int oriFaceID;
}
