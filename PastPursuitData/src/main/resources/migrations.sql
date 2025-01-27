CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY generated always as identity,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    wins INTEGER NOT NULL DEFAULT 0,
    losses INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS results (
    id INTEGER PRIMARY KEY generated always as identity,
    matchId INTEGER,
    userId INTEGER,
    matchResult SMALLINT
);