/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.vlet.gui.proxyvrs;

import java.util.List;

import javax.swing.Icon;

import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vlet.gui.UILogger;
import nl.esciencecenter.vlet.gui.data.ResourceRef;
import nl.esciencecenter.vlet.vrs.VDeletable;
import nl.esciencecenter.vlet.vrs.VRS;
import nl.esciencecenter.vlet.vrs.VRenamable;
import nl.esciencecenter.vlet.vrs.data.VAttributeConstants;
import nl.esciencecenter.vlet.vrs.events.ResourceEvent;
import nl.esciencecenter.vlet.vrs.events.ResourceEventListener;

/**
 * A ProxyNode is a the GUI side representation of a VNode or Resource Node.
 * <p>
 * This class is an abstract class for ProxyNode implementations. to allow
 * multiple 'factories' instead of the direct implementation
 * 
 * Current Implementations are ProxyDirectNode and ProxyWSNode
 * 
 * @see ProxyNodeFactory
 * 
 * @author P.T. de Boer
 * 
 */
public abstract class ProxyNode
{
    // === static fields ===
    private static long objectCounter = 0; // for debugging

    public static void fireGlobalEvent(ResourceEvent event)
    {
        ProxyVRSClient.getInstance().fireEvent(event);
    }

    public static ProxyNode getVirtualRoot() throws VrsException
    {
        ProxyVRSClient proxyVrs=ProxyVRSClient.getInstance();
        VRL vrl=proxyVrs.getVirtualRootLocation(); 
        return proxyVrs.getProxyNodeFactory().openLocation(vrl); 
    }

    public static ProxyNode[] toArray(List<ProxyNode> nodes)
    {
        ProxyNode arr[] = new ProxyNode[nodes.size()];
        arr = nodes.toArray(arr);
        return arr;
    }

    public static ProxyNodeFactory getProxyNodeFactory()
    {
        return ProxyVRSClient.getInstance().getProxyNodeFactory();
    }

    public static void disposeClass()
    {
        ProxyVRSClient.getInstance().dispose();
    }

    // ========================================================================
    // === object fields ===
    // ========================================================================

    private long id = -1;

    /** Experimental ref count for smart pointers */
    private int refCount = 0;

    // === constructors ===

    /**
     * Private Constructor, do not use this one, but use static createProxyTNode
     * to ensure cache consistency.
     * 
     * @see createProxyTNode
     */
    protected ProxyNode()
    {
        this.id = objectCounter++;
    }

    public synchronized void increaseReferenceCount()
    {
        this.refCount++;
        debugPrintf(">>> Increased ref count to:%d of %s\n", refCount, this);
    }

    public synchronized void decreaseReferenceCount()
    {
        this.refCount--;
        //Global.debugPrintln(this, ">>> Decreased ref count to:" + refCount + " of:" + this);

        if (this.refCount < 0)
            UILogger.errorPrintf(this, "*** Error: Negative reference count for:%s\n",this);

        if (this.refCount <= 0)
        {
            // DISPOZE !!! (not yet)
            debugPrintf(">>> DISPOSE:%s\n <<<", this);
        }
    }

    public long getID()
    {
        return this.id;
    }

    public void fireEventTo(final ResourceEventListener receiver, final ResourceEvent event)
    {
        ProxyVRSClient.getInstance().getProxyResourceEventNotifier().fireEventToListener(receiver, event);
    }

    public boolean equalsType(ProxyNode node)
    {
        if (node == null)
            return false;

        return StringUtil.equals(this.getType(), node.getType());
    }

    private void debugPrintf(String format, Object... args)
    {
        UILogger.debugPrintf(this, format, args);
    }

    /** Resource Reference. Combination of VRL+Type+(resolved)MimeType */
    public ResourceRef getResourceRef()
    {
        return new ResourceRef(getVRL(), getType(), getMimeType());
    }

    // ========================================================================
    // === Proxy Model Event. Reuses ResourceEvent but will sent them
    // === To ProxyModel Listeners !
    // ========================================================================

    /** Fire Proxy Model Event : Deleted ! */
    public void fireNodeDeleted(ProxyNode node)
    {
        // Create Delete event with this Node as parameter
        ResourceEvent event = ResourceEvent.createDeletedEvent(node.getVRL());
        fireGlobalEvent(event);
    }

    public void fireChildAdded(ProxyNode parent, ProxyNode child)
    {
        fireChildAdded(parent.getVRL(), child.getVRL());
    }

    public void fireChildAdded(VRL parent, VRL child)
    {
        ResourceEvent event = ResourceEvent.createChildAddedEvent(parent, child);
        fireGlobalEvent(event);
    }

    /**
     * Fire rename event.
     * 
     * When a rename has occured the VRL might have changed so both VRLs have to
     * be supplied.
     * 
     * @param oldLocation
     * @param newLocation
     */
    public void fireRenameEvent(VRL oldLocation, VRL newLocation, String name)
    {
        fireGlobalEvent(ResourceEvent.createRenameEvent(oldLocation, newLocation, name));
    }

    public boolean isMyVLe()
    {
        VRL vrl = getVRL();

        if (StringUtil.compare(vrl.getScheme(), VRS.MYVLE_SCHEME) != 0)
            return false;

        // check for empty or root path

        if (StringUtil.isEmpty(vrl.getPath()))
            return true;

        if (StringUtil.compare("/", vrl.getPath()) == 0)
            return true;

        return false;
    }

    /** Creat in this container a new LinkNode to the specified VRL */
    public void createLinkTo(VRL target) throws VrsException
    {
        // need info:
        // ProxyNode
        // pnode=ProxyVRSClient.getInstance().getProxyNodeFactory().openLocation(target);
        // createLinkTo(pnode);
    }

    public Attribute[] getAttributes() throws VrsException
    {
        return getAttributes(getAttributeNames());
    }

    /** get Ordened Attribute set in the specified order */
    public AttributeSet getAttributeSet(String[] names) throws VrsException
    {
        return new AttributeSet(getAttributes(names));
    }

    public AttributeSet getAttributeSet() throws VrsException
    {
        return new AttributeSet(getAttributes());
    }

    /**
     * @deprecated NOT efficient way to get child node.
     */
    public ProxyNode getChild(String basename) throws VrsException
    {
        ProxyNode childs[] = this.getChilds(null);
        if (childs == null)
            return null;

        for (ProxyNode child : childs)
        {
            if (StringUtil.equals(child.getName(), basename))
                return child;
        }

        return null;
    }

    public boolean isComposite()
    {
        return isComposite(true);
    }

    /**
     * If this node represents a Resource Link, return the mimetype of the
     * target resource.
     */
    public String getTargetMimeType() throws VrsException
    {
        Attribute attr = this.getAttribute(VAttributeConstants.ATTR_TARGET_MIMETYPE);
        if (attr != null)
            return attr.getStringValue();
        return null;
    }

    public Attribute getAttribute(String name) throws VrsException
    {
        Attribute attrs[] = getAttributes(new String[]
        { name });

        if ((attrs == null) || (attrs.length <= 0))
            return null;

        return attrs[0];
    }

    public boolean isRenamable()
    {
        return this.instanceOf(VRenamable.class);
    }

    public boolean isDeletable()
    {
        return this.instanceOf(VDeletable.class);
    }
    
    /** 
     * Tries to resolve logical parent location.
     */ 
    public VRL getParentLocation() throws VrsException
    {
        ProxyNode parent=getParent(); 
        VRL vrl=null;
        if (parent!=null)
            vrl=parent.getVRL();

        return vrl;  
    }
   
    // ========================================================================
    // Abstract Interface
    // ========================================================================

    abstract public VRL getVRL();

    /** Return all (alias) VRLs this ProxyNode is equivalent for ! */
    abstract public VRL[] getAliasVRLs();

    // Inspection methods
    abstract public boolean isComposite(boolean resolveLink);

    // ===
    // Logical Resource/Resource Link Methods
    // ===

    /**
     * Return whether this ProxyNode represents a LogicalNode. If this node
     * represent a vlink (or.vrsx) file, it is NOT a LogicalNode only when
     * loaded it is a LogicalNode (like MyVle and ResourceFolders).
     * 
     * @throws VrsException
     */
    abstract public boolean isLogicalNode() throws VrsException;

    /**
     * Whether this ProxyNode represents a Resource Link for example a Server
     * Description or a VLink.
     * 
     * @throws VrsException
     */
    abstract public boolean isResourceLink();

    /**
     * If this node represents a Resource Link, resolve the target and return
     * the new ProxyNode
     */
    abstract public ProxyNode getTargetPNode() throws VrsException;

    /**
     * Java reflection method to check the VRS Class of the wrapped VNode
     */
    abstract public boolean instanceOf(Class<?> classOrInterface);

    /**
     * Returns Target VRL if node is a ResourceLink. Null otherwise.
     */
    abstract public VRL getTargetVRL() throws VrsException;

    abstract public void createLinkTo(ProxyNode pnode) throws VrsException;

    // ===
    // Resource Attribute Interface
    // ===

    abstract public String getName();

    abstract public String getType();

    /** Gets Effective Mime type */
    abstract public String getMimeType();

    abstract public String[] getResourceTypes();

    abstract public Icon getDefaultIcon(int size, boolean isSelected);

    abstract public String[] getAttributeNames();

    abstract public String getIconURL(int size) throws VrsException;

    abstract public Attribute[] getAttributes(String[] attrNames) throws VrsException;

    abstract public void setAttributes(Attribute[] attrs, boolean refresh) throws VrsException;

    abstract public Presentation getPresentation();

    abstract public Attribute[][] getACL() throws VrsException;

    abstract public void setACL(Attribute[][] acl) throws VrsException;

    abstract public Attribute[] getACLEntities() throws VrsException;

    abstract public Attribute[] createACLRecord(Attribute entityAttr, boolean writeThrough) throws VrsException;

    //
    // Logical Tree Stucture + Composite Modification Methods:
    //

    abstract public VRL renameTo(String name, boolean nameIsPath) throws VrsException;

    abstract public ProxyNode getParent() throws VrsException;

    abstract public ProxyNode[] getChilds(nl.esciencecenter.vlet.gui.view.ViewFilter filter) throws VrsException;

    // abstract public int getNrOfChilds(nl.uva.vlet.gui.view.ViewFilter filter)
    // throws VlException;

    abstract public boolean delete(boolean compositeDelete) throws VrsException;

    abstract public ProxyNode create(String resourceType, String newName) throws VrsException;

    //
    // Misc
    //

    abstract public boolean isBusy();

    /**
     * Refresh node. May not throw VlException, goes to background if necessary
     */

    abstract public void refresh();

    // /**@deprecated ProxyTNode implementation uses cached ProxyNodes to check
    // links */
    // abstract public boolean locationEquals(VRL loc, boolean
    // checkLinkTargets);

    abstract public boolean isEditable() throws VrsException;

}
