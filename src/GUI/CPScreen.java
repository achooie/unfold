/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.awt.event.MouseEvent;
import oripa.MainScreen;

/**
 *
 * @author akitaya
 */
public class CPScreen extends MainScreen {

    FoldedModelScreen folded;

    public CPScreen(FoldedModelScreen folded) {
        this.folded = folded;
    }

    public FoldedModelScreen getFolded() {
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
