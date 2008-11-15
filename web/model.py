
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
mapper(Issue, metadata.tables['issues'])
mapper(Author, metadata.tables['authors'])
mapper(Article, metadata.tables['articles'])
