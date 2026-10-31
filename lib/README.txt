Place the MySQL Connector/J JAR file here.

Download from: https://dev.mysql.com/downloads/connector/j/
Select "Platform Independent" ZIP, extract it, and copy the JAR to this folder.

Rename the JAR to: mysql-connector-j-9.2.0.jar

OR, if your downloaded version has a different number (e.g., 8.3.0), 
edit the .classpath file in the project root to match your filename:
  <classpathentry kind="lib" path="lib/mysql-connector-j-YOUR_VERSION.jar"/>

After placing the JAR here, in Eclipse:
  Right-click project -> Refresh (F5)
  The JAR should now appear under "Referenced Libraries".
