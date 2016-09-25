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
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFViewport;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class ViewportFilter extends AbstractPostProcessor {
    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.tools.PostProcessor#process(org.kabeja.dxf.DXFDocument,
     *      java.util.Map)
     */
    public void process(DXFDocument doc, Map<String, Object> context) throws ProcessorException {
        DXFViewport viewport = null;
        Iterator<DXFViewport> i = doc.getDXFViewportIterator();

        boolean found = false;

        while (i.hasNext() && !found) {
            DXFViewport v = i.next();

            if (v.isActive()) {
                viewport = v;
                found = true;
            }
        }

        if (viewport != null) {
            double h = viewport.getHeight() / 2;
            double w = (viewport.getHeight() * viewport.getAspectRatio()) / 2;
            Bounds b = new Bounds();

            // the upper right corner
            b.addToBounds(viewport.getCenterPoint().getX() + w,
                viewport.getCenterPoint().getY() + h,
                viewport.getCenterPoint().getZ());

            // the lower left corner
            b.addToBounds(viewport.getCenterPoint().getX() - w,
                viewport.getCenterPoint().getY() - h,
                viewport.getCenterPoint().getZ());
            filterEntities(b, doc);
        }
    }

    protected void filterEntities(Bounds b, DXFDocument doc) {
        Iterator<DXFLayer> i = doc.getDXFLayerIterator();

        while (i.hasNext()) {
            DXFLayer l = i.next();
            Iterator<String> ti = l.getDXFEntityTypeIterator();

            while (ti.hasNext()) {
                String type = ti.next();
                Iterator<DXFEntity> ei = l.getDXFEntities(type).iterator();

                while (ei.hasNext()) {
                    DXFEntity entity = ei.next();
                    Bounds currentBounds = entity.getBounds();

                    if (!b.contains(currentBounds)) {
                        ei.remove();
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
         * @see org.kabeja.tools.PostProcessor#setProperties(java.util.Map)
         */
    @Override
    public void setProperties(Map<String, Object> properties) {
        // TODO Auto-generated method stub
    }
}
