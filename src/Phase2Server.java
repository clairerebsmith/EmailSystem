  import java.io.*;
  import java.net.*;
  import java.util.*;
  import java.sql.*;
  
public class Phase2Server
{
    
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = null;
        final int PORT = 1500;
        Socket client;
        ClientHandler handler;
        Connection connection = null;
        Statement statement = null;
        ResultSet results = null;
        ResultSet newUser = null;
        
        try
        {
            serverSocket = new ServerSocket(PORT);
        }
        catch (IOException ioEX)
        {
            System.out.println("\nUnable to set up port");
            System.exit(1);
        }
        try
        {
            connection = DriverManager.getConnection("jdbc:mysql://homepages.shu.ac.uk:3306/b6008661_db3", "b6008661", "claire1");
        }
        catch(SQLException sqlEx)
        {
            System.out.println("\nCannot connect to database! ");
            System.exit(1);
        }
        System.out.println("\nServer running...\n");
        
        do
        {
            client = serverSocket.accept();
            Scanner inputScanner = new Scanner(client.getInputStream());
            String username = inputScanner.nextLine();

            try
            {
                statement = connection.createStatement();
                results = statement.executeQuery("SELECT * FROM users");
            }
            catch (SQLException e)
            {
                System.out.println("Cannot execute query");
            }
            System.out.println(username);
                try
                {
                    while(results.next())
                    {
                        if(!results.getString("name").equals(username))
                        {
                            Statement newStatement = connection.createStatement();
                            int result = newStatement.executeUpdate("INSERT INTO users(name) VALUES ('" + username + "')");
                            System.out.println(result);
                            break;
                        }
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            handler = new ClientHandler(client, username);
            System.out.println("\nNew client accepted. \n");
            handler.start();
            
        } while(true);

    }
    
    static class ClientHandler extends Thread
    {
        private Socket client;
        private String username;
        private Scanner input;
        private Connection connection = null;
        private Statement statement = null;
        private ResultSet results = null;
        
        public ClientHandler(Socket socket, String username) throws IOException 
        {
            client = socket;
            this.username = username;
            input = new Scanner(client.getInputStream());
            try
            {
                connection = DriverManager.getConnection("jdbc:mysql://homepages.shu.ac.uk:3306/b6008661_db3", "b6008661", "claire1");
                System.out.println("Connected!");
            }
            catch (SQLException e)
            {
                System.out.println("\nCannot connect to database");
            }
        }
        
        public void run()
        {
            String recieved;
            recieved = input.nextLine();
           System.out.println("Waiting....");
            
            while(!recieved.equals("QUIT"))
            {
                switch(recieved)
                {
                    case "UPDATE": 
                        ObjectOutputStream os = null;
                        try
                        {
                            statement = connection.createStatement();
                            results = statement.executeQuery("SELECT * FROM messages WHERE recipient = '" + username + "'");
                            os = new ObjectOutputStream(client.getOutputStream());
                            EmailString email;
                            ArrayList<EmailString> inbox = new ArrayList<EmailString>();
                            while(results.next())
                            {
                                String from = results.getString("sender");
                                String subject = results.getString("subject");
                                String body = results.getString("body");
                                String filename = results.getString("fileName");
                                String filetype = results.getString("fileType");
                                Attachment attach = new Attachment(filename, filetype, results.getBytes("byteArray"));
                                email = new EmailString(from, subject, body, filename, filetype, attach); 
                                inbox.add(email);
                            }
                            System.out.println("Inbox created");
                            os.writeObject(inbox);
                            System.out.println("Inbox sent.");
                        }
                        catch(SQLException e)
                        {
                            System.out.println("Error retrieving data!");
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        
                        break;
                    case "SEND":
                        System.out.println("SEND");
                        ObjectInputStream is = null;
                        try
                        {
                            statement = connection.createStatement();
                        }
                        catch (SQLException e2)
                        {
                            e2.printStackTrace();
                        }
                        try
                        {
                            is = new ObjectInputStream(client.getInputStream());
                            System.out.println("Input stream set up.");
                        }
                        catch (IOException e1)
                        {
                            e1.printStackTrace();
                            System.out.print("Input stream not set up.");
                        }
                        EmailString newEmail = null;
                        byte[] bytearray = null;
                        try
                        {
                            
                            newEmail = (EmailString)is.readObject();
                            bytearray = (byte[])is.readObject();
                            System.out.println("Email has been read.");
//                            newEmail.attachment.setBytearray(bytearray);
//                            newEmail.attachment.setFilename(newEmail.getFilename());
                            Attachment attach = new Attachment(newEmail.getFilename(), newEmail.getFiletype(), bytearray);
                            newEmail.setAttachment(attach);
                            System.out.println("Attachment set up");
                            
                        }
                        catch (ClassNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                       
                        try
                        {
                            //insert file/byte array into table too 
                            String insert = "INSERT INTO messages(sender, recipient, subject, body, fileType, fileName, byteArray) "
                            	+ "VALUES ('" + username + "', '" + newEmail.getFrom() + "', '" + newEmail.getSubject() + "', '" 
                        	    + newEmail.getBody() + "', '" + newEmail.attachment.getFiletype() + "', '" + newEmail.getFilename() + "', '" + 
                            		newEmail.attachment.getBytearray() +"')";
                            System.out.println(insert);
                            int result = statement.executeUpdate(insert);
                            System.out.println(result);
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                        System.out.println("Message sent");
                        break;
                        
                    case "CLOSE":
                	try
                        {
                            client.close();
                        }
                        catch (IOException e)
                	{
                            e.printStackTrace();
                        }
                        break;
                }
                
                recieved = input.nextLine();
            }
            
        }
        
    }

}
