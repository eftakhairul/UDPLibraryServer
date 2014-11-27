package Library;

import java.util.Date;

/**
 * Register Class
 * 
 * @author Md Eftakhairul Islam 
 * Assignment on RMI Java 
 */
public class Register {
	
	public String userName;
	public String bookName;
	public Date date;
	public int dueDay = -14;
	
	
	public Register(String userName, String bookName, Date date){
		this.userName = userName;
		this.bookName = bookName;
		this.date     = date;		
	}
	
	
	/**
	 * Calculate Due Date 
	 */
	public long getDueDays()
	{
		Date currTime  = new Date();
		long startTime = this.date.getTime();;
		long endTime   = currTime.getTime();
		long diffTime  = endTime - startTime;
		long diffDays = diffTime / (1000 * 60 * 60 * 24);
		return (diffDays - dueDay);
	}
}
