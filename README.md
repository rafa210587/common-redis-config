# MongoDB Config for autoconfigure the connection to the Mongo Database

#### How to use:
Just add the dependency in your project pom and you can start using mongoDBConfig(no need to instanciate the bean, its already made for you, just use it wisely).

Also add in your application main class the @import({ MongoDBConfig.class })

You will also need to indicate where your repository is located with the annotation 
@EnableMongoRepositories(basePackages = "your.repository.package")
Just add it to your main class also.

Also it uses the Spring Cloud to get the variables for the connection, just add to your start configuration the .properties of mongodb or make one

ex.:
mongodb.host= localhost
mongodb.port = 27101
mongodb.database= yourDataBase
