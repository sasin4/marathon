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

        System.out.println("\n\n##### listener UpdaeSms PayCompleted : " + payCompleted.toJson() + "\n\n");
        //결제 완료 안내
        System.out.println("\n\n결제가 완료되었습니다. 신청번호 : "+payCompleted.getId()+ ", 신청자 : " + payCompleted.getName() + ", 금액 : " + payCompleted.getAmount() +"\n\n");
        System.out.println("\n\n###################################################");

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_UpdaeSms(@Payload PayCancelled payCancelled){

        if(!payCancelled.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms PayCancelled : " + payCancelled.toJson() + "\n\n");
        //결제 취소 안내
        System.out.println("\n\n결제가 취소었습니다. 신청번호 : "+payCancelled.getId()+ ", 신청자 : " + payCancelled.getName() +"\n\n");
        System.out.println("\n\n###################################################");

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRegisterComplete_UpdaeSms(@Payload RegisterComplete registerComplete){

        if(!registerComplete.validate()) return;

        System.out.println("\n\n##### listener UpdaeSms RegisterComplete : " + registerComplete.toJson() + "\n\n");
        //등록완료 안내
        System.out.println("\n\n마라톤 등록 완료. 신청번호 : "+registerComplete.getId()+ ", 신청자 : " + registerComplete.getName() +
         ", 발송상태 : " + registerComplete.getDeliveryStatus() + "\n\n");
        System.out.println("\n\n###################################################");

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRegisterRemoved_UpdaeSms(@Payload RegisterRemoved registerRemoved){

        if(!registerRemoved.validate()) return;

        //등록취소완료 안내
        System.out.println("\n\n마라톤 등록취소 완료. 신청번호 : "+registerRemoved.getId()+ ", 신청자 : " + registerRemoved.getName() +
         ", 발송상태 : " + registerRemoved.getDeliveryStatus() + "\n\n");
        System.out.println("\n\n###################################################");



        // Sample Logic //
        // Registration registration = new Registration();
        // registrationRepository.save(registration);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}