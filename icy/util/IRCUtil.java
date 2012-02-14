/**
 * 
 */
package icy.util;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * IRC utilities class.
 * 
 * @author Stephane
 */
public class IRCUtil
{
    public static class IRCAttribute
    {
        final static int UNKNOWN = -1;
        final static int NORMAL = 0;
        final static int BOLD = 1;
        final static int ITALIC = 2;
        final static int UNDERLINE = 4;

        final static int COLOR = 9;

        int type;
        int size;
        int arg1;
        int arg2;

        public IRCAttribute()
        {
            super();

            type = NORMAL;
            size = 1;
            arg1 = -1;
            arg2 = -1;
        }
    }

    public static class IRCAttributeSet
    {
        int style;
        Color foreground;
        Color background;

        public IRCAttributeSet()
        {
            super();

            // default
            style = IRCAttribute.NORMAL;
            foreground = null;
            background = null;
        }
    }

    /**
     * Insert the specified IRC string into specified Document.
     * 
     * @param ircString
     *        IRC string containing IRC code.
     * @param doc
     *        doc when we want to insert the IRC styled string.
     * @param defaultAttributes
     *        default string attributes.
     * @throws BadLocationException
     */
    public static void insertString(String ircString, Document doc, SimpleAttributeSet defaultAttributes)
            throws BadLocationException
    {
        final IRCAttributeSet state = new IRCAttributeSet();
        SimpleAttributeSet attr = new SimpleAttributeSet(defaultAttributes);

        final int len = ircString.length();
        int curInd = 0;

        while (curInd < len)
        {
            int ctrlIndex = StringUtil.getNextCtrlCharIndex(ircString, curInd);
            // end of line
            if (ctrlIndex == -1)
                ctrlIndex = len;

            if (curInd != ctrlIndex)
                // insert styled string
                doc.insertString(doc.getLength(), ircString.substring(curInd, ctrlIndex), attr);

            // end of string --> terminate
            if (ctrlIndex >= len)
                break;

            // get IRC attribute
            final IRCAttribute ircAttr = getAttribute(ircString, ctrlIndex);

            // not an IRC attribute --> insert control character in document
            if (ircAttr.type == IRCAttribute.UNKNOWN)
                doc.insertString(doc.getLength(), ircString.substring(ctrlIndex, ctrlIndex + 1), attr);
            else
            {
                // apply attribute to current state
                applyAttribute(state, ircAttr);
                // create attributes from default attributes and current IRC attribute state
                attr = createAttributeSet(defaultAttributes, state);
            }

            // next...
            curInd = ctrlIndex + ircAttr.size;
        }
    }

    /**
     * Return the IRC attribute corresponding to the control code at specified index.
     */
    public static IRCAttribute getAttribute(String ircString, int index)
    {
        final IRCAttribute result = new IRCAttribute();
        final int len = ircString.length();

        // no more text
        if (index >= len)
            return null;

        int offset = index + 1;
        int end;

        switch (ircString.charAt(index))
        {
            case 15:
                // reset default
                result.type = IRCAttribute.NORMAL;
                break;

            case 2:
                // switch bold
                result.type = IRCAttribute.BOLD;
                break;

            case 22:
                // switch italic
                result.type = IRCAttribute.ITALIC;
                break;

            case 31:
                // switch underline
                result.type = IRCAttribute.UNDERLINE;
                break;

            case 3:
                // color
                end = StringUtil.getNextNonDigitCharIndex(ircString, offset);
                // no more than 2 digits to encode color
                if ((end == -1) || (end > (offset + 2)))
                    end = Math.min(len, offset + 2);

                // color info ?
                if (end != offset)
                {
                    // get foreground color
                    result.arg1 = Integer.parseInt(ircString.substring(offset, end));

                    // update position
                    offset = end;

                    // search if we have background color
                    if ((offset < len) && (ircString.charAt(offset) == ','))
                    {
                        offset++;

                        end = StringUtil.getNextNonDigitCharIndex(ircString, offset);
                        // no more than 2 digits to encode color
                        if ((end == -1) || (end > (offset + 2)))
                            end = Math.min(len, offset + 2);

                        // get background color
                        if (end != offset)
                            result.arg2 = Integer.parseInt(ircString.substring(offset, end));
                    }
                }

                result.size = end - index;
                break;

            default:
                // unknown
                result.type = IRCAttribute.UNKNOWN;
//                System.out.println("code " + Integer.toString(ircString.charAt(index)));
                break;
        }

        return result;
    }

    /**
     * Apply the specified IRC attribute on specified IRC attributes set.<br>
     * Some IRC attribute work as switch :<br>
     * If bold is already set and you apply bold again, then bold is removed from set.
     * 
     * @param set
     *        IRC attributes set.
     * @param attr
     *        IRC single attribute.
     */
    public static void applyAttribute(IRCAttributeSet set, IRCAttribute attr)
    {
        switch (attr.type)
        {
            case IRCAttribute.NORMAL:
                // reset attribute
                set.style = IRCAttribute.NORMAL;
                set.foreground = null;
                set.background = null;
                break;

            case IRCAttribute.BOLD:
            case IRCAttribute.ITALIC:
            case IRCAttribute.UNDERLINE:
                // switch-able attribute
                set.style ^= attr.type;
                break;

            case IRCAttribute.COLOR:
                // no color information
                if (attr.arg1 == -1)
                {
                    // reset
                    set.foreground = null;
                    set.background = null;
                }
                else
                {
                    set.foreground = getIRCColor(attr.arg1);
                    // background color info ?
                    if (attr.arg2 != -1)
                        set.background = getIRCColor(attr.arg2);
                }
                break;
        }
    }

    /**
     * Return a new AttributeSet from the given default set and IRC attributes.
     * 
     * @param defaultAttributes
     *        default Attribute Set we use as base attributes.
     * @param ircAttributes
     *        IRC attributes used to modifying default attributes.
     */
    public static SimpleAttributeSet createAttributeSet(SimpleAttributeSet defaultAttributes,
            IRCAttributeSet ircAttributes)
    {
        final SimpleAttributeSet result = new SimpleAttributeSet(defaultAttributes);

        if ((ircAttributes.style & IRCAttribute.BOLD) != 0)
            StyleConstants.setBold(result, true);
        if ((ircAttributes.style & IRCAttribute.ITALIC) != 0)
            StyleConstants.setItalic(result, true);
        if ((ircAttributes.style & IRCAttribute.UNDERLINE) != 0)
            StyleConstants.setUnderline(result, true);

        if (ircAttributes.foreground != null)
            StyleConstants.setForeground(result, ircAttributes.foreground);
        if (ircAttributes.background != null)
            StyleConstants.setBackground(result, ircAttributes.background);

        return result;
    }

    /**
     * Return the color corresponding to the specified IRC color code.
     */
    public static Color getIRCColor(int num)
    {
        switch (num)
        {
            case 0:
            case 16:
                // white
                return Color.white;
            case 1:
                return Color.black;
            case 2:
                return Color.blue;
            case 3:
                return Color.green;
            case 4:
                return Color.red;
            case 5:
                // brown
                return new Color(0x8b4513);
            case 6:
                // purple
                return new Color(0xa020f0);
            case 7:
                return Color.orange;
            case 8:
                return Color.yellow;
            case 9:
                // light green
                return new Color(0x80ff00);
            case 10:
                return Color.cyan;
            case 11:
                // light cyan
                return new Color(0x80ffff);
            case 12:
                // light blue
                return new Color(0x8080ff);
            case 13:
                return Color.pink;
            case 14:
                return Color.darkGray;
            case 15:
                return Color.lightGray;
            default:
                // transparent
                return new Color(0, true);
        }
    }

}
