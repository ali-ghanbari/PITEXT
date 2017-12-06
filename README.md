# PITEXT: An Extension to PITest

An extension to PIT mutation tool is presented. We are extending PIT with 7 new mutation operators.
In what follows, I describe how you can run this tool.
You clone and install PITEXT as follows.

`git clone https://github.com/ali-ghanbari/PITEXT.git`

`cd PITEXT`

`mvn clean install`

PITEXT, in order to save some space and avoid unnecessary changes to mutated class files, has factored all of the auxiliary static functions out and stored them in separate JAR file names `pitext-utils`
In order to be able to use PITEXT you have to put this JAR file into the classpath.
You can do so by adding `pitext-utils` as a dependency in the target POM file.
Before you can add `pitext-utils`, however, you need to copy it to your local repository.
The file is accessible through https://github.com/ali-ghanbari/PITEXT-Utils.git.
All you need to do is to clone and install `pitext-util` as follows.

`git clone https://github.com/ali-ghanbari/PITEXT-Utils.git`

`cd PITEXT-Utils`

`mvn clean install`

After that you are ready to go.
