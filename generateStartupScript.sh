cd src

set -e

javac scriptGen.java
java scriptGen > ../clicap
