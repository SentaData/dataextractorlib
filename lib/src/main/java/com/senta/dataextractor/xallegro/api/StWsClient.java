package com.senta.dataextractor.xallegro.api;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.senta.dataextractor.SecurityUtilities;
import com.senta.dataextractor.parameters.ParamStaticPhone;
import com.senta.dataextractor.utils.EncodingUtils;
import com.senta.dataextractor.utils.Utilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class StWsClient {

    private static final String TAG = StWsClient.class.getName();

    public static Document createXmlDoc(String StructureXml) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document document = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new InputSource(new StringReader(StructureXml)));
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            Log.e(TAG, "SAXException:" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException:" + e.getMessage());
            e.printStackTrace();
        }
        return document;
    }


    public String CreateXmlForParameters(ParamStaticPhone a, String DynamicStructureXmlB64, ArrayList<String> List, String ParametersType, Context context) {

        String XmlString = "", B64XmlString = "";
        Document InfoDocument;
        InfoDocument = createXmlDoc(DynamicStructureXmlB64);

        if (ParametersType.equals("DynamicMemory"))
            List.add("StDevice-Imei," + a.getIMEINumber(context));

        String[] ElementNameArray = {"name", "value"};
        String[] ElementValueArray;

        InfoDocument = AddElementByXPath(InfoDocument, "/WsXmlData/login/username", "alex@xallegro.com"/*MainActivity.email*/);
        InfoDocument = AddElementByXPath(InfoDocument, "/WsXmlData/login/password", "abc");

        for (int k = 0; k < List.size(); k++) {
            ElementValueArray = List.get(k).split(",");
            String[] El = {ElementValueArray[0], ElementValueArray[1]};
            InfoDocument = AddParamElementByXPath(InfoDocument, "/WsXmlData/params", ElementNameArray, El);

        }
        XmlString = getXmlToString(InfoDocument);
        //Log.e("Dynamic Xml-->", XmlString);

        B64XmlString = EncodingUtils.EncodeToBase64(XmlString);
        Log.d("B64 String: ", B64XmlString);
        return B64XmlString;
    }

    public Document AddParamElementByXPath(Document document, String XpathExp, String[] ElementNameArray, String[] ElementValueArray) {

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        XPathExpression expr;
        NodeList params_list = null;

        try {
            expr = xpath.compile(XpathExp);
            params_list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        Node nNode = params_list.item(0);

        Element[] elementValues = new Element[ElementNameArray.length];

        Element paramElm = document.createElement("param");
        Node ParamNode = nNode.appendChild(paramElm);

        Element nameElm = document.createElement(ElementNameArray[0]);

        nameElm.setTextContent(ElementValueArray[0]);
        ParamNode.appendChild(nameElm);

        Element valueElm = document.createElement(ElementNameArray[1]);
        valueElm.setTextContent(ElementValueArray[1]);
        ParamNode.appendChild(valueElm);

        return document;

    }

    public Document AddElementByXPath(Document document, String XpathExp, String value) {

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;

        NodeList params_list = null;
        try {
            expr = xpath.compile(XpathExp);
            params_list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        Node nNode = params_list.item(0);
        nNode.appendChild(document.createTextNode(value));

        return document;
    }

    public static String GetElementByXPath(Document document, String XpathExp) {

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;

        NodeList params_list = null;
        Node nNode;

        try {

            expr = xpath.compile(XpathExp);
            params_list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        nNode = params_list != null ? params_list.item(0) : null;
        return nNode != null ? nNode.getTextContent() : null;
    }


    /*Return an XML document to its String format*/
    public String getXmlToString(Document document) {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        if (transformer == null) return null;
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return writer.getBuffer().toString().replaceAll("\n|\r", "");

    }

    public boolean DeviceRegistered(String ServerResponse) {
        return ServerResponse.contains("<root><message>ok</message></root>");
    }

    /*Convert a String to a byte array and returns the byte array back.*/
    public static byte[] StringToByteArray(String s) {
        return s.getBytes();
    }


    public String HttpsDeviceRegistration(String UrlString, String Base64Xml) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        // Tell the URLConnection to use a SocketFactory from our SSLContext
        URL url = new URL(UrlString);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

        urlConnection.setSSLSocketFactory(context.getSocketFactory());

        return "";
    }

    /* This method returns the appropriate HttpClient.
    * @param isTLS Whether Transport Layer Security is required.
    * @param trustStoreInputStream The InputStream generated from the BKS keystore.
    * @param trustStorePsw The password related to the keystore.
    * @return The DefaultHttpClient object used to invoke execute(request) method.*/
    public String DefaultHttpClientWithHttps(boolean isTLS, InputStream trustStoreInputStream, String trustStorePsw, String Base64Xml, String UrlString) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {

        DefaultHttpClient client = null;

        // Creating HTTP Post
        HttpPost httpPost = new HttpPost(UrlString);
        HttpEntity httpEntity = null;
        HttpResponse response;
        String return_response = "";

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        Scheme http = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
        schemeRegistry.register(http);

        try {
            addPostParameters(Base64Xml, httpPost);
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        if (isTLS) {
            KeyStore trustKeyStore = null;
            char[] trustStorePswCharArray = null;
            if (trustStorePsw != null) {
                trustStorePswCharArray = trustStorePsw.toCharArray();
            }
            trustKeyStore = KeyStore.getInstance("BKS");
            trustKeyStore.load(trustStoreInputStream, trustStorePswCharArray);

            SSLSocketFactory sslSocketFactory = null;
            sslSocketFactory = new SSLSocketFactory(trustKeyStore);

            Scheme https = new Scheme("https", sslSocketFactory, 443);
            schemeRegistry.register(https);
            Log.d("SSLSocetFactory", sslSocketFactory.getHostnameVerifier().toString());

            Log.d("TLS IS", Boolean.toString(isTLS));
        }

        int timeoutConnection = 10000;
        int timeoutSocket = 10000;

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
        ClientConnectionManager clientConnectionManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        client = new DefaultHttpClient(clientConnectionManager, httpParams);

        // Making HTTP Request
        try {
            response = client.execute(httpPost);
            // writing response to log
            httpEntity = response.getEntity();
            return_response = EntityUtils.toString(httpEntity);
            Log.d("Http Response:", response.toString());
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }
        return return_response;
    }

    public String DeviceRegistration(String UrlString, String Base64Xml, String HexKey, String HexIv) {

        // Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP Post
        HttpPost httpPost = new HttpPost(UrlString);
        HttpEntity httpEntity;
        HttpResponse response;
        String return_response = "";
        try {
            // Building post parameters - key and value pair
            List<NameValuePair> Values = new ArrayList<NameValuePair>(5);

            Values.add(new BasicNameValuePair("WsXml", "yes"));
            Values.add(new BasicNameValuePair("Encoding", "B64"));

            Values.add(new BasicNameValuePair("WsKey", HexKey));
            Values.add(new BasicNameValuePair("WsIV", HexIv));
            Values.add(new BasicNameValuePair("WsXmlData", Base64Xml));

            //Log.d("BEFORE SEND",Base64Xml);
            httpPost.setEntity(new UrlEncodedFormEntity(Values));

        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }
        // Making HTTP Request
        try {
            response = httpClient.execute(httpPost);
            // writing response to log
            httpEntity = response.getEntity();
            return_response = EntityUtils.toString(httpEntity);
            Log.d("Http Response:", response.toString());
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }
        return return_response;
    }


    public String DynamicParametersRegistration(String UrlString, String Base64Xml) {

        Log.d("Dynamic", "ParameterRegistration");
        // Creating HTTP Client and Post
        HttpPost httpPost = new HttpPost(UrlString);
        HttpEntity httpEntity = null;
        HttpResponse response;
        String return_response = "";
        byte[] data = null;
        int timeoutConnection = 30000;

        try {
            addPostParameters(Base64Xml, httpPost);
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        // Making HTTP Request
        try {
            HttpParams httpParameters = new BasicHttpParams();

            // Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 30000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);

            Log.d("Senta-Secure1", httpClient.toString());

            response = httpClient.execute(httpPost);
            // writing response to log
            httpEntity = response.getEntity();

            return_response = EntityUtils.toString(httpEntity);

            Log.d("Http Response:", response.toString());

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return return_response;
    }

    private void addPostParameters(String Base64Xml, HttpPost httpPost) throws UnsupportedEncodingException {
        List<NameValuePair> Values = new ArrayList<>(3);
        Values.add(new BasicNameValuePair("WsXml", "yes"));
        Values.add(new BasicNameValuePair("Encoding", "B64"));
        Values.add(new BasicNameValuePair("WsXmlData", Base64Xml));
        httpPost.setEntity(new UrlEncodedFormEntity(Values));
    }


    public static ApiRequestParams getDeviceDataForAuthentication(Context context, List<String> param_list, String XmlString) {
        byte[] ByteSymmetricKey = createSymmetricKey(context, param_list);
        String HexSymmetricKey = SecurityUtilities.byteArrayToHexString(ByteSymmetricKey);

        String strCipherTextStrCTR = encryptXml(XmlString, ByteSymmetricKey);
        String HexIv = SecurityUtilities.byteArrayToHexString(SecurityUtilities.IV);

        return new ApiRequestParams(strCipherTextStrCTR, HexSymmetricKey, HexIv);
    }


    private static String encryptXml(String XmlString, byte[] byteSymmetricKey) {
        byte[] bytePlainTextStrCTR = XmlString.getBytes();
        //IV must be at least 8 bytes string
        byte[] cipherTextStrCTR = SecurityUtilities.aesEncryptCTR(bytePlainTextStrCTR, byteSymmetricKey, SecurityUtilities.IV);
        String strCipherTextStrCTR = new String(Base64.encode(cipherTextStrCTR, Base64.NO_WRAP));
        Log.d(TAG, "Cipher text: " + strCipherTextStrCTR);
        return strCipherTextStrCTR;
    }


    private static byte[] createSymmetricKey(Context context, List<String> param_list) {
        String number_of_parameters = "";
        boolean ParametersNumberNotZero = true;
        //To refactor
        while (ParametersNumberNotZero) {
            String message = SecurityUtilities.CreateMessageChooseNumberOfValues(context.getApplicationContext());
            number_of_parameters = SecurityUtilities.parametersNumberforGrid(message);
            if (Integer.parseInt(number_of_parameters) != 0) {
                ParametersNumberNotZero = false;
            }
        }
        ArrayList<String> IndexOnGrid = SecurityUtilities.select_parameters(number_of_parameters, context.getApplicationContext());

        int previous_size = IndexOnGrid.size();
        IndexOnGrid = Utilities.RemoveDuplicateValuesInList(IndexOnGrid, previous_size);

        String XTS = SecurityUtilities.getXTS(context.getApplicationContext(), IndexOnGrid, param_list);

        String skm = "RandomSaltValueWithSpecialCharacters123456789";
        byte[] skm_byte = SecurityUtilities.stringToByteArray(skm);
        String SKM = SecurityUtilities.byteArrayToHexString(skm_byte);

        byte[] BytePRK = new byte[0], ByteSymmetricKey = new byte[0], ByteHashedCTXinfo = new byte[0];
        String HexHashedCTXinfo = "", CTXinfo = "";
        CTXinfo = SecurityUtilities.getCTXinfo(param_list);

        //Compute PRK and  and CTXinfo in Hexadecimal String format
        try {
            BytePRK = SecurityUtilities.HMacSHA256(SKM, XTS);
            ByteHashedCTXinfo = SecurityUtilities.computeSHA256(CTXinfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
        HexHashedCTXinfo = SecurityUtilities.byteArrayToHexString(ByteHashedCTXinfo);
        String HexPRK = SecurityUtilities.byteArrayToHexString(BytePRK);

        //Compute Symmetric Key
        try {
            ByteSymmetricKey = SecurityUtilities.HMacSHA256(HexPRK, HexHashedCTXinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ByteSymmetricKey;
    }


    public String getDecryptedDataFromServer(byte[] ByteSymmetricKey, String strCipherTextStrCTR) {

        byte[] bCipherTextStrCTR = Base64.decode(strCipherTextStrCTR, Base64.NO_WRAP);
        byte[] bPlainTextStrCTR = SecurityUtilities.aesDecryptCTR(bCipherTextStrCTR, ByteSymmetricKey, SecurityUtilities.IV);
        String clearTextStrCTR = SecurityUtilities.byteArrayToString(bPlainTextStrCTR);
        Log.d("Decrypted: ", clearTextStrCTR);

        return clearTextStrCTR;
    }
}
