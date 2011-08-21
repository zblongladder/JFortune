all:
	javac *.java
	javac rsa/*.java
clean:
	rm -v *.class
	rm -v rsa/*.class