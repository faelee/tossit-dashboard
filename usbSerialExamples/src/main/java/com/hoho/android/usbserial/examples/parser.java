package com.hoho.android.usbserial.examples;

import java.util.Scanner;

public class parser {
    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);
        String lineString = "";

        // Loop until user enters "exit"
        while (true) {
            // Prompt user to enter input string
            System.out.print("Enter input string: \n");
            lineString = scnr.nextLine();
            if(lineString.equals("exit")){
                System.out.println("Exiting program.");
                break;
            }
            // Convert input string to byte array to mimic serial input
            byte[] data = lineString.getBytes();
            // Convert byte array to string
            String process = new String(data);
            System.out.println(lineString);
            System.out.println(process);
            // Convert first 8 characters of string to hex to get Unix timestamp and then to date-time format
            Integer timeStamp = hex(process.substring(1,9));
            java.util.Date time=new java.util.Date((long)timeStamp*1000);
            System.out.println("Time: " + time);
            // Convert next 2 characters of string from hex to get device ID
            System.out.println("Device ID: " + process.substring(9,11));
            // Find alarm status from next 2 characters of string
            String status = process.substring(11,13);
            if(status.equals("00")){
                System.out.println("Alarm Status: No Alarm");
            } else if(status.equals("01")){
                System.out.println("Alarm Status: Alarm");
            } else {
                System.out.println("Alarm Status: Unknown");
            }
            // Convert next 2 characters of string from hex to get dye number
            System.out.println("Dye Number: " + process.substring(13,15));
            System.out.println();
            int i = 15;
            int k = 1;
            // Loop through the rest of the string to get dye values
            // Each dye value is 6 bytes long, with 2 bytes for L, 2 bytes for A, and 2 bytes for B
            while(i + 5 < process.length()){
                System.out.print("Dye #" + k + "\n" + "L = " + hex(process.substring(i,i+2)) + "\n"
                        + "A = " + hex(process.substring(i+2,i+4))+ "\n" + "B = " + hex(process.substring(i+4,i+6))+ "\n");
                i += 6;
                k+=1;
            }
        }
    }

    // Function to convert hex string to integer
    public static int hex(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }
}
