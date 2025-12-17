dist: clean build add_assets

build:
	./gradlew lwjgl3:jar --parallel & ./gradlew lwjgl3:LoadJDKs --configuration-cache --parallel
	./gradlew lwjgl3:FullBuild --configuration-cache --parallel

clean:
	rm -rf ./lwjgl3/build/construo

add_assets:
	for ARCHIVE in ./lwjgl3/build/construo/dist/*; do \
		cd ./assets/; zip -r ../$$ARCHIVE ./textures ./objects; \
	done

.PHONY: dist build clean add_assets
