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

import icy.gui.viewer.Viewer;
import icy.image.lut.LUT.LUTChannel;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

class LUTChannelViewer extends JPanel
{
    private static final long serialVersionUID = 8709589851405274816L;

    // GUI
    final ScalerPanel scalerPanel;
    final ColormapPanel colormapPanel;

    /**
     * associated Viewer & LUTBand
     */
    protected final Viewer viewer;
    protected final LUTChannel lutChannel;

    public LUTChannelViewer(final Viewer viewer, final LUTChannel lutChannel)
    {
        super();

        this.viewer = viewer;
        this.lutChannel = lutChannel;

        // scaler
        scalerPanel = new ScalerPanel(viewer, lutChannel);
        // colormap
        colormapPanel = new ColormapPanel(viewer, lutChannel);

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(scalerPanel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(colormapPanel);

        setLayout(new BorderLayout());

        add(mainPanel, BorderLayout.CENTER);

        validate();
    }

    public LUTChannel getLutChannel()
    {
        return lutChannel;
    }

    /**
     * @return the scalerPanel
     */
    public ScalerPanel getScalerPanel()
    {
        return scalerPanel;
    }

    /**
     * @return the colormapPanel
     */
    public ColormapPanel getColormapPanel()
    {
        return colormapPanel;
    }
}
