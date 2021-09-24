package marathon;

import marathon.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

public class AbstractEvent {

    String eventType;
    String timestamp;

    public AbstractEvent(){
        this.setEventType(this.getClass().getSimpleName());
        SimpleDateFormat defaultSimpleDateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
        this.timestamp = defaultSimpleDateFormat.format(new Date());
    }

    public String toJson(){
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }

        return json;
    }

    public void publish(String json){
        if( json != null ){

            /**
             * spring streams 방식
             */
            KafkaProcessor processor = PaymentApplication.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();

            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());

        }
    }

    public void publish(){
        this.publish(this.toJson());
    }

    public void publishAfterCommit(){
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

            @Override
            public void afterCompletion(int status) {
                AbstractEvent.this.publish();
            }
        });
    }

    public void saveJasonToPvc(String strJson){
        File file;

        if (strJson.equals("COMPLETE")){
		    file = new File("/mnt/aws/payCompleted_json.txt");
        }else{
            file = new File("/mnt/aws/payCancelled_json.txt");
        }

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(strJson);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}    

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean validate(){
        return getEventType().equals(getClass().getSimpleName());
    }
}