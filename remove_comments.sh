#first command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()\.]*);.*//g" {} \;

#second command
find . -name *.java -exec sed -i "s/\/\/.*logging\..*//g" {} \;

#third command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()\.]*);.*//g" {} \;

#4. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()<>{}\.]*);.*//g" {} \;

#5. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()<>{}\.,]*);.*//g" {} \;

#6. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()<>{}\.,]*;.*//g" {} \;
