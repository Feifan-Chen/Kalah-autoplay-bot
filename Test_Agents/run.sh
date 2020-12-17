./compile.sh

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar MKRefAgent.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar MKRefAgent.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar JimmyPlayer.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar JimmyPlayer.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar error404.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar error404.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar Group2Agent.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar Group2Agent.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo
rm temp.txt
