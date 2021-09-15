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
    @Autowired RegistrationRepository registrationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCompleted_UpdaeSms(@Payload PayCompleted payCompleted){

        if(!payCompleted.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + payCompleted.toJson() + "\n\n");



        // Sample Logic //
        // Registration registration = new Registration();
        // registrationRepository.save(registration);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_UpdaeSms(@Payload PayCancelled payCancelled){

        if(!payCancelled.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + payCancelled.toJson() + "\n\n");



        // Sample Logic //
        // Registration registration = new Registration();
        // registrationRepository.save(registration);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRegisterComplete_UpdaeSms(@Payload RegisterComplete registerComplete){

        if(!registerComplete.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + registerComplete.toJson() + "\n\n");



        // Sample Logic //
        // Registration registration = new Registration();
        // registrationRepository.save(registration);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRegisterRemoved_UpdaeSms(@Payload RegisterRemoved registerRemoved){

        if(!registerRemoved.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms : " + registerRemoved.toJson() + "\n\n");



        // Sample Logic //
        // Registration registration = new Registration();
        // registrationRepository.save(registration);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}