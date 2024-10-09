package sqw.apelspv;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

public class ApelSPVFile {

    private static final Logger LOGGER = Logger.getLogger(ApelSPV.class);
    private static final String PROXY_IP = null; 
    private static final String PROXY_PORT = null; 

    public File makeApiCall(String apiUrl,String destinationPath) {
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

            // Create a KeyManagerFactory using the default key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, null); // No password for default key store

            // Set up SSL context with the key managers
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), createTrustAllCerts(), new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            CookieHandler.setDefault(new CookieManager());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            socket = createSocket();
            InetSocketAddress dest = new InetSocketAddress("webserviced.anaf.ro", 443);

            try {
                socket.connect(dest);
            } catch (IOException e) {
                LOGGER.error("Failed to connect to " + dest + ": ", e);
            }

            sslSocket = (SSLSocket) factory.createSocket(socket, socket.getInetAddress().getHostName(),
                    socket.getPort(), true);
            sslSocket.setUseClientMode(true);
            sslSocket.setSoTimeout(100000);
            sslSocket.setKeepAlive(true);
            sslSocket.startHandshake();

            return makeApiRequest(apiUrl, sslSocket, factory,destinationPath);

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

    public static File makeApiRequest(String apiUrl, SSLSocket sslSocket, SSLSocketFactory factory, String destinationPath) {
        HttpsURLConnection con = null;
        File pdfFile = null;

        try {
            URL obj = new URL(apiUrl);
            con = (HttpsURLConnection) obj.openConnection();
            con.setSSLSocketFactory(factory);
            con.setRequestMethod("GET");

            // Check response code and content type
            int responseCode = con.getResponseCode();
            String contentType = con.getContentType();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // Ensure content type is PDF
                if (contentType != null && contentType.equals("application/pdf")) {
                    // Get the original file name from the response header (if available)
                    String originalFileName = con.getHeaderField("Content-Disposition");
                    if (originalFileName != null && originalFileName.contains("filename=")) {
                        // Extract the filename from the header
                        originalFileName = originalFileName.split("filename=")[1].replace("\"", "").trim();
                    } else {
                        originalFileName = "downloaded_file.pdf"; // Fallback name
                    }

                    // Create the full destination file path
                    pdfFile = new File(destinationPath, originalFileName);

                    // Create parent directories if they don't exist
                    pdfFile.getParentFile().mkdirs();

                    // Save the PDF to the specified location
                    try (InputStream inputStream = con.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(pdfFile)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    LOGGER.info("PDF file saved to: " + pdfFile.getAbsolutePath());
                } else {
                    LOGGER.warn("Unexpected content type: " + contentType);
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

        return pdfFile;
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
        java.util.Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement(); 
            LOGGER.info("Alias: " + alias);
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        }
    }
}
