/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part of the AIBench Project. 

AIBench Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

AIBench Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with AIBench Project.  If not, see <http://www.gnu.org/licenses/>.
*/

/*  
 * TextAreaAppender.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class TextAreaAppender extends AppenderSkeleton {
    private static Font DEBUGFont = new Font("monospaced", Font.BOLD, 12);
    private static Color DEBUGColor = Color.BLUE;
    private static Font INFOFont = new Font("monospaced", Font.BOLD, 12);
    private static Color INFOColor = Color.GREEN;
    private static Font WARNFont = new Font("monospaced", Font.BOLD, 12);
    private static Color WARNColor = new Color(255, 153, 51);
    private static Font ERRORFont = new Font("monospaced", Font.BOLD, 12);
    private static Color ERRORColor = Color.RED;
    private static Font FATALFont = new Font("monospaced", Font.BOLD, 12);
    private static Color FATALColor = Color.RED;
    private static Font defaultFont = new Font("monospaced", Font.BOLD, 12);
    private static Color defaultColor = Color.BLACK;
    private static LogTextArea _textArea;

    public static int MAXSIZE=-1;
    
    static{
    
    	_textArea = new LogTextArea();
    	/*JFrame frame = new JFrame();
    	frame.setLayout(new BorderLayout());
    	frame.setSize(500,500);
    	frame.add(_textArea);
    	frame.setVisible(true);*/
    }
    /**
     * Gives you the GUI component in which the log messages appear.
     * @return The GUI component.
     */
    public static JComponent getGUIComponent() {
    	
        return _textArea;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#close()
     */
    public void close() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    public boolean requiresLayout() {
        // TODO Auto-generated method stub
        return true;
    }

    protected void append(LoggingEvent arg0) {
        JTextPane pane = (JTextPane) _textArea.jTextPane;
       
        Font f = defaultFont;
        Color c = defaultColor;

        if (pane != null) {
        	
            this.layout.format(arg0);

            if (arg0.getLevel().equals(Level.DEBUG)) {
                f = DEBUGFont;
                c = DEBUGColor;
            } else if (arg0.getLevel().equals(Level.INFO)) {
                f = INFOFont;
                c = INFOColor;
            } else if (arg0.getLevel().equals(Level.WARN)) {
                f = WARNFont;
                c = WARNColor;
            } else if (arg0.getLevel().equals(Level.ERROR)) {
                f = ERRORFont;
                c = ERRORColor;
            } else if (arg0.getLevel().equals(Level.FATAL)) {
                f = FATALFont;
                c = FATALColor;
            }

            this.appendText(this.getLayout().format(arg0), f, c);
        }
    }

    private void appendText(String text, Font font, Color c) {
        JTextPane pane = (JTextPane) _textArea.jTextPane;
        Document d = pane.getDocument();
        int oldPosition = d.getEndPosition().getOffset();

        try {
            d.insertString(oldPosition, text, null);

            MutableAttributeSet attrs = pane.getInputAttributes();
            StyleConstants.setFontFamily(attrs, font.getFamily());
            StyleConstants.setFontSize(attrs, font.getSize());
            StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
            StyleConstants.setForeground(attrs, c);

            StyledDocument doc = pane.getStyledDocument();
            doc.setCharacterAttributes(oldPosition, doc.getLength() + 1, attrs,
                false);
            pane.setCaretPosition(pane.getDocument().getLength());
            
            if (MAXSIZE>0 && d.getLength()>MAXSIZE){
            	
    			d.remove(0, d.getLength()-MAXSIZE);
    			
            }
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
	 * @author   Rub�n Dom�nguez Carbajales 13-feb-2006 - 2006
	 */
    private static class LogTextArea extends JPanel {
        
    	private static final long serialVersionUID = 1L;
		private JScrollPane jScrollPane = null;
        private JTextPane jTextPane = null;
        

        /**
             * This is the default constructor
             */
        public LogTextArea() {
            super();
            initialize();        
        }

        

        /**
		 * This method initializes jScrollPane
		 * @return   javax.swing.JScrollPane
		 */
        private JScrollPane getJScrollPane() {
            if (jScrollPane == null) {
                jScrollPane = new JScrollPane();
                jScrollPane.setViewportView(getJTextPane());
            }

            return jScrollPane;
        }

        /**
		 * This method initializes jTextPane
		 * @return   javax.swing.JTextPane
		 */
        private JTextPane getJTextPane() {
            if (jTextPane == null) {
                jTextPane = new JTextPane();
                jTextPane.setBackground(Color.LIGHT_GRAY);
                jTextPane.setEditable(false);
                jTextPane.setBackground(Color.BLACK);
            }

            return jTextPane;
        }

      

        /**
             * This method initializes this
             *
             * @return void
             */
        private void initialize() {
            this.setLayout(new BorderLayout());
            this.setSize(100, 200);
            
            //this.add(getToolBar(), java.awt.BorderLayout.NORTH);
            this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        }
    }
}
