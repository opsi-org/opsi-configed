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
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z0-9 _'()<>{}\.,=]*;.*//g" {} \;

#11. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z0-9 _\!'()<>{}\.,=]*;.*//g" {} \;

#12. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z0-9 \-_\!'()<>{}\.,=]*;.*//g" {} \;

#13. command
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z0-9 \-_\!'()<>{}\.\\}\{,=]*;.*//g" {} \;

#14. command
find . -name *.java -exec sed -i "s/\/\/[^\"]*;.*//g" {} \;

#15. command
find . -name *.java -exec sed -i "s/\/\/[^\"]* if[^\"]*([^\"]*).*//g" {} \;

#16.command
find . -name *.java -exec sed -i "s/\/\/[^\"]* if[^\"]*([^\"]*).*//g" {} \;

#17.command
find . -name *.java -exec sed -i "s/\/\/[^\"]* void[^\"]*([^\"]*).*//g" {} \;

#18. command
find . -name *.java -exec sed -i "s/\/\/[^\"]* private[^\"]*([^\"]*).*//g" {} \;
find . -name *.java -exec sed -i "s/\/\/[^\"]* public[^\"]*([^\"]*).*//g" {} \;
find . -name *.java -exec sed -i "s/\/\/[^\"]* protected[^\"]*([^\"]*).*//g" {} \;

#19. command 
find . -name *.java -exec sed -i "s/\/\/[^\"][{]\{1,10\}.*//g" {} \;
find . -name *.java -exec sed -i "s/\/\/[^\"][}]\{1,10\}.*//g" {} \;
