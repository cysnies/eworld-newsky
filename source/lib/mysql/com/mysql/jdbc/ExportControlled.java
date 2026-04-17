package com.mysql.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ExportControlled {
   private static final String SQL_STATE_BAD_SSL_PARAMS = "08000";

   protected static boolean enabled() {
      return true;
   }

   protected static void transformSocketToSSLSocket(MysqlIO mysqlIO) throws SQLException {
      SSLSocketFactory sslFact = getSSLSocketFactoryDefaultOrConfigured(mysqlIO);

      try {
         mysqlIO.mysqlConnection = sslFact.createSocket(mysqlIO.mysqlConnection, mysqlIO.host, mysqlIO.port, true);
         ((SSLSocket)mysqlIO.mysqlConnection).setEnabledProtocols(new String[]{"TLSv1"});
         ((SSLSocket)mysqlIO.mysqlConnection).startHandshake();
         if (mysqlIO.connection.getUseUnbufferedInput()) {
            mysqlIO.mysqlInput = mysqlIO.mysqlConnection.getInputStream();
         } else {
            mysqlIO.mysqlInput = new BufferedInputStream(mysqlIO.mysqlConnection.getInputStream(), 16384);
         }

         mysqlIO.mysqlOutput = new BufferedOutputStream(mysqlIO.mysqlConnection.getOutputStream(), 16384);
         mysqlIO.mysqlOutput.flush();
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(mysqlIO.connection, mysqlIO.getLastPacketSentTimeMs(), mysqlIO.getLastPacketReceivedTimeMs(), ioEx);
      }
   }

   private ExportControlled() {
   }

   private static SSLSocketFactory getSSLSocketFactoryDefaultOrConfigured(MysqlIO mysqlIO) throws SQLException {
      String clientCertificateKeyStoreUrl = mysqlIO.connection.getClientCertificateKeyStoreUrl();
      String trustCertificateKeyStoreUrl = mysqlIO.connection.getTrustCertificateKeyStoreUrl();
      String clientCertificateKeyStoreType = mysqlIO.connection.getClientCertificateKeyStoreType();
      String clientCertificateKeyStorePassword = mysqlIO.connection.getClientCertificateKeyStorePassword();
      String trustCertificateKeyStoreType = mysqlIO.connection.getTrustCertificateKeyStoreType();
      String trustCertificateKeyStorePassword = mysqlIO.connection.getTrustCertificateKeyStorePassword();
      if (StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl) && StringUtils.isNullOrEmpty(trustCertificateKeyStoreUrl) && mysqlIO.connection.getVerifyServerCertificate()) {
         return (SSLSocketFactory)SSLSocketFactory.getDefault();
      } else {
         TrustManagerFactory tmf = null;
         KeyManagerFactory kmf = null;

         try {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         } catch (NoSuchAlgorithmException var25) {
            throw SQLError.createSQLException("Default algorithm definitions for TrustManager and/or KeyManager are invalid.  Check java security properties file.", "08000", 0, false);
         }

         if (StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl)) {
            try {
               if (!StringUtils.isNullOrEmpty(clientCertificateKeyStoreType)) {
                  KeyStore clientKeyStore = KeyStore.getInstance(clientCertificateKeyStoreType);
                  URL ksURL = new URL(clientCertificateKeyStoreUrl);
                  char[] password = clientCertificateKeyStorePassword == null ? new char[0] : clientCertificateKeyStorePassword.toCharArray();
                  clientKeyStore.load(ksURL.openStream(), password);
                  kmf.init(clientKeyStore, password);
               }
            } catch (UnrecoverableKeyException var19) {
               throw SQLError.createSQLException("Could not recover keys from client keystore.  Check password?", "08000", 0, false);
            } catch (NoSuchAlgorithmException nsae) {
               throw SQLError.createSQLException("Unsupported keystore algorithm [" + nsae.getMessage() + "]", "08000", 0, false);
            } catch (KeyStoreException kse) {
               throw SQLError.createSQLException("Could not create KeyStore instance [" + kse.getMessage() + "]", "08000", 0, false);
            } catch (CertificateException var22) {
               throw SQLError.createSQLException("Could not load client" + clientCertificateKeyStoreType + " keystore from " + clientCertificateKeyStoreUrl);
            } catch (MalformedURLException var23) {
               throw SQLError.createSQLException(clientCertificateKeyStoreUrl + " does not appear to be a valid URL.", "08000", 0, false);
            } catch (IOException ioe) {
               SQLException sqlEx = SQLError.createSQLException("Cannot open " + clientCertificateKeyStoreUrl + " [" + ioe.getMessage() + "]", "08000", 0, false);
               sqlEx.initCause(ioe);
               throw sqlEx;
            }
         }

         if (StringUtils.isNullOrEmpty(trustCertificateKeyStoreUrl)) {
            try {
               if (!StringUtils.isNullOrEmpty(trustCertificateKeyStoreType)) {
                  KeyStore trustKeyStore = KeyStore.getInstance(trustCertificateKeyStoreType);
                  URL ksURL = new URL(trustCertificateKeyStoreUrl);
                  char[] password = trustCertificateKeyStorePassword == null ? new char[0] : trustCertificateKeyStorePassword.toCharArray();
                  trustKeyStore.load(ksURL.openStream(), password);
                  tmf.init(trustKeyStore);
               }
            } catch (NoSuchAlgorithmException nsae) {
               throw SQLError.createSQLException("Unsupported keystore algorithm [" + nsae.getMessage() + "]", "08000", 0, false);
            } catch (KeyStoreException kse) {
               throw SQLError.createSQLException("Could not create KeyStore instance [" + kse.getMessage() + "]", "08000", 0, false);
            } catch (CertificateException var16) {
               throw SQLError.createSQLException("Could not load trust" + trustCertificateKeyStoreType + " keystore from " + trustCertificateKeyStoreUrl, "08000", 0, false);
            } catch (MalformedURLException var17) {
               throw SQLError.createSQLException(trustCertificateKeyStoreUrl + " does not appear to be a valid URL.", "08000", 0, false);
            } catch (IOException ioe) {
               SQLException sqlEx = SQLError.createSQLException("Cannot open " + trustCertificateKeyStoreUrl + " [" + ioe.getMessage() + "]", "08000", 0, false);
               sqlEx.initCause(ioe);
               throw sqlEx;
            }
         }

         SSLContext sslContext = null;

         try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl) ? null : kmf.getKeyManagers(), (TrustManager[])(mysqlIO.connection.getVerifyServerCertificate() ? tmf.getTrustManagers() : new X509TrustManager[]{new X509TrustManager() {
               public void checkClientTrusted(X509Certificate[] chain, String authType) {
               }

               public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
               }

               public X509Certificate[] getAcceptedIssuers() {
                  return null;
               }
            }}), (SecureRandom)null);
            return sslContext.getSocketFactory();
         } catch (NoSuchAlgorithmException var12) {
            throw SQLError.createSQLException("TLS is not a valid SSL protocol.", "08000", 0, false);
         } catch (KeyManagementException kme) {
            throw SQLError.createSQLException("KeyManagementException: " + kme.getMessage(), "08000", 0, false);
         }
      }
   }
}
