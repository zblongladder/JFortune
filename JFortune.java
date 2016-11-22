import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import rsa.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.math.*;
public class JFortune{
    //a Java Fortune client
    /*James Buchanan && Kwandwo Eck
     */
    int port;
    RSA myRSA = new RSA();
    InetAddress host;
    public JFortune(int port, String host){
	this.port = port;
	try{
	    this.host = InetAddress.getByName(host);
	}
	catch(Exception e){
	    throw new RuntimeException(e);
	}
    }
    static void print(Object o){
	System.out.print(o+"\n");
    }
    public String getFortune(){
	try{
	    Socket s = new Socket(host,port);
	    Scanner socketIn = new Scanner(s.getInputStream());
	    PrintWriter socketOut = new PrintWriter(s.getOutputStream(),true);
	    Key myPubKey = myRSA.getPublicKey();
	    socketOut.println(myPubKey.getExponent());
	    print("Public exponent sent\n");
	    socketOut.println(myPubKey.getModulus());
	    print("Public modulus sent\n");
	    ArrayList<String> fortuneV = new ArrayList<String>();
	    while(!socketIn.hasNext()){
		print("waiting\n");
	    }
	    int numOfBlocks = Integer.parseInt(socketIn.nextLine());
		
	    for(int i=0; i<numOfBlocks; i++){
		fortuneV.add(socketIn.nextLine());	
	    }
	    s.close();
	    return myRSA.decrypt(fortuneV);
	}
	catch(IOException e){
	    throw new RuntimeException(e);
	}
    }
    public static void main(String[] ARGV){
	JFortune jfort = new JFortune(Integer.parseInt(ARGV[0]),ARGV[1]);
	print(jfort.getFortune());
    }
}
