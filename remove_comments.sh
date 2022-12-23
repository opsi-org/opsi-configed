#first
find . -name *.java -exec sed -i "s/\/\/[a-zA-Z '()]*);.*//g" {} \;
