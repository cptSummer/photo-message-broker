### Спосіб налаштування smtp серверу

1. Створити файл email-server.env
2. Прописати у ньому та свої данні від поштової скриньки
   `SPRING_MAIL_HOST=smtp.gmail.com
   SPRING_MAIL_PORT=587
   SPRING_MAIL_USERNAME=your_email@gmail.com
   SPRING_MAIL_PASSWORD=your_password
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true`
3. Переконатись у відповідних записах у [application.properties](src%2Fmain%2Fresources%2Fapplication.properties)

### Спосіб запуску Dockerfile

1. Перейти до директорії із проектом
2. Прописати  `docker build -t photo-message-broker .`
3. Прописати `docker-compose up`

### Створення індексу для elasticsearch

PUT `http://localhost:9200/notifications`

````json
{"mappings": {
     "properties": {
       "subject": { "type": "text" },
       "content": { "type": "text" },
       "from": {
         "properties": {
           "email": { "type": "keyword" },
           "name": { "type": "text" }
         }
       },
       "recipients": {
         "properties": {
           "email": { "type": "keyword" },
           "name": { "type": "text" }
         }
       },
       "error":{
        "properties":{
            "errorMessage":{
                "properties":{
                    "message": {"type": "text"},
                    "errorClass":{"type": "text"}
                }
            },
            "resentCount": {"type": "integer"},
            "lastResent": {"type": "date"}
        }
       },
       "status": { "type": "keyword" },	
       "timestamp": { "type": "date" },
       "delayTime": { "type": "long" }  
     }
   },
  "settings": {
    "index": {
     "number_of_shards": 1,
     "number_of_replicas": 0
}}}
````
### Вигляд коду відправки повідомлення через RabbitMQ під час вдалої реєстрації

````java
private final RabbitTemplate rabbitTemplate;

public void createUser(String username, String email, String password) {
   User user = new User();

   user.setEmail(email);
   user.setJoinDate(LocalDate.parse(utilService.getCurrentDate()));
   user.setPassword(password);
   user.setRole("ROLE_USER");
   user.setUsername(username);
   save(user);
   EmailRecipientDto recipient = new EmailRecipientDto();
   recipient.setEmail(email);
   recipient.setName(username);
   rabbitTemplate.convertAndSend("user-registration-queue", recipient);
}
````
