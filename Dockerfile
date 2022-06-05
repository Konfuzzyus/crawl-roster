FROM eclipse-temurin:17-jre
ADD ./build/distributions/crawl-roster-0.1.tar /
EXPOSE 8080
ENTRYPOINT ["/crawl-roster-0.1/bin/crawl-roster"]