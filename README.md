#purple-include
##Granular transclusions for the common man.

Iteration 3 (December 25th, 2007)

##INTRODUCTION

purple-include is a client-side JavaScript library that allows you
to do client-side transclusions.

What the heck does that mean?

It means that you can include and display fragments of one HTML
page in another without copying and pasting any content.  For
example, you could quote the second paragraph from another
person's blog entry by embedding something like:

    &lt;div href="http://foo.com/bar.html#xpath(/p[2])"&gt;&lt;/div&gt;

in your blog page.  The expression following the explanation point
in the URL is an XPath expression.

If the page you want to transclude has a fragment identifier or a
purple number, you can transclude that directly:

    &lt;div href="http://foo.com/purple.html#nid32"&gt;&lt;/div&gt;

In fact, all you have to do is add an 'href' attribute to any of the following
types of HTML tags in order to have that URL transcluded right into the page
when the page loads:

&lt;p href="http://foobar.com#nid32"&gt;&lt;/p&gt;

&lt;blockquote href="http://foobar.com#xpath(/p[2])"&gt;&lt;/blockquote&gt;

&lt;div href="includeme.html#foobar"&gt;&lt;/div&gt;

&lt;span href="../../relativefile.html#foobar"&gt;&lt;/span&gt;

&lt;q href="http://foobar.com#foobar"&gt;&lt;/q&gt;

##INSTALLATION

There is no installation anymore! Brad Neuberg hosts everything on his webserver
now, at codinginparadise.org, even the inclusion service and
JavaScript, so all you have to do is add the following JavaScript
to the top of your page:

&lt;script src="http://codinginparadise.org/projects/purple-include/purple-include.js"&gt;&lt;/script&gt;

You can manually host everything yourself if you have a high
traffic web-site; see the JavaScript itself for how to configure
things in this scenario. Brad might ask you to do this if you end
up hosting something with massive traffic, but things should be
fine for the foreseeable future.

##EXAMPLES

See tests/examples.html if you have the source layout downloaded
(or http://codinginparadise.org/projects/purple-include/tests/examples.html
if you don't)

##OUR STORY

purple-include is one of many projects in the "Purple" universe --
simple tools that implement some of Doug Engelbart's many ideas in
today's world.  It was developed by core members of the HyperScope
(http://hyperscope.org/) team (Brad Neuberg, Jonathan Cheyer, and
Eugene Eric Kim) in response to the question, "What's something
cool, simple, and HyperScope-inspired we could hack in a couple of
hours?"

purple-include is an extension of Mark Nottingham's
most excellent hack, hinclude.js:

    http://www.mnot.net/javascript/hinclude/

We used Tony Chang's interactive XPath tester to test our XPath
expressions, then ended up stealing some of his code too:

    http://ponderer.org/download/xpath/

The hosted inclusion gateway is based off of the HyperScope
XHTML transformer, built by Brad Neuberg as part of the HyperScope
project.

THE FUTURE

The coolness doesn't end here.  There are a bunch of small hacks
we can make to the code that will really make this interesting.
See TODO and HAIRYISSUES for more ideas.

LICENSE

Apache 2 license.

The hosted inclusion gateway, inside the server/ directory,
used the HyperScope XHTML Transformer as a starting point,
and is therefore under the GPL.
