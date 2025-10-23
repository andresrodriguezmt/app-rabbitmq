# Producer-Consumer Pattern

In the repository, the implementation of the producer consumer pattern is done using RabbitMQ. 
The example consists of 2 producer and consumer projects, execute them separately and from the console you will send messages.

It is made up of 3 branches in each one you will find a different implementation using RabbitMQ. 
1. Publisher Subscriber      -> (master)
2. Task queue                -> (workqueues)  
3. Consumer publisher        -> (publisher-subscriber)

## Resources

1. IntelJ Comunity
2. JDK Open Jdk 22
3. Rabbit MQ - Using the Rabbit docker image
   #### URL Docker image
   * https://hub.docker.com/_/rabbitmq/
   * **$> docker pull rabbitmq**
   
   #### latest RabbitMQ 3.13 - Docker Command Execution
   * **$> docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management**

Good luck

Vinni
