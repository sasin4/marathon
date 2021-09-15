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
    @Autowired ApplyManagerRepository applyManagerRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayMade_SaveApply(@Payload PayMade payMade){

        if(!payMade.validate()) return;

        System.out.println("\n\n##### listener SaveApply : " + payMade.toJson() + "\n\n");



        // Sample Logic //
        // ApplyManager applyManager = new ApplyManager();
        // applyManagerRepository.save(applyManager);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_CancelApply(@Payload PayCancelled payCancelled){

        if(!payCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelApply : " + payCancelled.toJson() + "\n\n");



        // Sample Logic //
        // ApplyManager applyManager = new ApplyManager();
        // applyManagerRepository.save(applyManager);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}