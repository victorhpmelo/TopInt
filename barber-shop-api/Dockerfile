FROM postgres:17.4 AS pgtools

FROM maven:3.9.9-eclipse-temurin-21

ENV INSTALL_PATH=/barber-shop-api

# Versao alternativa: evita apt.postgresql.org e usa os binarios do cliente
# diretamente da imagem oficial do PostgreSQL 17 (mesma major version do banco).
COPY --from=pgtools /usr/lib/postgresql/17 /usr/lib/postgresql/17
COPY --from=pgtools /usr/share/postgresql /usr/share/postgresql

RUN ln -sf /usr/lib/postgresql/17/bin/pg_dump /usr/local/bin/pg_dump \
    && ln -sf /usr/lib/postgresql/17/bin/psql /usr/local/bin/psql

RUN mkdir $INSTALL_PATH

WORKDIR $INSTALL_PATH

COPY . .
