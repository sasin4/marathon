package marathon;

import marathon.config.kafka.KafkaProcessor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class DashboardViewHandler {


    @Autowired
    private DashboardRepository dashboardRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCompleted_then_CREATE_1 (@Payload PayCompleted payCompleted) {
        try {
            System.out.println("################################## DashboardViewHandler whenPayCompleted_then_CREATE_1");

            if (!payCompleted.validate()) return;

            // view 객체 생성
            Dashboard dashboard = new Dashboard();
            // view 객체에 이벤트의 Value 를 set 함
            dashboard.setRegisterId(payCompleted.getRegisterId());
            dashboard.setName(payCompleted.getName());
            dashboard.setPhoneNo(payCompleted.getPhoneNo());
            dashboard.setAddress(payCompleted.getAddress());
            dashboard.setStatus("REGISTERED");
            dashboard.setPayStatus(payCompleted.getPayStatus());
            dashboard.setAmount(payCompleted.getAmount());
            dashboard.setTopSize(payCompleted.getTopSize());
            dashboard.setBottomSize(payCompleted.getBottomSize());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRegisterComplete_then_UPDATE_1(@Payload RegisterComplete registerComplete) {
        try {
            System.out.println("################################## DashboardViewHandler whenRegisterComplete_then_UPDATE_1");
            if (!registerComplete.validate()) return;
                // view 객체 조회
            Dashboard dashboard = dashboardRepository.findByregisterId(registerComplete.getRegisterId());
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            
            System.out.println("################################## deliveryStatus : "+registerComplete.getDeliveryStatus());
            dashboard.setDeliveryStatus(registerComplete.getDeliveryStatus());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCancelled_then_UPDATE_2(@Payload PayCancelled payCancelled) {
        try {
            System.out.println("################################## DashboardViewHandler whenPayCancelled_then_UPDATE_2");
            if (!payCancelled.validate()) return;
                // view 객체 조회
            
            Dashboard dashboard = dashboardRepository.findByregisterId(payCancelled.getRegisterId());

            dashboard.setDeliveryStatus(payCancelled.getPayStatus());
            dashboard.setPayStatus(payCancelled.getPayStatus());
            dashboard.setStatus(payCancelled.getPayStatus());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}