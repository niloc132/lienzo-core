package com.ait.lienzo.client.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.event.AttributesChangedEvent;
import com.ait.lienzo.client.core.event.AttributesChangedHandler;
import com.ait.lienzo.client.core.event.HandlerRegistrationManager;
import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragEndHandler;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.DashArray;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.client.widget.DragConstraintEnforcer;
import com.ait.lienzo.client.widget.DragContext;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * This class indexes the top, vertical center, bottom, left, horizontal center and right parts of shape.
 *
 * All indexing is done by rounding the double value - using Math.round.
 *
 * It then uses this information to optional show guidelines or snapping. These can be turned on and off using the setter methods of this class
 *
 * It's possible to control the style of the guideline when drawn. By using hte setter methods of this class.
 *
 * The circa property controls the number of pixes to search from the current position. For instance a circle of 4, will search 4 pixels
 * above and 4 pixels below the current y position.
 *
 * The implementation is fairly generic and uses shape.getBoundingBox to do it's work. There is only one bit that is shape specific,
 * which is the attribute listener, so the engine can determine if a shape is moved or resized. for instance in the case of a rectangle
 * this is the x, y, w and h attributes. But this would be different for other shapes. For this reason each shape that is to be indexed
 * must have handler class that extends EdgeAndCenterIndexHandler. Currently only Rectangle has this. To make this invisible tot he engine each shape
 * has a method "public EdgeAndCenterIndexHandler getEdgeAndCenterIndexHandler(EdgeAndCenterIndex edgeAndCenterIndex, AlignmentCallback alignmentCallback)"
 * which encapsulates the shape specific part handler.
 *
 * The initial design actually allow for any generic callback when alignment is found - so users could provide their all listeners, if they wanted. However
 * until a use case is found for this, it has not been exposed yet.
 */
public class EdgeAndCenterIndex
{
    private Map<Double, LinkedList<EdgeAndCenterIndexHandler>>             m_leftIndex;

    private Map<Double, LinkedList<EdgeAndCenterIndexHandler>>             m_hCenterIndex;

    private Map<Double, LinkedList<EdgeAndCenterIndexHandler>>             m_rightIndex;

    private Map<Double, LinkedList<EdgeAndCenterIndexHandler>>             m_topIndex;

    private Map<Double, LinkedList<EdgeAndCenterIndexHandler>>             m_vCenterIndex;

    private Map<Double, LinkedList<EdgeAndCenterIndexHandler>>             m_bottomIndex;

    private Map<Double, LinkedList<ShapeDistribution>> m_leftDistIndex;

    private Map<Double, LinkedList<ShapeDistribution>> m_hCenterDistIndex;

    private Map<Double, LinkedList<ShapeDistribution>> m_rightDistIndex;

    private Map<Double, LinkedList<ShapeDistribution>> m_topDistIndex;

    private Map<Double, LinkedList<ShapeDistribution>> m_vCenterDistIndex;

    private Map<Double, LinkedList<ShapeDistribution>> m_bottomDistIndex;

    private DefaultAlignmentCallback                   m_alignmentCallback;

    private   Map<String, EdgeAndCenterIndexHandler> m_shapes         = new HashMap<String, EdgeAndCenterIndexHandler>();

    private   int                                    m_circa          = 4;

    protected boolean                                m_snap           = true;

    protected boolean                                m_drawGuideLines = true;

    public EdgeAndCenterIndex(Layer layer)
    {
        m_leftIndex = new HashMap<Double, LinkedList<EdgeAndCenterIndexHandler>>();
        m_hCenterIndex = new HashMap<Double, LinkedList<EdgeAndCenterIndexHandler>>();
        m_rightIndex = new HashMap<Double, LinkedList<EdgeAndCenterIndexHandler>>();

        m_topIndex = new HashMap<Double, LinkedList<EdgeAndCenterIndexHandler>>();
        m_vCenterIndex = new HashMap<Double, LinkedList<EdgeAndCenterIndexHandler>>();
        m_bottomIndex = new HashMap<Double, LinkedList<EdgeAndCenterIndexHandler>>();

        m_alignmentCallback = new DefaultAlignmentCallback(layer);

        m_leftDistIndex = new HashMap<Double, LinkedList<ShapeDistribution>>();
        m_hCenterDistIndex = new HashMap<Double, LinkedList<ShapeDistribution>>();
        m_rightDistIndex = new HashMap<Double, LinkedList<ShapeDistribution>>();

        m_topDistIndex = new HashMap<Double, LinkedList<ShapeDistribution>>();
        m_vCenterDistIndex = new HashMap<Double, LinkedList<ShapeDistribution>>();
        m_bottomDistIndex = new HashMap<Double, LinkedList<ShapeDistribution>>();
    }

    public double getStrokeWidth()
    {
        return m_alignmentCallback.getStrokeWidth();
    }

    public void setStrokeWidth(double strokeWidth)
    {
        m_alignmentCallback.setStrokeWidth(strokeWidth);
    }

    public String getStrokeColor()
    {
        return m_alignmentCallback.getStrokeColor();
    }

    public void setStrokeColor(String strokeColor)
    {
        m_alignmentCallback.setStrokeColor(strokeColor);
    }

    public DashArray getDashArray()
    {
        return m_alignmentCallback.getDashArray();
    }

    public void setDashArray(DashArray dashArray)
    {
        m_alignmentCallback.setDashArray(dashArray);
    }

    public int getSnapCirca()
    {
        return m_circa;
    }

    public void setSnapCirca(int circa)
    {
        m_circa = circa;
    }

    public boolean isSnap()
    {
        return m_snap;
    }

    public void setSnap(boolean snap)
    {
        m_snap = snap;
    }

    public boolean isDrawGuideLines()
    {
        return m_drawGuideLines;
    }

    public void setDrawGuideLines(boolean drawGuideLines)
    {
        m_drawGuideLines = drawGuideLines;
    }

    public void addShapeToIndex(Shape shape)
    {

        EdgeAndCenterIndexHandler handler = shape.getEdgeAndCenterIndexHandler(this, m_alignmentCallback);

        m_shapes.put(shape.uuid(), handler);
    }

    public void removeShapeFromIndex(Shape shape)
    {

        EdgeAndCenterIndexHandler handler = m_shapes.get(shape.uuid());
        ;
        m_shapes.remove(shape.uuid());

        handler.removeHandlerRegistrations();
    }

    public void addIndexEntry(Map<Double, LinkedList<EdgeAndCenterIndexHandler>> index, EdgeAndCenterIndexHandler handler, double pos)
    {
        double rounded = Math.round(pos);
        LinkedList<EdgeAndCenterIndexHandler> bucket = index.get(rounded);
        if (bucket == null)
        {
            bucket = new LinkedList<EdgeAndCenterIndexHandler>();
            index.put(rounded, bucket);
        }
        bucket.add(handler);
    }


    public void removeIndexEntry(Map<Double, LinkedList<EdgeAndCenterIndexHandler>> index, EdgeAndCenterIndexHandler handler, double pos)
    {
        double rounded = Math.round(pos);
        LinkedList<EdgeAndCenterIndexHandler> bucket = index.get(rounded);
        bucket.remove(handler);
        if (bucket.isEmpty())
        {
            index.remove(rounded);
        }
    }

    public void addDistIndexEntry(Map<Double, LinkedList<ShapeDistribution>> index, ShapeDistribution dist)
    {

        LinkedList<ShapeDistribution> bucket = index.get(dist.getPoint());
        if (bucket == null)
        {
            bucket = new LinkedList<ShapeDistribution>();
            index.put(dist.getPoint(), bucket);
        }
        bucket.add(dist);
    }

    public void removeDistIndexEntry(Map<Double, LinkedList<ShapeDistribution>> index, ShapeDistribution dist)
    {

        LinkedList<ShapeDistribution> bucket = index.get(dist.getPoint());
        bucket.remove(dist);
        if ( bucket.isEmpty() )
        {
            index.remove( dist.getPoint() );
        }
    }



    public void removeDistIndex(EdgeAndCenterIndexHandler handler)
    {
        for(ShapeDistribution dist : handler.getDistributionEntries() )
        {
            EdgeAndCenterIndexHandler h1 = dist.getShape1();
            EdgeAndCenterIndexHandler h2 = dist.getShape2();
            if ( handler == h1 )
            {
                h2.getDistributionEntries().remove(dist);
            }
            else
            {
                h1.getDistributionEntries().remove(dist);
            }

            switch( dist.getDistributionType() )
            {
                case ShapeDistribution.LEFT_DIST:
                    removeDistIndexEntry(m_leftDistIndex, dist);
                    break;
                case ShapeDistribution.H_CENTER_DIST:
                    removeDistIndexEntry(m_hCenterDistIndex, dist);
                    break;
                case ShapeDistribution.RIGHT_DIST:
                    removeDistIndexEntry(m_rightDistIndex, dist);
                    break;
                case ShapeDistribution.TOP_DIST:
                    removeDistIndexEntry(m_topDistIndex, dist);
                    break;
                case ShapeDistribution.V_CENTER_DIST:
                    removeDistIndexEntry(m_vCenterDistIndex, dist);
                    break;
                case ShapeDistribution.BOTTOM_DIST:
                    removeDistIndexEntry(m_bottomDistIndex, dist);
                    break;
            }
        }
        handler.getDistributionEntries().clear();
    }

    public void buildDistIndex(EdgeAndCenterIndexHandler handler) {
        double left = Math.round(handler.getLeft());
        double right = Math.round(handler.getRight());
        double top = Math.round(handler.getTop());
        double bottom = Math.round(handler.getBottom());
        BoundingBox box = handler.getShape().getBoundingBox();

        for ( EdgeAndCenterIndexHandler otherH : m_shapes.values() )
        {
            if ( otherH == handler )
            {
                // don't index against yourself
                continue;
            }

            Shape otherS =  otherH.getShape();
            double otherLeft = Math.round(otherS.getX());
            double otherTop = Math.round(otherS.getY());

            BoundingBox otherBox = otherS.getBoundingBox();
            double otherRight = Math.round(otherLeft + otherBox.getWidth());
            double otherBottom = Math.round(otherTop + otherBox.getHeight());

            ShapeDistribution leftDist = null;
            ShapeDistribution hCenterDist = null;
            ShapeDistribution rightDist = null;
            if ( otherRight < left )
            {
                double dx = left-otherRight;
                double leftPoint = otherLeft - dx;
                double rightPoint = right + dx;
                double centerPoint = Math.round(otherRight + ((left - otherRight)/2));
                leftDist = new ShapeDistribution(otherH, handler, leftPoint, ShapeDistribution.LEFT_DIST);
                hCenterDist = new ShapeDistribution(otherH, handler, centerPoint, ShapeDistribution.H_CENTER_DIST);
                rightDist = new ShapeDistribution(otherH, handler, rightPoint, ShapeDistribution.RIGHT_DIST);
            }
            else if ( otherLeft > right )
            {
                double dx = otherLeft-right;
                double leftPoint = left - dx;
                double rightPoint = otherRight + dx;
                double centerPoint = Math.round(otherLeft + ((right - otherLeft)/2));
                leftDist = new ShapeDistribution(handler, otherH, leftPoint, ShapeDistribution.LEFT_DIST);
                hCenterDist = new ShapeDistribution(handler, otherH,centerPoint, ShapeDistribution.H_CENTER_DIST);
                rightDist = new ShapeDistribution(handler, otherH, rightPoint, ShapeDistribution.RIGHT_DIST);
            }


            if ( leftDist != null )
            {
                addDistIndexEntry(m_leftDistIndex, leftDist);
                addDistIndexEntry(m_hCenterDistIndex, hCenterDist);
                addDistIndexEntry(m_rightDistIndex, rightDist);
            }


            ShapeDistribution topDist = null;
            ShapeDistribution vCenterDist = null;
            ShapeDistribution bottomDist = null;
            if ( otherBottom < top )
            {
                double dx = top-otherBottom;
                double topPoint = otherTop - dx;
                double bottomPoint = bottom + dx;
                double centerPoint = Math.round(otherBottom + ((top - otherBottom)/2));
                topDist = new ShapeDistribution(otherH, handler, topPoint, ShapeDistribution.TOP_DIST);
                vCenterDist = new ShapeDistribution(otherH, handler, centerPoint, ShapeDistribution.V_CENTER_DIST);
                bottomDist = new ShapeDistribution(otherH, handler, bottomPoint, ShapeDistribution.BOTTOM_DIST);
            }
            else if ( otherTop > bottom )
            {
                double dx = otherTop-bottom;
                double topPoint = top - dx;
                double bottomPoint = otherBottom + dx;
                double centerPoint = Math.round(bottom + ((otherTop - bottom)/2));
                topDist = new ShapeDistribution(handler, otherH, topPoint, ShapeDistribution.TOP_DIST);
                vCenterDist = new ShapeDistribution(handler, otherH,  centerPoint, ShapeDistribution.V_CENTER_DIST);
                bottomDist = new ShapeDistribution(handler, otherH,  bottomPoint, ShapeDistribution.BOTTOM_DIST);
            }

            if ( topDist != null )
            {
                addDistIndexEntry(m_topDistIndex, topDist);
                addDistIndexEntry(m_vCenterDistIndex, vCenterDist);
                addDistIndexEntry(m_bottomDistIndex, bottomDist);
            }
        }
    }

    public static class ShapeDistribution
    {
        private static final int LEFT_DIST     = 0;

        private static final int H_CENTER_DIST = 1;

        private static final int RIGHT_DIST    = 2;

        private static final int TOP_DIST      = 3;

        private static final int V_CENTER_DIST = 4;

        private static final int BOTTOM_DIST   = 5;

        private EdgeAndCenterIndexHandler m_shape1;

        private EdgeAndCenterIndexHandler m_shape2;

        private double                    m_point;

        private int                       m_distType;

        public ShapeDistribution(EdgeAndCenterIndexHandler shape1, EdgeAndCenterIndexHandler shape2, double point, int distType)
        {
            m_shape1 = shape1;
            m_shape2 = shape2;
            m_point = point;
            m_distType = distType;
            shape1.getDistributionEntries().add(this);
            shape2.getDistributionEntries().add(this);
        }

        public EdgeAndCenterIndexHandler getShape1()
        {
            return m_shape1;
        }

        public void setShape1(EdgeAndCenterIndexHandler shape1)
        {
            this.m_shape1 = shape1;
        }

        public EdgeAndCenterIndexHandler getShape2()
        {
            return m_shape2;
        }

        public void setShape2(EdgeAndCenterIndexHandler shape2)
        {
            this.m_shape2 = shape2;
        }

        public double getPoint()
        {
            return m_point;
        }

        public void setPoint(double point)
        {
            this.m_point = point;
        }

        public int getDistributionType()
        {
            return m_distType;
        }

        public void setDistributionType(int distType)
        {
            this.m_distType = distType;
        }
    }

    public AlignedMatches findNearestAlignedMatches(EdgeAndCenterIndexHandler handler, double left, double hCenter, double right, double top, double vCenter, double bottom)
    {
        LinkedList<EdgeAndCenterIndexHandler> leftList = null;
        LinkedList<EdgeAndCenterIndexHandler> hCenterList = null;
        LinkedList<EdgeAndCenterIndexHandler> rightList = null;

        LinkedList<EdgeAndCenterIndexHandler> topList = null;
        LinkedList<EdgeAndCenterIndexHandler> vCenterList = null;
        LinkedList<EdgeAndCenterIndexHandler> bottomList = null;

        LinkedList<ShapeDistribution> leftDistList = null;
        LinkedList<ShapeDistribution> hCenterDistList = null;
        LinkedList<ShapeDistribution> rightDistList = null;

        LinkedList<ShapeDistribution> topDistList = null;
        LinkedList<ShapeDistribution> vCenterDistList = null;
        LinkedList<ShapeDistribution> bottomDistList = null;

        int hOffset = 0;
        while (hOffset <= m_circa)
        {
            leftList = findNearestIndexEntry(m_leftIndex, left + hOffset);
            hCenterList = findNearestIndexEntry(m_hCenterIndex, hCenter + hOffset);
            rightList = findNearestIndexEntry(m_rightIndex, right + hOffset);

            leftDistList = findNearestDistIndexEntry(m_leftDistIndex, right + hOffset);
            hCenterDistList = findNearestDistIndexEntry(m_hCenterDistIndex, hCenter + hOffset);
            rightDistList = findNearestDistIndexEntry(m_rightDistIndex, left + hOffset);

            if (matchFound(leftList, hCenterList, rightList, leftDistList, hCenterDistList, rightDistList) )
            {
                break;
            }

            leftList = findNearestIndexEntry(m_leftIndex, left - hOffset);
            hCenterList = findNearestIndexEntry(m_hCenterIndex, hCenter - hOffset);
            rightList = findNearestIndexEntry(m_rightIndex, right - hOffset);

            leftDistList = findNearestDistIndexEntry(m_leftDistIndex, right - hOffset);
            hCenterDistList = findNearestDistIndexEntry(m_hCenterDistIndex, hCenter - hOffset);
            rightDistList = findNearestDistIndexEntry(m_rightDistIndex, left - hOffset);
            if (matchFound(leftList, hCenterList, rightList, leftDistList, hCenterDistList, rightDistList) )
            {
                hOffset = -hOffset;
                break;
            }

            hOffset++;
        }

        int vOffset = 0;
        while (vOffset <= m_circa)
        {
            topList = findNearestIndexEntry(m_topIndex, top + vOffset);
            vCenterList = findNearestIndexEntry(m_vCenterIndex, vCenter + vOffset);
            bottomList = findNearestIndexEntry(m_bottomIndex, bottom + vOffset);

            topDistList = findNearestDistIndexEntry(m_topDistIndex, bottom + vOffset);
            vCenterDistList = findNearestDistIndexEntry(m_vCenterDistIndex, vCenter + vOffset);
            bottomDistList = findNearestDistIndexEntry(m_bottomDistIndex, top + vOffset);

            if ( matchFound( topList, vCenterList, bottomList, topDistList, vCenterDistList, bottomDistList ) )
            {
                break;
            }

            topList = findNearestIndexEntry(m_topIndex, top - vOffset);
            vCenterList = findNearestIndexEntry(m_vCenterIndex, vCenter - vOffset);
            bottomList = findNearestIndexEntry(m_bottomIndex, bottom - vOffset);

            topDistList = findNearestDistIndexEntry(m_topDistIndex, bottom - vOffset);
            vCenterDistList = findNearestDistIndexEntry(m_vCenterDistIndex, vCenter - vOffset);
            bottomDistList = findNearestDistIndexEntry(m_bottomDistIndex, top - vOffset);

            if ( matchFound( topList, vCenterList, bottomList, topDistList, vCenterDistList, bottomDistList ) )
            {
                vOffset = -vOffset;
                break;
            }
            vOffset++;
        }

        AlignedMatches matches;
        if (matchFound(leftList, hCenterList, rightList, leftDistList, hCenterDistList, rightDistList) || matchFound( topList, vCenterList, bottomList, topDistList, vCenterDistList, bottomDistList ) )
        {
            matches = new AlignedMatches(handler,
                                         left + hOffset, leftList, hCenter + hOffset, hCenterList, right + hOffset, rightList,
                                         top + vOffset, topList, vCenter + vOffset, vCenterList, bottom + vOffset, bottomList,
                                         leftDistList, hCenterDistList, rightDistList, topDistList, vCenterDistList, bottomDistList);
        }
        else
        {
            matches = emptyAlignedMatches;
        }

        return matches;
    }

    private boolean matchFound(LinkedList<EdgeAndCenterIndexHandler> l1, LinkedList<EdgeAndCenterIndexHandler> l2, LinkedList<EdgeAndCenterIndexHandler> l3, LinkedList<ShapeDistribution> l4, LinkedList<ShapeDistribution> l5, LinkedList<ShapeDistribution> l6)
    {
        if (l1 != null || l2 != null || l3 != null || l4 != null || l5 != null || l6 != null )
        {
            return true;
        }
        return false;
    }

    private static LinkedList<EdgeAndCenterIndexHandler> findNearestIndexEntry(Map<Double, LinkedList<EdgeAndCenterIndexHandler>> map, double pos)
    {
        double rounded = Math.round(pos);
        LinkedList<EdgeAndCenterIndexHandler> indexEntries = map.get(rounded);
        return indexEntries;
    }

    private static LinkedList<ShapeDistribution> findNearestDistIndexEntry(Map<Double, LinkedList<ShapeDistribution>> map, double pos)
    {
        double rounded = Math.round(pos);
        LinkedList<ShapeDistribution> indexEntries = map.get(rounded);
        return indexEntries;
    }

    private static final EmptyAlignedMatches emptyAlignedMatches = new EmptyAlignedMatches();

    public static class EmptyAlignedMatches extends AlignedMatches
    {
        public EmptyAlignedMatches()
        {
            m_hasMatch = false;
        }
    }

    public void addIndex(EdgeAndCenterIndexHandler handler, double left, double hCenter, double right, double top, double vCenter, double bottom)
    {

        addIndexEntry(m_leftIndex, handler, left);
        addIndexEntry(m_hCenterIndex, handler, hCenter);
        addIndexEntry(m_rightIndex, handler, right);

        addIndexEntry(m_topIndex, handler, top);
        addIndexEntry(m_vCenterIndex, handler, vCenter);
        addIndexEntry(m_bottomIndex, handler, bottom);
    }

    public void removeIndex(EdgeAndCenterIndexHandler handler, double left, double hCenter, double right, double top, double vCenter, double bottom)
    {
        removeIndexEntry(m_leftIndex, handler, left);
        removeIndexEntry(m_hCenterIndex, handler, hCenter);
        removeIndexEntry(m_rightIndex, handler, right);

        removeIndexEntry(m_topIndex, handler, top);
        removeIndexEntry(m_vCenterIndex, handler, vCenter);
        removeIndexEntry(m_bottomIndex, handler, bottom);
    }

    public void addLeftIndexEntry(EdgeAndCenterIndexHandler shape, double left)
    {
        addIndexEntry(m_leftIndex, shape, left);
    }

    public void addHCenterIndexEntry(EdgeAndCenterIndexHandler shape, double hCenter)
    {
        addIndexEntry(m_hCenterIndex, shape, hCenter);
    }

    public void addRightIndexEntry(EdgeAndCenterIndexHandler shape, double right)
    {
        addIndexEntry(m_rightIndex, shape, right);
    }

    public void addTopIndexEntry(EdgeAndCenterIndexHandler shape, double top)
    {
        addIndexEntry(m_topIndex, shape, top);
    }

    public void addVCenterIndexEntry(EdgeAndCenterIndexHandler shape, double vCenter)
    {
        addIndexEntry(m_vCenterIndex, shape, vCenter);
    }

    public void addBottomIndexEntry(EdgeAndCenterIndexHandler shape, double bottom) {
        addIndexEntry(m_bottomIndex, shape, bottom);
    }

    public void removeLeftIndexEntry(EdgeAndCenterIndexHandler shape, double left) {
        addIndexEntry(m_leftIndex, shape, left);
    }

    public void removeHCenterIndexEntry(EdgeAndCenterIndexHandler shape, double hCenter) {
        removeIndexEntry(m_hCenterIndex, shape, hCenter);
    }

    public void removeRightIndexEntry(EdgeAndCenterIndexHandler shape, double right) {
        removeIndexEntry(m_rightIndex, shape, right);
    }

    public void removeTopIndexEntry(EdgeAndCenterIndexHandler shape, double top) {
        removeIndexEntry(m_topIndex, shape, top);
    }

    public void removeVCenterIndexEntry(EdgeAndCenterIndexHandler shape, double vCenter) {
        removeIndexEntry(m_vCenterIndex, shape, vCenter);
    }

    public void removeBottomIndexEntry(EdgeAndCenterIndexHandler shape, double bottom) {
        removeIndexEntry(m_bottomIndex, shape, bottom);
    }

    public static class AlignedMatches {
        private EdgeAndCenterIndexHandler m_handler;

        private double            m_leftPos;
        private LinkedList<EdgeAndCenterIndexHandler> m_left;

        private double            m_hCenterPos;
        private LinkedList<EdgeAndCenterIndexHandler> m_hCenter;

        private double            m_rightPos;
        private LinkedList<EdgeAndCenterIndexHandler> m_right;

        private double            m_topPos;
        private LinkedList<EdgeAndCenterIndexHandler> m_top;

        private double            m_vCenterPos;
        private LinkedList<EdgeAndCenterIndexHandler> m_vCenter;

        private double            m_bottomPos;
        private LinkedList<EdgeAndCenterIndexHandler> m_bottom;

        private LinkedList<ShapeDistribution> m_leftDistList;

        private LinkedList<ShapeDistribution> m_hCenterDistList;

        private LinkedList<ShapeDistribution> m_rightDistList;

        private LinkedList<ShapeDistribution> m_topDistList;

        private LinkedList<ShapeDistribution> m_vCenterDistList;

        private LinkedList<ShapeDistribution> m_bottomDistList;

        protected boolean         m_hasMatch;

        public AlignedMatches() {

        }

        public AlignedMatches(EdgeAndCenterIndexHandler handler,
                              double leftPos, LinkedList<EdgeAndCenterIndexHandler> left, double hCenterPos, LinkedList<EdgeAndCenterIndexHandler> hCenter, double rightPos, LinkedList<EdgeAndCenterIndexHandler> right,
                              double topPos, LinkedList<EdgeAndCenterIndexHandler> top, double vCenterPos, LinkedList<EdgeAndCenterIndexHandler> vCenter, double bottomPos, LinkedList<EdgeAndCenterIndexHandler> bottom,
                              LinkedList<ShapeDistribution> leftDistList, LinkedList<ShapeDistribution> hCenterDistList, LinkedList<ShapeDistribution> rightDistList,
                              LinkedList<ShapeDistribution> topDistList, LinkedList<ShapeDistribution> vCenterDistList, LinkedList<ShapeDistribution> bottomDistList)
        {
            m_handler = handler;
            m_leftPos = leftPos;
            m_left = left;
            m_hCenterPos = hCenterPos;
            m_hCenter = hCenter;
            m_rightPos = rightPos;
            m_right = right;
            m_topPos = topPos;
            m_top = top;
            m_vCenterPos = vCenterPos;
            m_vCenter = vCenter;
            m_bottomPos = bottomPos;
            m_bottom = bottom;

            m_leftDistList = leftDistList;
            m_hCenterDistList = hCenterDistList;
            m_rightDistList = rightDistList;

            m_topDistList = topDistList;
            m_vCenterDistList = vCenterDistList;
            m_bottomDistList = bottomDistList;

            m_hasMatch = true;
        }

        public EdgeAndCenterIndexHandler getHandler() {
            return m_handler;
        }

        public boolean hashMatch() {
            return m_hasMatch;
        }

        public LinkedList<EdgeAndCenterIndexHandler> getLeft()
        {
            return m_left;
        }

        public void setLeft(LinkedList<EdgeAndCenterIndexHandler> left)
        {
            m_left = left;
        }


        public LinkedList<EdgeAndCenterIndexHandler> getHorizontalCenter()
        {
            return m_hCenter;
        }

        public void setHorizontalCenter(LinkedList<EdgeAndCenterIndexHandler> hCenter)
        {
            m_hCenter = hCenter;
        }

        public LinkedList<EdgeAndCenterIndexHandler> getRight()
        {
            return m_right;
        }

        public void setRight(LinkedList<EdgeAndCenterIndexHandler> right)
        {
            m_right = right;
        }


        public LinkedList<EdgeAndCenterIndexHandler> getTop()
        {
            return m_top;
        }

        public void setTop(LinkedList<EdgeAndCenterIndexHandler> top)
        {
            m_top = top;
        }

        public LinkedList<EdgeAndCenterIndexHandler> getVerticalCenter()
        {
            return m_vCenter;
        }

        public void setVerticalCenter(LinkedList<EdgeAndCenterIndexHandler> vCenter)
        {
            m_vCenter = vCenter;
        }

        public LinkedList<EdgeAndCenterIndexHandler> getBottom()
        {
            return m_bottom;
        }

        public void setBottom(LinkedList<EdgeAndCenterIndexHandler> bottom)
        {
            m_bottom = bottom;
        }

        public double getLeftPos()
        {
            return m_leftPos;
        }

        public double getHorizontalCenterPos()
        {
            return m_hCenterPos;
        }

        public double getRightPos()
        {
            return m_rightPos;
        }

        public double getTopPos()
        {
            return m_topPos;
        }

        public double getVerticalCenterPos()
        {
            return m_vCenterPos;
        }

        public double getBottomPos()
        {
            return m_bottomPos;
        }

        public LinkedList<ShapeDistribution> getLeftDistList()
        {
            return m_leftDistList;
        }

        public LinkedList<ShapeDistribution> getHorizontalCenterDistList()
        {
            return m_hCenterDistList;
        }

        public LinkedList<ShapeDistribution> getRightDistList()
        {
            return m_rightDistList;
        }

        public LinkedList<ShapeDistribution> getTopDistList()
        {
            return m_topDistList;
        }

        public LinkedList<ShapeDistribution> getVerticalCenterDistList()
        {
            return m_vCenterDistList;
        }

        public LinkedList<ShapeDistribution> getBottomDistList()
        {
            return m_bottomDistList;
        }
    }


    public static abstract class EdgeAndCenterIndexHandler implements AttributesChangedHandler, DragConstraintEnforcer, NodeDragEndHandler
    {
        protected EdgeAndCenterIndex         m_edgeAndCenterIndex;

        protected Shape                      m_shape;

        protected double                     m_xBoxOffset;

        protected double                     m_yBoxOffset;

        protected boolean                    m_isDragging;

        protected HandlerRegistrationManager m_attrHandlerRegs;

        protected HandlerRegistration        m_dragEndHandlerReg;

        protected AlignmentCallback          m_alignmentCallback;

        protected double                     m_startX;

        protected double                     m_startY;

        protected double                     m_x;

        protected double                     m_y;

        protected double                     m_left;

        protected double                     m_hCenter;

        protected double                     m_right;

        protected double                     m_top;

        protected double                     m_vCenter;

        protected double                     m_bottom;

        protected Set<ShapeDistribution>     m_distEntries;

        protected DragConstraintEnforcer     m_enforcerDelegate;

        public EdgeAndCenterIndexHandler(Shape shape, EdgeAndCenterIndex edgeAndCenterIndex, AlignmentCallback alignmentCallback, Attribute... attributes)
        {
            m_yBoxOffset = 0;

            m_shape = shape;
            m_xBoxOffset = 0;
            m_edgeAndCenterIndex = edgeAndCenterIndex;

            m_alignmentCallback = alignmentCallback;

            capturePositions(shape.getX(), shape.getY());
            m_edgeAndCenterIndex.addIndex(this, m_left, m_hCenter, m_right, m_top, m_vCenter, m_bottom);

            m_edgeAndCenterIndex.buildDistIndex(this);

            if (m_shape.isDraggable())
            {
                dragOn();
            }

            int length = attributes.length;
            m_attrHandlerRegs = new HandlerRegistrationManager();
            for (int i = 0; i < length; i++)
            {
                m_attrHandlerRegs.register(m_shape.addAttributesChangedHandler(attributes[i], this));
            }
        }

        public Set<ShapeDistribution> getDistributionEntries()
        {
            if (m_distEntries == null)
            {
                m_distEntries = new HashSet<ShapeDistribution>();
            }
            return m_distEntries;
        }

        public Shape getShape()
        {
            return m_shape;
        }

        public double getLeft()
        {
            return m_left;
        }

        public double getHorizontalCenter()
        {
            return m_hCenter;
        }

        public double getRight()
        {
            return m_right;
        }

        public double getTop()
        {
            return m_top;
        }

        public double getVerticalCenter()
        {
            return m_vCenter;
        }

        public double getBottom()
        {
            return m_bottom;
        }

        public void captureVerticalPositions(BoundingBox box, double y)
        {
            double height = box.getHeight();
            m_top = y + m_yBoxOffset;
            m_vCenter = (m_top + (height / 2));
            m_bottom = (m_top + height);
        }

        public void captureHorizontalPositions(BoundingBox box, double x)
        {
            double width = box.getWidth();
            m_left = x + m_xBoxOffset;
            m_hCenter = m_left + (width / 2);
            m_right = m_left + width;
        }

        public void capturePositions(double x, double y)
        {
            BoundingBox box = null;
            if (x != m_x)
            {
                box = m_shape.getBoundingBox();
                captureHorizontalPositions(box, x);
            }

            if (y != m_y)
            {
                if (box == null)
                {
                    box = m_shape.getBoundingBox();
                }
                captureVerticalPositions(box, y);
            }

            m_x = x;
            m_y = y;
        }

        public void updateIndex(boolean leftChanged, boolean hCenterChanged, boolean rightChanged,
                                boolean topChanged, boolean vCenterChanged, boolean bottomChanged)
        {
            // This method attempts to avoid uneeded work, by only updating based on which edge or center changed
            BoundingBox box = m_shape.getBoundingBox();

            if (leftChanged || hCenterChanged || rightChanged)
            {
                if (leftChanged)
                {
                    m_edgeAndCenterIndex.removeLeftIndexEntry(this, m_left);
                }

                if (hCenterChanged)
                {
                    m_edgeAndCenterIndex.removeHCenterIndexEntry(this, m_hCenter);
                }

                if (rightChanged)
                {
                    m_edgeAndCenterIndex.removeRightIndexEntry(this, m_right);
                }

                m_x = m_shape.getX();
                captureHorizontalPositions(box, m_x);
                if (leftChanged)
                {
                    m_edgeAndCenterIndex.addLeftIndexEntry(this, m_left);
                }

                if (hCenterChanged)
                {
                    m_edgeAndCenterIndex.addHCenterIndexEntry(this, m_hCenter);
                }

                if (rightChanged)
                {
                    m_edgeAndCenterIndex.addRightIndexEntry(this, m_right);
                }
            }

            if (topChanged || vCenterChanged || bottomChanged)
            {
                if (topChanged)
                {
                    m_edgeAndCenterIndex.removeTopIndexEntry(this, m_top);
                }

                if (vCenterChanged)
                {
                    m_edgeAndCenterIndex.removeVCenterIndexEntry(this, m_vCenter);
                }

                if (bottomChanged)
                {
                    m_edgeAndCenterIndex.removeRightIndexEntry(this, m_right);
                }

                m_y = m_shape.getY();
                captureHorizontalPositions(box, m_y);
                if (topChanged)
                {
                    m_edgeAndCenterIndex.addTopIndexEntry(this, m_top);
                }

                if (vCenterChanged)
                {
                    m_edgeAndCenterIndex.addVCenterIndexEntry(this, m_vCenter);
                }

                if (bottomChanged)
                {
                    m_edgeAndCenterIndex.addRightIndexEntry(this, m_right);
                }
            }
        }

        public void dragOn()
        {
            m_enforcerDelegate = m_shape.getDragConstraints();
            m_shape.setDragConstraints(this);
            m_dragEndHandlerReg = m_shape.addNodeDragEndHandler(this);
        }

        public void draggOff()
        {
            m_shape.setDragConstraints(m_enforcerDelegate);
            removeDragHandlerRegistrations();
        }

        public void onAttributesChanged(AttributesChangedEvent event)
        {
            if (m_isDragging)
            {
                return;
            }

            if (event.has(Attribute.DRAGGABLE))
            {
                boolean isDraggable = m_shape.isDraggable();
                if (!m_isDragging && isDraggable)
                {
                    // was off, now on
                    dragOn();
                }
                else if (m_isDragging && !isDraggable)
                {
                    // was on, now on off
                    draggOff();
                }
                m_isDragging = m_shape.isDraggable();
            }

            doOnAttributesChanged(event);
        }

        public abstract void doOnAttributesChanged(AttributesChangedEvent event);

        @Override public void startDrag(DragContext dragContext)
        {
            // shapes being dragged must be removed from the index, so that they don't snap to themselves
            m_startX = dragContext.getNode().getX();
            m_startY = dragContext.getNode().getY();

            m_isDragging = true;
            m_edgeAndCenterIndex.removeIndex(this, m_left, m_hCenter, m_right, m_top, m_vCenter, m_bottom);

            m_edgeAndCenterIndex.removeDistIndex(this);
        }

        @Override public void adjust(Point2D dxy)
        {
            double x = m_startX + dxy.getX();
            double y = m_startY + dxy.getY();
            capturePositions(x, y);
            AlignedMatches matches = m_edgeAndCenterIndex.findNearestAlignedMatches(this, m_left, m_hCenter, m_right,
                                                                                    m_top, m_vCenter, m_bottom);

            if (m_edgeAndCenterIndex.isSnap())
            {
                BoundingBox box = null;
                double height = 0;
                double width = 0;
                boolean recapture = false;

                // Adjust Vertical
                if (matches.getTop() != null)
                {
                    dxy.setY(matches.getTopPos() - m_startY);
                    recapture = true;
                }
                else if (matches.getVerticalCenter() != null)
                {
                    box = m_shape.getBoundingBox();
                    height = box.getHeight();
                    width = box.getWidth();
                    dxy.setY((matches.getVerticalCenterPos() - (height / 2)) - m_startY);
                    recapture = true;
                }
                else if (matches.getBottom() != null)
                {
                    box = m_shape.getBoundingBox();
                    height = box.getHeight();
                    width = box.getWidth();
                    dxy.setY((matches.getBottomPos() - height) - m_startY);
                    recapture = true;
                }

                // Adjust horizontal
                if (matches.getLeft() != null)
                {
                    dxy.setX(matches.getLeftPos() - m_startX);
                    recapture = true;
                }
                else if (matches.getHorizontalCenter() != null)
                {
                    if (box == null)
                    {
                        box = m_shape.getBoundingBox();
                        height = box.getHeight();
                        width = box.getWidth();
                    }
                    dxy.setX((matches.getHorizontalCenterPos() - (width / 2)) - m_startX);
                    recapture = true;
                }
                else if (matches.getRight() != null)
                {
                    if (box == null)
                    {
                        box = m_shape.getBoundingBox();
                        height = box.getHeight();
                        width = box.getWidth();
                    }
                    dxy.setX((matches.getRightPos() - width) - m_startX);
                    recapture = true;
                }


                // Adjust horizontal distribution
                if (matches.getLeftDistList() != null)
                {
                    if (box == null)
                    {
                        box = m_shape.getBoundingBox();
                        height = box.getHeight();
                        width = box.getWidth();
                    }
                    dxy.setX(matches.getLeftDistList().getFirst().getPoint() - width - m_startX);
                    recapture = true;
                }
                else if (matches.getRightDistList() != null)
                {
                    dxy.setX(matches.getRightDistList().getFirst().getPoint() - m_startX);
                    recapture = true;
                }
                else if (matches.getHorizontalCenterDistList() != null)
                {
                    if (box == null)
                    {
                        box = m_shape.getBoundingBox();
                        height = box.getHeight();
                        width = box.getWidth();
                    }
                    dxy.setX(matches.getHorizontalCenterDistList().getFirst().getPoint() - ( width / 2 ) - m_startX );
                    recapture = true;
                }

                // Adjust vertical distribution
                if (matches.getTopDistList() != null)
                {
                    if (box == null)
                    {
                        box = m_shape.getBoundingBox();
                        height = box.getHeight();
                        width = box.getWidth();
                    }
                    dxy.setY(matches.getTopDistList().getFirst().getPoint() - height - m_startY);
                    recapture = true;
                }
                else if (matches.getBottomDistList() != null)
                {
                    dxy.setY(matches.getBottomDistList().getFirst().getPoint() - m_startY);
                    recapture = true;
                }
                else if (matches.getVerticalCenterDistList() != null)
                {
                    if (box == null)
                    {
                        box = m_shape.getBoundingBox();
                        height = box.getHeight();
                        width = box.getWidth();
                    }
                    dxy.setY(matches.getVerticalCenterDistList().getFirst().getPoint() - (height / 2) - m_startY);
                    recapture = true;
                }

                if (m_enforcerDelegate != null)
                {
                    // Try to obey the default or user provided enforcer too.
                    double dx = dxy.getX();
                    double dy = dxy.getY();
                    m_enforcerDelegate.adjust(dxy);
                    if (!recapture && (dx != dxy.getX() || dy != dxy.getY()))
                    {
                        // if the delegate adjusted, we must recapture
                        recapture = true;
                    }
                }

                // it was adjusted, so recapture points
                if (recapture)
                {
                    x = m_startX + dxy.getX();
                    y = m_startY + dxy.getY();
                    capturePositions(x, y);
                }
            }

            if (m_edgeAndCenterIndex.isDrawGuideLines())
            {
                m_alignmentCallback.call(matches);
            }
        }

        public void onNodeDragEnd(NodeDragEndEvent event)
        {
            m_isDragging = false;
            capturePositions(m_shape.getX(), m_shape.getY());

            m_alignmentCallback.dragEnd();

            // shape was removed from the index, so add it back in
            m_edgeAndCenterIndex.addIndex(this, m_left, m_hCenter, m_right, m_top, m_vCenter, m_bottom);

            m_edgeAndCenterIndex.buildDistIndex(this);
        }

        private void removeDragHandlerRegistrations()
        {
            m_dragEndHandlerReg.removeHandler();
            m_dragEndHandlerReg = null;
        }

        public void removeHandlerRegistrations()
        {
            m_attrHandlerRegs.delete();
            m_attrHandlerRegs = null;

            removeDragHandlerRegistrations();
        }
    }

    public static interface AlignmentCallback
    {
        void call(AlignedMatches matches);

        void dragEnd();
    }

    public static class DefaultAlignmentCallback implements AlignmentCallback
    {
        private final Shape[] m_lines = new Shape[18];

        private Layer         m_layer;

        private double        m_strokeWidth = 0.5;

        private String        m_strokeColor = "#000000";

        private DashArray     m_dashArray   = new DashArray(10, 10);

        public DefaultAlignmentCallback(Layer layer)
        {
            m_layer = layer;
        }

        public DefaultAlignmentCallback(Layer layer, double strokeWidth, String strokeColor, DashArray dashArray)
        {
            this(layer);
            m_strokeWidth = strokeWidth;
            m_strokeColor = strokeColor;
            m_dashArray = dashArray;
        }

        public double getStrokeWidth()
        {
            return m_strokeWidth;
        }

        public void setStrokeWidth(double strokeWidth)
        {
            m_strokeWidth = strokeWidth;
        }

        public String getStrokeColor()
        {
            return m_strokeColor;
        }

        public void setStrokeColor(String strokeColor)
        {
            m_strokeColor = strokeColor;
        }

        public DashArray getDashArray()
        {
            return m_dashArray;
        }

        public void setDashArray(DashArray dashArray)
        {
            m_dashArray = dashArray;
        }

        @Override public void dragEnd()
        {
            for (int i = 0; i < m_lines.length; i++)
            {
                if (m_lines[i] != null)
                {
                    m_layer.remove(m_lines[i]);
                    m_lines[i] = null;
                }
            }

            m_layer.draw();
        }

        @Override public void call(EdgeAndCenterIndex.AlignedMatches matches)
        {
            EdgeAndCenterIndex.EdgeAndCenterIndexHandler handler = matches.getHandler();

            drawLinesIfMatches(handler, matches.getLeft(), matches.getLeftPos(), 0, true);
            drawLinesIfMatches(handler, matches.getHorizontalCenter(), matches.getHorizontalCenterPos(), 1, true);
            drawLinesIfMatches(handler, matches.getRight(), matches.getRightPos(), 2, true);

            drawLinesIfMatches(handler, matches.getTop(), matches.getTopPos(), 3, false);
            drawLinesIfMatches(handler, matches.getVerticalCenter(), matches.getVerticalCenterPos(), 4, false);
            drawLinesIfMatches(handler, matches.getBottom(), matches.getBottomPos(), 5, false);

            drawDistLinesIfMatches(handler, matches.getLeftDistList(), 6, false);
            drawDistLinesIfMatches(handler, matches.getHorizontalCenterDistList(), 8, false);
            drawDistLinesIfMatches(handler, matches.getRightDistList(), 10, false);

            drawDistLinesIfMatches(handler, matches.getTopDistList(), 12, true);
            drawDistLinesIfMatches(handler, matches.getVerticalCenterDistList(), 14, true);
            drawDistLinesIfMatches(handler, matches.getBottomDistList(), 16, true);
        }

        private void drawLinesIfMatches(EdgeAndCenterIndexHandler handler, LinkedList<EdgeAndCenterIndexHandler> shapes, double pos, int index, boolean vertical)
        {

            if (shapes != null)
            {

                if (vertical)
                {
                    drawVerticalLine(handler, pos, shapes, index);
                }
                else
                {
                    drawHorizontalLine(handler, pos, shapes, index);
                }
                m_layer.draw();  // @dean can we avoid calling draw here REVIEW
            }
            else if (m_lines[index] != null)
            {
                removeLine(index, m_lines[index]);
                m_layer.draw();
            }
        }

        private void drawDistLinesIfMatches(EdgeAndCenterIndex.EdgeAndCenterIndexHandler h, LinkedList<ShapeDistribution> shapes, int index, boolean vertical)
        {

            if (shapes != null)
            {
                for (ShapeDistribution dist : shapes)
                {
                    double pos = dist.getPoint();

                    EdgeAndCenterIndexHandler h1 = dist.getShape1();
                    EdgeAndCenterIndexHandler h2 = dist.getShape2();

                    if ( !vertical )
                    {
                        double bottom = h.getBottom();
                        if (h1.getBottom() > bottom)
                        {
                            bottom = h1.getBottom();
                        }

                        if (h2.getBottom() > bottom)
                        {
                            bottom = h2.getBottom();
                        }

                        bottom = bottom + 20;

                        double x0 = 0, y0 = 0, x1 = 0, y1 = 0;
                        double x2 = 0, y2 = 0, x3 = 0, y3 = 0;

                        switch (dist.getDistributionType())
                        {
                            case ShapeDistribution.LEFT_DIST:
                                x0 = h.getRight();
                                y0 = h.getBottom() + 5;
                                x1 = h1.getLeft();
                                y1 = h1.getBottom() + 5;

                                x2 = h1.getRight();
                                y2 = h1.getBottom() + 5;
                                x3 = h2.getLeft();
                                y3 = h2.getBottom() + 5;
                                break;
                            case ShapeDistribution.H_CENTER_DIST:
                                x0 = h1.getRight();
                                y0 = h1.getBottom() + 5;
                                x1 = h.getLeft();
                                y1 = h.getBottom() + 5;

                                x2 = h.getRight();
                                y2 = h.getBottom() + 5;
                                x3 = h2.getLeft();
                                y3 = h2.getBottom() + 5;
                                break;
                            case ShapeDistribution.RIGHT_DIST:
                                x0 = h1.getRight();
                                y0 = h1.getBottom() + 5;
                                x1 = h2.getLeft();
                                y1 = h2.getBottom() + 5;

                                x2 = h2.getRight();
                                y2 = h2.getBottom() + 5;
                                x3 = h.getLeft();
                                y3 = h.getBottom() + 5;
                                break;
                        }

                        drawPolyLine(index, bottom, x0, y0, x1, y1,false);
                        drawPolyLine(index + 1, bottom, x2, y2, x3, y3,false);
                    }
                    else
                    {
                        double right = h.getRight();
                        if (h1.getRight() > right)
                        {
                            right = h1.getRight();
                        }

                        if (h2.getRight() > right)
                        {
                            right = h2.getRight();
                        }

                        right = right + 20;

                        double x0 = 0, y0 = 0, x1 = 0, y1 = 0;
                        double x2 = 0, y2 = 0, x3 = 0, y3 = 0;

                        switch (dist.getDistributionType())
                        {
                            case ShapeDistribution.TOP_DIST:
                                x0 = h.getRight() + 5;
                                y0 = h.getBottom();
                                x1 = h1.getRight() + 5;
                                y1 = h1.getTop();

                                x2 = h1.getRight() + 5;
                                y2 = h1.getBottom();
                                x3 = h2.getRight() + 5;
                                y3 = h2.getTop();
                                break;
                            case ShapeDistribution.V_CENTER_DIST:
                                x0 = h1.getRight() + 5;
                                y0 = h1.getBottom();
                                x1 = h.getRight() + 5;
                                y1 = h.getTop();

                                x2 = h.getRight() + 5;
                                y2 = h.getBottom();
                                x3 = h2.getRight() + 5;
                                y3 = h2.getTop();
                                break;
                            case ShapeDistribution.BOTTOM_DIST:
                                x0 = h1.getRight() + 5;
                                y0 = h1.getBottom();
                                x1 = h2.getRight();
                                y1 = h2.getTop();

                                x2 = h2.getRight() + 5;
                                y2 = h2.getBottom();
                                x3 = h.getRight() + 5;
                                y3 = h.getTop();
                                break;
                        }

                        drawPolyLine(index, right, x0, y0, x1, y1, true);
                        drawPolyLine(index + 1, right, x2, y2, x3, y3, true);
                    }
                }
                m_layer.draw();  // @dean can we avoid calling draw here REVIEW
            }
            else if (m_lines[index] != null)
            {
                removeLine(index,  m_lines[index]);
                removeLine(index+1,  m_lines[index+1]);
                m_layer.draw();
            }
        }

        private void drawPolyLine(int index, double edge, double x0, double y0, double x1, double y1, boolean vertical)
        {
            Point2DArray points;
            if ( vertical)
            {
                points = new Point2DArray(new Point2D(x0, y0), new Point2D(edge, y0),
                                          new Point2D(edge, y1), new Point2D(x1, y1));
            }
            else
            {
                points = new Point2DArray(new Point2D(x0, y0), new Point2D(x0, edge),
                                          new Point2D(x1, edge), new Point2D(x1, y1));
            }

            PolyLine pline = (PolyLine) m_lines[index];
            if ( pline == null ) {
                pline = new PolyLine(points);
                pline.setStrokeWidth(m_strokeWidth);
                pline.setStrokeColor(m_strokeColor);
                pline.setDashArray(m_dashArray);
                m_lines[index] = pline;
                m_layer.add(pline);
            }
            else
            {
                pline.setPoints(points);
            }
        }

        private void removeLine(int index, Shape line)
        {
            m_layer.remove(line);
            m_lines[index] = null;
        }

        private void drawHorizontalLine(EdgeAndCenterIndex.EdgeAndCenterIndexHandler handler, double pos, LinkedList<EdgeAndCenterIndexHandler> shapes, int index)
        {
            double left = handler.getLeft();
            double right = handler.getRight();

            for (EdgeAndCenterIndexHandler otherHandler : shapes)
            {
                Shape shape = otherHandler.getShape();
                double newLeft = shape.getX();
                double newRight = newLeft + shape.getBoundingBox().getWidth();

                if (newLeft < left)
                {
                    left = newLeft;
                }

                if (newRight > right)
                {
                    right = newRight;
                }
            }

            drawHorizontalLine(pos, left, right, index);
        }

        private void drawVerticalLine(EdgeAndCenterIndex.EdgeAndCenterIndexHandler handler, double pos, LinkedList<EdgeAndCenterIndexHandler> shapes, int index)
        {
            double top = handler.getTop();
            double bottom = handler.getBottom();

            for (EdgeAndCenterIndexHandler otherHandler : shapes)
            {
                Shape shape = otherHandler.getShape();
                double newTop = shape.getY();
                double newBottom = newTop + shape.getBoundingBox().getHeight();

                if (newTop < top)
                {
                    top = newTop;
                }

                if (newBottom > bottom)
                {
                    bottom = newBottom;
                }
            }

            drawVerticalLine(pos, top, bottom, index);
        }

        private void drawHorizontalLine(double pos, double left, double right, int index)
        {
            Line line = (Line)m_lines[index];
            if (line == null)
            {
                line = new Line(left, pos, right, pos);
                line.setStrokeWidth(m_strokeWidth);
                line.setStrokeColor(m_strokeColor);
                line.setDashArray(m_dashArray);
                m_layer.add(line);
                m_lines[index] = line;
            }
            else
            {
                line.setPoints(new Point2DArray(new Point2D(left, pos), new Point2D(right, pos)));
            }
        }

        private void drawVerticalLine(double pos, double top, double bottom, int index)
        {
            Line line = (Line) m_lines[index];
            if (line == null)
            {
                line = new Line(pos, top, pos, bottom);
                line.setStrokeWidth(m_strokeWidth);
                line.setStrokeColor(m_strokeColor);
                line.setDashArray(m_dashArray);
                m_layer.add(line);
                m_lines[index] = line;
            }
            else
            {
                line.setPoints(new Point2DArray(new Point2D(pos, top), new Point2D(pos, bottom)));
            }
        }
    }
}
