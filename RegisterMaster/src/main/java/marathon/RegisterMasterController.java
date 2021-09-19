package marathon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

 @RestController
 public class RegisterMasterController {

    @Autowired
    RegisterMasterRepository registerMasterRepository;

	//@ApiOperation(value = "결제 진행하기")
	/*
	@PostMapping("/request")
	public boolean saveRegister(@RequestBody RegisterMaster registerMaster) {
        System.out.println("################################### RegisterMasterController : /request");
		registerMaster.setDeliveryStatus("SHIPPED");
		registerMaster.setRegisterId(registerMaster.getId());
		RegisterMaster savedRegisterMaster = registerMasterRepository.save(registerMaster);
		/*
        // CB test 용 지연 코드.
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }		
		*/
		
		/*
		return true;
	}*/

 }