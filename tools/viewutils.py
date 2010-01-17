
from genshi import XML, Markup, Stream

def striptags(x):
    if isinstance(x, (Stream, Markup)):
        return Markup(x).striptags()
    else:
        return x
