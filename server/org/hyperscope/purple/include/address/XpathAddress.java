/* 
	Copyright 2007 Bootstrap Foundation.

	This code is under the GPL version 2 or later.
	
	@author Brad Neuberg 
*/

package org.hyperscope.purple.include.address;

import java.net.URL;

import java.io.StringWriter;
import java.io.StringReader;

import java.util.List;
import java.util.Iterator;

import javax.xml.*;
import javax.xml.xpath.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import net.sf.saxon.om.*;
import net.sf.saxon.xpath.*;
import net.sf.saxon.trans.*;

import org.xml.sax.*;

class XpathAddress implements Address{
	public String resolve(String infileAddress, 
							String content, 
							String mimeType) throws AddressException{
		try{
			initialize();
			
			StringBuffer results = new StringBuffer();

	        XPathFactory xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
	        XPath xpe = xpf.newXPath();
		
			Source source = (Source)new StreamSource(new StringReader(content));
			NodeInfo doc = ((XPathEvaluator)xpe).setSource(source);
		
			List matches = (List)xpe.evaluate(infileAddress, doc, XPathConstants.NODESET);
			Iterator it = matches.iterator();
			while(it.hasNext()){
				NodeInfo node = (NodeInfo)it.next();
				//System.out.println("node="+node);
				// serialize results
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer serializer = factory.newTransformer();
    
				StringWriter output = new StringWriter();
				serializer.setOutputProperty(OutputKeys.INDENT, "yes");
				serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				serializer.transform(node, new StreamResult(output));

				results.append(output.toString());
			}
			
			return results.toString();
		}catch(Exception e){
			throw new AddressException(e);
		}
	}
	
	protected void initialize(){
		// FIXME: Is this threadsafe? Probably not. Place
		// in better location so that it is run only once.
		System.setProperty("javax.xml.transform.TransformerFactory", 
							"net.sf.saxon.TransformerFactoryImpl");			
		System.setProperty(
				"javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON,
                "net.sf.saxon.xpath.XPathFactoryImpl");
	}
}