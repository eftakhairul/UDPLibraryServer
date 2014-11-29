package Library;

import UDP.UDPClient;

public class Cleient {
	public static void main(String args[]) {
		String request = "req:create:eftakhairul:islam:rain@gmail.com:12342499:rain:pass123456:van";
		UDPClient uc =  new UDPClient("localhost", 4000);
		System.out.print(uc.send(request));
	}
}
