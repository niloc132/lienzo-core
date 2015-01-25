package com.ait.lienzo.client.core.shape;

import com.ait.lienzo.client.core.types.NFastArrayList;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.client.core.util.WiresShape.Handle;

public interface HandleMapper
{
    Point2DArray getHandleLocations();

    void moveHandle(Handle handle, double x, double y);

    public static class RectangleHandleMapper implements HandleMapper
    {
        private final Rectangle rect;

        public RectangleHandleMapper(Rectangle rect)
        {
            this.rect = rect;
        }

        @Override
        public Point2DArray getHandleLocations()
        {
            double x = rect.getX();
            double y = rect.getY();
            double w = rect.getWidth();
            double h = rect.getWidth();
            Point2DArray array = new Point2DArray();
            array.push(new Point2D(x, y),
                    new Point2D(x + w, y),
                    new Point2D(x + w, y + h),
                    new Point2D(x, y + h));

            return array;
        }

        @Override
        public void moveHandle(Handle handle, double x, double y)
        {
            NFastArrayList<Handle> handles = handle.getHandlesContext().getHandles();
            switch (handle.getIndex())
            {
                case 0:
                {
                    // top left
                    if (rect.getX() != x)
                    {
                        double width = rect.getWidth() - (x - rect.getX());

                        rect.setWidth(width);
                        rect.setX(x);

                        handles.get(3).getShape().setX(x);
                    }
                    if (rect.getY() != y)
                    {
                        double height = rect.getHeight() - (y - rect.getY());

                        rect.setHeight(height);
                        rect.setY(y);
                        handles.get(1).getShape().setY(y);
                    }
                    break;
                }
                case 1:
                {
                    // top right
                    double oldX = rect.getX() + rect.getWidth();
                    if (x != oldX)
                    {
                        double width = rect.getWidth() + (x - oldX);

                        rect.setWidth(width);

                        handles.get(2).getShape().setX(x);
                    }
                    if (rect.getY() != y)
                    {
                        double height = rect.getHeight() + rect.getY() - y;

                        //double height = rect.getHeight() - (y - rect.getY());

                        rect.setHeight(height);
                        rect.setY(y);

                        handles.get(0).getShape().setY(y);
                    }
                    break;
                }
                case 2:
                {
                    // bottom right
                    double oldX = rect.getX() + rect.getWidth();
                    if (x != oldX)
                    {
                        double width = x - rect.getX();

                        rect.setWidth(width);

                        handles.get(1).getShape().setX(x);
                    }

                    double oldY = rect.getX() + rect.getWidth();
                    if (y != oldY)
                    {
                        double height = y - rect.getY();

                        rect.setHeight(height);

                        handles.get(3).getShape().setY(y);
                    }
                    break;
                }
                case 3:
                {
                    // bottom left
                    if (rect.getX() != x)
                    {
                        double width = rect.getWidth() - (x - rect.getX());

                        rect.setWidth(width);
                        rect.setX(x);

                        handles.get(0).getShape().setX(x);
                    }

                    double oldY = rect.getX() + rect.getWidth();
                    if (y != oldY)
                    {
                        double height = y - rect.getY();

                        rect.setHeight(height);

                        handles.get(2).getShape().setY(y);
                    }
                    break;
                }
            }
        }
    }

    public static class OrthogonalPolyLineHandleMapper implements HandleMapper
    {
        private final OrthogonalPolyLine m_line;

        public OrthogonalPolyLineHandleMapper(OrthogonalPolyLine line)
        {
            m_line = line;
        }

        public Point2DArray getHandleLocations()
        {
            return m_line.getControlPoints();
        }

        @Override
        public void moveHandle(Handle handle, double x, double y)
        {

            Point2DArray points =  m_line.getControlPoints();
            Point2D point = points.get(handle.getIndex());
            point.setX(x);
            point.setY(y);
            m_line.setControlPoints(points);
        }
    }
}
