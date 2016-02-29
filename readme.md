Application requires Java 8 and Maven 3

How to work with application
- Build it using command *mvn clean install*
- Create property file somewhere on disk
- Run application using command *mvn exec:java -Dexec.args="path_to_your_property_file"*
- Try to add property **foo** to property file and see application output, then try to add property **bar** to property file

