import java.io.Serializable;

public class Attachment implements Serializable
{
    private String filename;
    private String filetype;
    private byte[] bytearray;
    
    public Attachment(String filename, String filetype, byte[] bytearray)
    {
	this.filename = filename;
	this.filetype = filetype;
	this.bytearray = bytearray;
    }
    
    public String getFilename()
    {
	return filename;
    }
    public String getFiletype()
    {
	return filetype;
    }
    public byte[] getBytearray()
    {
	return bytearray;
    }
    public void setBytearray(byte[] bytearray)
    {
        this.bytearray = bytearray;
    }
    public void setFilename(String filename)
    {
        this.filename = filename;
    }
}
//