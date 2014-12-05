package Library;

import UDP.UDPClient;

public class Client {
	public static void main(String args[]) throws InterruptedException {
		String request = "req:create:eftakhairul:islam:rain@gmail.com:12342499:rain:pass123456:van";
		UDPClient uc   =  new UDPClient("localhost", 2000);		
		uc.sendOnly(request);
		
		
		Thread.sleep(100);		
		uc 		=  new UDPClient("localhost", 2000);
		request = "req:intrese:rain:pass123456:cuda:aa";				
		uc.sendOnly(request);
		
		Thread.sleep(100);
		request = null;
		uc      =  new UDPClient("localhost", 2000);
		request = "req:getnon:admin:admin:van:3";
		uc.sendOnly(request);
		
		Thread.sleep(100);	
		uc      =  new UDPClient("localhost", 4001);
		request = "req:replica";		
		System.out.println(uc.send(request));	
	}
}
