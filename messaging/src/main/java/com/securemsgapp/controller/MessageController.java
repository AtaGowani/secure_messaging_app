package com.securemsgapp.controller;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.json.simple.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.securemsgapp.model.Message;
import com.securemsgapp.service.RabbitMQSender;

@RestController
@RequestMapping(value = "/msg-api")
public class MessageController {

    @Autowired
    private AmqpAdmin admin;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private DirectMessageListenerContainer messageListenerContainer;

    @Value("${securemsgapp.rabbitmq.exchange}")
    private String exchange;
	
	private MessageLists messageListManger = new MessageLists();

    @PostMapping(value = "/send")
    public String sendMessage(@RequestBody String recipientName) {
        // Queue args: durable = false, exclusive = false, autoDelete = true
        Queue queue = new Queue(recipientName, false, false, true);
        Binding binding = new Binding(recipientName, Binding.DestinationType.QUEUE, exchange, recipientName, null);
        admin.declareQueue(queue);
        admin.declareBinding(binding);
        messageListenerContainer.addQueues(queue);
        amqpTemplate.convertAndSend(exchange, recipientName, recipientName);

        return "Message sent";
    }
	
	  @GetMapping(value = "/mailbox")
    public ArrayList<JSONObject> getMailbox() {
        MessageController controller = new MessageController();
        controller.convertTOJson(messageListManger);
        return messageListManger.getjsonMessageList();
    }

    public void convertTOJson(MessageLists messagelists) {
        for (int i = 0; i < messagelists.getMessageList().size(); i++) {
            JSONObject json = new JSONObject();
            json.put("body", messagelists.getMessageList().get(i));
            messagelists.addItemTojsonMessageList(json);

        }
    }
}
