package com.dcounter.plugins.tlssender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@CapacitorPlugin(name = "TLSSender")
public class TLSSenderPlugin extends Plugin {

    @PluginMethod
    public void makeTLSTCPRequest(PluginCall call) {
        final String host = call.getString("host");
        final int port = call.getInt("port");
        final String message = call.getString("message");

        try {
            // Disable certificate validation for self-signed certificates
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }
            } };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);

            // Perform your raw TLS request logic for Android here
            // Example: Send the message and retrieve the response
            // Remember to handle the TLS handshake, data exchange, and socket closure

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(message.getBytes());
            outputStream.flush();

            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            socket.close();

            JSObject result = new JSObject();
            result.put("response", response.toString());
            call.success(result);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            call.reject("Failed to make TLS request: " + e.getMessage());
        }
    }
}

