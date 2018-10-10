/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translations;

import animation.OriFace3D;
import animation.OriHalfedge3D;
import animation.Origami3D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import oripa.Doc;
import oripa.geom.OriEdge;
import oripa.geom.OriFace;
import oripa.geom.OriHalfedge;
import oripa.geom.OriVertex;

/**
 *
 * @author akitaya
 */
public class OBJExporter {

    public static void export(Origami3D doc, String filepath) throws Exception {
//        File file = new File(filepath);
//        file.mkdirs();
        FileWriter fw = new FileWriter(filepath);
        BufferedWriter bw = new BufferedWriter(fw);

        // Align the center of the model, combine scales
        bw.write("# Created by ORIPA\n");
        bw.write("\n");
        String faces = "";

        int id = 1;
        for (OriFace3D face : doc.faces3d) {
            Transform3D transf = new Transform3D(face.transf);
            faces += "f ";
            for (OriHalfedge3D he : face.halfEdges) {
                Point3d p = new Point3d(he.v);
                transf.transform(p);
                bw.write("v " + p.x + " " + p.y + " " + p.z + "\n");
                faces += id + " ";
                id++;
            }
            faces += "\n";
        }
        bw.write(faces);

        bw.close();
    }
}
