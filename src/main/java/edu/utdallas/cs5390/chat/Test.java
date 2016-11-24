package edu.utdallas.cs5390.chat;

/**
 * Created by adisor on 11/20/2016.
 */
public class Test {
    public static void main(String[] args) {
        byte[] cipheredBytes = new byte[]{123 ,3};
        char[] chars = new char[cipheredBytes.length];
        System.arraycopy(cipheredBytes, 0, chars, 0, chars.length);
        new String(chars);
    }
    interface A{
        public void test();
    }
}
