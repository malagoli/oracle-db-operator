package com.oracle.OrdsClient;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.Constants;
import com.oracle.Utilities;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.radanalytics.operator.common.AbstractOperator;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;


public class OrdsClient {



    private static String ORDS_PATH_CREATE_PDB="_/db-api/stable/database/pdbs/";
    private static String ORDS_PATH_DROP_PDB="_/db-api/stable/database/pdbs/##PDB_NAME##/?action=##ACTION##";


    private static final Logger log = LoggerFactory.getLogger(AbstractOperator.class.getName());

    private static URI getUrl(String path) {
        return URI.create(Utilities.getEnv(Constants.Environment.ENV_ORDS_PROTOCOL)
                + "://" + Utilities.getEnv(Constants.Environment.ENV_ORDS_HOST)
                + ":" + Utilities.getEnv(Constants.Environment.ENV_ORDS_PORT)
                + Utilities.getEnv(Constants.Environment.ENV_ORDS_BASEPATH)
                + "/" + path);
    }

    private static CloseableHttpClient getClient(String username, String password) {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(Utilities.getEnv(Constants.Environment.ENV_ORDS_HOST),
                        Integer.parseInt(Utilities.getEnv(Constants.Environment.ENV_ORDS_PORT))),
                new UsernamePasswordCredentials(username, password));

        CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();



        return client;
    }

    public static void createPdb(KubernetesClient k8sClient, String namespace, PdbCreationRequestModel pdbDetail)
            throws IOException {

        Secret s = getCredential(k8sClient, namespace);

        final String ordsPassword =  Utilities.decodeBase64(s.getData().get("password"));
        final String ordsUsername = Utilities.decodeBase64(s.getData().get("username"));

        log.info("Returned credentials [" + ordsUsername + "/*****]");


        ObjectMapper mapper = new ObjectMapper();
        String bodyRequest = mapper.writeValueAsString(pdbDetail);

        //log.info("Request JSON ["+bodyRequest+"]");

        CloseableHttpClient client = getClient(ordsUsername, ordsPassword);

        URI url = getUrl(ORDS_PATH_CREATE_PDB);
        log.info("Calling ORDS service at ["+url.toString()+"]");

        HttpPost request = new HttpPost( url );
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");

        request.setEntity(new StringEntity( bodyRequest ));

        ResponseHandler< String > responseHandler = response -> {
        int status = response.getStatusLine().getStatusCode();
        if (status == 200 ) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        } else {
            log.error( EntityUtils.toString( response.getEntity()) );
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
};
        String responseBody = client.execute(request, responseHandler);



        log.info(responseBody);




    }

    public static void deletePdb(KubernetesClient k8sClient, String namespace, String pdbName)
            throws IOException {

        Secret s = getCredential(k8sClient, namespace);

        final String ordsPassword =  Utilities.decodeBase64(s.getData().get("password"));
        final String ordsUsername = Utilities.decodeBase64(s.getData().get("username"));

        log.info("Returned credentials [" + ordsUsername + "/*****]");

        CloseableHttpClient client = getClient(ordsUsername, ordsPassword);

        URI url = getUrl(ORDS_PATH_DROP_PDB.replaceAll("##PDB_NAME##", pdbName).replaceAll("##ACTION##", "INCLUDING"));
        log.info("Calling ORDS service at ["+url.toString()+"]");

        HttpDelete request = new HttpDelete( url );
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");

        ResponseHandler< String > responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                log.error( EntityUtils.toString( response.getEntity()) );
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
        String responseBody = client.execute(request, responseHandler);

        log.info(responseBody);

    }


    private static Secret getCredential(KubernetesClient client, String namespace) {
        String secretName = Utilities.getEnv(Constants.Environment.ENV_ORDS_CREDENTIAL_SECRET_NAME);


        Secret s = client.secrets().inNamespace(namespace).withName(secretName).get();

        if(s == null || s.getData().equals(null)) {
            Utilities.errorAndExit("Unable to find secret [" + Utilities.getEnv(Constants.Environment.ENV_ORDS_CREDENTIAL_SECRET_NAME) +"] in namespace [" + namespace + "]");

        }

        if(s.getData().get("password").equals(null)) {
            Utilities.errorAndExit("Unable to find [password] in secret ["+ Utilities.getEnv(Constants.Environment.ENV_ORDS_CREDENTIAL_SECRET_NAME) +"]");
        }

        if(s.getData().get("username").equals(null)) {
            Utilities.errorAndExit("Unable to find [password] in secret ["+ Utilities.getEnv(Constants.Environment.ENV_ORDS_CREDENTIAL_SECRET_NAME) +"]");
        }

        return s;
    }



}
