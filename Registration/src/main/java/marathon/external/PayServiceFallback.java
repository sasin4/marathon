package marathon.external;

//No fallback instance of type class marathon.external.PayServiceFallback found for feign client pays
//관련 추가
import org.springframework.stereotype.Service;

@Service
public class PayServiceFallback implements PayService{
//    @Override
    public boolean payRequest(Pay pay) {
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        return false;
    }
}