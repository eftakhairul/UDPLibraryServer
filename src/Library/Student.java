package Library;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Student Class
 * 
 * @author Md Eftakhairul Islam 
 * Assignment on RMI Java 
 */
public class Student {
	
	public String firstName;
	public String lastName; 
	public String emailAddress; 
	public String phoneNumber; 
	public String username; 
	public String password;
	public String educationalInstitude;
	
	
	
	/**
	 * Student constructor	 
	 */
	public Student(String firstName, 
				   String lastName, 
				   String emailAddress, 
				   String phoneNumber, 
				   String username, 
				   String password, 
				   String educationalInstitude) {
		this.firstName       		= firstName;
		this.lastName        		= lastName;
		this.emailAddress    		= emailAddress;
		this.phoneNumber     		= phoneNumber;
		this.username        		= username;
		this.password		 		= password;
		this.educationalInstitude 	= educationalInstitude;		
	}
}
