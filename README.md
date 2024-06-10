# p2p-service-payment-microservice
There are 3 main microservices: User, Card, Service

Used technologies: Spring Boot, Spring Security, Docker and Kafka

There is a gateway service which distributes requests to other microservices. 
It means api gateway controls all requests come to web app.
So security is applied to this server using JWT token which includes data of signed in user.
For communication of microservices is used WebClient which is an asynchronous tool to get and send REST apis.
Kafka is used for communication between microservices too, and is used to distribute message queues.

In this web app Kafka is used in multiple situations:
1. notification about registered user
2. Notification about deducting funds from balance
3. Notification about adding funds to balance
4. Notification about adding card to user
   
All notifications are sent as mails to users.
   
For example, user John registered and he gets mail about registration. He adds or removes card himself and gets email 
about card addition or deletion. This mail includes data: card added or removed, card number of added or removed card.

John sends money to Bob, and John gets mail about deduction money from his balance and Bob gets notification about 
adding to balance. If John pays service, he will get the same mail. This mail includes data: type of transaction
(addition or deduction), card number(last 4 digits), updated card balance, date of transaction.