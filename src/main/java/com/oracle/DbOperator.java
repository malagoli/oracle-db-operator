package com.oracle;

import com.oracle.OrdsClient.OrdsClient;
import com.oracle.OrdsClient.PdbCreationRequestModel;
import io.fabric8.kubernetes.api.model.*;

import io.radanalytics.operator.common.AbstractOperator;
import io.radanalytics.operator.common.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Operator(forKind = OracleService.class, prefix = "com.oracle")
public class DbOperator extends AbstractOperator<OracleService> {

    private static final Logger log = LoggerFactory.getLogger(DbOperator.class.getName());

    private static String prefix = "oracle";


    private static String DEFAULT_USERNAME="DBUSER";

    public DbOperator() {
    }


    protected void onAdd(OracleService srv) {

        String resName = getResourceName(srv.getName());
        String pdbName = Utilities.sanitizePDBName(resName);

        String pdbUsername = DEFAULT_USERNAME;
        String pdbPassword = Utilities.randomPassword();

        PdbCreationRequestModel request = new PdbCreationRequestModel();
        request.setAdminPwd(pdbPassword);
        request.setAdminUser(pdbUsername);

        if(Utilities.isEnvSet(Constants.Environment.ENV_DB_FILENAME_CONVERSION_PATTERN)) {
            String conversionPattern = Utilities.getEnv(Constants.Environment.ENV_DB_FILENAME_CONVERSION_PATTERN);
            conversionPattern = conversionPattern.replaceAll("##PDBNAME##", pdbName);
            request.setFileNameConversions(conversionPattern);
        }

        request.setPdb_name(pdbName);
        request.setTempSize(srv.getTempStorage());
        request.setTotalSize(srv.getStorage());

        try {
            OrdsClient.createPdb(client, namespace, request);

            Integer connectionManagerPort = Integer.parseInt(
                    Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_PORT)
            );

            Secret secret = new SecretBuilder()
                    .withNewMetadata()
                    .withName(resName)
                    .endMetadata()
                    .addToData("jdbcUrl", Utilities.encodeBase64(getJdbcUrl( pdbName )))
                    .addToData("username", Utilities.encodeBase64(pdbUsername))
                    .addToData("passwd", Utilities.encodeBase64(pdbPassword))
                    .build();

            KubernetesList resources = new KubernetesListBuilder()
                    .addToSecretItems(secret)
                    /*.addToServiceItems(service)*/
                    /*.addToDeploymentItems(deployment)*/
                    .build();


            client.resourceList(resources).inNamespace(namespace).createOrReplace();


            log.info("new service has been created: {}", srv);

        } catch (IOException e) {
            log.error("error", e);
        }




        // this create a specific service but since we are using connection manager it is not needed
        /*
        Service service = new  ServiceBuilder()
                .withNewMetadata()
                .withName(resName)
                .endMetadata()
                .withNewSpec()
                .addNewPort()
                .withProtocol("TCP")
                .withPort(connectionManagerPort)
                .withNewTargetPort(connectionManagerPort)
                .endPort()
                .endSpec()
                .build();
*/




/*




;
       /*
       it is just a try, not useful in prod
        ArrayList<Container> containers = new ArrayList<>();


            Container container = new ContainerBuilder()
                    .withImage("fra.ocir.io/emeaseitalysandbox/ocm:1.0.0")
                    .withName(resName)
                    .build();

            containers.add(container);
        

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(resName)
                .addToLabels("app", resName)
                .endMetadata()
                .withNewSpec()
                .withNewSelector()
                .addToMatchLabels("app", resName)
                .endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .withName(resName)
                .addToLabels("app", resName)
                .endMetadata()
                .withNewSpec()
                .addAllToContainers(containers)
                .endSpec()
                .endTemplate()
                .endSpec().build();

*/



    }

    protected void onDelete(OracleService srv) {

        String resName = getResourceName(srv.getName());

        try {
            OrdsClient.deletePdb(client, namespace, Utilities.sanitizePDBName(resName));

            client.services().inNamespace(namespace).withName(getResourceName(srv.getName())).delete();
            client.secrets().inNamespace(namespace).withName(getResourceName(srv.getName())).delete();

        } catch (IOException e) {
            log.error("error", e);
        }



    }

    protected void onModify(OracleService example) {
        log.info("existing example has been modified: {}", example);
    }


    private static String getResourceName(String app) {
        return prefix + "-" + app;
    }

    private static String getJdbcUrl(String serviceName) {
        /*return "jdbc:oracle:thin:@(DESCRIPTION = (TRANSPORT_CONNECT_TIMEOUT=3)(CONNECT_TIMEOUT=60)(RETRY_COUNT=20)(RETRY_DELAY=3)(FAILOVER=ON)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=tcp)(HOST="+Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_NAME
                +"))(PORT="+ Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_PORT +"))))(CONNECT_DATA=(SERVICE_NAME="+serviceName+")))";
        */
        return  "jdbc:oracle:thin:@" + Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_NAME)
                + ":" + Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_PORT) + "/" + serviceName;


    }


}