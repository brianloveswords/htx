FROM scratch
COPY htx.linux.static /go
ENTRYPOINT [ "/go" ]
