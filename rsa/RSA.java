package rsa;
import java.math.*;
import java.util.Random;
import java.util.Scanner;
import java.io.*;
import java.util.Vector;
public class RSA{
    Key publicKey; //mildly annoying that "public" and "private" are keywords
    Key privateKey;
    BigInteger p,q,n,totient,e,d;
    static final String KEYFILENAME = "rsakey.dat";
    static final int PRIMESIZE = 1024;
    public Key getPublicKey(){
	return publicKey;
    }
    public Key getPrivateKey(){
	return privateKey;
    }
    public RSA(){
	File rsaFile = new File(KEYFILENAME);
	try{
	    if(!rsaFile.createNewFile()){//if the file already existed
		Scanner keyfile = new Scanner(rsaFile);
		e = new BigInteger(keyfile.nextLine().trim());
		d = new BigInteger(keyfile.nextLine().trim());
		n = new BigInteger(keyfile.nextLine().trim());
	    }
	    else{//generate a new key for the instance of RSA
		rsaFile.createNewFile();
		PrintWriter rsadat = new PrintWriter(rsaFile);
		for(;;){
		    for(;;){
			p = getPrime();
			q = getPrime();
			if(!p.equals(q))
			    break;
		    }
		    n = p.multiply(q);
		    totient = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
		    for(;;){
			e = (new BigDecimal(totient).multiply(new BigDecimal(Math.random()))).toBigInteger();
			//print(gcd(e,totient)+"\n");
			if(gcd(e,totient).equals(BigInteger.ONE))
			    break;
		    }
		    d = getPrivateExponent(e,totient);
		    if(d.compareTo(BigInteger.ONE)>0)
			break;
		}
		rsadat.print(e+"\n"+d+"\n"+n+"\n");
		rsadat.close();
		
	    }
	}
	catch(Exception e){
	    throw new RuntimeException(e);
	}
	//print("p:"+p+"\nq:"+q+"\n");
	//print("e:"+e+"\nd:"+d+"\nn:"+n+"\n");
	publicKey = new Key(e,n);
	privateKey = new Key(d,n);
	
	
    }
    void print(Object e){
	System.out.print(e+"");
    }
    BigInteger getPrime(){
	/*note that one can change the method of generating primes
	  by changing this method--all the primes gotten are gotten 
	  by calling getPrime().
	*/
	return BigInteger.probablePrime(PRIMESIZE,new Random());
    }
    BigInteger getPrivateExponent(BigInteger e, BigInteger totient){
	
	BigInteger[] exeuclids = ExtendedEuclids(e,totient);
	
	return exeuclids[0];
	
	
	
    }
    BigInteger[] ExtendedEuclids(BigInteger a, BigInteger b){
	//to solve x*a + y*b = gcd(a,b), or in this case,
	//d*e - k*totient = 1	    
	if(a.mod(b).equals(BigInteger.ZERO)){
	    return new BigInteger[] {BigInteger.ZERO,BigInteger.ONE};
	}
	BigInteger[] r =  ExtendedEuclids(b, a.mod(b));
	return new BigInteger[] {r[1],r[0].subtract(r[1].multiply(a.divide(b)))};
    }
    BigInteger gcd(BigInteger a, BigInteger b){
	//a straight-up implementation of Euclid's algorithm
	if(b.equals(BigInteger.ZERO))
	    return a;
	return gcd(b, a.mod(b));
    }

    public Vector<String> encrypt(String toEncrypt, Key theirPublicKey){
	Vector toReturn = new Vector<String>();
	byte[] sbytes = toEncrypt.getBytes();
	int additionalSlots=0;//0 or 1
	if(sbytes.length%(n.bitLength()/8)!=0)
	    additionalSlots=1;
	byte[][] blocks = new byte[(sbytes.length/(n.bitLength()/8))+additionalSlots][n.bitLength()/8];
	if(additionalSlots==1)
	    blocks[blocks.length-1] = new byte[sbytes.length%(n.bitLength()/8)];
	int sbytesCounter = 0;
	for(int i=0; i<blocks.length; i++){
	    /*
	    for(int j=0; j<blocks[i].length; j++){
		blocks[i][j]=sbytes[sbytesCounter++];
	    }
	    */
	    System.arraycopy(sbytes,i*(blocks[0].length),blocks[i],0,blocks[i].length);
	}    
	
	for(byte[] bytes:blocks){
	    BigInteger e = theirPublicKey.getExponent();
	    BigInteger n = theirPublicKey.getModulus();
	    BigInteger ret = new BigInteger(1,bytes);
	    //print("unencrypted:" +ret+"\n");
	    BigInteger encrypted = ret.modPow(e,n);
	    //print("encrypted:"+encrypted+ "\n");
	    toReturn.add(encrypted+"");
	}
	return toReturn;
    }
    public String decrypt(Vector<String> toDecrypt){
	byte[] returnbytes = new byte[10];//prob. going to resize
	int currentPos = 0;
	String toReturn = "";
	for(String s:toDecrypt){

	    //print("undecrypted:"+s+"\n");
	    BigInteger r = new BigInteger(s).modPow(d,n);
	    //print("decrypted:"+r+"\n");
	    byte[] rbytes = r.toByteArray();
	    while(rbytes.length+currentPos>returnbytes.length)
		returnbytes = byteArrayResize(returnbytes);
	    System.arraycopy(rbytes,0,returnbytes,currentPos,rbytes.length);
	    currentPos += rbytes.length;
	    //toReturn += new String(r.toByteArray());
	}
	returnbytes = byteArrayFitToSize(returnbytes,currentPos);
	return new String(returnbytes);
    }
    byte[] byteArrayResize(byte[] bytes){
	byte[] toReturn = new byte[2*bytes.length];
	System.arraycopy(bytes,0,toReturn,0,bytes.length);
	return toReturn;
    }
    byte[] byteArrayFitToSize(byte[] bytes, int size){
	if(!(size<bytes.length))
	    return bytes;
	byte[] toReturn = new byte[size];
	System.arraycopy(bytes,0,toReturn,0,toReturn.length);
	return toReturn;
    }
	
}
