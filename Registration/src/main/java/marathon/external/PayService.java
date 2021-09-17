package marathon.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="Payment", url="http://localhost:8082")
public interface PayService {
    @RequestMapping(method= RequestMethod.GET, path="/pays")
    public void payRequest(@RequestBody Pay pay);

}

