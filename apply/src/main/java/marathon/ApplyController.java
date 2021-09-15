package marathon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 @RestController
 @RequestMapping("/apply")
 public class ApplyController {
     
	@Autowired
    ApplyApplication applyApplication;
	
	//@ApiOperation(value = "결제 진행하기")
	@PostMapping("/applied")
	public boolean Applied(@RequestBody Apply application) {
		
		ApplyApplication saveApplication = applyApplication.save(application);
		/*
        // CB test 용 지연 코드.
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }		
		*/
		return true;
	}

 }