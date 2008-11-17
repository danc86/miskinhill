
from sqlalchemy import *
from sqlalchemy.orm import *
from sqlalchemy.pool import SingletonThreadPool
import genshi

from config import DB

__all__ = ['DbSession', 'Journal', 'Issue', 'Author', 'Article']

engine = create_engine(DB, convert_unicode=True,
        poolclass=SingletonThreadPool, pool_size=20,
        echo=True)
metadata = MetaData(bind=engine, reflect=True)
DbSession = sessionmaker(bind=engine, autoflush=True, transactional=False)

class Journal(object):

    pass

class Issue(object):

    pass

class Author(object):

    @property
    def name(self):
        return u'%s %s' % (self.given_names, self.surname)

class Article(object):

    @property
    def title(self):
        return genshi.Markup(self._title)

mapper(Journal, metadata.tables['journals'])
mapper(Issue, metadata.tables['issues'], properties={
    '_journal': metadata.tables['issues'].c.journal, 
    'journal': relation(Journal, backref='issues'), 
    'articles': relation(Article, order_by=metadata.tables['articles'].c.order, backref='issue')
})
mapper(Author, metadata.tables['authors'], properties={
    'articles': relation(Article, backref='author')
})
mapper(Article, metadata.tables['articles'], properties={
    '_issue': metadata.tables['articles'].c.issue, 
    '_author': metadata.tables['articles'].c.author, 
    '_title': metadata.tables['articles'].c.title # moved so that it can be a Markup property
})
