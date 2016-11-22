package rsa;
import java.math.*;
import java.util.Random;
import java.util.Scanner;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
public class RSA{
    Key publicKey;
    Key privateKey;
    static final String DEFAULTKEYFILENAME = "rsakey.dat";
    static final int PRIMESIZE = 1024;
    public Key getPublicKey(){
	return publicKey;
    }
    public Key getPrivateKey(){
	return privateKey;
    }
    public RSA(){
	this(DEFAULTKEYFILENAME);
    }
    public RSA(String keyfileName){
	File rsaFile = new File(keyfileName);
	boolean keyfileIsNew;
	try{
	    keyfileIsNew = rsaFile.createNewFile();
	}
	catch(IOException ex){
	    throw new RuntimeException("Error creating or accessing file "+keyfileName+":"+ex);
	}
	BigInteger e,d,n;
	if(!keyfileIsNew){//if the file already existed
	    Scanner keyfile;
	    try{
		keyfile = new Scanner(rsaFile);
	    }
	    catch(FileNotFoundException ex){
		throw new RuntimeException("Could not find RSA keyfile "+keyfileName+":"+ex);
	    }
	    e = new BigInteger(keyfile.nextLine().trim());
	    d = new BigInteger(keyfile.nextLine().trim());
	    n = new BigInteger(keyfile.nextLine().trim());
	}
	else{//generate a new key for the instance of RSA
	    PrintWriter rsadat;
	    try{
		rsadat = new PrintWriter(rsaFile);
	    }
	    catch(FileNotFoundException ex){
		throw new RuntimeException("Could not find RSA keyfile "+keyfileName+":"+ex);
	    }
	    BigInteger p,q,totient;
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

	publicKey = new Key(e,n);
	privateKey = new Key(d,n);		
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

    public ArrayList<String> encrypt(String toEncrypt, Key theirPublicKey){
	/* A simple implementation of RSA encryption. Does not implement
	 * some features, notably padding, necessary for use in a
	 * production environment.
	 */
	ArrayList<String> toReturn = new ArrayList<String>();
	byte[] toEncryptBytes = toEncrypt.getBytes();
	int additionalSlots=0;//0 or 1
	int blockbytes = theirPublicKey.getModulus().bitLength()/8;
	if(toEncryptBytes.length%blockbytes!=0)
	    additionalSlots=1;
	byte[][] blocks = new byte[(toEncryptBytes.length/blockbytes)+additionalSlots][blockbytes];
	if(additionalSlots==1)
	    blocks[blocks.length-1] = new byte[toEncryptBytes.length%blockbytes];
	for(int i=0; i<blocks.length; i++){
	    System.arraycopy(toEncryptBytes,i*(blocks[0].length),blocks[i],0,blocks[i].length);
	}    
	for(byte[] block:blocks){
	    BigInteger ret = new BigInteger(1,block);
	    BigInteger encrypted = ret.modPow(theirPublicKey.getExponent(), theirPublicKey.getModulus());
	    toReturn.add(encrypted.toString());
	}
	return toReturn;
    }
    public String decrypt(ArrayList<String> toDecrypt){
	ArrayList<Byte> bytes = new ArrayList<Byte>();
	for(String s:toDecrypt){
	    BigInteger decryptedByteInt = new BigInteger(s).modPow(privateKey.getExponent(),privateKey.getModulus());
	    bytes.addAll(Arrays.asList(ArrayUtils.toObject(decryptedByteInt.toByteArray())));
	}
	return new String(ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()])));
    }
}
