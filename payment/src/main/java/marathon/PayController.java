package marathon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


 @RestController
 @RequestMapping("/pays")
 public class PayController {
    @Autowired
	PayRepository payRepository;
	//@ApiOperation(value = "결제 진행하기")

	@PostMapping("/request")
	public boolean requestPay(@RequestBody Pay pay) {
        System.out.println("################################### PayController: /pays/request");
		
        // CB test 용 지연 코드.
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }	
		

		pay.setPayStatus("COMPLETE");
		pay.setRegisterId(pay.getId());
		Pay savedPay = payRepository.save(pay);
		
		return true;
	}    

 }
