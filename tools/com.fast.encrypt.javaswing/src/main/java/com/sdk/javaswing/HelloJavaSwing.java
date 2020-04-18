package com.sdk.javaswing;

import javax.swing.SwingUtilities;

public class HelloJavaSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                new SwingFrame();
            }
        });
    }
}
