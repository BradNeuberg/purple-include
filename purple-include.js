/*
purple-include.js -- Purple Include (version 2.0)

Based on Mark Nottingham's most excellent hinclude.js (version 0.9)

Copyright (c) 2007 Jonathan Cheyer,
				   Eugene Eric Kim,
				   Brad Neuberg
Copyright (c) 2005-2006 Mark Nottingham

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

-------------------------------------------------------------------------------

See http://blueoxen.net/c/purple/purple-include/ for documentation.
See http://www.mnot.net/javascript/hinclude/ for hinclude documentation.

-------------------------------------------------------------------------------
*/

var purpleInclude = {
	_urlToElement: new Object(),
	_addressService: null,
	_rollerURL: null,
	_completed: 0,
	_handleMe: null,
	_listeners: new Array(),
	
	addLoaded: function(l){
		// summary: Called when we are finished loading
		// and inlining all inclusions
		this._listeners.push(l);
	},

	run: function(){
		this._handleMe = this._newLookupContainer();
		this._rollerURL = this._getMeta("purple.include.rollerURL",
										"http://codinginparadise.org/projects/purple-include/roller.gif");
		this._addressService = this._getMeta("purple.include.addressService",
											"http://codinginparadise.org/purple_include/");	
		// get any div, span, blockquote, pre, or q element that has an href
		// attribute and process it's inclusion;
		// note that using an 'href' is deprecated. From now on, we promote
		// using a blockquote or q element with the 'cite' attribute but leave
		// this for backwards compatibility
		var elems = new Array();
		elems = this._expandArray(elems, document.getElementsByTagName("div"));
		elems = this._expandArray(elems, document.getElementsByTagName("span"));
		elems = this._expandArray(elems, document.getElementsByTagName("p"));
		elems = this._expandArray(elems, document.getElementsByTagName("blockquote"));
		elems = this._expandArray(elems, document.getElementsByTagName("pre"));
		elems = this._expandArray(elems, document.getElementsByTagName("q"));
		
		for(var i = 0; i < elems.length; i++){
			var e = elems[i];
			var href = e.getAttribute("cite") || e.getAttribute("href");
			if(href){
				e.innerHTML = '<img width="16" height="16" '
								+ 'style="width: 16px !important; height: 16px !important;" '
								+ 'class="include_roller" '
								+ 'src="' + this._getRollerPath() + '" />';
				this._handleMe.add(href, e);
			}
		}
		
		for(var i = 0; i < this._handleMe.length(); i++){
			var current = this._handleMe.get(i);
			this._include(current.href, current.elements);
		}
	},
	
	_setContentAsync: function(results){
		try{
			var url = results.url;
			var html = results.content;
			var error = results.error;			
			var lookupObj = this._handleMe.find(url);
			var elements = lookupObj.elements;
			for(var i = 0; i < elements.length; i++){
				if(!error){
					elements[i].className = "included include_ok";
					try{
						elements[i].innerHTML = html;
					}catch(exp){ // IE doesn't like XHTML style tags: <a name=""/> for example
						elements[i].innerHTML = "";
						html = "This browser can not handle the remote HTML for " + url + ": ";
						html += (exp.message) ? exp.message : exp;
						elements[i].appendChild(document.createTextNode(html));
						elements[i].className += " include_error";
					}
				}else{
					html = "Include error for " + url + ": " + html;
					elements[i].innerHTML = "";
					elements[i].appendChild(document.createTextNode(html));
					elements[i].className += " include_error"; 
				}
			}
			
			// are we done?
			this._completed++;
			if(this._completed == this._handleMe.length()){
				// re-execute jumping to any anchors on this page
				// since the page length has changed due to inclusions
				if(window.location.hash){
					var anchor = window.location.hash;
					if(anchor.charAt(0) == "#"){
						anchor = anchor.substring(1, anchor.length);
					}
					if(document.getElementsByName && anchor != ""){
						var elems = document.getElementsByName(anchor);
						for(var i = 0; i < elems.length; i++){
							var name = elems[i].getAttribute("name");
							if(name == anchor && elems[i].scrollIntoView){
								elems[i].scrollIntoView();
								break;
							}
						}
					}
				}
				
				for(var i = 0; i < this._listeners.length; i++){
					this._listeners[i].call();
				}
			}
		}catch(exp){
			// TODO: Print exception into element's innerHTML
			this.debug(exp);
		}
	},
	
	_include: function(url, elements){
		var scheme = url.substring(0,url.indexOf(":"));
 
		if(scheme.toLowerCase() == "data"){ // just text/plain for now
			data = unescape(url.substring(url.indexOf(",") + 1, url.length));
		
			for(var i = 0; i < elements.length; i++){
				element.innerHTML = data;
			}
		}else{
			// parse out the anchor
			var oldURL = url;
			url = decodeURI(url);
			var anchor = url.match(/#(.*)$/);
			if(!anchor){
				anchor = "";
			}else{
				anchor = anchor[1];
			}
				
			if(this._isRelative(url)){
				url = this._expandRelativeURL(url);
			}
			
			// update our URL mapping to the new expanded
			// URL
			this._handleMe.update(oldURL, url);
			
			url = url.match(/^([^#]*)/)[1];

			// talk to our hosted infile addressing 
			// service
			var scriptSrc = this._addressService
								+ "?url=" + encodeURIComponent(url) 
								+ "&address=" + encodeURIComponent(anchor) 
								+ "&callback=purpleInclude._setContentAsync";	
			var script = document.createElement("script");                        
			script.setAttribute("src", scriptSrc);     
			document.getElementsByTagName("head")[0].appendChild(script);
		}
	},
	
	_getRollerPath: function(){
		return this._rollerURL;
	},
	
	_expandArray: function(oldArray, newArray){
		for(var i = 0; i < newArray.length; i++){
			oldArray.push(newArray[i]);
		}

		return oldArray;
	},

	_getMeta: function(name, value_default){
		var metas = document.getElementsByTagName("meta");
		for(var m=0; m < metas.length; m++){
			var meta_name = metas[m].getAttribute("name");
			if(meta_name == name){
				return metas[m].getAttribute("content");
			}
		}
		return value_default;
	},
	
	_isRelative: function(url){
		return !(/^https?:/i.test(url));
	},
	
	_expandRelativeURL: function(url){
		var results;
		
		// get where we are right now
		var scheme = window.location.protocol;
		// some browsers have the : at the end, some don't
		if(scheme.charAt(scheme.length - 1) == ":"){
			scheme = scheme.substring(0, scheme.length - 1);
		}
		var port = (window.location.port) ? window.location.port : 80;
		var hostname = window.location.hostname;
		
		if(url.charAt(0) == "/"){
			results = scheme + "://" + hostname + ":" + port + url;
			return results;
		}
		
		var path = window.location.pathname;
		while(path.length && path.charAt(path.length - 1) != "/"){
			path = path.substring(0, path.length - 1);
		}
		if(path.length == 0){
			path = "/";
		}
		
		results = scheme + "://" + hostname + ":" + port + path + url;
		return results;
	},
	
	/** A container for our elements and href's */
	_newLookupContainer: function(){
		var Container = function(){
			this._list = new Array();
		}
		
		Container.prototype.add = function(href, element){
			var result = this._list["_" + href];
			if(!result){
				var addMe = [];
				addMe.push(element);
				var obj = {href: href, elements: addMe};
				this._list.push(obj);
				this._list["_" + href] = obj;
			}else{
				result.elements.push(element);
			}
		}
		
		Container.prototype.update = function(oldHref, newHref){
			var r = this.find(oldHref);
			if(!r){
				return;
			}
			this._list["_" + oldHref] = undefined;
			this._list["_" + newHref] = r;
		}
		
		Container.prototype.get = function(i){
			return this._list[i];
		}
		
		Container.prototype.find = function(href){
			var r = this._list["_" + href];
			return r || null;
		}
		
		Container.prototype.length = function(){
			return this._list.length;
		}
		
		return new Container();
	},
	
	debug: function(msg){
		var p = document.createElement("p");
		p.appendChild(document.createTextNode(msg));
		document.body.appendChild(p);
	},
	
	/*
	 * (c)2006 Dean Edwards/Matthias Miller/John Resig
	 * Special thanks to Dan Webb's domready.js Prototype extension
	 * and Simon Willison's addLoadEvent
	 *
	 * For more info, see:
	 * http://dean.edwards.name/weblog/2006/06/again/
	 * http://www.vivabit.com/bollocks/2006/06/21/a-dom-ready-extension-for-prototype
	 * http://simon.incutio.com/archive/2004/05/26/addLoadEvent
	 * 
	 * Thrown together by Jesse Skinner (http://www.thefutureoftheweb.com/)
	 *
	 *
	 * To use: call addDOMLoadEvent one or more times with functions, ie:
	 *
	 *	  function something() {
	 *		 // do something
	 *	  }
	 *	  addDOMLoadEvent(something);
	 *
	 *	  addDOMLoadEvent(function() {
	 *		  // do other stuff
	 *	  });
	 *
	 */ 
	addDOMLoadEvent: function(func){
	   if(!window.__load_events){
		  var init = function(){
			  // quit if this function has already been called
			  if(arguments.callee.done) return;
		  
			  // flag this function so we don't do the same thing twice
			  arguments.callee.done = true;
		  
			  // kill the timer
			  if(window.__load_timer){
				  clearInterval(window.__load_timer);
				  window.__load_timer = null;
			  }
			  
			  // execute each function in the stack in the order they were added
			  for(var i=0;i < window.__load_events.length;i++){
				  window.__load_events[i]();
			  }
			  window.__load_events = null;
			  
			  // clean up the __ie_onload event
			  /*@cc_on @*/
			  /*@if (@_win32)
				  document.getElementById("__ie_onload").onreadystatechange = "";
			  /*@end @*/
		  };
	   
		  // for Mozilla/Opera9
		  if(document.addEventListener){
			  document.addEventListener("DOMContentLoaded", init, false);
		  }
		  
		  // for Internet Explorer
		  /*@cc_on @*/
		  /*@if (@_win32)
			  document.write("<scr"+"ipt id=__ie_onload defer src=javascript:void(0)><\/scr"+"ipt>");
			  var script = document.getElementById("__ie_onload");
			  script.onreadystatechange = function() {
				  if (this.readyState == "complete") {
					  init(); // call the onload handler
				  }
			  };
		  /*@end @*/
		  
		  // for Safari
		  if(/WebKit/i.test(navigator.userAgent)){ // sniff
			  window.__load_timer = setInterval(function(){
				  if(/loaded|complete/.test(document.readyState)){
					  init(); // call the onload handler
				  }
			  }, 10);
		  }
		  
		  // for other browsers
		  window.onload = init;
		  
		  // create event function stack
		  window.__load_events = [];
	   }
	   
	   // add function to event stack
	   window.__load_events.push(func);
	}
}

//purpleInclude.addDOMLoadEvent(function() { purpleInclude.run(); });

if(!window.onload){
	window.onload = function(){ purpleInclude.run(); }
}else{
	var oldOnload = window.onload;
	window.onload = function(){ 
		purpleInclude.run();
		oldOnload();
	}
}