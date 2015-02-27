package com.senta.dataextractor;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtilities {

    public static final byte[] IV = "senta_techno2014".getBytes();

    public static String getXTS(Context context, List<String> Grid, List<String> params) {

        String XTS = "";
        if (Grid.size() == params.size()) {
            for (int j = 0; j < params.size(); j++) {
                String[] Values = params.get(j).split(",");
                XTS += XTS;
            }
        } else {

            for (int i = 0; i < Grid.size(); i++) {
                for (int j = 0; j < params.size(); j++) {
                    String[] Values = params.get(j).split(",");
                    if (Integer.parseInt(Grid.get(i)) == j) {
                        XTS += Values[1];
                        //Toast.makeText(context, i + ": " + XTS, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        return XTS;
    }

    public static String getCTXinfo(List<String> params) {

        String CTXinfo = "";

        for (int j = 0; j < params.size(); j++) {

            String[] Values = params.get(j).split(",");
            if (Values.length == 2) {

                CTXinfo += Values[1];
            }
        }
        return CTXinfo;
    }

    public static byte[] HMacSHA256(String keySKM, String dataXTS) throws Exception {


        //SecureRandom sr = new SecureRandom();
        //byte[] seed = sr.generateSeed(20);
        //String HexSeed = byteArrayToHexString(seed);

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(keySKM.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] HashByteArray = sha256_HMAC.doFinal(dataXTS.getBytes());
        //String HexHashValue  = byteArrayToHexString(HashByteArray);

        return HashByteArray;
    }

    public static String generatePBKDF2HMacSHA1(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;

        char[] chars = password.toCharArray();

        byte[] salt = getSalt();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + byteArrayToHexString(salt) + ":" + byteArrayToHexString(hash);
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    /*Converts a String to Byte array*/
    public static byte[] stringToByteArray(String s) {
        return s.getBytes();
    }

    //Convert an arrays of bytes to String
    public static String byteArrayToString(byte[] b) {
        return b == null ? null : new String(b);
    }

    //Convert byte array to Hex string array
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (((int) bytes[i] & 0xff) < 0x10)
                buffer.append("0");
            buffer.append(Long.toString((int) bytes[i] & 0xff, 16));
        }
        return buffer.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int size = s.length();
        byte[] Bytedata = new byte[size / 2];
        for (int i = 0; i < size; i += 2) {
            Bytedata[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return Bytedata;
    }


    public static String CreateMessageChooseNumberOfValues(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String IMEI = telephonyManager.getDeviceId();
        if (IMEI == null) {
            IMEI = "NaN";
        }
        byte[] IMEI_byte = stringToByteArray(IMEI);
        String IMEI_Number = byteArrayToHexString(IMEI_byte);


        SecureRandom sr = new SecureRandom();
        byte[] seed = sr.generateSeed(20);
        String seed_string = byteArrayToHexString(seed);

        byte[] pad = {0x0000};
        String pad_string = byteArrayToHexString(pad);

        String s = "RandomSaltValueWithSpecialCharacters123456789";
        byte[] salt_byte = stringToByteArray(s);
        String salt = byteArrayToHexString(salt_byte);

        return salt + seed_string + IMEI_Number + pad_string;

    }

    public static String parametersNumberforGrid(String message) {

        byte[] byte_data;
        byte_data = SecurityUtilities.computeSHA256(message);
        String string_data = SecurityUtilities.byteArrayToHexString(byte_data);

        for (int i = 0; i < 9; i++) {
            byte_data = SecurityUtilities.computeSHA256(string_data);
            string_data = SecurityUtilities.byteArrayToHexString(byte_data);
        }

        int byte_data_int = new BigInteger(byte_data).intValue();
        Integer byte_integer = byte_data_int;
        String integer_to_string = byte_integer.toString();
        String number_of_grid_param = "10";
        BigInteger b1 = new BigInteger(integer_to_string);
        BigInteger b2 = new BigInteger(number_of_grid_param);
        BigInteger b3 = b1.mod(b2);

        Integer a = b3.intValue();
        return a.toString();

    }

    /* Returns the position from the grid for each parameter.
     * Takes as input: The number of the parameters that would be selected from
     * the grid, and the message =  salt+IMEI_Number in Hex format.
     * Within the function:
     * Message must be concatenate with seed and counter(counter is increased by one
       for each parameter).*/
    public static ArrayList<String> select_parameters(String number, Context context) {

        ArrayList<String> IndexOfParametersOnGrid = new ArrayList<String>();

        String b3str = "";
        BigInteger n = new BigInteger(number);
        Integer params = n.intValue();
        for (int i = 0; i < params; i++) {

            Integer pad_int = i;
            String pad_intToString = pad_int.toString();
            byte[] byte_padding = stringToByteArray(pad_intToString);
            String pad_string = byteArrayToHexString(byte_padding);

            SecureRandom sr = new SecureRandom();
            byte[] seed = sr.generateSeed(20);
            String seed_string = byteArrayToHexString(seed);

            String s = "RandomSaltValueWithSpecialCharacters123456789";
            byte[] salt_byte = SecurityUtilities.stringToByteArray(s);
            String salt = SecurityUtilities.byteArrayToHexString(salt_byte);

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String IMEI = telephonyManager.getDeviceId();
            if (IMEI == null) {
                IMEI = "NaN";
            }
            byte[] IMEI_byte = stringToByteArray(IMEI);
            String IMEI_Number = byteArrayToHexString(IMEI_byte);

            String padding_message = IMEI_Number + salt + seed_string + pad_string;

            byte[] byte_array = null;

            for (int l = 0; l < 10; l++) {
                byte_array = computeSHA256(padding_message);
                padding_message = byteArrayToHexString(byte_array);
            }

            int byte_data_int = new BigInteger(byte_array).intValue();
            Integer byte_integer = byte_data_int;
            String integer_to_string = byte_integer.toString();

            String number_of_grid_param = "10";

            BigInteger b1 = new BigInteger(integer_to_string);
            BigInteger b2 = new BigInteger(number_of_grid_param);

            BigInteger b3 = b1.mod(b2);

            //String str = b1 + " mod " + b2 + " is " +b3;
            Integer b3int = b3.intValue();
            b3str = b3int.toString();

            Integer b1int = b1.intValue();
            String b1str = b1int.toString();

            Integer b2int = b2.intValue();
            String b2str = b2int.toString();

            IndexOfParametersOnGrid.add(b3str);
        }
        return IndexOfParametersOnGrid;
    }

    /*Computes SHA-256 taking as input the message*/
    public static byte[] computeSHA256(String message) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            byte[] passBytes = message.getBytes();

            return md.digest(passBytes);
        } catch (NoSuchAlgorithmException ignored) {
        }

        return null;
    }

    //-----------------------------------------------------------------------------//

    /**
     * AES encrypt function
     *
     * @param original
     * @param key      16, 24, 32 bytes available
     * @param iv       initial vector (16 bytes) - if null: ECB mode, otherwise: CBC mode
     * @return
     */
    public static byte[] aesEncryptCTR(byte[] original, byte[] key, byte[] iv) {
        if (key == null || (key.length != 16 && key.length != 24 && key.length != 32)) {
            Log.e("Error: ", "key's bit length is not 128/192/256");
            return null;
        }
        if (iv != null && iv.length != 16) {
            Log.e("Error: ", "iv's bit length is not 16");
            return null;
        }

        try {
            SecretKeySpec keySpec = null;
            Cipher cipher = null;
            if (iv != null) {
                keySpec = new SecretKeySpec(key, "AES/CTR/PKCS7Padding");
                cipher = Cipher.getInstance("AES/CTR/PKCS7Padding");


                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            } else  //if(iv == null)
            {
                keySpec = new SecretKeySpec(key, "AES/ECB/PKCS7Padding");
                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            }

            return cipher.doFinal(original);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
        return null;
    }

    /**
     * AES decrypt function
     *
     * @param encrypted
     * @param key       16, 24, 32 bytes available
     * @param iv        initial vector (16 bytes) - if null: ECB mode, otherwise: CBC mode
     * @return
     */
    public static byte[] aesDecryptCTR(byte[] encrypted, byte[] key, byte[] iv) {
        if (key == null || (key.length != 16 && key.length != 24 && key.length != 32)) {
            Log.e("Error: ", "Key's bits length is not 128/192/256");
            return null;
        }
        if (iv != null && iv.length != 16) {
            Log.e("Error: ", "IV's bits length is not 16");
            return null;
        }

        try {
            SecretKeySpec keySpec = null;
            Cipher cipher = null;
            if (iv != null) {
                keySpec = new SecretKeySpec(key, "AES/CTR/NoPadding");
                cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            } else  //if(iv == null)
            {
                keySpec = new SecretKeySpec(key, "AES/ECB/PKCS7Padding");
                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
            }
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
        return null;
    }


}
