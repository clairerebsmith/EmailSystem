import java.io.Serializable;
import javafx.beans.property.SimpleStringProperty;

public class Email implements Serializable
{
    private static final long serialVersionUID = 1L;
    SimpleStringProperty from;
    SimpleStringProperty subject;
    SimpleStringProperty body;
    SimpleStringProperty filename;
    Attachment attachment;
    
    public Email(String from, String subject, String body, String filename, Attachment attachment)
    {
        this.from = new SimpleStringProperty(from);
        this.subject = new SimpleStringProperty(subject);
        this.body = new SimpleStringProperty(body);
        this.filename = new SimpleStringProperty(filename);
        this.attachment = attachment;
    }
    
    public String getFrom()
    {
        return from.get();
    }
    
    public String getSubject()
    {
        return subject.get();
    }
    
    public String getBody()
    {
        return body.get();
    }
    public String getFilename()
    {
	return filename.get();
    }
    public Attachment getAttachment()
    {
	return attachment;
    }
}
