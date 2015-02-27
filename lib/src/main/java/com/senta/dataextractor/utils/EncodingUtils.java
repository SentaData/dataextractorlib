package com.senta.dataextractor.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class EncodingUtils {
    /*Encodes a normal String to Base 64 format*/
    public static String EncodeToBase64(String StructureXml) {

        byte[] ByteArray = StructureXml.getBytes();
        String B64Xml = Base64.encodeToString(ByteArray, Base64.NO_WRAP);

        return B64Xml.replaceAll("\n|\r ", "");

    }

    /*Decode a Base 64 String to a normal String*/
    public static String DecodeToString(String B64Xml) {

        byte[] base64_byte = Base64.decode(B64Xml, Base64.NO_WRAP);

        String StructureXmlB64 = "";
        try {
            StructureXmlB64 = new String(base64_byte, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return StructureXmlB64.replaceAll("\\s", "");
    }
}
