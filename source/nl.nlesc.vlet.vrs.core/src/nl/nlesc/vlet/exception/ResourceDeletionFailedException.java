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

package nl.nlesc.vlet.exception;

public class ResourceDeletionFailedException extends ResourceException
{
    private static final long serialVersionUID = -5697916428807131886L;

    public ResourceDeletionFailedException(String message)
    {
        super(ExceptionStrings.RESOURCE_DELETION_FAILED, message);
    }

    /** Constructor which keeps original System Exception */
    public ResourceDeletionFailedException(String message, Throwable e)
    {
        super(ExceptionStrings.RESOURCE_DELETION_FAILED, message, e);
    }

}