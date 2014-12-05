package Library;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import UDP.UDPClient;
import UDP.UDPMulticastServer;
import UDP.UDPServer;

/*
 *Server Implementation by UDP
 *@author: Eftakahirul Islam <eftakhairul@gmail.com> 
 * 
 */
public class LibraryImpl  implements Runnable {
	/* Institute Name */
	public String instituteName;
	public int institutePort;
	public String frontendHost;
	

	/* Global Libraries definition */
	public static Map<String, Book> VanLibrary = Collections.synchronizedMap(new HashMap<String, Book>(1000));
	public static Map<String, Book> ConLibrary = Collections.synchronizedMap(new HashMap<String, Book>(1000));
	public static Map<String, Book> DowLibrary = Collections.synchronizedMap(new HashMap<String, Book>(1000));

	/* Global Student definition */
	public static Map<String, Student> StudentRecord = Collections.synchronizedMap(new HashMap<String, Student>(1000));

	/* Ports */
	public static int vanport;
	public static int conport;
	public static int dowport;
	

	/*
	 * Creating the student record
	 * 
	 * @param firstName	 
	 * @param lastName
	 * @param emailAddress
	 * @param phoneNumber
	 * @param username
	 * @param password
	 * @param educationalInstitude
	 * @return boolean
	 * @throws RemoteException
	 */	
	public String createAccount(String firstName, 
								 String lastName,
								 String emailAddress, 
								 String phoneNumber, 
								 String username,
								 String password, 
								 String educationalInstitude) {
		
		Student student 		 = new Student(firstName, 
											   lastName, 
											   emailAddress,
											   phoneNumber, 
											   username, 
											   password, 
											   educationalInstitude);
		
		
		
			if (StudentRecord.get(username) == null) {
				
				synchronized (StudentRecord) {
					StudentRecord.put(username.trim(), student);
				}
				
				// Log the operation
				logFile("studend", "Student name: " + student.firstName + " record is created");
				return "true";
			} else {
				logFile("studend", "Student name: " + student.firstName + " record is failed");
				return "false";
			}		
	}

	/*
	 * Reserve a book for a student
	 * 
	 * @param username
	 * @param password
	 * @param bookName
	 * @param authorName
	 * @return boolean
	 * 
	 * @return boolean
	 */	
	public String reserveBook(String username, 
							  String password,
							  String bookName, 
							  String authorName) {
		
		String reserveBookeRecordFlag = "false";
		Student student 			   = StudentRecord.get(username.trim());
		
		if (student == null) {
			System.out.println("inside null");
			return reserveBookeRecordFlag;
		}		
				
		switch (this.instituteName) {
		
			// Vanier college
			case "van": {	
					Book book = VanLibrary.get(bookName.trim());
					if (book != null) {						
						synchronized (book) {
							reserveBookeRecordFlag = book.addBook(student.username)? "true":"false";
						}					
						// Log the operation
						logFile("book", "book: "+ bookName +" is reserved for student: " + student.firstName);			
					}
			}
			break;
			
			// Concordia University
			case "con": {				
					Book book = ConLibrary.get(bookName.trim());
					if (book != null) {						
						synchronized (book) {
							reserveBookeRecordFlag = book.addBook(student.username)? "true":"false";
						}
						// Log the operation
						logFile("book", "book: "+ bookName +" is reserved for student: " + student.firstName);	
					}
			}
			break;
			
			// Dawson College
			case "dow": {				
					Book book = DowLibrary.get(bookName.trim());
					if (book != null) {
						synchronized (book) {
							reserveBookeRecordFlag = book.addBook(student.username)? "true":"false";
						}
					// Log the operation
					logFile("book", "book: "+ bookName +" is reserved for student: " + student.firstName);										
					}
			}
			break;
			
			default: {}
			break;
		}
		
		//return success and failure message
		return reserveBookeRecordFlag;
	}
	
	/*
	 * Reserve a book for a student. 
	 * 
	 * If book is not inserted own library, then send call to other library
	 * by UDP call
	 * 
	 * @param username
	 * @param password
	 * @param bookName
	 * @param authorName
	 * @return boolean
	 * 
	 * @return boolean
	 */	
	public String reserveInterLibrary(String username, 
									   String password,
									   String bookName, 
									   String authorName) {

		String output = reserveBook(username, 
									 password, 
									 bookName, 
									 authorName);
		
		

		//If fail, then call to other server by UDP
		if (output.equals("false")) {

			String requestData = "req:reserv:"+username+":"+password+":"+bookName+":"+authorName;		

			UDPClient ucVan = new UDPClient("localhost", vanport);
			UDPClient ucCon = new UDPClient("localhost", conport);
			UDPClient ucDow = new UDPClient("localhost", dowport);		
			
			System.out.println("Inter request start");
				switch (this.instituteName) {
				// Vanier college
				case "van": {
					String liboutput = ucCon.send(requestData);
					output = liboutput;
					if (liboutput.equals("false")) {						
						output = ucDow.send(requestData);
					}
				}
				break;
	
				// Concordia University
				case "con": {
					String liboutput = ucVan.send(requestData);
					output = liboutput;
					if (liboutput.equals("false")) {						
						output = ucDow.send(requestData);
					}
				}
					break;
	
				// Dawson College
				case "dow": {
					String liboutput = ucVan.send(requestData);
					output = liboutput;
					if (liboutput.equals("false")) {						
						output = ucCon.send(requestData);
					}
				}
					break;
	
				default: {
				}
					break;
				}
			}
		
		return output;
	}

	
	/*
	 * All records return as string
	 * 
	 * @param userName
	 * @param password
	 * @param educationalInstitude
	 * @return message
	 * 
	 * @throws RemoteException
	 */	
	public String getNonReturn(String userName, 
							   String password,
							   String educationalInstitude, 
							   int days) {
		String message = "";
		
		if((!userName.equals("admin")) || (!password.equals("admin"))) {
			message = "Sorry!!! You are not admin";
			return message;
		}		
		message += this.getVan(days);
		message += this.getDow(days);
		message += this.getCon(days);	
		
		return message;
	}
	
	
	public String getVan(int days)
	{
		String message = "";	
		
		if (VanLibrary.size() > 0) {
			for (Book book : VanLibrary.values()) {
				if (book.index != 0) {
					for (int i = 0; i < book.index; i++) {
						Student student = StudentRecord.get(book.assocStudents[i].userName);
						if (book.assocStudents[i].getDueDays() >= days) {
							message += "van:"+ student.firstName+":" + student.lastName+":"+ student.phoneNumber+"::";
						}
					}
					message += "||";
				}
			}
		}
		
		return message;		
	}
	
	
	public String getCon(int days)
	{
		String message = "";	
		
		if (ConLibrary.size() > 0) {
			for (Book book : ConLibrary.values()) {
				if (book.index != 0) {
					for (int i = 0; i < book.index; i++) {
						Student student = StudentRecord.get(book.assocStudents[i].userName);
						if (book.assocStudents[i].getDueDays() >= days) {
							message += "con:"+ student.firstName+":" + student.lastName+":"+ student.phoneNumber+"::";
						}
					}
					message += "||";
				}
			}
		}
		
		return message;		
	}
	
	public String getDow(int days)
	{
		String message = "";	
		
		if (DowLibrary.size() > 0) {
			for (Book book : DowLibrary.values()) {
				if (book.index != 0) {
					for (int i = 0; i < book.index; i++) {
						Student student = StudentRecord.get(book.assocStudents[i].userName);
						if (book.assocStudents[i].getDueDays() >= days) {
							message += "dow:"+ student.firstName+":" + student.lastName+":"+ student.phoneNumber+"::";
						}
					}
					message += "||";
				}
			}
		}
		
		return message;		
	}
	
	public void inerClassRun()
	{
		InterLibUDP interLibUDP = new InterLibUDP();
		interLibUDP.start();
	}
	
	public String processUDPCall(String nam, String request)
	{
		UDPClient uc   =  null;
		String output = null;
		switch (nam) {
		// Vanier college
		case "van": {
			uc = new UDPClient("localhost", LibraryImpl.vanport);	
			output = uc.send(request);
		}			
		break;

		// Concordia University
		case "con": {
			uc = new UDPClient("localhost", LibraryImpl.conport);	
			output = uc.send(request);
		}
			break;

		// Dawson College
		case "dow": {
			uc = new UDPClient("localhost", LibraryImpl.dowport);	
			output = uc.send(request);
		}
			break;

		default: {
		}
			break;
		}
		
		return output;		
	}
	
	class InterLibUDP extends Thread
	{	
		
		public void run()
		{	
			UDPClient uc   =  null;
			UDPMulticastServer udpServer = null;
			try {	
				udpServer = new UDPMulticastServer("225.4.5.6", 6000);	
				
				String response = "";
				
				while(true) {
					uc        =  new UDPClient(LibraryImpl.this.frontendHost, 11000);															
					String data = udpServer.recieveRequest();
					//Server Log
					System.out.println("----------------------------Multicast Server: "+LibraryImpl.this.frontendHost+"-6000 ----------------------------------------------");
					System.out.println("Response Details: " +data);
					
					String[] requestParts = data.split(":");
					
					if(requestParts[1].equals("create")) {
						System.out.println("create executed");
						response = LibraryImpl.this.createAccount(requestParts[2], requestParts[3], requestParts[4], requestParts[5], requestParts[6], requestParts[7], requestParts[requestParts.length -2]);					
					}else if(requestParts[1].equals("reserv")) {
						System.out.println("reserve executed");
						response = LibraryImpl.this.processUDPCall(requestParts[requestParts.length -2], data);					
					}else if(requestParts[1].equals("getnon")) {
						System.out.println("getNOnReturn executed");
						response = LibraryImpl.this.getNonReturn(requestParts[2], requestParts[3], requestParts[4], 3);						
					} else if(requestParts[1].equals("intrese")) {
						System.out.println("inter-library executed");
						response = LibraryImpl.this.processUDPCall(requestParts[requestParts.length -2], data);				
					}
					response += ":4000";
					System.out.println("Frontend response: "+response);
					uc.sendOnly(response);				
				}
			}		
			catch(Exception err) {
				err.printStackTrace();
			}
			finally{
				udpServer.close();
			}
		}
	}
	

	
	/*
	 * logs the activities of servers
	 * 
	 * @return: void
	 * 
	 * @throws: SecurityException
	 */
	public static void logFile(String fileName, String Operation) throws SecurityException {
		fileName = fileName + "_log.txt";
		File log = new File(fileName);
		try {
			if (!log.exists()) {
			}
			log.setWritable(true);
			FileWriter fileWriter = new FileWriter(log, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(Operation);
			bufferedWriter.newLine();
			bufferedWriter.close();
			
		} catch (IOException e) {
			System.out.println("COULD NOT LOG!!");
		}
	}
	
	/*
	 * Raw book creating for all libraries
	 * 
	 * @return: void
	 */
	public static void rawBookEntry()
	{
		Book bookA = new Book("english", "aa", 3);
		Book bookB = new Book("french", "bb", 3);
	
		//Vanlien Library Book insertion
		VanLibrary.put(bookA.bookName, bookA);
		VanLibrary.put(bookB.bookName, bookB);		
		System.out.println("Varnier's books are: "+bookA.bookName+" (3 copies), "+bookB.bookName+" (3 copies). Size: "+ VanLibrary.size());
		
		
		//Concordia Library Book insertion
		Book bookc = new Book("cuda", "nicholas", 2);
		Book bookd = new Book("opencl", "munshi", 3);
		ConLibrary.put(bookc.bookName,bookc);
		ConLibrary.put(bookd.bookName,bookd);		
		System.out.println("Concordia's books are: "+bookc.bookName+" (2 copies), "+bookd.bookName+" (3 copies). Size: "+ ConLibrary.size());
		
		
		//Dowson ULibrary Book insertion
		Book booke = new Book("Bones", "Kathy", 2);
		Book bookf = new Book("4dmath", "Sr. plecher", 3);
		DowLibrary.put(booke.bookName, booke);
		DowLibrary.put(bookf.bookName, bookf);	
		System.out.println("Dowson's books are: "+booke.bookName+" (2 copies), "+bookf.bookName+" (3 copies) Size: "+ DowLibrary.size());
	}

	

	
	/**
	* Run UDP Server by threading
	*/
	public void run()
	{	
		String response = null;
		UDPServer us    = null;
		String data 	= null; 
		try{		
			us 			  = new UDPServer("localhost", this.institutePort);
			while(true) {				
				data 		  = us.recieveRequest();				
				//Server Log
				System.out.println("----------------------------Server: "+this.instituteName+"-"+this.institutePort+"-----------------------------------------------");
				System.out.println("Response Details: " +data);
				
				String[] requestParts = data.split(":");
				
				if(requestParts[1].equals("create")) {
					System.out.println("create executed");
					response = this.createAccount(requestParts[2], requestParts[3], requestParts[4], requestParts[5], requestParts[6], requestParts[7], requestParts[8]);					
				}else if(requestParts[1].equals("reserv")) {
					System.out.println("reserve executed");
					response = this.reserveBook(requestParts[2], requestParts[3], requestParts[4], requestParts[5]);					
				}else if(requestParts[1].equals("getnon")) {
					System.out.println("getNOnReturn executed");
					response = this.getNonReturn(requestParts[2], requestParts[3], requestParts[4], Integer.parseInt(requestParts[5]));
					
				}else if(requestParts[1].equals("replica")) {
					System.out.println("heartbeats executed");
					response = "true";				
				}else if(requestParts[1].equals("intrese")) {
					System.out.println("inter-library executed");
					response = this.reserveInterLibrary(requestParts[2], requestParts[3], requestParts[4], requestParts[5]);				
				} 					
				
				System.out.println("Response Details: " +response);
				us.sendResponse(response);				
			}				
		}catch(Exception err) {
			err.printStackTrace();
		}finally {
			if (us.isNull()) {
				us.close();
			}
				
		}			
	}

	/*
	 * Main Method for loading and running all UDP Servers
	 */
	public static void main(String[] args) {
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream("config.properties");
			 
			// load a properties file
			prop.load(input);
	 
			// get the property value and print it out
			String frontend = prop.getProperty("frontend");		
			
			
			rawBookEntry();
			
			// Invoke message for running all UDP Server
			LibraryImpl ls = null;	
			
			//Web  service block
			ls = new LibraryImpl();
			ls.instituteName 		= "van";
			ls.institutePort 		= 4001;
			LibraryImpl.vanport 	= ls.institutePort;
			
			ls.frontendHost = frontend;
			ls.inerClassRun();
			System.out.println("Multicast server: "+ls.frontendHost+" started at port: 6000");
			
			Thread server1 = new Thread(ls);
			server1.start();
			System.out.println("Van server started at port: "+ls.institutePort);
			
			ls = new LibraryImpl();
			ls.instituteName 	= "con";
			ls.institutePort 	= 4002;
			LibraryImpl.conport = ls.institutePort;
			Thread server2 		= new Thread(ls);			
			server2.start();
			System.out.println("con server started at port: "+ls.institutePort);
			
			ls = new LibraryImpl();
			ls.instituteName 	= "dow";
			ls.institutePort 	= 4003;
			LibraryImpl.dowport = ls.institutePort;
			Thread server3 		= new Thread(ls);
			server3.start();
			System.out.println("dow server started at port: "+ls.institutePort);
			
		} catch (IOException io) {
			io.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception in servers Startup:" + e);
		}finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
