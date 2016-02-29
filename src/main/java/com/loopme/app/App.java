package com.loopme.app;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

    public static void main(String[] args) throws Exception {
        System.setProperty("filePath", args.length > 0 ? args[0] : "");
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        context.registerShutdownHook();
    }

}
