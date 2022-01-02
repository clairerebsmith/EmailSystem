import javafx.application.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageWriter;
import javax.swing.JOptionPane;
import java.io.*;
import java.awt.Desktop;
import java.awt.image.BufferedImage;

public class Phase2Client extends Application
{
    static InetAddress host = null;
    final static int PORT = 1500;
    static Socket socket;
    Scanner input;
    PrintWriter output;
    String filename;
    String filetype;
    
    public static void main(String[] args) throws IOException
    {
        try 
        {
            host = InetAddress.getLocalHost();
            socket = new Socket(host, PORT);
        }
        catch (UnknownHostException uhEx)
        {
            System.out.println("\nHost ID not found!\n");
            System.exit(1);
        }
        do
        {
            launch(args);
        }while(true);
        
    }

    TextField username = new TextField();
    TextField recipient = new TextField();
    TextField subject = new TextField();
    TextArea message = new TextArea();
    TableView<Email> table = new TableView<Email>();
    ObservableList<Email> data = null;
    MediaPlayer player;
    Media sound;
    
    
    public void start(Stage primaryStage) throws Exception
    {
        try
        {
            
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            
        }
        catch(IOException io)
        {
            io.printStackTrace();
            return;
        }
        
        Label usernamePrompt = new Label("Enter username: ");
        Button loginBtn, composeBtn, readBtn, updateBtn, closeBtn;
        
        loginBtn = new Button("Login");
        loginBtn.setOnAction(event->login());
        
        HBox loginPane = new HBox(usernamePrompt, username, loginBtn);
        
        Label inboxLabel = new Label("Inbox");
        HBox inboxTitle = new HBox(inboxLabel);
        
          TableColumn fromCol = new TableColumn("From");
          fromCol.setMinWidth(100);
          fromCol.setCellValueFactory(new PropertyValueFactory<Email, String>("from"));
         
          TableColumn subjectCol = new TableColumn("Subject");
          subjectCol.setMinWidth(100);
          subjectCol.setCellValueFactory(new PropertyValueFactory<Email, String>("subject"));
          
          table.setItems(data);
          table.getColumns().addAll(fromCol, subjectCol);
            
        composeBtn = new Button("Compose");
        composeBtn.setOnAction(event->compose());
        
        readBtn = new Button("Read");
        readBtn.setOnAction(event->read());
        
        updateBtn = new Button("Update");
        updateBtn.setOnAction(event->update());
        
        closeBtn = new Button("Close");
        closeBtn.setOnAction(event->close(primaryStage));
        
        HBox actions = new HBox(composeBtn, readBtn, updateBtn, closeBtn);
        

        
        VBox pane = new VBox(10, loginPane, inboxTitle, table, actions);
        pane.setPadding(new Insets(10, 100, 200, 100)); 

        Scene scene = new Scene(pane, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Home");
        primaryStage.show();
    }
    
    public void login()
    {
        String newUser = username.getText();
        output.println(newUser);
    }
    
    public void compose()
    {
        
        Stage composeStage = new Stage();
        Label recipientPrompt = new Label("To: ");
        Label subjectPrompt = new Label("Subject: ");
        Label messagePrompt = new Label("Message: ");
        Button attachBtn, sendBtn, cancelBtn;
        
        Label attachFilename = new Label("");
       
        attachBtn = new Button("Attach");
        attachBtn.setOnAction(event->attach(composeStage, attachFilename));
        
        
        sendBtn = new Button("Send");
        sendBtn.setOnAction(event->send(composeStage));
        
        cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(event->cancel(composeStage));
        
        HBox recipientLine = new HBox(recipientPrompt, recipient);
        HBox subjectLine = new HBox(subjectPrompt, subject);
        HBox messageLine = new HBox(messagePrompt, message);
        HBox actionLine = new HBox(attachFilename, attachBtn, sendBtn, cancelBtn);
        
        VBox composePane = new VBox(recipientLine, subjectLine, messageLine, actionLine);
        
        Scene composeScene = new Scene(composePane, 500, 500);
        composeStage.setScene(composeScene);
        composeStage.setTitle("Compose message");
        composeStage.show();
        

    }
    
    @SuppressWarnings("unchecked")
    public void update()
    {
        output.println("UPDATE");
        try
        {
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            ArrayList<EmailString> inboxString = new ArrayList<EmailString>();
            inboxString = (ArrayList<EmailString>)is.readObject();
            ArrayList<Email> inbox = new ArrayList<Email>();
            for(EmailString eachEmail:inboxString)
            {
                Email newEmail = new Email(eachEmail.getFrom(), eachEmail.getSubject(), eachEmail.getBody(), eachEmail.getFilename(), eachEmail.attachment);
                inbox.add(newEmail);
            }
            
            ObservableList<Email> odata = null;
            odata = FXCollections.observableArrayList(inbox);
            table.setItems(odata);  
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    public void read()
    {
        Stage readStage = new Stage();
        Email selectedEmail = table.getSelectionModel().getSelectedItem();
        
        Label fromPrompt = new Label("From: ");
        Label fromLabel = new Label(selectedEmail.getFrom());
        HBox fromPane = new HBox(fromPrompt, fromLabel);
        
        Label subjectPrompt = new Label("Subject: ");
        Label subjectLabel = new Label(selectedEmail.getSubject());
        HBox subjectPane = new HBox(subjectPrompt, subjectLabel);
        
        Label bodyPrompt = new Label("Message: ");
        Label bodyLabel = new Label(selectedEmail.getBody());
        HBox bodyPane = new HBox(bodyPrompt, bodyLabel);
        
        Label filenameLabel = new Label("Filename: "); 
        Button openBtn = new Button("Open");
        openBtn.setOnAction(event->openFile(selectedEmail));
        HBox filenamePane = new HBox(filenameLabel, openBtn);
        
        VBox readPane = new VBox(10, fromPane, subjectPane, bodyPane, filenamePane);
        
        Scene read = new Scene(readPane, 500, 300);
        readStage.setScene(read);
        readStage.setTitle("Read message");
        readStage.show();
    }
    public void close(Stage primaryStage)
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        primaryStage.close();
    }
    
    public void attach(Stage stage, Label attachFilename)
    {
        System.out.println(System.getProperty("user.dir"));
        FileChooser fileChooser = new FileChooser();
        Button selectBtn = new Button("Select");
        fileChooser.setTitle("Select file to attach");
        File file = fileChooser.showOpenDialog(stage);
        if(file != null)
        {
            filename = file.getName();
            filetype = getFileExtension(file);
            attachFilename.setText(filename);
        }     
    }
    private static String getFileExtension(File file) 
    {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    public void send(Stage composeStage)
    {
        output.println("SEND");
        String srecipient = recipient.getText();
        String ssubject = subject.getText();
        String smessage = message.getText();
        EmailString newEmail = new EmailString(srecipient, ssubject, smessage, filename, filetype, null);
        try
        {
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeObject(newEmail);
            sendFile(filename, os);
            composeStage.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void cancel(Stage composeStage)
    {
        composeStage.close();
    }
    public void sendFile(String filename, ObjectOutputStream outstream) throws IOException
    {
        FileInputStream fileIn = new FileInputStream(filename);
        long fileLen = (new File(filename)).length();
        int intFileLen = (int) fileLen;
        byte[] byteArray = new byte[intFileLen];
        fileIn.read(byteArray);
        fileIn.close();
        outstream.writeObject(byteArray);
        outstream.flush();
    }
    public void openFile(Email email) 
    {
        Attachment attachment = email.getAttachment();
	switch(attachment.getFiletype())
	{ 
	    case "gif": 
		displayImage(attachment); 
		break;
	    case "mp3":
		playSound(attachment);
		break;
	    case "flv":
		//playVideo(attachment);
		break;
	    case "txt":
		//readText(attachment);
		break;
	default:
	    break;
	}
    }
    public void displayImage(Attachment attachment)
    {
    	Stage imageStage = new Stage();
    	Image image = new Image(new File(attachment.getFilename()).toURI().toString());
    	ImageView imageView = new ImageView(image);
    	imageView.setFitWidth(500);
    	imageView.setFitHeight(300);
    	imageView.setPreserveRatio(true);
    	
    	Button closeBtn = new Button("Close");
    	closeBtn.setOnAction(event->close(imageStage));
    	BorderPane pane = new BorderPane();
    	pane.setCenter(imageView);
    	pane.setBottom(closeBtn);
    	
    	Scene imageScene = new Scene(pane);
    	imageStage.setScene(imageScene);
    	imageStage.show();
    }
    public void playSound(Attachment attachment)
    {
        File file;
        Media music;
        MediaPlayer mediaPlayer;
        file = new File(attachment.getFilename());
        music = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(music);
        mediaPlayer.setAutoPlay(true);
    	Stage soundStage = new Stage();
    	Button play, pause, stop, close;
    	
    	play = new Button("Play");
    	play.setOnAction(event->{if (file != null) player.play();});
    	
    	pause = new Button("Pause");
    	pause.setOnAction(event->{ if (file != null) player.pause();});
    	
    	stop = new Button("Stop");
    	stop.setOnAction(event->{ if (file != null) player.stop();});
    	
    	close = new Button("Close");
    	close.setOnAction(event->cancel(soundStage));
    	
    	HBox playBar = new HBox(play, pause, stop, close);
    	
    	Scene sound = new Scene(playBar);
    	soundStage.setScene(sound);
    	soundStage.show();
    }
    public void playVideo(Attachment attachment)
    {
    	Stage videoStage = new Stage();
    	File file = new File(attachment.getFilename());
    	Media videoToWatch = new Media(file.toURI().toString());
    	MediaPlayer player = new MediaPlayer(videoToWatch);
    	player.setAutoPlay(true);
    	
    	 Button btnPlay, btnPause, btnStop;
    	 
    	 btnPlay = new Button("Play");
    	 btnPlay.setOnAction(event->{ if (file != null) player.play();});
    	 
    	 btnPause = new Button("Pause");
    	 btnPause.setOnAction(event->{ if (file != null) player.pause();});
    	 
    	 btnStop = new Button("Stop");
    	 btnStop.setOnAction(event->{ if (file != null) player.stop();});
    	 
    	 HBox hbox = new HBox(btnPlay, btnPause, btnStop);
    	
    	MediaView viewer = new MediaView(player);
    	viewer.setFitWidth(700);
    	viewer.setFitHeight(3000);
    	viewer.setPreserveRatio(true);
    	
    	StackPane pane = new StackPane(viewer, hbox);
    	Scene video = new Scene(pane);
    	videoStage.setScene(video);
    	videoStage.setTitle(filename);
    	videoStage.show();
    }
    public void readText(Attachment attachment)
    {
	Stage textStage = new Stage(); 
	TextArea text = new TextArea();
	File file = new File(attachment.getFilename());
	
	if (file != null) 
	{
	    Scanner scanner;
	    try
	    {
		scanner = new Scanner(file);
		while(scanner.hasNextLine())
		    {
			text.appendText("\n" + scanner.nextLine());
		    }
	    } catch (FileNotFoundException e)
	    {
		System.out.println("Cant set up scanner to print text.");
	    }
	    
	}	

	Button closeBtn = new Button("CLOSE");
	closeBtn.setOnAction(event->close(textStage));
	BorderPane pane = new BorderPane();
	pane.setCenter(text);
	pane.setBottom(closeBtn);
	
	Scene textFile = new Scene(pane);
	textStage.setScene(textFile);
	textStage.show();
	
    }
    public static String getFileExtension(String fullName) 
    {
    	String fileName = new File(fullName).getName();
    	int dotIndex = fileName.lastIndexOf('.');
    	return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
    
//    public void play()
//    {
//        if(player != null)
//        {
//            player.play();
//        }
//
//    }
//    public void pause()
//    {
//	if(player != null)
//	{
//	    player.pause();
//	}
//    }
//    public void stop()
//    {
//	if(player != null)
//	{
//	    player.stop();
//	}
//    }

}
