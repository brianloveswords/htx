
install : ${HOME}/bin/mdlink

${HOME}/bin/mdlink : bin/mdlink
	@echo "Installing..."
	@cp bin/mdlink $(HOME)/bin


bin/mdlink : bin/mdlink.original
	rm -f bin/mdlink
	upx bin/mdlink.original -o bin/mdlink


bin/mdlink.original : src/graal/reflect-config.json bin
	native-image \
		-H:ReflectionConfigurationFiles=src/graal/reflect-config.json \
		-dsa \
		--enable-url-protocols=http,https \
		--no-fallback \
		-jar target/scala-3.0.1/mdlink-assembly-0.1.0.jar \
		bin/mdlink.original


bin :
	mkdir -p bin


src/graal/reflect-config.json : target/scala-3.0.1/mdlink-assembly-0.1.0.jar
	java \
	-agentlib:native-image-agent=config-output-dir=src/graal \
	-jar target/scala-3.0.1/mdlink-assembly-0.1.0.jar \
	https://example.com


target/scala-3.0.1/mdlink-assembly-0.1.0.jar : $(shell fd . 'src/main')
	sbt assembly
	touch $@
