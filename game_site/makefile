site:
	rm -rf build
	mkdir build
	cp template.html build/index.html
	cp style.css build/style.css
	cp todo.html build/todo.html
	sed -i 's/##STABLE##/\/todo.html/' build/index.html
	sed -i 's/##NIGHTLY##/\/todo.html/' build/index.html
