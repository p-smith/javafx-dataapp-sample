Prerequisites:
1. JDK 1.7
2. Apache Derby (bundled with JDK)
3. NetBeans 8
4. Wildfly 8.2 web server

Setting up the project:
1. Open the main (parent) project and its required projects.
2. Build the main project with Maven.
3. Make sure Derby is running.
4. Run the dataapp-loader project which creates the database, then loads some data into it.
5. Install the Derby JDBC driver into Wildfly (if it's not done already):
5.1 Start the web server
5.2 Connect to it by running <Wildfly installation dir>\bin\jboss-cli.bat or jboss-cli.sh if you're on Linux
5.3 Then execute the following commands:
    module add --name=apache.derby.driver --resources=<path to derbyclient.jar> --dependencies=javax.api
    /subsystem=datasources/jdbc-driver=derby:add(driver-name=derby,driver-module-name=apache.derby.driver,driver-class-name=org.apache.derby.jdbc.ClientDriver)

Run the app:
1. Deploy dataapp-server to Wildfly.
2. Run dataapp-client.