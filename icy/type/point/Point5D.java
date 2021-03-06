package icy.type.point;

/**
 * Point5D interface.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Point5D
{
    /**
     * Returns the X coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the X coordinate of this <code>Point5D</code>.
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the Y coordinate of this <code>Point5D</code>.
     */
    public abstract double getY();

    /**
     * Returns the Z coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the Z coordinate of this <code>Point5D</code>.
     */
    public abstract double getZ();

    /**
     * Returns the T coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the T coordinate of this <code>Point5D</code>.
     */
    public abstract double getT();

    /**
     * Returns the C coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the C coordinate of this <code>Point5D</code>.
     */
    public abstract double getC();

    /**
     * Sets the X coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setX(double x);

    /**
     * Sets the Y coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setY(double y);

    /**
     * Sets the Z coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setZ(double z);

    /**
     * Sets the T coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setT(double t);

    /**
     * Sets the C coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setC(double c);

    /**
     * Sets the location of this <code>Point5D</code> to the
     * specified <code>double</code> coordinates.
     * 
     * @param x
     *        the new X coordinate of this {@code Point5D}
     * @param y
     *        the new Y coordinate of this {@code Point5D}
     * @param z
     *        the new Z coordinate of this {@code Point5D}
     * @param t
     *        the new T coordinate of this {@code Point5D}
     * @param c
     *        the new C coordinate of this {@code Point5D}
     */
    public void setLocation(double x, double y, double z, double t, double c)
    {
        setX(x);
        setY(y);
        setZ(z);
        setT(t);
        setC(c);
    }

    /**
     * Sets the location of this <code>Point5D</code> to the same
     * coordinates as the specified <code>Point5D</code> object.
     * 
     * @param p
     *        the specified <code>Point5D</code> to which to set
     *        this <code>Point5D</code>
     */
    public void setLocation(Point5D p)
    {
        setLocation(p.getX(), p.getY(), p.getZ(), p.getT(), p.getC());
    }

    public static class Double extends Point5D
    {
        public double x;
        public double y;
        public double z;
        public double t;
        public double c;

        public Double(double x, double y, double z, double t, double c)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
        }

        public Double(double[] xyztc)
        {
            final int len = xyztc.length;

            if (len > 0)
                this.x = xyztc[0];
            if (len > 1)
                this.y = xyztc[1];
            if (len > 2)
                this.z = xyztc[2];
            if (len > 3)
                this.t = xyztc[3];
            if (len > 4)
                this.c = xyztc[4];
        }

        public Double()
        {
            this(0, 0, 0, 0, 0);
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public void setX(double x)
        {
            this.x = x;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setY(double y)
        {
            this.y = y;
        }

        @Override
        public double getZ()
        {
            return z;
        }

        @Override
        public void setZ(double z)
        {
            this.z = z;
        }

        @Override
        public double getT()
        {
            return t;
        }

        @Override
        public void setT(double t)
        {
            this.t = t;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double c)
        {
            this.c = c;
        }

        @Override
        public void setLocation(double x, double y, double z, double t, double c)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
        }

    }

    public static class Float extends Point5D
    {
        public float x;
        public float y;
        public float z;
        public float t;
        public float c;

        public Float(float x, float y, float z, float t, float c)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
        }

        public Float(float[] xyztc)
        {
            final int len = xyztc.length;

            if (len > 0)
                this.x = xyztc[0];
            if (len > 1)
                this.y = xyztc[1];
            if (len > 2)
                this.z = xyztc[2];
            if (len > 3)
                this.t = xyztc[3];
            if (len > 4)
                this.c = xyztc[4];
        }

        public Float()
        {
            this(0, 0, 0, 0, 0);
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public void setX(double x)
        {
            this.x = (float) x;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setY(double y)
        {
            this.y = (float) y;
        }

        @Override
        public double getZ()
        {
            return z;
        }

        @Override
        public void setZ(double z)
        {
            this.z = (float) z;
        }

        @Override
        public double getT()
        {
            return t;
        }

        @Override
        public void setT(double t)
        {
            this.t = (float) t;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double c)
        {
            this.c = (float) c;
        }

        @Override
        public void setLocation(double x, double y, double z, double t, double c)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.t = (float) t;
            this.c = (float) c;
        }
    }

    public static class Integer extends Point5D
    {
        int x;
        int y;
        int z;
        int t;
        int c;

        public Integer(int x, int y, int z, int t, int c)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
        }

        public Integer(int[] xyztc)
        {
            final int len = xyztc.length;

            if (len > 0)
                this.x = xyztc[0];
            if (len > 1)
                this.y = xyztc[1];
            if (len > 2)
                this.z = xyztc[2];
            if (len > 3)
                this.t = xyztc[3];
            if (len > 4)
                this.c = xyztc[4];
        }

        public Integer()
        {
            this(0, 0, 0, 0, 0);
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public void setX(double x)
        {
            this.x = (int) x;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setY(double y)
        {
            this.y = (int) y;
        }

        @Override
        public double getZ()
        {
            return z;
        }

        @Override
        public void setZ(double z)
        {
            this.z = (int) z;
        }

        @Override
        public double getT()
        {
            return t;
        }

        @Override
        public void setT(double t)
        {
            this.t = (int) t;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double c)
        {
            this.c = (int) c;
        }

        @Override
        public void setLocation(double x, double y, double z, double t, double c)
        {
            this.x = (int) x;
            this.y = (int) y;
            this.z = (int) z;
            this.t = (int) t;
            this.c = (int) c;
        }
    }

}
