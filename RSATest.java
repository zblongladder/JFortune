import rsa.*;
import java.io.*;
public class RSATest{
    public static void main(String[] args){
	RSA myRSA = new RSA();
	String text = "Ὅσον ζῇς φαίνου";
	System.out.println(text);
	System.out.println(myRSA.decrypt(myRSA.encrypt(text, myRSA.getPublicKey())));
    }
}
