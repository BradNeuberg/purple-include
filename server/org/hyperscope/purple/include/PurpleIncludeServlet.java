/* 
	Copyright 2007 Bootstrap Foundation.

	This code is based on the HyperScope XHTML Transformer, and is therefore
	under the GPL Version 2 or later.
	
	@author Brad Neuberg 
*/

package org.hyperscope.purple.include;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONException;

import org.hyperscope.purple.include.address.Resolver;

public class PurpleIncludeServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req,
						HttpServletResponse res)
							throws IOException,
									ServletException,
									MalformedURLException {
		//System.out.println("\nNew Request");
		String urlParam = req.getParameter("url");
		String address = req.getParameter("address");
		String callback = req.getParameter("callback");
		//System.out.println("url="+urlParam);
		//System.out.println("address="+address);
		//System.out.println("callback="+callback);
		
		String infileAddress = urlParam;
		if(address != null && address.trim().equals("") == false){
			infileAddress = urlParam + "#" + address;
		}
		//System.out.println("infileAddress="+infileAddress);
		
		// FIXME: Make sure the callback only has the following
		// characters (same as Yahoo's JSONP functions):
		// "Callback function names may only use upper and 
		// lowercase alphabetic characters (A-Z, a-z), numbers 
		// (0-9), the period (.), the underscore (_), and 
		// brackets ([ and ]). Brackets must be URL-encoded"
		
		String results = null;
		try{
			URL url = new URL(infileAddress);
			//System.out.println("url="+url);
			results = Resolver.resolve(url);
		}catch(Exception e){
			e.printStackTrace();
			returnError(res, urlParam, address, callback, e);
			return;
		}
		
		// return results
		returnResults(res, results, urlParam, address, callback, false);
	}
	
	private void returnResults(HttpServletResponse res,
								String content,
								String url,
								String address,
								String callback,
								boolean error)
										throws IOException,
												ServletException{
		try{
			res.setContentType("text/javascript");
			PrintWriter out = res.getWriter();
		
			// write the results out as JSON
			out.write(callback);
			out.write("(");
		
			if(!address.trim().equals("")){
				url = url + "#" + address;
			}
		
			JSONObject results = new JSONObject();
			results.put("url", url);
			results.put("content", content);
			results.put("error", error);
		
			out.write(results.toString());
		
			out.write(");");
			out.close();
		}catch(JSONException e){
			throw new ServletException((Exception)e);
		}
	}
	
	private void returnError(HttpServletResponse res,
							String url,
							String address, 
							String callback,
							Exception e)
										throws IOException,
												ServletException{
		String message;
		
		if(e instanceof SecurityException){
			message = "You do not have permission to access this resource";
		}else if(e instanceof IOException){
			message = "Error while fetching resource: " + e.toString();
		}else{
			// turn the stack trace into a string
			message = "Server error: " + e.toString();
		}
		
		returnResults(res, message, url, address, callback, true);
	}
}