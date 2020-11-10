
#
# Super simple example of a Dockerfile
# commands to add: https://www.digitalocean.com/community/tutorials/how-to-remove-docker-images-containers-and-volumes
# docker rm $(docker ps -a -f status=exited -f status=created -q)
#
FROM postgres:12.2-alpine
ARG TOOLKIT_APPLICATION
ARG TOOLKIT_TYPE
ARG TOOLKIT_VERSION

LABEL application=$TOOLKIT_APPLICATION
LABEL type=$TOOLKIT_TYPE
LABEL version=TOOLKIT_VERSION

ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD not24get
ENV PGDATA /var/lib/postgresql/data
EXPOSE 5432:5432
add  init-user-db.sh /docker-entrypoint-initdb.d/init-user-db.sh