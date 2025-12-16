build: clean
	./gradlew lwjgl3:jar --parallel & ./gradlew lwjgl3:LoadJDKs --configuration-cache --parallel
	./gradlew lwjgl3:FullBuild --configuration-cache --parallel

clean:
	rm -rf ./lwjgl3/build/construo

