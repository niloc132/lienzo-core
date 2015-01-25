package com.ait.lienzo.client.core.shape;

import com.ait.lienzo.client.core.types.NFastArrayList;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.client.core.util.WiresShape.Magnet;

public interface MagnetMapper
{
    Point2DArray getMagnetLocations();

    public void updateMagnetLocations(NFastArrayList<Magnet> magnets);

    public static class RectangleMagnetMapper implements MagnetMapper
    {
        private Rectangle rect;

        private MagnetsPosition position;

        public RectangleMagnetMapper(Rectangle rect)
        {
            this.rect = rect;
            position = MagnetsPosition.NESW4;
        }

        @Override
        public Point2DArray getMagnetLocations()
        {
            double x = rect.getX();
            double y = rect.getY();
            double w = rect.getWidth();
            double w12 = w / 2;
            double h = rect.getWidth();
            double h12 = h / 2;

            Point2DArray positions = new Point2DArray();
            switch (position)
            {
                case NESW4:
                    positions.push(new Point2D(x + w12, y),
                            new Point2D(x + w, y + h12),
                            new Point2D(x + w12, y + h),
                            new Point2D(x, y + h12));

                    break;
                case EW2:
                    break;
                case NS2:
                    break;
                case ON_EACH_VERTEX:
                    break;
            }
            return positions;
        }

        @Override
        public void updateMagnetLocations(NFastArrayList<Magnet> magnets)
        {
            double x = rect.getX();
            double y = rect.getY();
            double w = rect.getWidth();
            double w12 = w / 2;
            double h = rect.getHeight();
            double h12 = h / 2;

            switch (position)
            {
                case NESW4:
                    double x0 = x + w12;
                    double y0 = y;

                    double x1 = x + w;
                    double y1 = y + h12;

                    double x2 = x + w12;
                    double y2 = y + h;

                    double x3 = x;
                    double y3 = y + h12;

                    Magnet m = magnets.get(0);
                    Shape north = m.getShape();
                    if (north.getX() != x0 || north.getY() != y0)
                    {
                        m.moveMagnet(x0, y0);
                    }

                    m = magnets.get(1);
                    Shape east = m.getShape();
                    if (east.getX() != x1 || east.getY() != y1)
                    {
                        m.moveMagnet(x1, y1);
                    }

                    m = magnets.get(2);
                    Shape south = m.getShape();
                    if (south.getX() != x2 || south.getY() != y2)
                    {
                        m.moveMagnet(x2, y2);
                    }

                    m = magnets.get(3);
                    Shape west = m.getShape();
                    if (west.getX() != x3 || west.getY() != y3)
                    {
                        m.moveMagnet(x3, y3);
                    }
                    break;
                case EW2:
                    break;
                case NS2:
                    break;
                case ON_EACH_VERTEX:
            }
        }
    }

    public static class OrthogonalPolyLineMagnetMapper implements MagnetMapper
    {
        OrthogonalPolyLine m_line;

        public OrthogonalPolyLineMagnetMapper(OrthogonalPolyLine line)
        {
            m_line = line;
        }

        @Override
        public Point2DArray getMagnetLocations()
        {
            return m_line.getControlPoints();
        }

        @Override
        public void updateMagnetLocations(NFastArrayList<Magnet> magnets)
        {
            Point2DArray points = m_line.getControlPoints();
            for (int i = 0, size = magnets.size(); i < size; i++)
            {
                Magnet m = magnets.get(i);
                Shape s = m.getShape();
                Point2D p = points.get(i);
                if (s.getX() != p.getX() || s.getY() != p.getY())
                {
                    m.moveMagnet(p.getX(), p.getY());
                }
            }
        }
    }
}
