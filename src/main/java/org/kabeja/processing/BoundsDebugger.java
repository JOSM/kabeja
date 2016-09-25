/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja.processing;

import java.util.Iterator;
import java.util.Map;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXF3DFace;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFText;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class BoundsDebugger extends AbstractPostProcessor {
    public static final String LAYER_NAME = "kabeja_bounds_debug";

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.tools.PostProcessor#process(de.miethxml.kabeja.dxf.DXFDocument)
     */
    public void process(DXFDocument doc, Map<String, Object> context) throws ProcessorException {
        //set all blocks to color gray
        Iterator<DXFBlock> i = doc.getDXFBlockIterator();

        while (i.hasNext()) {
            DXFBlock b = i.next();
            Iterator<DXFEntity> ie = b.getDXFEntitiesIterator();

            while (ie.hasNext()) {
                DXFEntity entity = ie.next();

                //set to gray
                entity.setColor(9);
            }
        }

        DXFEntity left = null;
        DXFEntity top = null;
        DXFEntity right = null;
        DXFEntity bottom = null;

        Bounds b = doc.getBounds();
        double x = b.getMinimumX() + (b.getWidth() / 2);
        double y = b.getMinimumY() + (b.getHeight() / 2);

        //starting at the center point of the draft
        Bounds lBounds = new Bounds(x, x, y, y);
        Bounds rBounds = new Bounds(x, x, y, y);
        Bounds tBounds = new Bounds(x, x, y, y);
        Bounds bBounds = new Bounds(x, x, y, y);

        Iterator<DXFLayer> i2 = doc.getDXFLayerIterator();

        while (i2.hasNext()) {
            DXFLayer l = i2.next();

            //set color to gray
            l.setColor(8);

            Iterator<String> ti = l.getDXFEntityTypeIterator();

            while (ti.hasNext()) {
                String type = ti.next();
                Iterator<DXFEntity> ei = l.getDXFEntities(type).iterator();

                while (ei.hasNext()) {
                    DXFEntity entity = ei.next();

                    //set to gray
                    entity.setColor(8);

                    Bounds currentBounds = entity.getBounds();

                    if (currentBounds.isValid()) {
                        if (currentBounds.getMinimumX() <= lBounds.getMinimumX()) {
                            lBounds = currentBounds;
                            left = entity;
                        }

                        if (currentBounds.getMinimumY() <= bBounds.getMinimumY()) {
                            bBounds = currentBounds;
                            bottom = entity;
                        }

                        if (currentBounds.getMaximumX() >= rBounds.getMaximumX()) {
                            rBounds = currentBounds;
                            right = entity;
                        }

                        if (currentBounds.getMaximumY() >= tBounds.getMaximumY()) {
                            tBounds = currentBounds;
                            top = entity;
                        }
                    }
                }
            }
        }

        //left -> red
        left.setColor(0);
        addBounds(lBounds, doc, 0, left.getType() + "=" + left.getID());

        //right -> green
        right.setColor(2);
        addBounds(rBounds, doc, 2, right.getType() + "=" + right.getID());

        //bottom blue
        bottom.setColor(4);
        addBounds(bBounds, doc, 4, bottom.getType() + "=" + bottom.getID());

        //top color -> magenta
        top.setColor(5);
        addBounds(tBounds, doc, 5, top.getType() + "=" + top.getID());

        //the  color -> magenta
        top.setColor(5);
        addBounds(b, doc, 6, "ALL");
    }

    protected void addBounds(Bounds bounds, DXFDocument doc, int color,
        String type) {
        DXF3DFace face = new DXF3DFace();
        face.getPoint1().setX(bounds.getMinimumX());
        face.getPoint1().setY(bounds.getMinimumY());

        face.getPoint2().setX(bounds.getMinimumX());
        face.getPoint2().setY(bounds.getMaximumY());

        face.getPoint3().setX(bounds.getMaximumX());
        face.getPoint3().setY(bounds.getMaximumY());

        face.getPoint4().setX(bounds.getMaximumX());
        face.getPoint4().setY(bounds.getMinimumY());

        face.setColor(color);
        face.setLayerName(LAYER_NAME);

        doc.addDXFEntity(face);

        DXFText t = new DXFText();
        t.setDXFDocument(doc);
        t.setText("DEBUG-" + type);
        t.getInsertPoint().setX(bounds.getMinimumX());
        t.getInsertPoint().setY(bounds.getMaximumY());
        t.setColor(color);
        t.setLayerName(LAYER_NAME);
        doc.addDXFEntity(t);
    }

    /* (non-Javadoc)
     * @see org.kabeja.tools.PostProcessor#setProperties(java.util.Map)
     */
    @Override
    public void setProperties(Map<String, Object> properties) {
    }
}
