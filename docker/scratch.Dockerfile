FROM scratch
COPY htx.amd64.linux.static /htx
ENTRYPOINT [ "/htx" ]
