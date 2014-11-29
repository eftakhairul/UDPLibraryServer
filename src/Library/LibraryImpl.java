package Library;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import UDP.UDPServer;

/*
 *Server Implementation by web service
 * 
 *@author: Eftakahirul Islam <eftakhairul@gmail.com> 
 * 
 */
public class LibraryImpl  implements Runnable {
	/* Institute Name */
	public String instituteName;
	public int institutePort;
	

	/* Global Libraries definition */
	public static Map<String, Book> VanLibrary = Collections.synchronizedMap(new HashMap<String, Book>(1000));
	public static Map<String, Book> ConLibrary = Collections.synchronizedMap(new HashMap<String, Book>(1000));
	public static Map<String, Book> DowLibrary = Collections.synchronizedMap(new HashMap<String, Book>(1000));

	/* Global Student definition */
	public static Map<String, Student> StudentRecord = Collections.synchronizedMap(new HashMap<String, Student>(1000));


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
					StudentRecord.put(username, student);
				}
				
				// Log the operation
				logFile("stuend_create", "Student name: " + student.firstName + " record is created");
				return "true";
			} else {
				logFile("stuend_create", "Student name: " + student.firstName + " record is failed");
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
		Student student 			   = StudentRecord.get(username);
		
		if (student == null) {
			return reserveBookeRecordFlag;
		}
		
		//Just for debug
		logFile("debug", "server: " + student.educationalInstitude);
		
		switch (student.educationalInstitude) {
		
			// Vanier college
			case "van": {
				
					Book book = VanLibrary.get(bookName);
					if (book != null) {
						
						synchronized (book) {
							reserveBookeRecordFlag = book.addBook(student.username)? "true":"false";
						}					
									
				}
			}
			break;
			
			// Concordia University
			case "con": {		
					Book book = ConLibrary.get(bookName);
					if (book != null) {
						
						synchronized (book) {
							reserveBookeRecordFlag = book.addBook(student.username)? "true":"false";
						}			
				}
			}
			break;
			
			// Dawson College
			case "dow": {			
					Book book = DowLibrary.get(bookName);
					if (book != null) {
						synchronized (book) {
							reserveBookeRecordFlag = book.addBook(student.username)? "true":"false";
						}
										
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
//	public boolean reserveInterLibrary(String username, 
//									   String password,
//									   String bookName, 
//									   String authorName) {
//
//		boolean output = reserveBook(username, 
//									 password, 
//									 bookName, 
//									 authorName);
//
//		// if fail, then call to other server by UDP
//		if (output == false) {
//
//			String requestData = "reserve:" + username + ":" + bookName;
//			logFile("debug", "request data: " + requestData);
//			Student student 			   = StudentRecord.get(username);
//			
//			System.out.println(student.educationalInstitude);
//			switch (student.educationalInstitude) {
//			// Vanier college
//			case "van": {
//				boolean liboutput = this.processsUDPRequest(ConLibraryPort,						requestData);
//				if (!liboutput) {
//					output = true;
//					liboutput = this.processsUDPRequest(DowLibraryPort,
//							requestData);
//				}
//			}
//			break;
//
//			// Concordia University
//			case "con": {
//				boolean liboutput = this.processsUDPRequest(VanLibraryPort,
//						requestData);
//				if (!liboutput) {
//					output = true;
//					liboutput = this.processsUDPRequest(DowLibraryPort,
//							requestData);
//				}
//			}
//				break;
//
//			// Dawson College
//			case "dow": {
//				boolean liboutput = this.processsUDPRequest(ConLibraryPort,
//						requestData);
//				if (!liboutput) {
//					output = true;
//					liboutput = this.processsUDPRequest(VanLibraryPort,
//							requestData);
//				}
//			}
//				break;
//
//			default: {
//			}
//				break;
//			}
//		}
//
//		return true;
//	}

	

	public String getNonReturn(String userName, 
							   String password,
							   String educationalInstitude, 
							   int days) {
		String message = null;
		
		if((!userName.equals("admin")) || (!password.equals("admin"))) {
			message = "Sorry!!! You are not admin";
			return message;
		}
		
		message = "Vanier College : ";
		
		if (VanLibrary.size() > 0) {
			for (Book book : VanLibrary.values()) {
				if (book.index != 0) {
					for (int i = 0; i < book.index; i++) {
						Student student = StudentRecord.get(book.assocStudents[i].userName);
						if (book.assocStudents[i].getDueDays() >= days) {
							message += student.firstName + " "
														 + student.lastName + " "
														 + student.phoneNumber + "\n";
						}
					}
				}
			}
		}
	
		message += "\nConcordia College : ";
		if (ConLibrary.size() > 0) {
			for (Book book : ConLibrary.values()) {
				if (book.index != 0) {
					for (int i = 0; i < book.index; i++) {
						Student student = StudentRecord.get(book.assocStudents[i].userName);
						if (book.assocStudents[i].getDueDays() >= days) {
							message += student.firstName + " "
														 + student.lastName + " "
														 + student.phoneNumber + "\n";
						}
					}
				}
			}
		}
		message += "\nDowson College : ";
		if (DowLibrary.size() > 0) {
			for (Book book : DowLibrary.values()) {
				if (book.index != 0) {
					for (int i = 0; i < book.index; i++) {
						Student student = StudentRecord.get(book.assocStudents[i].userName);
						if (book.assocStudents[i].getDueDays() >= days) {
							message += student.firstName + " "
														 + student.lastName + " "
														 + student.phoneNumber + "\n";
						}
					}
				}
			}
		}
		
		return message;
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

	

	
	/**
	* Run Web Server by threading
	*/
	public void run()
	{	
		String response = null;
		UDPServer us  = null;
		try{		
			us = new UDPServer("localhost", this.institutePort);			
			String data = us.recieveRequest();
			String[] requestParts = data.split(":");
			
			System.out.println("Incomeing Request Details: " +Arrays.toString(requestParts));
			
			if(requestParts[1].equals("create")) {
				response = this.createAccount(requestParts[2], requestParts[3], requestParts[4], requestParts[5], requestParts[6], requestParts[7], requestParts[8]);
				
			}else if(requestParts[1].equals("reserv")) {
				response = this.reserveBook(requestParts[2], requestParts[3], requestParts[4], requestParts[5]);				
			}else if(requestParts[1].equals("getnon")) {
				response = this.getNonReturn(requestParts[2], requestParts[3], requestParts[4], Integer.parseInt(requestParts[5]));				
			}	
			
			
			System.out.println("Response Details: " +response);
			us.sendResponse(response);
				
		}catch(Exception err) {
			err.printStackTrace();
		}finally {
			if (us.isNull()) {
				us.close();
			}
				
		}			
	}

	/*
	 * Main Method
	 */
	public static void main(String[] args) {
		
		try {
			// Invoke message for running all UDP Server
			LibraryImpl ls = new LibraryImpl();
			
			
			//Web  service block
			ls = new LibraryImpl();
			ls.instituteName = "van";
			ls.institutePort = 3000;
			
			Thread server1 = new Thread(ls);
			server1.start();
			System.out.println("Van server started at port: "+ls.institutePort);
			
			ls = new LibraryImpl();
			ls.instituteName = "con";
			ls.institutePort = 4000;
			Thread server2 = new Thread(ls);			
			server2.start();
			System.out.println("Van server started at port: "+ls.institutePort);
			
			ls = new LibraryImpl();
			ls.instituteName = "dow";
			ls.institutePort = 5000;
			Thread server3 = new Thread(ls);
			server3.start();
			System.out.println("Van server started at port: "+ls.institutePort);
			
		} catch (Exception e) {
			System.out.println("Exception in servers Startup:" + e);
		}
	}
}
