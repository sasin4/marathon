package marathon;

import marathon.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired RegisterMasterRepository registerMasterRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCompleted_SaveRegister(@Payload PayCompleted payCompleted){

        if(!payCompleted.validate()) return;

        System.out.println("\n\n##### listener SaveRegister : " + payCompleted.toJson() + "\n\n");



        // Sample Logic //
        // RegisterMaster registerMaster = new RegisterMaster();
        // registerMasterRepository.save(registerMaster);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_CancelRegister(@Payload PayCancelled payCancelled){

        if(!payCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelRegister : " + payCancelled.toJson() + "\n\n");



        // Sample Logic //
        // RegisterMaster registerMaster = new RegisterMaster();
        // registerMasterRepository.save(registerMaster);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}