package nl.nlesc.vlet.vfs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import nl.nlesc.vlet.exception.NotImplementedException;
import nl.nlesc.vlet.exception.VlException;
import nl.nlesc.vlet.exception.VlIOException;
import nl.nlesc.vlet.vrs.io.ResourceWriter;
import nl.nlesc.vlet.vrs.io.VRandomAccessable;
import nl.nlesc.vlet.vrs.io.VStreamWritable;

public class FileWriter extends ResourceWriter
{
    public FileWriter(VFile file)
    {   
        super(file); 
    }
    
    /**
     * Write buffer to (remote) File. An offset can be specified into the file 
     * as well into the buffer. 
     * <p> 
     * Use isRandomAccessable() first to determine whether this file can be
     * randomly written to. <br>
     * <br>
     * 
     * @see VRandomAccessable
     */
    public void write(long offset, byte buffer[], int bufferOffset, int nrOfBytes) throws VlException
    {
        writeBytes(offset,buffer,bufferOffset,nrOfBytes);
    }
    
    /** Actual write method */
    protected void writeBytes(long offset, byte buffer[], int bufferOffset, int nrOfBytes) throws VlException
    {
        // writing as a single stream usually is faster:
        if (offset==0) 
        {
            this.streamWrite(buffer,bufferOffset,nrOfBytes); 
        }
        else if (source instanceof VRandomAccessable)
        {
            try
            {
                ((VRandomAccessable) source).writeBytes(offset, buffer, bufferOffset,
                        nrOfBytes);
            }
            catch (IOException e)
            {
               throw new VlIOException(e); 
            }
        }
        else
        {
            throw new NotImplementedException(
                    "This resource is not Random Accessable (interface VRandomAccessable not implemented):"
                    + this);
        }
    }

    /** Write complete buffer to beginning of file */ 
    public void write(byte buffer[], int bufferOffset,int nrOfBytes) throws VlException
    {
        write(0,buffer,bufferOffset,nrOfBytes);
    }

    /** Write specified number of bytes from buffer to the beginning of the file. */ 
    public void write(byte buffer[],int nrOfBytes) throws VlException
    {
        write(0,buffer,0,nrOfBytes);
    }

    
    /**
     * Set contents using specified String and encoding.
     * 
     * @param contents 
     *            - new String Contents
     * @param encoding 
     *            - charset to use
     * @throws VlException if contents can not be set somehow
     */
    public void setContents(String contents, String encoding) throws VlException
    {
        byte[] bytes;

        try
        {
            bytes = contents.getBytes(encoding);
            setContents(bytes);
            return;
        }
        catch (UnsupportedEncodingException e)
        {
            throw (new VlException("Encoding not supported:" + encoding, e));
        }
    }

    /**
     * Replace or create File contents with data from the bytes array. The new
     * file length will match the byte array lenth thus optionally truncating or
     * extend an existing file.
     */
    public void setContents(byte bytes[]) throws VlException
    {
        this.streamWrite(bytes,0,bytes.length); 
    }

    /**
     * Set contents using specified String. Note that the default encoding
     * format is 'UTF-8'.
     * 
     * @param contents
     *            -  new Contents String
     * @throws VlException
     * @see #setContents(String contents, String encoding) to specify the coding
     */
    public void setContents(String contents) throws VlException
    {
        setContents(contents, "UTF-8");
        return;
    }
   
}
