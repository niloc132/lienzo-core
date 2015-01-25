package com.ait.lienzo.client.core.util;

import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragEndHandler;
import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveHandler;
import com.ait.lienzo.client.core.event.NodeDragStartEvent;
import com.ait.lienzo.client.core.event.NodeDragStartHandler;
import com.ait.lienzo.client.core.shape.HandleMapper;
import com.ait.lienzo.client.core.shape.IContainer;
import com.ait.lienzo.client.core.shape.MagnetMapper;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.types.NFastArrayList;
import com.ait.lienzo.client.core.types.NFastDoubleArrayJSO;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Each Handle has an index position starting at the top left and working clockwise around the shape
 *
 * Objects and DataStructures are initialized lazily, to keep things lighter - pay for what you use.
 */
public class WiresShape
{
    private NFastArrayList<HandlerRegistration> m_onNodeDragStartHandlers;

    private HandlerRegistration m_onNodeDragMoveHandler;

    private HandlerRegistration m_onNodeDragEndHandler;

    private Shape m_targetShape;

    private Shape m_protoHandleShape;

    private HandleDragStartHandler m_handleDragStartHandler;

    private NFastArrayList<Handle> m_handles;

    private HandleMapper m_handleMapper;

    private Shape m_protoMagnetShape;

    private NFastArrayList<Magnet> m_magnets;

    private MagnetMapper m_magnetMapper;

    private HandlerRegistration m_startRegHandler;

    private HandlerRegistration m_moveRegHandler;

    private boolean m_handlesVisible;

    private boolean m_magnetsVisible;

    public WiresShape(Shape targetShape, Shape protoHandleShape, HandleMapper handleMapper, Shape protoMagnetShape, MagnetMapper magnetMapper)
    {
        m_targetShape = targetShape;
        m_handleMapper = handleMapper;
        m_protoHandleShape = protoHandleShape;

        m_magnetMapper = magnetMapper;
        m_protoMagnetShape = protoMagnetShape;
    }

    public WiresShape(Shape targetShape, Shape protoHandleShape, HandleMapper cpMapper)
    {
        m_targetShape = targetShape;
        m_handleMapper = cpMapper;
        m_protoHandleShape = protoHandleShape;
    }

    public WiresShape(Shape targetShape, Shape protoMagnetShape, MagnetMapper magnetMapper)
    {
        m_targetShape = targetShape;

        m_magnetMapper = magnetMapper;
        m_protoMagnetShape = protoMagnetShape;
    }

    public NFastArrayList<Handle> getHandles()
    {
        initHandles();
        return m_handles;
    }

    public NFastArrayList<Magnet> getMagnets()
    {
        initMagnets();
        return m_magnets;
    }

    /**
     * Initializes the instances and related data structures. But does not add it to the Layer, or setup any listeners.
     * This is done on showHandlers.
     */
    private void initHandles()
    {
        if (m_handles == null)
        {
            m_handles = new NFastArrayList<Handle>();

            Point2DArray points = m_handleMapper.getHandleLocations();
            addHandle(points.get(0), m_protoHandleShape);

            for (int i = 1, size = points.size(); i < size; i++)
            {
                Shape cp = m_protoHandleShape.copy();
                addHandle(points.get(i), cp);
            }
        }
    }

    private void initMagnets()
    {
        if (m_magnets == null)
        {
            m_magnets = new NFastArrayList<Magnet>();

            Point2DArray points = m_magnetMapper.getMagnetLocations();
            addMagnet(points.get(0), m_protoMagnetShape);

            for (int i = 1, size = points.size(); i < size; i++)
            {
                Shape cp = m_protoMagnetShape.copy();
                addMagnet(points.get(i), cp);
            }
        }
    }

    public Shape getTargetShape()
    {
        return m_targetShape;
    }

    public HandleMapper getHandleMapper()
    {
        return m_handleMapper;
    }

    public HandlerRegistration getOnNodeDragMoveHandler()
    {
        return m_onNodeDragMoveHandler;
    }

    public void setOnNodeDragMoveHandler(HandlerRegistration onNodeDragMoveHandler)
    {
        this.m_onNodeDragMoveHandler = onNodeDragMoveHandler;
    }

    public HandlerRegistration getOnNodeDragEndHandler()
    {
        return m_onNodeDragEndHandler;
    }

    public void setOnNodeDragEndHandler(HandlerRegistration onNodeDragEndHandler)
    {
        this.m_onNodeDragEndHandler = onNodeDragEndHandler;
    }

    /**
     * Adds the handles to the layer, while initializing any data structures and listeners
     */
    public void showHandles()
    {
        initHandles();

        if (m_handleDragStartHandler == null)
        {
            m_handleDragStartHandler = new HandleDragStartHandler(this);
            m_onNodeDragStartHandlers = new NFastArrayList<HandlerRegistration>();
        }

        IContainer container = (IContainer) m_targetShape.getParent();
        for (int i = 0, size = m_handles.size(); i < size; i++)
        {
            Shape shape = m_handles.get(i).getShape();
            container.add(shape);
            addDragHandler(shape);
        }

        m_handlesVisible = true;

        createTargetShapeDragHandler();
    }

    public void hideHandles()
    {
        for (int i = 0, size = m_handles.size(); i < size; i++)
        {
            m_handles.get(i).getShape().removeFromParent();
            m_onNodeDragStartHandlers.get(i).removeHandler();
        }

        m_targetShape.getLayer().draw();

        m_handlesVisible = false;

        // release the objects for GC, they'll be created again for showHandles
        m_handles = null;
        m_onNodeDragStartHandlers = null;
        m_handleDragStartHandler = null;

        destroyTargetShapeDragHandler();
    }

    public void addHandle(Point2D point, Shape handleShape)
    {
        handleShape.setDraggable(true);
        handleShape.setLocation(point);
        m_handles.add(new Handle(this, m_handles.size(), handleShape));
    }

    public void addDragHandler(Shape handleShape)
    {
        if (m_handleDragStartHandler == null)
        {
            m_handleDragStartHandler = new HandleDragStartHandler(this);
            m_onNodeDragStartHandlers = new NFastArrayList<HandlerRegistration>();
        }

        HandlerRegistration handler = handleShape.addNodeDragStartHandler(m_handleDragStartHandler);
        m_onNodeDragStartHandlers.add(handler);
    }

    public void showMagnets()
    {
        initMagnets();

        IContainer container = (IContainer) m_targetShape.getParent();
        for (int i = 0, size = m_magnets.size(); i < size; i++)
        {
            container.add(m_magnets.get(i).getShape());
        }

        m_magnetsVisible = true;

        createTargetShapeDragHandler();
    }

    public void hideMagnets()
    {
        for (int i = 0, size = m_magnets.size(); i < size; i++)
        {
            m_magnets.get(i).getShape().removeFromParent();
        }

        m_magnets = null;

        m_targetShape.getLayer().draw();

        m_handlesVisible = false;

        destroyTargetShapeDragHandler();
    }

    public void addMagnet(Point2D point, Shape shape)
    {
        shape.setLocation(point);
        m_magnets.add(new Magnet(m_magnets.size(), shape));
    }

    public void updateMagnetLocations()
    {
        m_magnetMapper.updateMagnetLocations(m_magnets);
    }

    /**
     * Will create the target drag handler, if it doesn't already exist. It may already exist if either magnets or
     * handles are already visible
     */
    private void createTargetShapeDragHandler()
    {
        if (m_startRegHandler == null)
        {
            TargetDragStartMoveHandler eventHandler = new TargetDragStartMoveHandler(this);
            m_startRegHandler = m_targetShape.addNodeDragStartHandler(eventHandler);
            m_moveRegHandler = m_targetShape.addNodeDragMoveHandler(eventHandler);
        }
    }

    /**
     * Will remove the event handlers and make instances available for GC, as long as neither handles or magnets are
     * visible. Must be called after the boolean flag is set, in the hideX method.
     */
    private void destroyTargetShapeDragHandler()
    {
        if (m_startRegHandler != null && !m_handlesVisible && !m_magnetsVisible)
        {
            m_startRegHandler.removeHandler();
            m_moveRegHandler.removeHandler();

            // release the objects for GC, they'll be created again for showHandles
            m_startRegHandler = null;
            m_moveRegHandler = null;
        }
    }

    public static class HandleDragStartHandler implements NodeDragStartHandler
    {
        WiresShape m_handlesCtx;

        public HandleDragStartHandler(WiresShape handlesCtx)
        {
            this.m_handlesCtx = handlesCtx;
        }

        @Override
        public void onNodeDragStart(NodeDragStartEvent event)
        {
            Shape shape = (Shape) event.getDragContext().getNode();

            NFastArrayList<Handle> handles = m_handlesCtx.getHandles();
            Handle handle = null;
            for (int i = 0, size = handles.size(); i < size; i++)
            {
                handle = handles.get(i);
                if (handle.getShape() == shape)
                {
                    break;
                }
                handle = null;
            }
            if (handle == null)
            {
                throw new IllegalStateException(
                        "Unable to find dragged Handle"); // defensive programming, should never happen.
            }

            HandleDragMoveEndHandler handleDragMoveEndHandler =
                    new HandleDragMoveEndHandler(m_handlesCtx, handle, shape.getX(), shape.getY());

            HandlerRegistration onNodeDragMoveHandlerHandler = shape.addNodeDragMoveHandler(handleDragMoveEndHandler);
            HandlerRegistration onNodeDragEndHandlerHandler = shape.addNodeDragEndHandler(handleDragMoveEndHandler);
            m_handlesCtx.setOnNodeDragMoveHandler(onNodeDragMoveHandlerHandler);
            m_handlesCtx.setOnNodeDragEndHandler(onNodeDragEndHandlerHandler);
        }
    }

    public static class HandleDragMoveEndHandler implements NodeDragMoveHandler, NodeDragEndHandler
    {
        // This class is separate, because we want an instance per handle. Where as we can share the start instance with all handles.

        private Handle m_handle;

        private WiresShape m_handlesCtx;

        private double m_startX;

        private double m_startY;

        public HandleDragMoveEndHandler(WiresShape handlesCtx, Handle handle, double x, double y)
        {
            m_handlesCtx = handlesCtx;
            m_handle = handle;

            m_startX = x;
            m_startY = y;
        }

        @Override
        public void onNodeDragMove(NodeDragMoveEvent event)
        {
            m_handle.moveHandle(m_startX + event.getDragContext().getDx(), m_startY + event.getDragContext().getDy());

            if (m_handlesCtx.m_magnets != null)
            {
                m_handlesCtx.updateMagnetLocations();
            }

            Shape shape = m_handlesCtx.getTargetShape();
            shape.getLayer().draw();
        }

        @Override
        public void onNodeDragEnd(NodeDragEndEvent event)
        {
            m_handlesCtx.getOnNodeDragMoveHandler().removeHandler();
            m_handlesCtx.getOnNodeDragEndHandler().removeHandler();
        }
    }

    public static class TargetDragStartMoveHandler implements NodeDragStartHandler, NodeDragMoveHandler
    {
        WiresShape m_handlesCtx;

        NFastDoubleArrayJSO m_xHandleStarts;

        NFastDoubleArrayJSO m_yHandleStarts;

        NFastDoubleArrayJSO m_xMagnetStarts;

        NFastDoubleArrayJSO m_yMagnetStarts;

        public TargetDragStartMoveHandler(WiresShape handlesCtx)
        {
            this.m_handlesCtx = handlesCtx;
        }

        @Override
        public void onNodeDragStart(NodeDragStartEvent event)
        {
            if (m_handlesCtx.m_handles != null)
            {
                m_xHandleStarts = NFastDoubleArrayJSO.make();
                m_yHandleStarts = NFastDoubleArrayJSO.make();
                NFastArrayList<Handle> handles = m_handlesCtx.m_handles;
                for (int i = 0, size = handles.size(); i < size; i++)
                {
                    Shape handle = handles.get(i).getShape();
                    m_xHandleStarts.push(handle.getX());
                    m_yHandleStarts.push(handle.getY());
                }
            }

            if (m_handlesCtx.m_magnets != null)
            {
                m_xMagnetStarts = NFastDoubleArrayJSO.make();
                m_yMagnetStarts = NFastDoubleArrayJSO.make();
                NFastArrayList<Magnet> magnets = m_handlesCtx.m_magnets;
                for (int i = 0, size = magnets.size(); i < size; i++)
                {
                    Shape magnet = magnets.get(i).getShape();
                    m_xMagnetStarts.push(magnet.getX());
                    m_yMagnetStarts.push(magnet.getY());
                }
            }
        }

        @Override
        public void onNodeDragMove(NodeDragMoveEvent event)
        {
            double dx = event.getDragContext().getDx();
            double dy = event.getDragContext().getDy();

            if (m_handlesCtx.m_handles != null)
            {
                NFastArrayList<Handle> handles = m_handlesCtx.m_handles;
                for (int i = 0, size = handles.size(); i < size; i++)
                {
                    Shape handle = m_handlesCtx.m_handles.get(i).getShape();
                    double x = m_xHandleStarts.get(i) + dx;
                    double y = m_yHandleStarts.get(i) + dy;
                    handle.setX(x).setY(y);
                }
            }

            if (m_handlesCtx.m_magnets != null)
            {
                NFastArrayList<Magnet> magnets = m_handlesCtx.m_magnets;
                for (int i = 0, size = magnets.size(); i < size; i++)
                {
                    Magnet magnet = m_handlesCtx.m_magnets.get(i);
                    double x = m_xMagnetStarts.get(i) + dx;
                    double y = m_yMagnetStarts.get(i) + dy;
                    magnet.moveMagnet(x, y);
                }
            }

            m_handlesCtx.getTargetShape().getLayer().draw();
        }
    }

    public static class Handle
    {
        private int m_index;

        private Shape m_shape;

        private Magnet m_magnet;

        private WiresShape m_wiresShape;

        public Handle(WiresShape wiresShape, int index, Shape shape)
        {
            m_wiresShape = wiresShape;
            m_index = index;
            m_shape = shape;
        }

        public int getIndex()
        {
            return m_index;
        }

        public Shape getShape()
        {
            return m_shape;
        }

        public Magnet getMagnet()
        {
            return m_magnet;
        }

        public void setMagnet(Magnet magnet)
        {
            m_magnet = magnet;
        }

        public void moveHandle(double x, double y)
        {
            m_shape.setX(x);
            m_shape.setY(y);
            m_wiresShape.getHandleMapper().moveHandle(this, x, y);
        }

        public WiresShape getHandlesContext()
        {
            return m_wiresShape;
        }
    }

    public static class Magnet
    {
        private int m_index;

        private Shape m_shape;

        private NFastArrayList<Handle> m_handles;

        public Magnet(int index, Shape shape)
        {
            m_index = index;
            m_shape = shape;
        }

        public int getIndex()
        {
            return m_index;
        }

        public Shape getShape()
        {
            return m_shape;
        }

        public void addHandle(Handle handle)
        {
            if (m_handles == null)
            {
                m_handles = new NFastArrayList<Handle>();
            }

            m_handles.add(handle);
            handle.setMagnet(this);
        }

        public void removeHandle(Handle handle)
        {
            m_handles.remove(handle);
            handle.setMagnet(null);
        }

        public NFastArrayList<Handle> getHandles()
        {
            return m_handles;
        }

        public boolean hasHandles()
        {
            return m_handles != null && m_handles.size() > 0;
        }

        public void moveMagnet(double x, double y)
        {
            m_shape.setX(x).setY(y);

            if (hasHandles())
            {
                NFastArrayList<Handle> handles = m_handles;

                for (int i = 0, size = handles.size(); i < size; i++)
                {
                    Handle handle = handles.get(i);
                    handle.moveHandle(x, y);
                }
            }
        }
    }
}
