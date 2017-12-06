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
Here is a template for target POM files.
Please note that using this pattern all of the mutation operators are going to be activated.
```xml
<plugins>
<plugin>
	<groupId> org.pitest </groupId>
	<artifactId> pitest-maven </artifactId>
	<version> 1.2.4 </version>
	<configuration>
		<mutationEngine> pitext </mutationEngine>
		<mutators>
      <!-- TODO: IF YOU NEED TO NAME SPECIFIC MUTATION OPERATORS YOU CAN LIST THEM HERE -->
    </mutator>
	</configuration>
	<dependencies>
		<dependency>
			<groupId> edu.utdallas </groupId>
			<artifactId> pitext </artifactId>
			<version> 1.0.0-SNAPSHOT </version>
		</dependency>
	 </dependencies>
</plugin>
<plugins>
<dependencies>
	<dependency>
		<groupId> org.ow2.asm </groupId>
		<artifactId> asm </artifactId>
		<version> 6.0  </version>
	</dependency>
  <dependency>
		<groupId> edu.utdallas </groupId>
		<artifactId> pitext-utils </artifactId>
		<version> 1.0.0-SNAPSHOT </version>
	</dependency>	
</dependencies>
```

As you can see, the program is also dependent on version 6 of ASM.
Here is the complete list of all mutation opreator identifiers of PITEXT.
You can selectively activate them by mentioning them inside `<mutators> </mutators>` tag.

`UNARY-OPERATOR-INSERTION-512`

`UNARY-OPERATOR-INSERTION-32`

`RELATIONAL-OPERATOR-REPLACEMENT-268710416`

`RELATIONAL-OPERATOR-REPLACEMENT-271856144`

`RELATIONAL-OPERATOR-REPLACEMENT-273691152`

`REQUIRED-CONSTANT-REPLACEMENT-8`

`BITWISE-OPERATOR`

`RELATIONAL-OPERATOR-REPLACEMENT-273953301`

`RELATIONAL-OPERATOR-REPLACEMENT-273953344`

`RELATIONAL-OPERATOR-REPLACEMENT-273953300`

`ARITHMETIC-OPERATOR-REPLACEMENT-262672`

`UNARY-OPERATOR-INSERTION-1`

`UNARY-OPERATOR-INSERTION-2`

`UNARY-OPERATOR-INSERTION-4`

`ARITHMETIC-OPERATOR-REPLACEMENT-274976`

`UNARY-OPERATOR-INSERTION-8`

`ABSOLUTE-VALUE-INSERTION-2`

`ABSOLUTE-VALUE-INSERTION-0`

`RELATIONAL-OPERATOR-REPLACEMENT-273961488`

`ABSOLUTE-VALUE-INSERTION-1`

`RELATIONAL-OPERATOR-REPLACEMENT-273941008`

`RELATIONAL-OPERATOR-REPLACEMENT-273954064`

`ARITHMETIC-OPERATOR-REPLACEMENT-266768`

`RELATIONAL-OPERATOR-REPLACEMENT-274018832`

`RELATIONAL-OPERATOR-REPLACEMENT-273953297`

`RELATIONAL-OPERATOR-REPLACEMENT-273953299`

`RELATIONAL-OPERATOR-REPLACEMENT-273953298`

`RELATIONAL-OPERATOR-REPLACEMENT-269758992`

`REQUIRED-CONSTANT-REPLACEMENT-1`

`RELATIONAL-OPERATOR-REPLACEMENT-273949200`

`ARITHMETIC-OPERATOR-REPLACEMENT-274944`

`ARITHMETIC-OPERATOR-REPLACEMENT-274704`

`REQUIRED-CONSTANT-REPLACEMENT-2`

`REQUIRED-CONSTANT-REPLACEMENT-4`

`ARITHMETIC-OPERATOR-REPLACEMENT-275472`

`ARITHMETIC-OPERATOR-REPLACEMENT-270864`

`ARITHMETIC-OPERATOR-DELETION-2`

`ARITHMETIC-OPERATOR-DELETION-1`

`UNARY-OPERATOR-INSERTION-16`

`RELATIONAL-OPERATOR-REPLACEMENT-273953280`

`UNARY-OPERATOR-INSERTION-256`

`RELATIONAL-OPERATOR-REPLACEMENT-5517840`

`RELATIONAL-OPERATOR-REPLACEMENT-273953040`

`ARITHMETIC-OPERATOR-REPLACEMENT-209424`

`ARITHMETIC-OPERATOR-REPLACEMENT-279056`

`RELATIONAL-OPERATOR-REPLACEMENT-290730512`

`RELATIONAL-OPERATOR-REPLACEMENT-273953360`

`ARITHMETIC-OPERATOR-REPLACEMENT-143888`

`ARITHMETIC-OPERATOR-REPLACEMENT-275008`

`RELATIONAL-OPERATOR-REPLACEMENT-273887760`

`RELATIONAL-OPERATOR-REPLACEMENT-273953328`

`RELATIONAL-OPERATOR-REPLACEMENT-273953808`

`ARITHMETIC-OPERATOR-REPLACEMENT-78352`

`ARITHMETIC-OPERATOR-REPLACEMENT-274992`

`RELATIONAL-OPERATOR-REPLACEMENT-273957392`

`UNARY-OPERATOR-INSERTION-1024`

`ARITHMETIC-OPERATOR-REPLACEMENT-12816`

`RELATIONAL-OPERATOR-REPLACEMENT-273953552`

`RELATIONAL-OPERATOR-REPLACEMENT-273952784`

`RELATIONAL-OPERATOR-REPLACEMENT-273953312`

`RELATIONAL-OPERATOR-REPLACEMENT-273822224`

`RELATIONAL-OPERATOR-REPLACEMENT-270807568`

`ARITHMETIC-OPERATOR-REPLACEMENT-274964`

`ARITHMETIC-OPERATOR-REPLACEMENT-275216`

`ARITHMETIC-OPERATOR-REPLACEMENT-274963`

`RELATIONAL-OPERATOR-REPLACEMENT-272904720`

`ARITHMETIC-OPERATOR-REPLACEMENT-274448`

`RELATIONAL-OPERATOR-REPLACEMENT-273756688`

`ARITHMETIC-OPERATOR-REPLACEMENT-274962`

`RELATIONAL-OPERATOR-REPLACEMENT-273945104`

`ARITHMETIC-OPERATOR-REPLACEMENT-274961`

Good luck
