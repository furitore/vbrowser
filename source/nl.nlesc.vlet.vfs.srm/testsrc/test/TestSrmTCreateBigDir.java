/*
 * Copyright 2006-2010 The Virtual Laboratory for e-Science (VL-e) 
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
 * $Id: TestSrmTCreateBigDir.java,v 1.1 2011-10-03 14:40:38 ptdeboer Exp $  
 * $Date: 2011-10-03 14:40:38 $
 */ 
// source: 

package test;


import nl.nlesc.vlet.VletConfig;
import nl.nlesc.vlet.exception.VlException;
import nl.nlesc.vlet.vfs.VFSClient;
import nl.nlesc.vlet.vfs.VFSNode;
import nl.nlesc.vlet.vfs.VFile;
import nl.nlesc.vlet.vfs.srm.SRMFSFactory;
import nl.nlesc.vlet.vrl.VRL;
import nl.nlesc.vlet.vrs.VRS;
import nl.nlesc.vlet.vrs.VRSContext;

public class TestSrmTCreateBigDir
{

	public static void main(String args[])
	{
	    
		try
		{
			VletConfig.init();
			VRS.getRegistry().registerVRSDriverClass(SRMFSFactory.class);
			
		    VRSContext context=new VRSContext(); 
		    VFSClient vfs=new VFSClient(context);
		    
		    VRL dirVrl=new VRL("srm://srm-t.grid.sara.nl:8443/pnfs/grid.sara.nl/data/dteam/ptdeboer/bigdir/"); 
		    
		    if (vfs.existsDir(dirVrl)==false)
		    {
                System.out.println("Creating dir:"+dirVrl); 
		        vfs.mkdirs(dirVrl); 
		    }
		    
		    int numFiles=500; 
		    String filePref="test_file";
		    
		      
            boolean createFiles=false; 
		    
            int start=99; 
            
		    if (createFiles) 
		        for (int i=start;i<numFiles;i++)
		        {  
    		        VRL fileVrl=dirVrl.appendPath(filePref+i); 
    		        
    		        if (vfs.existsFile(fileVrl))
    		        {
                        System.out.println("keeping file:"+fileVrl);
    		        }
    		        else
    		        {
                        System.out.println("creating file:"+fileVrl);
                        VFile result = vfs.createFile(fileVrl,true);
    		        }
	            }
		    
		    
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("=== END ===");
		
	}

	
}