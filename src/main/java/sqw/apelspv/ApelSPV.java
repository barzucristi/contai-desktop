package sqw.apelspv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sun.security.pkcs11.SunPKCS11;

public class ApelSPV {

    private static final Logger LOGGER = Logger.getLogger(ApelSPV.class);
    private static final String PROXY_IP = null; 
    private static final String PROXY_PORT = null; 
           

    public JsonObject makeApiCall(String apiUrl) {
        SSLSocket sslSocket = null;
        Socket socket = null;

        if (PROXY_IP != null) {
            System.setProperty("https.proxyHost", PROXY_IP);
            System.setProperty("https.proxyPort", PROXY_PORT);
            System.setProperty("http.proxyHost", PROXY_IP);
            System.setProperty("http.proxyPort", PROXY_PORT);
        }

        try {
            // Load the default Windows certificate store
            KeyStore ks = KeyStore.getInstance("Windows-MY");
            ks.load(null, null); // No password needed for default store

            // List all certificates in the keystore
            listAllCertificates(ks);

            // Select the certificate at index 0
            X509Certificate selectedCert = (X509Certificate) ks.getCertificate(ks.aliases().nextElement()); // Get the first certificate
            LOGGER.info("Selected Certificate: " + selectedCert.getSubjectDN().getName());

            // Create a KeyManagerFactory using the default key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, null); // No password for default key store

            // Set up SSL context with the key managers
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), createTrustAllCerts(), new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            CookieHandler.setDefault(new CookieManager());
            LOGGER.error("A============");
            SSLSocketFactory factory = sslContext.getSocketFactory();
            socket = createSocket();
            InetSocketAddress dest = new InetSocketAddress("webserviced.anaf.ro", 443);
            LOGGER.error("b============");
            try {
                socket.connect(dest);
            } catch (IOException e) {
                LOGGER.error("Failed to connect to " + dest + ": ", e);
            }
            LOGGER.error("c============");
            sslSocket = (SSLSocket) factory.createSocket(socket, socket.getInetAddress().getHostName(),
                    socket.getPort(), true);
            sslSocket.setUseClientMode(true);
            sslSocket.setSoTimeout(100000);
            sslSocket.setKeepAlive(true);
            sslSocket.startHandshake();
            LOGGER.error("d============");
            return makeApiRequest(apiUrl, sslSocket, factory);

        } catch (Exception e) {
            LOGGER.error("Error in ApelSPV: ", e);
            return null;
        } finally {
            if (sslSocket != null) {
                try {
                    sslSocket.close(); 
                } catch (IOException e) {
                    LOGGER.error("Error closing SSLSocket: ", e);
                }
            }
            if (socket != null) {
                try {
                    socket.close(); 
                } catch (IOException e) {
                    LOGGER.error("Error closing Socket: ", e);
                }
            }
            
        }
    }

    public static JsonObject makeApiRequest(String apiUrl, SSLSocket sslSocket, SSLSocketFactory factory) {
        StringBuilder responseBuilder = new StringBuilder();
        JsonObject jsonResponse = null;
        HttpsURLConnection con = null;
        
        try {
        	  LOGGER.error("e============");
           
            URL obj = new URL(apiUrl);
            con = (HttpsURLConnection) obj.openConnection();
            con.setSSLSocketFactory(factory);
            con.setRequestMethod("GET");

            // Check response code and content type
            int responseCode = con.getResponseCode();
            String contentType = con.getContentType();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // Read and log response
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseBuilder.append(inputLine);
                    }
                }

                // Only parse if content type is JSON
                if (contentType != null && contentType.contains("application/json")) {
                    jsonResponse = new JsonParser().parse(responseBuilder.toString()).getAsJsonObject();
                } else {
                    LOGGER.error("Unexpected content type: " + contentType);
                    LOGGER.error("Response: " + responseBuilder.toString());
                }
            } else {
                LOGGER.error("HTTP error code: " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.error("Error during API request: ", e);
        } finally {
            if (con != null) {
                con.disconnect(); 
            }
        }

        return jsonResponse;
    }

    private static TrustManager[] createTrustAllCerts() {
        return new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };
    }

    private static Socket createSocket() {
        if (PROXY_IP != null) {
            SocketAddress addr = new InetSocketAddress(PROXY_IP, Integer.parseInt(PROXY_PORT));
            return new Socket(new Proxy(Proxy.Type.HTTP, addr));
        } else {
            return new Socket();
        }
    }


    private void listAllCertificates(KeyStore ks) throws Exception {
        LOGGER.info("Listing all certificates in the Windows certificate store:");

        // Get the aliases from the KeyStore
        java.util.Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement(); // Get the next alias
            LOGGER.info("Alias: " + alias);
            
            // Get the certificate associated with the alias
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            
            // Log certificate information
            if (cert != null) {
                LOGGER.info("Certificate Information:");
                LOGGER.info("Subject: " + cert.getSubjectDN().getName());
                LOGGER.info("Issuer: " + cert.getIssuerDN().getName());
                LOGGER.info("Serial Number: " + cert.getSerialNumber());
                LOGGER.info("Signature Algorithm: " + cert.getSigAlgName());
                LOGGER.info("Valid From: " + cert.getNotBefore());
                LOGGER.info("Valid Until: " + cert.getNotAfter());
            } else {
                LOGGER.warn("No certificate found for alias: " + alias);
            }
        }
    }


}
