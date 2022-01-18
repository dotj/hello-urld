FROM mozilla/sbt
ENV APP_HOME /service
WORKDIR $APP_HOME
COPY ./ $APP_HOME
ENTRYPOINT ["sbt", "run"]