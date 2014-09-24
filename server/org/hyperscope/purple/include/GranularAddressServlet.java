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

import org.hyperscope.purple.include.address.Resolver;

public class GranularAddressServlet extends HttpServlet {
	
	/*
		We proxy this servlet to the actual remote web
		site:
		http://brad.com:8080/
	*/
	public void doGet(HttpServletRequest req,
						HttpServletResponse res)
							throws IOException,
									ServletException,
									MalformedURLException {
		//System.out.println("\nNew Request");
		String urlParam = req.getParameter("url");
		String address = req.getParameter("address");
		//System.out.println("url="+urlParam);
		//System.out.println("address="+address);
		
		String infileAddress = urlParam;
		if(address != null && address.trim().equals("") == false){
			infileAddress = urlParam + "#" + address;
		}
		
		String results = null;
		try{
			URL url = new URL(infileAddress);
			//System.out.println("url="+url);
			results = Resolver.resolve(url);
		}catch(Exception e){
			e.printStackTrace();
			returnError(res, e);
			return;
		}
		
		// return results
		returnResults(res, results, false);
	}
	
	private void returnResults(HttpServletResponse res,
								String content,
								boolean error)
										throws IOException,
												ServletException{
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
	
		out.write(content);

		out.close();
	}
	
	private void returnError(HttpServletResponse res,
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
		
		returnResults(res, message, true);
	}
}