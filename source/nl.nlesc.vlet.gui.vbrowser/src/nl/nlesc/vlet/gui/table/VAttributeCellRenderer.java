/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.nlesc.vlet.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import nl.esciencecenter.vbrowser.vrs.ui.presentation.UIPresentable;
import nl.esciencecenter.vbrowser.vrs.ui.presentation.UIPresentation;
import nl.nlesc.vlet.gui.Messages;
import nl.nlesc.vlet.gui.icons.LabelIcon;
import nl.nlesc.vlet.vrs.data.VAttribute;
import nl.nlesc.vlet.vrs.data.VAttributeConstants;
import nl.nlesc.vlet.vrs.data.VAttributeType;

public class VAttributeCellRenderer implements TableCellRenderer
{
    private static int default_text_alignment=JLabel.LEFT;
    private static int default_icon_alignment=JLabel.CENTER;
    
    // Note that the DefaultTableCellRenderer is an optimized JLabel class for 
    // rubber stamp cell rendering ! 
    
    private DefaultTableCellRenderer defaultRenderer=new DefaultTableCellRenderer(); 
   // private EnumCellRenderer enumCellRenderer=new EnumCellRenderer(); 
    private JLabel iconLabelRenderer=new DefaultTableCellRenderer();
    private JLabel vrlLabelRenderer=new DefaultTableCellRenderer();
     //private JLabel textLabelRenderer=new DefaultTableCellRenderer();
    //private JLabel nullRenderer=new DefaultTableCellRenderer(); 
    private JTable tablePanel;

    
    public VAttributeCellRenderer(JTable panel)
    {
        this.tablePanel=panel;
        
        //textLabelRenderer.setHorizontalAlignment(default_text_alignment);
        //nullRenderer.setHorizontalAlignment(default_text_alignment);
        iconLabelRenderer.setHorizontalAlignment(default_icon_alignment);
        //nullRenderer.setText("<?>");
        
        iconLabelRenderer.setBackground(Color.WHITE);
        vrlLabelRenderer.setToolTipText(Messages.TT_CLICK_ON_VRL);
    }
    
    
   
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column)
    {
        //int shade=255-10*(row%2);
        //textLabelRenderer.setBackground(new Color(shade,shade,255)); 
        //iconLabelRenderer.setBackground(new Color(shade,shade,255)); 
        //defaultRenderer.setBackground(new Color(shade,shade,255)); 
        
        if (value==null)
        {
            String strval="?"; 
            //textLabelRenderer.setText(strval); 
            //return textLabelRenderer; 
            // use default renderer
             return defaultRenderer.getTableCellRendererComponent(table,strval,isSelected,hasFocus,row,column);
 
        }
        else if (value instanceof Icon)
        {
            iconLabelRenderer.setIcon((Icon)value);
            
            if (isSelected)
                iconLabelRenderer.setBackground(this.tablePanel.getSelectionBackground());
            else
                iconLabelRenderer.setBackground(this.tablePanel.getBackground());

            
            return iconLabelRenderer; 
        }
        else if (value instanceof LabelIcon)
        {
            //((ButtonLabelIcon)value).setBackground(new Color(shade,shade,255)); 
            
            // ButtonLabelIcon can render itself ! 
            return (LabelIcon)value;
        }
        else if (value instanceof VAttribute)
        {
            VAttribute attr=(VAttribute)value;
            String strval=attr.getStringValue();
            
            UIPresentation pres=null; 
            
            if (tablePanel instanceof UIPresentable)
            	pres=((UIPresentable)tablePanel).getPresentation();
            
            if (pres==null) 
            {
                pres=UIPresentation.createDefault(); 
            }
            
            // When enabling the EnumCellRenderer, enum type
            // will always apear as a combobox. 
            // Now only when the Cell is being edited
            // the EnumCellEditor will appear (and a combobox is shown). 
            
            //if (attr.getType()==VAttributeType.ENUM)
            //{   
            //    this.enumCellRenderer.set(attr); 
            //    return enumCellRenderer; 
            //}
            
            // create default JLabel render: 
            Component renderer = defaultRenderer.getTableCellRendererComponent(table,strval,isSelected,hasFocus,row,column);
            
            // update some fields:
            
            if (renderer instanceof JLabel)
            {
                JLabel label=(JLabel)renderer;
                label.setToolTipText(attr.getStringValue());
                 
                if (attr.getType()==VAttributeType.VRL)
                {
                    label.setText("<html><u>"+attr.getValue()+"</u></html>");
                    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                else if (attr.getName().compareToIgnoreCase(VAttributeConstants.ATTR_NAME)==0)
                {
                    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));   
                }
                else if (attr.getName().compareToIgnoreCase(VAttributeConstants.ATTR_ICON)==0)
                {
                    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));   
                }
                
                //if ((hasFocus) && (attr.getName().compareToIgnoreCase(VAttributeConstants.ATTR_NAME)==0))
                //{
                //    label.setText("<html><u>"+attr.getValue()+"</u></html>");
                //}
               else if (attr.getType()==VAttributeType.TIME)
               {
                   label.setText(UIPresentation.relativeTimeString(attr.getDateValue()));  
               }
               else if (attr.getName()==VAttributeConstants.ATTR_LENGTH)
               {
                   label.setText(pres.sizeString(attr.getLongValue())); 
               }
               else if ((strval!=null) && (strval.length()>UIPresentation.getBigStringSize()))
               {
                   // add dialog icon to string ?:
                   //label.setIcon(GuiSettings.getIcon"get());
                   label.setText("<html>"+strval+"</html>");
               }

            }
            
            return renderer;
            
        }
        else //whatever...
        {
            return defaultRenderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        }
        
    }
    
    public void setValue(Object value) 
    {
        // must ignore setValue since this renderer is SHARED amongst table cells!
        return;
        
        /*
        
        if (value==null)
        {
            
        }
        else if (value instanceof String)
        {
            setHorizontalAlignment(default_text_alignment);
            setText((String)value);
        }
        else if (value instanceof Icon)
        {
            setHorizontalAlignment(default_icon_alignment);
            this.setIcon((Icon)value);
        }
        else if (value instanceof ButtonLabelIcon)
        {
            //setHorizontalAlignment(default_icon_alignment);
            //this.setIcon((Icon)value);
        }
        else if (value instanceof VAttribute)
        {
            setText((String)value);; // nothing...
        }
        else //whatever
        {
            super.setValue(value);
        } */


    }
}
