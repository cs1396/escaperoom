site:
	rm -rf build
	rsync --exclude="template.html" --exclude="makefile" --exclude="infrastructure" ./* ./build/
	cp template.html build/index.html
