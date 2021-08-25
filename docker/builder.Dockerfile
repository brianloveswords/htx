FROM debian:latest

ARG GRAAL_VERSION=21.2.0
ARG JDK_VERSION=11
ARG SCALA_VERSION=3.0.1

ENV PATH=/graalvm/bin:$PATH

RUN set -xeu \
    && apt-get update && apt-get install -y --no-install-recommends \
    make \
    ca-certificates \
    curl \
    xz-utils \
    gcc g++ \
    && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* \
    && curl -L https://git.io/sbt >/bin/sbt && chmod +x /bin/sbt \
    && mkdir /graalvm \
    && curl -L "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/graalvm-ce-java${JDK_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz" \
    | tar -zxC /graalvm --strip-components 1 \
    && gu install native-image


ARG SBT_VERSION=1.5.5
RUN set -xeu \
    && mkdir -p /tmp/sbt/project \
    && echo "sbt.version=${SBT_VERSION}" >/tmp/sbt/project/build.properties \
    && echo 'val main = project.in(file(".")).settings(scalaVersion := "'${SCALA_VERSION}'")'>/tmp/sbt/build.sbt \
    && cd /tmp/sbt \
    && sbt clean `# used to prime sbt cache` \
    && rm -rf /tmp/sbt


ARG MUSL_VERSION=1.2.2
RUN set -xeu \
    && mkdir /tmp/musl \
    && curl -L "https://musl.libc.org/releases/musl-${MUSL_VERSION}.tar.gz" | tar -zxC /tmp/musl --strip-components 1 \
    && cd /tmp/musl \
    && ./configure --disable-shared --prefix=/usr/local \
    && make && make install


ENV CC=musl-gcc


ARG ZLIB_VERSION=1.2.11
RUN set -xeu \
    && mkdir /tmp/zlib \
    && curl -L "https://zlib.net/zlib-${ZLIB_VERSION}.tar.gz" | tar -zxC /tmp/zlib --strip-components 1 \
    && cd /tmp/zlib \
    && ./configure --static --prefix=/usr/local \
    && make && make install


ARG UPX_VERSION=3.96
RUN set -xeu \
    && mkdir /tmp/upx \
    && curl -L "https://github.com/upx/upx/releases/download/v${UPX_VERSION}/upx-${UPX_VERSION}-amd64_linux.tar.xz" | xz -d | tar -xC /tmp/upx --strip-components 1 \
    && cp /tmp/upx/upx /usr/local/bin


ENV NATIVE_IMAGE_INSTALLED=true
ENV NATIVE_IMAGE_MUSL=true
ENV NATIVE_IMAGE_STATIC=true
ENV GRAAL_HOME=/graalvm
ENV JAVA_HOME=/graalvm

