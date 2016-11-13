Prerequisites:
1. JDK 8u111
2. Apache Derby (bundled with JDK)
4. Wildfly 10.1

Setting up the project:
1. Run "gradlew build" in the project root.
2. Make sure Derby is running.
3. Run dataapp-loader.jar which sets up the database.
4. Install the Derby JDBC driver into Wildfly:
4.1 Start Wildfly.
4.2 Connect to it by running <Wildfly installation dir>\bin\jboss-cli.bat/sh
4.3 Then execute the following commands:
    module add --name=apache.derby.driver --resources=<path to derbyclient.jar> --dependencies=javax.api
    /subsystem=datasources/jdbc-driver=derby:add(driver-name=derby,driver-module-name=apache.derby.driver,driver-class-name=org.apache.derby.jdbc.ClientDriver)

Run the app:
1. Deploy dataapp-server to Wildfly.
2. Run dataapp-client.
