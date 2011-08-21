import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import rsa.*;
import java.util.Vector;
import java.util.Scanner;
import java.io.*;
import java.math.*;
public class JFortuned{
    /*JFortuned: a Java fortune daemon
      James Buchanan && Kwadwo Eck
     */
    int port = 42042; //default value
    HashMap<Key,HashSet<String>> fortuneHistory = new HashMap<Key,HashSet<String>>();
    /*The plan: fortuneHistory is a hashmap that is supposed to hash users'
     *public keys to a HashSet of the fortunes they have recieved. Thus,
     *one can lookup the fortune one has selected in the hash set, and
     *if it's already been sent, pick another one, unless the sizes of
     *fortunes and fortuneHistory.get() are the same, in which case all
     *fortunes have been sent and it's time to clear the history. Somehow,
     *this isn't working in practice...and the server's not being happy
     *with debugging output. Driving me crazy. RSA's working, transmission's
     *working, this one little detail...grr...
     *
     *Now that I've changed the .hashCode method in Key(), I think this will
     *work right, like it should--the only problem was that things weren't
     *hashing to the right places. I've got seven minutes to test this.
     *God help me.
     *
     *Five minutes left. It seems to work...but I can't be too scientific
     *about the testing. Also, it seems that every so often (*very*
     *occasionally), the client hangs,
     *but you can kill the client and resubmit without touching the server. 
     */
    Vector<String> fortunes = new Vector<String>();
    static final String FORTUNEFILENAME = "fortunes.dat";
    public JFortuned(String[] ARGV){
	String fFileName = null;
	for(int i=0; i<ARGV.length; i++){
	    if(ARGV[i].matches("\\d*"))
		port = Integer.parseInt(ARGV[i]);
	    if((ARGV[i].matches("-f")||ARGV[i].matches("--fortunefile"))
		&& i<ARGV.length-1)
	       fFileName = ARGV[i+1];
	}
	if(fFileName==null)
	    this.getFortunes();
	else
	    this.getFortunes(fFileName);
    }
    public JFortuned(int port){
	this.port = port;
	this.getFortunes();
    }
    void getFortunes(){
	this.getFortunes(FORTUNEFILENAME);
    }
    void getFortunes(String filename){
	Scanner infile;
	try{
	    infile = new Scanner(new File(filename));
	}
	catch(FileNotFoundException e){
	    throw new RuntimeException("File "+filename+" not found." +
				       "Please specify valid infile.");
	    
	}
	while(infile.hasNextLine())
	    fortunes.add(infile.nextLine());
	if(fortunes.isEmpty() && filename != FORTUNEFILENAME){
	    print("Specified fortune file empty. Using default file.\n");
	    this.getFortunes();
	}
	else if(fortunes.isEmpty()){
	    print("Default fortune file empty. Please specify input file.");
	}
    }
    void print(Object o){
	System.out.print(o+"");
    }
    public void listen(){
	try{
	    ServerSocket SocketServ = new ServerSocket(port);
	    for(;;){		
		Socket s = SocketServ.accept();
		Scanner socketIn = new Scanner(s.getInputStream());
		PrintWriter socketOut = new PrintWriter(s.getOutputStream(),true);
		BigInteger pubKeyExponent = BigInteger.ZERO;
		BigInteger pubKeyModulus = BigInteger.ZERO;
		
		while(!socketIn.hasNext()){}
		pubKeyExponent = 
		    new BigInteger(socketIn.nextLine().trim());
		while(!socketIn.hasNext()){}
		
		pubKeyModulus = 
		    new BigInteger(socketIn.nextLine().trim());
		
		Key theirPublicKey = new Key(pubKeyExponent,pubKeyModulus);
		if(!fortuneHistory.containsKey(theirPublicKey)){
		    //this should use the .equals method in key, so 
		    //should check to see if the user has logged on before.
		    //I suspect it's not checking .equals, but only ==.
		    //However, it's not being cooperative in debugging.
		    fortuneHistory.put(theirPublicKey,new HashSet<String>());
		}
		deliverFortune(socketOut,theirPublicKey);
		s.close();
	    }
		
	}
	catch(IOException e){
	    throw new RuntimeException(e);
	}
	
    }
    void deliverFortune(PrintWriter out, Key pubKey){
	if(fortuneHistory.get(pubKey).size() == fortunes.size())
	    fortuneHistory.get(pubKey).clear();
	for(;;){
	    String fortune = fortunes.get((int)(Math.random()*fortunes.size()));
	    if(!fortuneHistory.get(pubKey).contains(fortune)){		
		Vector<String> fortuneV = new RSA().encrypt(fortune,pubKey);
		out.println(fortuneV.size());
		for(String fEncrypted: fortuneV)
		    out.println(fEncrypted);
		fortuneHistory.get(pubKey).add(fortune);
		break;
	    }	    
	}
    }
    public static void main(String[] ARGV){
	JFortuned jfort = new JFortuned(ARGV);
	jfort.listen();
    }
}