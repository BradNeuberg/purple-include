/* 
	Copyright 2007 Bootstrap Foundation.

	This code is based on the HyperScope XHTML Transformer, and is therefore
	under the GPL Version 2 or later.
	
	@author Brad Neuberg 
*/

package org.hyperscope.purple.include;

import java.net.URL;

import org.hyperscope.purple.include.address.Resolver;

public class PurpleInclude{
	
	public static void main(String args[]){
		if(args.length == 0){
			System.out.println("Usage:");
			System.out.println("org.hyperscope.purple.include.PurpleInclude url");
			System.exit(1);
		}
		
		try{
			URL url = new URL(args[0]);
			System.out.println("url="+url);
			String results = Resolver.resolve(url);
			System.out.println("results="+results);
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
}