package rsa;
public class test{
    static void print(Object o){
	System.out.print(o+"");
    }
    public static void main(String[] ARGV){
	RSA rsa = new RSA();
	print(rsa.decrypt(rsa.encrypt(ARGV[0],rsa.getPublicKey()))+"\n"); 
    }
}