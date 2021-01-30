drop table IF EXISTS fantarose;
drop table IF EXISTS allenatori ;
drop table IF EXISTS giocatori;
drop table IF EXISTS configurazione;
drop table IF EXISTS loggerMessaggi;
create table IF NOT EXISTS  loggerMessaggi (
    id LONG not null
);
create table IF NOT EXISTS  fantarose (
    idgiocatore INT not null
);
create table IF NOT EXISTS  allenatori (
    id INT not null
);
create table IF NOT EXISTS  giocatori (
    id INT not null
);
create table IF NOT EXISTS  configurazione (
    id INT not null
);
#INSERT INTO configurazione (id)   SELECT 1 FROM DUAL WHERE NOT EXISTS   (SELECT id FROM configurazione WHERE id=1);
