CREATE DATABASE crawler;
use crawler;

CREATE TABLE url (
    urlid INT NOT NULL AUTO_INCREMENT,
    url VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY (urlid)
);

CREATE TABLE word (
    word VARCHAR(50) NOT NULL,
    urlid INT NOT NULL,
    FOREIGN KEY (urlid)
        REFERENCES url(urlid),
    PRIMARY KEY (word)
);
