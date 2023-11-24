package com.wayos.util;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("serial")
public class Secure implements Serializable {

    public String encryptPassword(String password) {

        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(password.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return generatedPassword;
    }

    public boolean checkPassword(String p1,String p2) {

        if (p1 != null && p2 != null) {
            String Resault = encryptPassword(p1);
            if(Resault.equals(p2)) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[]args) {

        Secure encryptor = new Secure();
        System.out.println(encryptor.encryptPassword("Passw0rd"));

    }

}
