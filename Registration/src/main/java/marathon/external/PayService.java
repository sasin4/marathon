package marathon.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@FeignClient(name="Payment", url="http://localhost:8082")
@FeignClient(name="pays", url="http://localhost:8082/pays", fallback = PayServiceFallback.class)
public interface PayService {
    //@RequestMapping(method= RequestMethod.GET, path="/pays")
    @RequestMapping(method= RequestMethod.GET, path="/request")
    public boolean payRequest(@RequestBody Pay pay);

}

