CREATE DATABASE blog_repository;

CREATE USER bloguser WITH PASSWORD 'yourPassword';

GRANT ALL ON DATABASE blog_repository TO bloguser;