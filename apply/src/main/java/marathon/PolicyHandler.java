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
    @Autowired ApplyRepository applyRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayMade_UpdaeSms(@Payload PayMade payMade){

        if(!payMade.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + payMade.toJson() + "\n\n");



        // Sample Logic //
        // Apply apply = new Apply();
        // applyRepository.save(apply);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_UpdaeSms(@Payload PayCancelled payCancelled){

        if(!payCancelled.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + payCancelled.toJson() + "\n\n");



        // Sample Logic //
        // Apply apply = new Apply();
        // applyRepository.save(apply);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverApplySaved_UpdaeSms(@Payload ApplySaved applySaved){

        if(!applySaved.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + applySaved.toJson() + "\n\n");



        // Sample Logic //
        // Apply apply = new Apply();
        // applyRepository.save(apply);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverApplyCancelled_UpdaeSms(@Payload ApplyCancelled applyCancelled){

        if(!applyCancelled.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + applyCancelled.toJson() + "\n\n");



        // Sample Logic //
        // Apply apply = new Apply();
        // applyRepository.save(apply);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}