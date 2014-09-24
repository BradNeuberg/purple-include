/* 
	Copyright 2007 Bootstrap Foundation.

	This code is under the GPL version 2 or later.
	
	@author Brad Neuberg 
*/

package org.hyperscope.purple.include.address;

import java.net.URL;

class LineAddress implements Address{
	public String resolve(String infileAddress, 
							String content, 
							String mimeType) throws AddressException{
		return "LineAddress called";
	}
}