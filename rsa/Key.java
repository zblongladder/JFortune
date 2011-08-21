package rsa;
import java.math.*;
public class Key{
    BigInteger modulus;
    BigInteger exponent;
    public boolean equals(Object comp){
	if(!(comp instanceof Key))
	    return false;
	Key toCompare = (Key) comp;
	return toCompare.modulus.equals(this.modulus) && toCompare.exponent.equals(this.exponent);
    }
    public int hashCode(){
	//God, what a hack.
	return modulus.hashCode() & exponent.hashCode();
    }
    public Key(BigInteger e, BigInteger n){//no pun intended
	modulus = n;
	exponent = e;
    }
    public void setModulus(BigInteger n){
	modulus = n;
    }
    public BigInteger getModulus(){
	return modulus;
    }
    public void setExponent(BigInteger e){
	exponent = e;
    }
    public BigInteger getExponent(){
	return exponent;
    }
}