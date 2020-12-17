cd ../src/MKAgent
javac *.java
cd ../
jar cfe Minimax.jar MKAgent/Main MKAgent/*.class
mv Minimax.jar ../Test_Agents
rm MKAgent/*.class