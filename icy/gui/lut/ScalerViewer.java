/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.lut;

import icy.gui.component.BorderedPanel;
import icy.gui.component.FontUtil;
import icy.gui.util.GuiUtil;
import icy.image.lut.LUTBand;
import icy.image.lut.LUTBandEvent;
import icy.image.lut.LUTBandEvent.LUTBandEventType;
import icy.image.lut.LUTBandListener;
import icy.math.MathUtil;
import icy.math.Scaler;
import icy.system.thread.SingleProcessor;
import icy.util.ColorUtil;
import icy.util.EventUtil;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public class ScalerViewer extends BorderedPanel implements MouseListener, MouseMotionListener, MouseWheelListener,
        LUTBandListener
{
    private enum actionType
    {
        NULL, MODIFY_LOWBOUND, MODIFY_HIGHBOUND
    }

    public interface ScalerPositionListener extends EventListener
    {
        public void positionChanged(double index, double value);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -1236985071716650592L;

    private static final int ISOVER_DEFAULT_MARGIN = 3;
    private static final int BORDER_WIDTH = 2;
    private static final int BORDER_HEIGHT = 2;

    /**
     * associated lutBand
     */
    private LUTBand lutBand;

    /**
     * histogram data (if wanted)
     */
    private double[] histoData;

    /**
     * length of histogram data
     */
    private int histoLen;

    /**
     * samples producer for histogram
     */
    private SamplesProducer samplesProducer;

    /**
     * listeners
     */
    private final EventListenerList scalerMapPositionListeners;

    /**
     * cached
     */
    private double dataToHistoRatio;
    private double dataToPixRatio;
    private double pixToDataRatio;
    private double pixToHistoRatio;

    /**
     * internals
     */
    private actionType action;
    private final SingleProcessor processor;
    private final Runnable histoUpdater;
    private String message;
    private final Point2D positionInfo;
    private boolean logScale;

    /**
     * 
     */
    public ScalerViewer(SamplesProducer samplesProducer, LUTBand lutBand)
    {
        super(BORDER_WIDTH, BORDER_HEIGHT);

        this.samplesProducer = samplesProducer;
        this.lutBand = lutBand;

        histoData = new double[0];
        histoLen = 0;
        action = actionType.NULL;
        message = "";
        positionInfo = new Point2D.Double();
        logScale = true;
        scalerMapPositionListeners = new EventListenerList();
        processor = new SingleProcessor(true);
        processor.setPriority(Thread.MIN_PRIORITY);
        histoUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // refresh histogram
                    refreshHistoDataInternal();
                }
                catch (Exception e)
                {
                    // just ignore error, it's permitted here
                }
            }
        };

        // dimension (don't change it or you will regret !)
        setMinimumSize(new Dimension(100, 100));
        setPreferredSize(new Dimension(210, 100));
        // faster draw
        setOpaque(true);
        setDoubleBuffered(true);

        updateHistoDataSize(true);

        // add listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        // add listeners
        lutBand.addListener(this);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();

        // remove listeners
        lutBand.removeListener(this);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        // nicy background :)
        GuiUtil.paintBackGround(this, g);

        // udpate histogram size if needed
        updateHistoDataSize(false);

        final Graphics2D g2 = (Graphics2D) g.create();

        displayHistogram(g2);

        // display mouse position infos
        if (positionInfo.getX() != -1)
        {
            final int x = dataToPix(positionInfo.getX());
            final int hRange = getClientHeight() - 1;
            final int bottom = hRange + getClientY();
            final int y = bottom - (int) (positionInfo.getY() * hRange);

            g2.setColor(Color.green);
            g2.drawLine(x, bottom, x, y);
        }

        displayBounds(g2);

        // string display
        g2.setFont(FontUtil.setSize(g2.getFont(), 10));
        g2.setFont(FontUtil.setStyle(g2.getFont(), Font.BOLD));

        if (!StringUtil.isEmpty(message))
            drawString(g2, message, 10, 14);

        g2.dispose();
    }

    private void drawString(Graphics g, String str, int x, int y)
    {
        g.setColor(Color.black);
        g.drawString(str, x + 1, y + 1);
        g.setColor(Color.green);
        g.drawString(str, x, y);
    }

    /**
     * draw bounds
     */
    private void displayBounds(Graphics2D g)
    {
        final int y = getClientY();
        final int h = getClientHeight() - 1;
        final int lowBound = getLowBoundPos();
        final int highBound = getHighBoundPos();

        g.setColor(ColorUtil.mix(Color.blue, Color.white, false));
        g.drawRect(lowBound - 2, y, 3, h);
        g.setColor(Color.blue);
        g.fillRect(lowBound - 1, y + 1, 2, h - 1);
        g.setColor(ColorUtil.mix(Color.red, Color.white, false));
        g.drawRect(highBound - 1, y, 3, h);
        g.setColor(Color.red);
        g.fillRect(highBound, y + 1, 2, h - 1);
    }

    /**
     * draw histogram
     */
    private void displayHistogram(Graphics2D g)
    {
        final int hRange = getClientHeight() - 1;
        final int bottom = getClientY() + hRange;
        final int l = getClientX();
        final int r = l + getClientWidth();
        final double pixToHistoRatioCache = pixToHistoRatio;

        g.setColor(Color.WHITE);

        synchronized (histoData)
        {
            if (histoData.length > 0)
            {
                for (int i = l; i < r; i++)
                {
                    // fast pixToHisto conversion
                    int value = (int) ((i - l) * pixToHistoRatioCache);

                    if (value < 0)
                        value = 0;
                    else if (value > (histoData.length - 1))
                        value = histoData.length - 1;

                    g.drawLine(i, bottom, i, bottom - (int) (histoData[value] * hRange));
                }
            }
            else
                g.drawString("loading...", 20, 50);
        }
    }

    private void updateHistoDataSize(boolean force)
    {
        final Scaler scaler = getScaler();
        final int w;

        if (scaler.isIntegerData())
        {
            // data range
            final double range = (scaler.getAbsRightIn() - scaler.getAbsLeftIn()) + 1;
            // get new width from min between pix range and data range
            w = Math.max(Math.min(getClientWidth(), (int) range), 32);
        }
        else
            // get new width from pix range
            w = Math.max(getClientWidth(), 32);

        final boolean sizeChanged = force || (w != histoLen);

        // update histogram size
        histoLen = w;

        // refresh data only if ratios or size changed
        if (updateRatios() || sizeChanged)
            refreshHistoData();
    }

    public int dataToPix(double value)
    {
        return (int) ((value - getScaler().getAbsLeftIn()) * dataToPixRatio) + getClientX();
    }

    public double pixToData(int value)
    {
        final double min = getScaler().getAbsLeftIn();
        final double data = ((value - getClientX()) * pixToDataRatio) + min;
        return Math.min(Math.max(data, min), getScaler().getAbsRightIn());
    }

    public int pixToHisto(int value)
    {
        final int histoInd = (int) ((value - getClientX()) * pixToHistoRatio);
        return Math.min(Math.max(histoInd, 0), histoData.length - 1);
    }

    private boolean updateRatios()
    {
        final double histoRange = histoLen - 1;
        final double pixRange = Math.max(getClientWidth() - 1, 32);
        final Scaler scaler = getScaler();
        final double dataRange = (scaler.getAbsRightIn() - scaler.getAbsLeftIn());

        final double newDataToHistoRatio;
        final double newDataToPixRatio;
        final double newPixToDataRatio;
        final double newPixToHistoRatio;

        if (dataRange != 0d)
        {
            newDataToHistoRatio = histoRange / dataRange;
            newDataToPixRatio = pixRange / dataRange;
        }
        else
        {
            newDataToHistoRatio = 0d;
            newDataToPixRatio = 0d;
        }

        if (pixRange != 0d)
        {
            newPixToDataRatio = dataRange / pixRange;
            newPixToHistoRatio = histoRange / pixRange;
        }
        else
        {
            newPixToDataRatio = 0d;
            newPixToHistoRatio = 0d;
        }

        boolean result = (newDataToHistoRatio != dataToHistoRatio) || (newDataToPixRatio != dataToPixRatio)
                || (newPixToDataRatio != pixToDataRatio) || (newPixToHistoRatio != pixToHistoRatio);

        dataToHistoRatio = newDataToHistoRatio;
        dataToPixRatio = newDataToPixRatio;
        pixToDataRatio = newPixToDataRatio;
        pixToHistoRatio = newPixToHistoRatio;

        return result;

    }

    public void refreshHistoData()
    {
        // we want background refresh
        processor.requestProcess(histoUpdater, false);
    }

    // this method is called by processor, we don't mind about exception here
    void refreshHistoDataInternal()
    {
        final int len = histoLen;
        final double[] newHistoData = new double[len];

        // init histoGram
        Arrays.fill(newHistoData, 0, len - 1, 0f);

        if (samplesProducer != null)
        {
            final double absLeftIn = getScaler().getAbsLeftIn();
            final double dataToHistoRatioCache = dataToHistoRatio;
            final boolean isInteger = getScaler().isIntegerData();

            // we can have *many* samples --> no duplication wanted
            samplesProducer.requestSamples();
            while (samplesProducer.hasNextSample())
            {
                // need to be recalculated so don't waste time here...
                if (processor.hasWaitingTasks())
                    return;

                final double sample = samplesProducer.nextSample();

                // fast dataToHisto conversion
                int offset = (int) ((sample - absLeftIn) * dataToHistoRatioCache);
                // for integer data type we adjust to integer data only
                if (isInteger && (sample >= 1d))
                {
                    // outside bean --> don't use this data
                    if ((int) ((sample - (absLeftIn + 1d)) * dataToHistoRatioCache) == offset)
                        offset = -1;
                }

                if ((offset >= 0) && (offset < len))
                    newHistoData[offset]++;
            }
        }

        // we want all values to >= 1
        final double min = MathUtil.min(newHistoData);
        MathUtil.add(newHistoData, min + 1f);
        // log
        if (logScale)
            MathUtil.log(newHistoData);
        // normalize data
        MathUtil.normalize(newHistoData);

        histoData = newHistoData;

        // repaint component as histogram changed
        repaint();
    }

    /**
     * @return the histoData
     */
    public double[] getHistoData()
    {
        return histoData;
    }

    /**
     * @return the scaler
     */
    public Scaler getScaler()
    {
        return lutBand.getScaler();
    }

    public double getLowBound()
    {
        return lutBand.getMin();
    }

    public double getHighBound()
    {
        return lutBand.getMax();
    }

    public int getLowBoundPos()
    {
        return dataToPix(getLowBound());
    }

    public int getHighBoundPos()
    {
        return dataToPix(getHighBound());
    }

    private void setLowBound(double value)
    {
        lutBand.setMin(value);
    }

    private void setHighBound(double value)
    {
        lutBand.setMax(value);
    }

    private void setLowBoundPos(int pos)
    {
        setLowBound(pixToData(pos));
    }

    private void setHighBoundPos(int pos)
    {
        setHighBound(pixToData(pos));
    }

    /**
     * tasks to do on scaler changes
     */
    public void onScalerChanged()
    {
        // repaint component now
        repaint();
    }

    /**
     * Check if Point p is over area (u, *)
     * 
     * @param p
     *        point
     * @param x
     *        area position
     * @return boolean
     */
    private boolean isOverX(Point p, int u)
    {
        return isOver(p.x, p.y, u, -1, ISOVER_DEFAULT_MARGIN);
    }

    /**
     * Check if (x, y) is over area (u, v)
     * 
     * @param x
     * @param y
     *        pointer
     * @param u
     * @param v
     *        area position
     * @param margin
     *        allowed margin
     * @return boolean
     */
    private boolean isOver(int x, int y, int u, int v, int margin)
    {
        final boolean x_ok;
        final boolean y_ok;

        x_ok = (u == -1) || ((x >= (u - margin)) && (x <= (u + margin)));
        y_ok = (v == -1) || ((y >= (v - margin)) && (y <= (v + margin)));

        return x_ok && y_ok;
    }

    /**
     * @return the samplesProducer
     */
    public SamplesProducer getSamplesProducer()
    {
        return samplesProducer;
    }

    /**
     * @param samplesProducer
     *        the samplesProducer to set
     */
    public void setSamplesProducer(SamplesProducer samplesProducer)
    {
        this.samplesProducer = samplesProducer;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param value
     *        the message to set
     */
    public void setMessage(String value)
    {
        if (!StringUtil.equals(message, value))
        {
            message = value;
            repaint();
        }
    }

    /**
     * @return the logScale
     */
    public boolean getLogScale()
    {
        return logScale;
    }

    /**
     * @param value
     *        the logScale to set
     */
    public void setLogScale(boolean value)
    {
        if (logScale != value)
        {
            logScale = value;
            refreshHistoData();
        }
    }

    private void setPositionInfo(double index, double value)
    {
        if ((positionInfo.getX() != index) || (positionInfo.getY() != value))
        {
            positionInfo.setLocation(index, value);
            scalerPositionChanged(index, value);
            repaint();
        }
    }

    /**
     * show popup menu
     */
    // private void showPopupMenu(final Point pos)
    // {
    // // rebuild menu
    // final JPopupMenu menu = new JPopupMenu();
    //
    // final JMenu scaleMenu = new JMenu("Scaling");
    //
    // final JCheckBoxMenuItem logItem = new JCheckBoxMenuItem("Log", logScale);
    // logItem.addActionListener(new ActionListener()
    // {
    // @Override
    // public void actionPerformed(ActionEvent e)
    // {
    // setLogScale(true);
    // }
    // });
    //
    // final JCheckBoxMenuItem linearItem = new JCheckBoxMenuItem("Linear", !logScale);
    // linearItem.addActionListener(new ActionListener()
    // {
    // @Override
    // public void actionPerformed(ActionEvent e)
    // {
    // setLogScale(false);
    // }
    // });
    //
    // scaleMenu.add(logItem);
    // scaleMenu.add(linearItem);
    //
    // menu.add(scaleMenu);
    //
    // menu.pack();
    // menu.validate();
    //
    // // display menu
    // menu.show(this, pos.x, pos.y);
    // }

    /**
     * update mouse cursor
     */
    private void updateCursor(Point pos)
    {
        final int cursor;

        if (action != actionType.NULL)
            cursor = Cursor.W_RESIZE_CURSOR;
        else if (isOverX(pos, getLowBoundPos()) || isOverX(pos, getHighBoundPos()))
            cursor = Cursor.HAND_CURSOR;
        else
            cursor = Cursor.DEFAULT_CURSOR;

        // only if different
        if (getCursor().getType() != cursor)
            setCursor(Cursor.getPredefinedCursor(cursor));
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addScalerPositionListener(ScalerPositionListener listener)
    {
        scalerMapPositionListeners.add(ScalerPositionListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeScalerPositionListener(ScalerPositionListener listener)
    {
        scalerMapPositionListeners.remove(ScalerPositionListener.class, listener);
    }

    /**
     * mouse position on scaler info changed
     */
    public void scalerPositionChanged(double index, double value)
    {
        for (ScalerPositionListener listener : scalerMapPositionListeners.getListeners(ScalerPositionListener.class))
            listener.positionChanged(index, value);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {

    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        updateCursor(e.getPoint());
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        if (getCursor().getType() != Cursor.getDefaultCursor().getType())
            setCursor(Cursor.getDefaultCursor());

        // hide message
        setMessage("");
        setPositionInfo(-1, -1);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        final Point pos = e.getPoint();

        if (EventUtil.isLeftMouseButton(e))
        {
            if (isOverX(pos, getLowBoundPos()))
                action = actionType.MODIFY_LOWBOUND;
            else if (isOverX(pos, getHighBoundPos()))
                action = actionType.MODIFY_HIGHBOUND;

            // show message
            if (action != actionType.NULL)
            {
                if (EventUtil.isShiftDown(e))
                    setMessage("GLOBAL MOVE");
                else
                    setMessage("Maintain 'Shift' for global move");
            }

            updateCursor(e.getPoint());
        }
        else if (EventUtil.isRightMouseButton(e))
        {
            // showPopupMenu(pos);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (EventUtil.isLeftMouseButton(e))
        {
            action = actionType.NULL;

            updateCursor(e.getPoint());

            setMessage("");
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        final Point pos = e.getPoint();
        final boolean shift = EventUtil.isShiftDown(e);

        switch (action)
        {
            case MODIFY_LOWBOUND:
                setLowBoundPos(pos.x);
                // also modify others bounds
                if (shift)
                {
                    final double newLowBound = getLowBound();
                    for (LUTBand lb : lutBand.getLut().getLutBands())
                        lb.setMin(newLowBound);
                }
                break;

            case MODIFY_HIGHBOUND:
                setHighBoundPos(pos.x);
                // also modify others bounds
                if (shift)
                {
                    final double newHighBound = getHighBound();
                    for (LUTBand lb : lutBand.getLut().getLutBands())
                        lb.setMax(newHighBound);
                }
                break;
        }

        // message
        if (action != actionType.NULL)
        {
            if (shift)
                setMessage("GLOBAL MOVE");
            else
                setMessage("Maintain 'Shift' for global move");
        }

        if (histoData.length > 0)
            setPositionInfo(pixToData(pos.x), histoData[pixToHisto(pos.x)]);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        final Point pos = e.getPoint();

        updateCursor(e.getPoint());

        if (histoData.length > 0)
            setPositionInfo(pixToData(pos.x), histoData[pixToHisto(pos.x)]);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {

    }

    @Override
    public void lutBandChanged(LUTBandEvent e)
    {
        if (e.getType() == LUTBandEventType.SCALER_CHANGED)
            onScalerChanged();
    }

}