# authenticatePKIUser
Java Servlet that provides REST service to integrate with Active Directory for PKI DN Lookup

## Properties File
This assuming a file named ldap.properties exists in /WEB-INF/ldap.properties with the following information:

```sh
userpwd=secretpwd
user=ad_domain\\username
ldapconn=ldap://myservver.abc.com:389/dc=myorg,dc=com
```