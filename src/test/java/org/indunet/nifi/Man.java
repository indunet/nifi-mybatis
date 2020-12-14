package org.indunet.nifi;

/**
 * @author
 * @version 1.0
 */
public class Man {
    String name;

    public Man(String name) {
        this.name = name;
    }

    public void print() {
        System.out.println(this.name);
    }
}
