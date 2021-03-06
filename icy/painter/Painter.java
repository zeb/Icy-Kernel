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
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.sequence.Sequence;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Painter interface used by IcyCanvas subclasses to forward mouse and key events to sequence
 * painters.
 * 
 * @deprecated Prefer the {@link Overlay} class to this interface when possible.
 * @author Stephane
 */
@Deprecated
public interface Painter
{
    /**
     * Paint method called to draw the painter.
     */
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas);

    /**
     * Mouse press event forwarded to the painter.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas);

    /**
     * Mouse release event forwarded to the painter.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas);

    /**
     * Mouse click event forwarded to the painter.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas);

    /**
     * Mouse move event forwarded to the painter.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas);

    /**
     * Mouse drag event forwarded to the painter.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas);

    /**
     * Key press event forwarded to the painter.
     * 
     * @param e
     *        key event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas);

    /**
     * Key release event forwarded to the painter.
     * 
     * @param e
     *        key event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas);

}
