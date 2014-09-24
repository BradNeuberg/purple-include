/* 
	Copyright 2007 Bootstrap Foundation.

	This code is under the GPL version 2 or later.
	
	@author Brad Neuberg 
*/

package org.hyperscope.purple.include.address;

import java.net.URL;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import org.w3c.tidy.Tidy;

import net.sf.saxon.om.NamespaceConstant;

/**
	Resolves a given URL into an actual string fragment
	that can be inlined into a document.
*/
public class Resolver{
	/** The default address type if none is specified. */
	public static String DEFAULT_TYPE = "xpath";
	
	public static String resolve(URL url) 
						throws AddressException{
		try{
			//System.out.println("resolve, url="+url);
			// make sure URL is safe
			assertSafe(url);
			
			//System.out.println("url is safe");
		
			// get the content
			String content = getContent(url);
		
			// TODO: Get the remote MIME type
			// TODO: Make sure we have the right XHTML MIME type defined
			// here
			String mimeType = "text/xml";
		
			// tidy up the content and turn it
			// into XML (XHTML)
			// TODO: Don't do this for text files
			String xml = tidyHTML(content);
		
			// apply the infile-address expression
			String fragment = applyAddress(url, xml, mimeType);
			
			return fragment;
		}catch(Exception e){
			throw new AddressException(e);
		}
	}
	
	/**
		Ensures that this is a safe URL.
	*/
	protected static void assertSafe(URL url) throws SecurityException{
		// secure protocol?
		if(!url.getProtocol().equals("http") &&
			!url.getProtocol().equals("https")){
			throw new SecurityException("Protocol not allowed");
		}
			
		// Loopback address or DNS name that is
		// only resolvable behind the firewall?
		if(url.getHost().equals("localhost")
			|| url.getHost().equals("127.0.0.1")
			|| url.getHost().indexOf(".local") != -1
			|| url.getHost().indexOf(".") == -1){
			throw new SecurityException("You do not have permission "
										+ "to access this host");
		}
	}
	
	private static String getContent(URL url) 
						throws HttpException,
								IOException,
								SecurityException{
		//System.out.println("Getting content");
		// get the contents
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url.toString());
		//System.out.println("executing method");
		int statusCode = client.executeMethod(method);
		//System.out.println("executed");
		
		// make sure it executed correctly
		if(statusCode != HttpStatus.SC_OK){
			throw new HttpException("Request failed: "
									+ method.getStatusLine());
		}
		
		// make sure we have HTML or XHTML
		String mimeType = method.getResponseHeader("Content-type").toString();
		//System.out.println("mimeType="+mimeType);
		if(mimeType.indexOf("text/html") == -1
			&& mimeType.indexOf("application/xhtml+xml") == -1
			&& mimeType.indexOf("text/xml") == -1
			&& mimeType.indexOf("application/xml") == -1){
				throw new SecurityException("Insecure MIME type returned");
		}
		
		// get the actual response returned; should automatically
		// use the underlying response character encoding when
		// creating the string
		String content = method.getResponseBodyAsString();
		//System.out.println("content="+content);
		method.releaseConnection();
		
		return content;
	}
	
	private static String tidyHTML(String content)
									throws IOException{
		Tidy tidy = new Tidy();
		
		// set configuration values
		tidy.setDropEmptyParas(false); // drop empty P elements
		tidy.setDocType("omit"); // omit the doctype
		tidy.setEncloseBlockText(true); // wrap blocks of text in P elements
		tidy.setEncloseText(true); // wrap text right under BODY element in P elements
		tidy.setHideEndTags(false); // force optional end tags
		tidy.setIndentContent(false); // indent content for easy reading
		tidy.setLiteralAttribs(false); // no new lines in attributes
		tidy.setLogicalEmphasis(false); // replace i and b by em and strong, respectively
		tidy.setMakeClean(false); // strip presentational cruft
		tidy.setNumEntities(true); // convert entities to their numeric form
		tidy.setWord2000(true); // strip Word 2000 cruft
		tidy.setXHTML(true); // output XHTML
		tidy.setXmlPi(true); // add <?xml?> processing instruction
		
		// parse
		StringReader in = new StringReader(content);
		StringWriter out = new StringWriter();
		tidy.parse(in, out);
		in.close();
		out.close();
		String results = out.toString();
		
		// remove the XML namespace declaration,
		// since it makes trouble for us in the XPath
		// evaluator
		// FIXME: this is ghetto and needs to be fixed
		// with a namespace evaluator in the XPath section,
		// but namespace evaluators are a pain in the butt
		// to get working
		
		// String.replace() does not work on 1.4 JVMs when compiled
		// with 1.5 JVMs, even with target="1.4" (this is a known Java
		// bug). Using workaround instead. -- Brad Neuberg
		//results = results.replace("xmlns=\"http://www.w3.org/1999/xhtml\"", "");
		
		StringBuffer buffer = new StringBuffer(results);
		int startCut = buffer.indexOf("xmlns=\"http://www.w3.org/1999/xhtml\"");
		if(startCut != -1){
			buffer.replace(startCut, 
							startCut + "xmlns=\"http://www.w3.org/1999/xhtml\"".length(), 
							"");
			results = buffer.toString();
		}
		
		//System.out.println("tidied results="+results);
				
		return results;
	}
	
	/** 
		Normalize a default address where no type is specified
		into something we can work with. 
	*/
	protected static String normalizeDefault(String anchor){
		//System.out.println("normalizeDefault, anchor="+anchor);
		if(anchor != null && anchor.trim().equals("") == false){
			anchor = "xpath(//a[@name='" + anchor + "' or "
					 + "@id='" + anchor + "']/.."
					 + " | "
					 + "//p[@name='" + anchor + "' or "
					 + "@id='" + anchor + "'])";
		}else{
			anchor = "xpath(/body)";
		}
		
		//System.out.println("normalized anchor="+anchor);
		return anchor;
	}
	
	protected static String applyAddress(URL url, 
											String content,
											String mimeType)
												throws AddressException,
												ClassNotFoundException,
												InstantiationException,
												IllegalAccessException{
		// extract the anchor section of the URL
		String anchor = url.getRef();
		
		// grab the address type
		Pattern typePattern = Pattern.compile("^(\\w{2,})\\(.*\\)$");
		Matcher m = typePattern.matcher(anchor);
		if(m.matches() == false){ // default type
			anchor = normalizeDefault(anchor);
			m = typePattern.matcher(anchor);
		}
		
		if(m.matches() == false){
			throw new AddressException("Invalid address given: " + url);
		}
		
		String type = m.group(1);
		//System.out.println("type match=" + type);
		
		// instantiate the class that can handle this
		StringBuffer typeClass = new StringBuffer();
		typeClass.append("org.hyperscope.purple.include.address.");
		char firstLetter[] = new char[1];
		firstLetter[0] = type.charAt(0);
		typeClass.append(new String(firstLetter).toUpperCase());
		typeClass.append(type.substring(1));
		typeClass.append("Address");
		//System.out.println("class name = "+typeClass);
		Class makeMe = Class.forName(typeClass.toString());
		Address address = (Address)makeMe.newInstance();
		
		// get the infile address to hand to this address;
		// (i.e. everything in parantheses)
		String infileAddress = anchor.substring(type.length() + 1, anchor.length() - 1);
		//System.out.println("infileAddress=" + infileAddress);
		
		// handle the address
		String results = address.resolve(infileAddress, content, mimeType);
		
		// return its values
		return results;
	}
	
	
}