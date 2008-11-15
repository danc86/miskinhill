
from sqlalchemy import *
from sqlalchemy.orm import *
from sqlalchemy.pool import SingletonThreadPool

from config import DB

__all__ = ['DbSession', 'Journal', 'Issue']

engine = create_engine(DB, poolclass=SingletonThreadPool, pool_size=20, echo=True)
metadata = MetaData(bind=engine, reflect=True)
DbSession = sessionmaker(bind=engine, autoflush=True, transactional=False)

class Journal(object):

    pass

class Issue(object):

    pass

class Author(object):

    pass

class Article(object):

    pass

mapper(Journal, metadata.tables['journals'])
mapper(Issue, metadata.tables['issues'], properties={
    '_journal': metadata.tables['issues'].c.journal, 
    'journal': relation(Journal, backref='issues'), 
})
mapper(Author, metadata.tables['authors'])
mapper(Article, metadata.tables['articles'], properties={
    '_issue': metadata.tables['articles'].c.issue, 
    'issue': relation(Issue, backref='articles'), 
    '_author': metadata.tables['articles'].c.author, 
    'author': relation(Author, backref='articles')
})
