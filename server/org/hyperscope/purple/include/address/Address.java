/* 
	Copyright 2007 Bootstrap Foundation.

	This code is under the GPL version 2 or later.
	
	@author Brad Neuberg 
*/

package org.hyperscope.purple.include.address;

/**
	An interface which provides a way to resolve a given URL, with an
	optional anchor portion, into a String which can be inlined into
	an HTML page.
*/
interface Address{
	/**
		Resolves the given infile address against the given content, and
		returns the results as a String suitable for inlining into an 
		HTML document.
		
		@param infileAddress An infile address to apply to the given content,
		such as "//p". This will not have an anchor or the address type
		around it, such as "#xpath1(//p)".
		@param content The full content to apply the anchor address to.
		@param mimeType The MIME type of the content. If this is HTML, then it
		is pre-normalized into XHTML before calling the address.
		@return String Must return the results ready to be inlined into an
		HTML document; if the underlying content is not HTML, such as a text file,
		then this address must HTMLize the return results so they will display
		properly in the context of an HTML document.
	*/
	public String resolve(String infileAddress, 
							String content, 
							String mimeType) throws AddressException;
}