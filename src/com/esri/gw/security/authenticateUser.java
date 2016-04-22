package com.esri.gw.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.*;

/**
 * Servlet implementation class authenticateUser
 */
@WebServlet(name="authenticateUser", urlPatterns="/authenticateUser")
public class authenticateUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Properties properties = null;
	
	static Logger mainLogger = Logger.getLogger("authenticateUser");

    /**
     * Default constructor. 
     */
    public authenticateUser() {
        // TODO Auto-generated constructor stub
    }
    

    public void init()
    {
    	ServletContext context = getServletContext();
    	InputStream inputStream = context.getResourceAsStream("/WEB-INF/ldap.properties");
		properties = new Properties();

        // load the inputStream using the Properties
        try {
			properties.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();

		//create Json Object
		JSONObject json = new JSONObject();
		
		String user = request.getParameter("dn");
		if (user == null)
		{
			// put some value pairs into the JSON object .
		    try {
		    	json.put("success",false);
		    	json.put("error", "No DN parameter passed.");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    // finally output the json string       
		    out.print(json.toString());
		}
		else
		{
			Attributes atts = getLDAPAttributes(user);
			Attribute cnatt = atts.get("cn");
			Attribute mailatt = atts.get("mail");
			Attribute useridatt = atts.get("sAMAccountName");
            String cn = null;
            String mail = null;
            String userid = null;
			try {
				cn = (String) cnatt.get();
				mail = (String) mailatt.get();
				userid = (String) useridatt.get();
			} catch (NamingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
		    // put some value pairs into the JSON object .
		    try {
		    	if (cn.length() > 0){
		    		json.put("success",true);
		    		json.put("cn", cn);
		    		json.put("email", mail);
		    		json.put("userid", userid);
		    		List<String> groups = getGroups(atts);
		    		JSONArray members = new JSONArray();
		    		for (String temp : groups) {
		    			members.put(temp);
		    		}
		    		json.put("groups", members);
		    	}
		
		    	else {
		    		json.put("success",false);
		    		json.put("cn", "");
		    	}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		    // finally output the json string       
		    out.print(json.toString());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private Attributes getLDAPAttributes(String dn)
	{
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, properties.getProperty("ldapconn"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, properties.getProperty("user"));
        env.put(Context.SECURITY_CREDENTIALS, properties.getProperty("userpwd"));

        DirContext ctx = null;
        NamingEnumeration<?> results = null;
        Attributes attributes = null;
        try {
            ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search("", "(&(objectclass=person)(distinguishedName=" + dn + "))", controls);
            if (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
            }
            return attributes;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }
	}
	
	private List<String> getGroups(Attributes attributes) throws NamingException
	{
        List<String> memberOf = new ArrayList<String>();
       
        for(NamingEnumeration<?> vals = attributes.get("memberOf").getAll(); vals.hasMoreElements();){
        	String member = (String)vals.nextElement();
        	Pattern pattern = Pattern.compile("CN=(.*?),");
        	Matcher matcher = pattern.matcher(member);
        	if (matcher.find()) {
        		memberOf.add(matcher.group(1));
        	}
        }
        return memberOf;
	}
}
