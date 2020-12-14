./compile.sh

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar MKRefAgent.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar MKRefAgent.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar ../Test_Agents/JimmyPlayer.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar ../Test_Agents/JimmyPlayer.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar ../Test_Agents/error404.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar ../Test_Agents/error404.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar Minimax.jar" "java -jar ../Test_Agents/Group2Agent.jar" &> temp.txt
tail -8 temp.txt
echo

java -jar ManKalah.jar "java -jar ../Test_Agents/Group2Agent.jar" "java -jar Minimax.jar" &> temp.txt
tail -8 temp.txt
echo
rm temp.txt