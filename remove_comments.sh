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

#7. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()<>{}\.,=]*);.*//g" {} \;

#8. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()<>{}\.,=]*;.*//g" {} \;

#9. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z0-9 '()<>{}\.,=]*;.*//g" {} \;

#10. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z0-9 '()<>{}\.,=]*;.*//g" {} \;
