/*
 * Copyright 2006-2011 The Virtual Laboratory for e-Science (VL-e) 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache Licence at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * See: http://www.vl-e.nl/ 
 * See: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 * $Id: ResourceTableCellRenderer.java,v 1.1 2013/01/22 15:42:15 piter Exp $  
 * $Date: 2013/01/22 15:42:15 $
 */ 
// source: 

package nl.nlesc.vlet.gui.panels.resourcetable;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import nl.nlesc.vlet.data.VAttribute;

public class ResourceTableCellRenderer extends DefaultTableCellRenderer 
{
    private static final long serialVersionUID = -7461721298242661750L;

    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) 
	{
	    if (value==null)
	        value="?";
	    
	    if (value instanceof VAttribute)
	    {
	        value=((VAttribute)value).getStringValue(); 
	    }
	    
	    // thiz should be *this* 
	    Component thiz = super.getTableCellRendererComponent(table,value, isSelected, hasFocus, row, column); 
	    
		return thiz;    
	}

}