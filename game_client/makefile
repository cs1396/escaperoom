build: clean
	./gradlew lwjgl3:downloadJdks --configuration-cache --parallel
	./gradlew lwjgl3:unpackJdks --configuration-cache --parallel
	./gradlew lwjgl3:jar --parallel
	./gradlew lwjgl3:fullBuild --configuration-cache --parallel

clean:
	rm -rf ./lwjgl3/build/construo

