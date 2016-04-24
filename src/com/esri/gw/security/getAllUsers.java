package com.esri.gw.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
//import javax.naming.NamingException;
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
//import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name="getAllUsers", urlPatterns="/getAllUsers")
public class getAllUsers extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Properties properties = null;
	
	static Logger mainLogger = Logger.getLogger("authenticateUser");

    /**
     * Default constructor. 
     */
    public getAllUsers() {
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
		
		List<String> members = getusers();

	    // put some value pairs into the JSON object .
	    try {
	    	if (members.size() > 0){
	    		Collections.sort(members);
	    		json.put("success",true);
	    		json.put("numusers", members.size());
	    		JSONArray names = new JSONArray();
	    		for (String temp : members) {
	    			JSONObject user = new JSONObject();
	    			String[] vals = temp.split(Pattern.quote("||"));
	    			// Ignore any values that didn't have a cn and email returned
	    			if (vals.length == 2){
	    				for(int i =0; i < vals.length; i++) {
		    				String[] subvals = vals[i].split(Pattern.quote("::"));
		    				if (subvals[0].equalsIgnoreCase("cn")) {
		    					user.put("name", subvals[1]);
		    					user.put("fullname", subvals[1]);
				    			user.put("description", subvals[1]);
		    				}
		    				else if (subvals[0].equalsIgnoreCase("mail")) {
			    				user.put("email", subvals[1]);
			    			}
	    				}
	    				names.put(user);
	    			}
	    		}
	    		json.put("users", names);
	    	}
	    	else {
	    		json.put("success",false);
	    		json.put("users", "");
	    	}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	    // finally output the json string       
	    out.print(json.toString());
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private List<String> getusers()
	{
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, properties.getProperty("ldapconn"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, properties.getProperty("user"));
        env.put(Context.SECURITY_CREDENTIALS, properties.getProperty("userpwd"));

        DirContext ctx = null;
        NamingEnumeration<?> results = null;
        List<String> grpmembers = new ArrayList<String>();
        
        try {
            ctx = new InitialDirContext(env);
            //String userdn = "OU=Security Groups,OU=Managed Objects,DC=esri,DC=com";
			SearchControls searchCtrls = new SearchControls();
			searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String[] atts = {"cn", "mail"};
			searchCtrls.setReturningAttributes(atts);
 
            //Change the NameOfGroup for the group name you would like to retrieve the members of.
			String filter = "(&(objectClass=person))";
 
            //use the context we created above and the filter to return all members of a group.
			NamingEnumeration<?> values = ctx.search("", filter, searchCtrls);

			//Loop through the search results
			while (values.hasMoreElements()) {
				SearchResult sr = (SearchResult)values.next();
				Attributes attrs = sr.getAttributes();
 
				if (attrs != null)
				{
					String memberinfo = "";
					for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMore();)
					{
						Attribute atr = (Attribute) ae.next();
						String attribute = (String)atr.getID();
						for (NamingEnumeration<?> e = atr.getAll();e.hasMore();) {
							memberinfo = memberinfo + attribute + "::" + (String)e.nextElement() + "||";
						}
					}
					if (memberinfo.endsWith("||")) {
						memberinfo = memberinfo.substring(0, memberinfo.length() - 2);
					}
					grpmembers.add(memberinfo);
				}
				else {
					return null;
				}
			}
			return grpmembers;
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
}
