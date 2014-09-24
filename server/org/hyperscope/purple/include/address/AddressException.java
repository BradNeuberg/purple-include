/* 
	Copyright 2007 Bootstrap Foundation.

	This code is under the GPL version 2 or later.
	
	@author Brad Neuberg 
*/

package org.hyperscope.purple.include.address;

public class AddressException extends RuntimeException{
	public AddressException(){
		super();
	}
	
	public AddressException(String msg){
		super(msg);
	}
	
	public AddressException(Throwable t){
		super(t);
	}
}