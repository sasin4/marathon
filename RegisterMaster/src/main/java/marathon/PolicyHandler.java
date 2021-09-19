package marathon;

import marathon.config.kafka.KafkaProcessor;
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
        System.out.println("\n\n##### RegisterMaster PolicyHandler");
        System.out.println("\n\n##### listener SaveRegister : " + payCompleted.toJson() + "\n\n");

        RegisterMaster registerMaster = new RegisterMaster();
        registerMaster.setRegisterId(payCompleted.getRegisterId());
        registerMaster.setName(payCompleted.getName());
        registerMaster.setAddress(payCompleted.getAddress());     
        registerMaster.setPhoneNo(payCompleted.getPhoneNo());
        registerMaster.setTopSize(payCompleted.getTopSize());
        registerMaster.setBottomSize(payCompleted.getBottomSize());
        
        registerMaster.setDeliveryStatus("DELIVERED");
        registerMasterRepository.save(registerMaster);


    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_CancelRegister(@Payload PayCancelled payCancelled){

        if(!payCancelled.validate()) return;
        System.out.println("\n\n##### RegisterMaster PolicyHandler");
        System.out.println("\n\n##### listener CancelRegister : " + payCancelled.toJson() + "\n\n");
        System.out.println("\n\n##### payCancelled.getPayStatus() : " +payCancelled.getPayStatus());
        System.out.println("\n\n##### payCancelled.getId() : " + payCancelled.getId());

        if (payCancelled.getPayStatus().equals("CANCEL")) {
        	RegisterMaster registerMaster = registerMasterRepository.findByRegisterId(payCancelled.getId());
        	registerMaster.setDeliveryStatus("CANCEL");
        	registerMasterRepository.save(registerMaster);
        }    

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}