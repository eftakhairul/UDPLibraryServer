package Library;

import UDP.UDPClient;

public class Client {
	public static void main(String args[]) throws InterruptedException {
		String request = "req:create:eftakhairul:islam:rain@gmail.com:12342499:rain:pass123456:van";
		UDPClient uc   =  new UDPClient("localhost", 4001);		
		System.out.println(uc.send(request));

		Thread.sleep(100);		
		uc 		=  new UDPClient("localhost", 4002);
		request = "req:intrese:rain:pass123456:cuda:aa";				
		System.out.println(uc.send(request));
		
		Thread.sleep(100);
		request = null;
		uc      =  new UDPClient("localhost", 4003);
		request = "req:getnon:admin:admin:van:3";
		
		System.out.println(uc.send(request));	
	}
}
