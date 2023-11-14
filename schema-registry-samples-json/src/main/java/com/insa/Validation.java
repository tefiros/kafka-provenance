package com.insa;

import org.dom4j.DocumentException;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class Validation {

    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        System.out.println("Start validation");
        //JSON
        Check.checkJSON("Valid Json", "true", "json/test.yang","json/valid.json");
        Check.checkJSON("Invalid Json", "false", "json/test.yang","json/invalid.json");
        Check.checkJSON("Incorrect Json", "false", "json/test.yang","json/incorrect.json");
        Check.checkJSON("Missing Json", "false", "json/test.yang","json/missing.json");
        //JSON AUGMENTATIONS
        Check.checkJSON("Valid JSON augmentations", "true", "jsonsub", "jsonsub/valid.json");

        //NOTIFICATION
        Check.checkNotification("Valid Notification", "true", "notification/yang","notification/json/valid.json");
        Check.checkNotification("Invalid Notification", "false", "notification/yang","notification/json/invalid.json");
        Check.checkNotification("Incorrect Notification", "false", "notification/yang","notification/json/incorrect.json");
        Check.checkNotification("Missing Notification", "false", "notification/yang","notification/json/missing.json");
        //XML
        Check.checkXML("Valid XML", "true", "xml/test.yang","xml/valid.xml");
        Check.checkXML("Invalid XML", "false", "xml/test.yang","xml/invalid.xml");
        Check.checkXML("Incorrect XML", "false", "xml/test.yang","xml/incorrect.xml");
        Check.checkXML("Missing XML", "false", "xml/test.yang","xml/missing.xml");

        System.out.println("End validation");
    }

}
