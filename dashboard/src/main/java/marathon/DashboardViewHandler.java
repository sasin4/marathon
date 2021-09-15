package marathon;

import marathon.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardViewHandler {


    @Autowired
    private DashboardRepository dashboardRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenApplied_then_CREATE_1 (@Payload Applied applied) {
        try {

            if (!applied.validate()) return;

            // view 객체 생성
            Dashboard dashboard = new Dashboard();
            // view 객체에 이벤트의 Value 를 set 함
            dashboard.setId(applied.getId());
            dashboard.setApplicantName(applied.getApplicantName());
            dashboard.setPhoneNo(applied.getPhoneNo());
            dashboard.setAddress(applied.getAddress());
            dashboard.setStatus(applied.getStatus());
            dashboard.setTopSize(applied.getTopSize());
            dashboard.setAmount(applied.getAmount());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayMade_then_UPDATE_1(@Payload PayMade payMade) {
        try {
            if (!payMade.validate()) return;
                // view 객체 조회
            Optional<Dashboard> dashboardOptional = dashboardRepository.findById(payMade.getApplyId());

            if( dashboardOptional.isPresent()) {
                 Dashboard dashboard = dashboardOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 dashboard.setPayStatus(payMade.getPayStatus());
                // view 레파지 토리에 save
                 dashboardRepository.save(dashboard);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCancelled_then_UPDATE_2(@Payload PayCancelled payCancelled) {
        try {
            if (!payCancelled.validate()) return;
                // view 객체 조회
            Optional<Dashboard> dashboardOptional = dashboardRepository.findById(payCancelled.getApplyId());

            if( dashboardOptional.isPresent()) {
                 Dashboard dashboard = dashboardOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 dashboard.setPayStatus(payCancelled.getPayStatus());
                // view 레파지 토리에 save
                 dashboardRepository.save(dashboard);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenApplySaved_then_UPDATE_3(@Payload ApplySaved applySaved) {
        try {
            if (!applySaved.validate()) return;
                // view 객체 조회
            Optional<Dashboard> dashboardOptional = dashboardRepository.findById(applySaved.getApplyId());

            if( dashboardOptional.isPresent()) {
                 Dashboard dashboard = dashboardOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 dashboard.setDeliveryStatus(applySaved.getDeliveryStatus());
                // view 레파지 토리에 save
                 dashboardRepository.save(dashboard);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenApplyCancelled_then_UPDATE_4(@Payload ApplyCancelled applyCancelled) {
        try {
            if (!applyCancelled.validate()) return;
                // view 객체 조회
            Optional<Dashboard> dashboardOptional = dashboardRepository.findById(applyCancelled.getApplyId());

            if( dashboardOptional.isPresent()) {
                 Dashboard dashboard = dashboardOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 dashboard.setDeliveryStatus(applyCancelled.getDeliveryStatus());
                // view 레파지 토리에 save
                 dashboardRepository.save(dashboard);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

