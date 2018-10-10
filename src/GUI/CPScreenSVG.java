/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import diagram.CanvasSVG;
import diagram.CanvasSVG1;
import java.awt.event.MouseEvent;
import oripa.MainScreen;

/**
 *
 * @author akitaya
 */
public class CPScreenSVG extends MainScreen {

    CanvasSVG folded;
    public CanvasSVG1 currentCanvas;

    public CPScreenSVG(CanvasSVG folded) {
        this.folded = folded;
    }

    CPScreenSVG(CanvasSVG1 currentCanvas) {
        this.currentCanvas = currentCanvas;
    }

    public CanvasSVG getFolded() {
        return folded;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    public void setScale(double nscale) {
        this.scale = nscale;
    }
}
