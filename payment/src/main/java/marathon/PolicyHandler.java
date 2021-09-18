package marathon;

import marathon.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired PayRepository payRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRegisterCancelled_PayCancel(@Payload RegisterCancelled registerCancelled){

        if(!registerCancelled.validate()) return;
        System.out.println("\n\n##### listener PayCancel : " + registerCancelled.toJson() + "\n\n");

        if (registerCancelled.getStatus().equals("CANCEL")) {
        	Pay pay = payRepository.findByRegisterId(registerCancelled.getId());
        	pay.setPayStatus("CANCEL");
        	pay.setRegisterStatus("CANCEL");
        	payRepository.save(pay);        	
        }  
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}