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

package nl.nlesc.vlet.gui.panels.resourcetable;

import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/** Generic Popup Menu */ 
public class TablePopupMenu extends JPopupMenu
{
    private static final long serialVersionUID = -4451069363938392675L;

    public TablePopupMenu()
    {
        super(); 
//        JMenuItem item=new JMenuItem("<empty>"); 
//        this.add(item); 
    }
    
    /** Override for context based menu 
     * @param canvasMenu */ 
    public void updateFor(ResourceTable resourceTable, MouseEvent e, boolean canvasMenu)
    {
        
    }
    
}