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
 * $Id: ServerConfigController.java,v 1.1 2013/01/22 15:48:05 piter Exp $  
 * $Date: 2013/01/22 15:48:05 $
 */ 
// source: 

package nl.nlesc.vlet.gui.editors;

import nl.nlesc.vlet.data.VAttribute;
import nl.nlesc.vlet.gui.panels.attribute.AttributePanelListener;


/** Dedicated Controller for the Sever Configuration Panel */ 
public class ServerConfigController implements AttributePanelListener
{
	private ResourceEditor editorPanel;

	private ResourceEditorController editorController;

	public ServerConfigController(ResourceEditor panel,ResourceEditorController controller)
	{
		this.editorPanel=panel; 
		
		this.editorController=controller; 
	}

	public void notifyAttributeChanged(VAttribute attr) 
	{
		// TODO Auto-generated method stub
		
	}



}