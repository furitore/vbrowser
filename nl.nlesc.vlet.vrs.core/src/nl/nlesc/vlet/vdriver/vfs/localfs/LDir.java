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

package nl.nlesc.vlet.vdriver.vfs.localfs;

import static nl.nlesc.vlet.data.VAttributeConstants.ATTR_UNIX_FILE_MODE;

import java.io.File;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.nlesc.vlet.data.VAttribute;
import nl.nlesc.vlet.exception.ResourceReadAccessDeniedException;
import nl.nlesc.vlet.exception.VlException;
import nl.nlesc.vlet.tasks.VRSTaskMonitor;
import nl.nlesc.vlet.vfs.VDir;
import nl.nlesc.vlet.vfs.VFSNode;
import nl.nlesc.vlet.vfs.VUnixFileAttributes;
import nl.nlesc.vlet.vrl.VRL;
import nl.nlesc.vlet.vrs.VNode;
import nl.nlesc.vlet.vrs.VRS;


/**
 * Local File System implementation of VDir.
 */
public class LDir extends nl.nlesc.vlet.vfs.VDir implements VUnixFileAttributes
{
    /**
     * The local path into the local filesystem, this MIGHT differ from the URI
     * path !
     */
    private String path = null;

    /** In Java a directory is implemented as a file also */
    private java.io.File _file = null;

    private StatInfo statInf;

    // public boolean ignoreErrors=false;
    private LocalFilesystem localfs;

    // =================================================================
    // Constructors
    // =================================================================

    /**
     * Contructs new local LDir reference (Not the directory itself).
     * 
     * @throws VlException
     * @throws VlException
     */
    private void init(File file) throws VlException
    {
        // under windows: will return windows path
        String path = file.getAbsolutePath();

        //
        // Forward Flip backslashes !
        // Do this ONLY for the local filesystem !
        //

        if (File.separatorChar != VRL.SEP_CHAR)
            path = VRL.uripath(path, true, File.separatorChar);

        // under widows: will convert windows path to URI path !
        setLocation(new VRL(VRS.FILE_SCHEME, null, path));
        this.path = getPath(); // use URI path !

        _file = file;
    }

    public LDir(LocalFilesystem local, String path) throws VlException
    {
        // VRL creation is done in init as well
        super(local, new VRL(VRS.FILE_SCHEME + ":///" + VRL.uripath(path, true, java.io.File.separatorChar)));
        this.localfs = local;
        _file = new java.io.File(path);
        init(_file);
    }

    public LDir(LocalFilesystem local, java.io.File file) throws VlException
    {
        super(local, new VRL(file.toURI()));
        this.localfs = local;
        init(file);
    }

    private StatInfo getStat() throws VlException
    {
        synchronized (_file)
        {
            if (statInf == null)
            {
                statInf = this.localfs.stat(_file);
            }
        }

        return statInf;
    }

    public boolean sync()
    {
        this.statInf = null;
        return true;
    }

    // *** Instance Attributes ***

    /**
     * @throws VlException
     * @see nl.uva.vlet.vfs.localfs.i.VNode#getParent()
     */
    public VDir getParentDir() throws VlException
    {
        // Debug("LDir:Getting parent of:"+_file.getPath());

        String parentpath = null;

        if (_file.getPath().compareTo("/") == 0)
        {
            // Root of root is root. _file.getParent returns NULL otherwise
            // Optional provide root of root as root:
            // path="/";
        }
        else
            parentpath = _file.getParent();

        if (parentpath == null)
            return null;

        return new LDir(localfs, parentpath);
    }

    /** Returns all default attributes names */
    public String[] getAttributeNames()
    {
        String superNames[] = super.getAttributeNames();

        if (localfs.isUnixFS())
        {
            StringList list = new StringList(superNames);
            list.add(LocalFSFactory.unixFSAttributeNames);
            return list.toArray();
        }

        return superNames;
    }

    public VAttribute getAttribute(String name) throws VlException
    {
        if (name == null)
            return null;

        // Check if super class has this attribute
        VAttribute supervalue = super.getAttribute(name);

        // Super class has this attribute, and since I do not overide
        // any attribute, return this one:
        if (supervalue != null)
            return supervalue;

        if (name.compareTo(ATTR_UNIX_FILE_MODE) == 0)
            return new VAttribute(name, Integer.toOctalString(getMode()));

        // return null;
        return null; //
    }

    public long getNrOfNodes()
    {
        if (_file == null)
            return 0;

        String list[] = _file.list();

        if (list != null)
            return list.length;

        return 0;
    }

    public VFSNode[] list() throws VlException
    {
        String list[] = _file.list();

        if (list == null)
        {
            if (isReadable() == false)
                throw new ResourceReadAccessDeniedException("Cannot read path:" + getPath());
            else
                return null; // empty dir
        }

        VFSNode nodes[] = new VFSNode[list.length];

        for (int i = 0; i < list.length; i++)
        {
            java.io.File subFile = new java.io.File(path + VRL.SEP_CHAR + list[i]);

            if (subFile.isDirectory() == true)
            {
                nodes[i] = new LDir(localfs, path + VRL.SEP_CHAR + list[i]);
            }
            else
            {
                nodes[i] = new LFile(localfs, path + VRL.SEP_CHAR + list[i]);
            }
        }

        return nodes;
    }

    // *** Instance Attributes ***

    public boolean exists()
    {
        return _file.isDirectory();
    }

    public boolean isReadable()
    {
        return _file.canRead();
    }

    public boolean isWritable()
    {
        return _file.canWrite();
    }

    public boolean create(boolean ignoreExisting) throws VlException
    {
        VDir dir = this.localfs.createDir(this.path, ignoreExisting);
        return (dir != null);
    }

    public boolean delete(boolean recurse) throws VlException
    {
        // Debug("Deleting local directory:"+this);

        ITaskMonitor monitor = getVRSContext().getTaskWatcher().getCurrentThreadTaskMonitor("Deleting (local) directory:" + this.getPath(), 1);

        boolean result = true;

        // delete children first.
        if (recurse == true)
            this.getVRSContext().getTransferManager().recursiveDeleteDirContents(monitor,this, true); 

        // delete myself
        result = result && _file.delete();

        if (result)
        {
             //* sendNotification(new VRSEvent(this,VRSEvent.NODE_DELETED));
        }
        else
        {
            //Global.warnPrintf(this, "Deletion returned FALSE for:%s\n", this);
        }

        return result;

    }

    public VRL rename(String newname, boolean nameIsPath) throws VlException
    {
        File newFile = localfs.renameTo(this.getPath(), newname, nameIsPath);

        if (newFile != null)
        {
            return new VRL(newFile.toURI());
        }

        return null;
    }

    public boolean delNode(VNode node) throws VlException
    {
        return ((VFSNode) node).delete();
    }

    public boolean isHidden()
    {
        return _file.isHidden();
    }

    /** Must overide isLocal() since a Local Directory is accessable locally ! */
    public boolean isLocal()
    {
        return true;
    }

    public void setMode(int mode) throws VlException
    {
        this.localfs.setMode(getPath(), mode);
        sync();
    }

    // ===
    // Misc.
    // ===

    @Override
    public boolean isSymbolicLink() throws VlException
    {
        return this.getStat().isSoftLink();
    }

    @Override
    public String getSymbolicLinkTarget() throws VlException
    {
        if (isSymbolicLink() == false)
        {
            // not a link
            return null;
        }

        // windows lnk or shortcut (.lnk under *nix is also windows link!)
        /*
         * Directories can not be .lnks if ((Global.isWindows()) ||
         * (getPath().endsWith(".lnk"))) return
         * localfs.getWindowsLinkTarget(this._file); else
         */
        if (localfs.isUnixFS())
            return localfs.getSoftLinkTarget(this.getPath());

        //Global.warnPrintf(this, "getLinkTarget(): could not resolve local filesystem's link:%s\n", this);

        return null;
    }

    public String getGid() throws VlException
    {
        return this.getStat().getGroupName();
    }

    public String getUid() throws VlException
    {
        return this.getStat().getUserName();
    }

    public int getMode() throws VlException
    {
        return this.getStat().getMode();
    }

    public long getModificationTime() throws VlException
    {
        return this.getStat().getModTime();
    }

    public String getPermissionsString() throws VlException
    {
        return this.getStat().getPermissions();
    }
}