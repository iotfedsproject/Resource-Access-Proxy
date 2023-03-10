FROM openjdk:8-jre-alpine

WORKDIR /home

ENV componentName "ResourceAccessProxy"
ENV componentVersion 3.0.5

#RUN apk --no-cache add \
#	git \
#	unzip \
#	wget \
#	bash \
#	&& echo "Downloading $componentName $componentVersion" \
#	&& wget "https://jitpack.io/com/github/symbiote-h2020/$componentName/$componentVersion/$componentName-$componentVersion-run.jar"
COPY ./ResourceAccessProxy-3.0.5-run.jar ResourceAccessProxy-3.0.5-run.jar

EXPOSE 8103

CMD java $JAVA_HTTP_PROXY $JAVA_HTTPS_PROXY $JAVA_NON_PROXY_HOSTS -DSPRING_BOOT_WAIT_FOR_SERVICES=symbiote-aam:8080 -jar $(ls *run.jar)