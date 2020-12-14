cd ../MKAgent
javac *.java
cd ..
jar cfe Minimax.jar MKAgent/Main MKAgent/*.class
rm MKAgent/*.class
mv Minimax.jar libs
cd libs