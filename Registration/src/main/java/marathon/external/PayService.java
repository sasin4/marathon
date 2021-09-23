package marathon.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@FeignClient(name="Payment", url="http://localhost:8082")
//@FeignClient(name="pays", url="http://localhost:8082/pays", fallback = PayServiceFallback.class)
//@FeignClient(name="pays", url="http://localhost:8082/pays")
@FeignClient(name="pays", url="http://ab05340fcb5a549468186b7f51ec793c-1525260050.ap-northeast-2.elb.amazonaws.com:8080/pays")
public interface PayService {
    //@RequestMapping(method= RequestMethod.GET, path="/pays")
    //@RequestMapping(method= RequestMethod.GET, path="/request")
    @RequestMapping(method= RequestMethod.POST, path="/request")
    public boolean payRequest(@RequestBody Pay pay);
    
}

