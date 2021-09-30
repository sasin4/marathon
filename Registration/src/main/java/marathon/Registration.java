package marathon;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Registration_table")
public class Registration {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private String phoneNo;
    private String address;
    private String status;
    private String topSize;
    private String bottomSize;
    private Integer amount;

    @PostPersist
    public void onPostPersist(){

        marathon.external.Pay pay = new marathon.external.Pay();
        // mappings goes here
        pay.setId(this.id);
        pay.setName(this.name);
        pay.setPhoneNo(this.phoneNo);
        pay.setAddress(this.address);
        pay.setAmount(this.amount);
        pay.setRegisterStatus("REGISTERED");
        pay.setTopSize(this.topSize);
        pay.setBottomSize(this.bottomSize);
        pay.setAmount(this.amount);
        


        
        System.out.println("########################## Registration.java -> PostPersist -> payRequest");
        System.out.println("id : "+ this.id);
        System.out.println("name : "+ this.name);
        System.out.println("phoneNo : "+ this.phoneNo);
        System.out.println("address : "+ this.address);
        System.out.println("status : "+ this.status);
        System.out.println("topSize : "+ this.topSize);
        System.out.println("bottomSize : "+ this.bottomSize);
        System.out.println("amount : "+ this.amount);

        boolean result = RegistrationApplication.applicationContext.getBean(marathon.external.PayService.class).payRequest(pay);
        
        if(result) {
        	System.out.println("########## 결제가 완료되었습니다 ############");
        } else {
            System.out.println("########## 결제가 실패하였습니다 ############");
        }  

        System.out.println("id : "+ pay.getId());
        System.out.println("name : "+ pay.getName());
        System.out.println("phoneNo : "+ pay.getPhoneNo());
        System.out.println("address : "+ pay.getAddress());
        System.out.println("payStatus : "+ pay.getPayStatus());
        System.out.println("registerStatus : "+ pay.getRegisterStatus());
        System.out.println("topSize : "+ pay.getTopSize());
        System.out.println("bottomSize : "+ pay.getBottomSize());
        System.out.println("amount : "+ pay.getAmount());
    
        PayRequested payRequested = new PayRequested();
        BeanUtils.copyProperties(this, payRequested);
        payRequested.publishAfterCommit();

        //PVC
        payRequested.saveJasonToPvc(payRequested.toJson());

    }

    @PostUpdate
    public void onPostUpdate() {
    	if (this.status.equals("CANCEL")) {    
	    	RegisterCancelled registrationCancelled = new RegisterCancelled();
	        BeanUtils.copyProperties(this, registrationCancelled);
	        registrationCancelled.publishAfterCommit();

			//PVC
            registrationCancelled.saveJasonToPvc(registrationCancelled.toJson());
    	}
    }    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getTopSize() {
        return topSize;
    }

    public void setTopSize(String topSize) {
        this.topSize = topSize;
    }
    public String getBottomSize() {
        return bottomSize;
    }

    public void setBottomSize(String bottomSize) {
        this.bottomSize = bottomSize;
    }
    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }




}