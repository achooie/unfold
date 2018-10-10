/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

/**
 *
 * @author akitaya
 */

class FileFilterEx extends javax.swing.filechooser.FileFilter {

    private String extensions[];
    private String msg;

    public FileFilterEx(String[] extensions, String msg) {
        this.extensions = extensions;
        this.msg = msg;
    }

    public boolean accept(java.io.File f) {
        for (int i = 0; i < extensions.length; i++) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getName().endsWith(extensions[i])) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return msg;
    }
}
