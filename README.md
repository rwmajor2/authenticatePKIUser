# authenticatePKIUser
Java Servlet that provides REST service to integrate with Active Directory for PKI DN Lookup

## Properties File
This assuming a file named ldap.properties exists in /WEB-INF/ldap.properties with the following information:

```sh
userpwd=secretpwd
user=ad_domain\\username
ldapconn=ldap://myserver.abc.com:389/dc=myorg,dc=com
```

## web.xml sample
```sh
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.4" xmlns="http://java.sun.com/xml/ns/j2ee" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="
    http://java.sun.com/xml/ns/j2ee
    http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">
	
	<servlet>
		<servlet-name>CheckDN</servlet-name>
		<servlet-class>com.esri.gw.security.authenticateUser</servlet-class>
	</servlet>
	
	<servlet-mapping>
		<servlet-name>CheckDN</servlet-name>
		<url-pattern>/authenticateUser</url-pattern>
	</servlet-mapping>
	
</web-app>
```

## Sample usage
http://myserver.esri.com/checkDN/authenticateUser?dn=CN=Joe%20Smith,OU=Users,OU=Departments,DC=esri,DC=com