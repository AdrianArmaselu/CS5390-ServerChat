package edu.utdallas.cs5390.chat;

/**
 * Created by adisor on 11/20/2016.
 */
public class Test {
    public static void main(String[] args) {
        A a = new A() {
            @Override
            public void test() {
                System.out.println("B");
            }
        };
        try {
            A b =a.getClass().newInstance();
            b.test();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    interface A{
        public void test();
    }
}
