Have the watermark dir created in your local file system and you can either compile the java or you can directly run the compiled java and it will create a result.pdf

Compile:
javac -classpath "%CLASSPATH%";./lib/* WaterMarkProofComponent.java

Run:
java -classpath "%CLASSPATH%";./lib/*;. WaterMarkProofComponent


