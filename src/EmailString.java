import java.io.Serializable;
public class EmailString implements Serializable
{
    String from;
    String subject;
    String body;
    String filename;
    String filetype;
    Attachment attachment;
    
    public EmailString(String from, String subject, String body, String filename, String filetype, Attachment attachment)
    {
        this.from = from;
        this.subject = subject;
        this.body = body;
        this.filename = filename;
        this.filetype = filetype;
        this.attachment = attachment;
    }
    
    public String getFrom()
    {
        return from;
    }
    public String getSubject()
    {
        return subject;
    }
    public String getBody()
    {
        return body;
    }
    public String getFilename()
    {
	return filename;
    }
    public String getFiletype()
    {
        return filetype;
    }
    public Attachment getAttachment()
    {
	return attachment;
    }
    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }
    
}
