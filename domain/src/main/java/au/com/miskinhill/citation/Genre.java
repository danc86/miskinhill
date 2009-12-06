package au.com.miskinhill.citation;

public enum Genre {
    
    book("info:ofi/fmt:kev:mtx:book"),
    bookitem("info:ofi/fmt:kev:mtx:book"),
    thesis("info:ofi/fmt:kev:mtx:book", "document"),
    proceeding("info:ofi/fmt:kev:mtx:book"),
    article("info:ofi/fmt:kev:mtx:journal");

    private final String coinsFormat;
    private final String coinsGenre;
    
    private Genre(String coinsFormat) {
        this.coinsFormat = coinsFormat;
        this.coinsGenre = name();
    }
    
    private Genre(String coinsFormat, String coinsGenre) {
        this.coinsFormat = coinsFormat;
        this.coinsGenre = coinsGenre;
    }
    
    public String getCoinsFormat() {
        return coinsFormat;
    }
    
    public String getCoinsGenre() {
        return coinsGenre;
    }
    
}
