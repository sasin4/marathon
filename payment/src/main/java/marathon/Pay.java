package marathon;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Pay_table")
public class Pay {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long registerId;
    private String name;
    private String phoneNo;
    private String address;
    private String registerStatus;
    private String payStatus;
    private String topSize;
    private String bottomSize;
    private Integer amount;

    @PostPersist
    public void onPostPersist(){
        System.out.println("############################## Registration payRequest -> pay.java");
        PayCompleted payCompleted = new PayCompleted();
        BeanUtils.copyProperties(this, payCompleted);

        System.out.println("id : "+ payCompleted.getId());
        System.out.println("name : "+ payCompleted.getName());
        System.out.println("phoneNo : "+ payCompleted.getPhoneNo());
        System.out.println("address : "+ payCompleted.getAddress());
        System.out.println("payStatus : "+ payCompleted.getPayStatus());
        System.out.println("registerStatus : "+ payCompleted.getRegisterStatus());
        System.out.println("topSize : "+ payCompleted.getTopSize());
        System.out.println("bottomSize : "+ payCompleted.getBottomSize());
        System.out.println("amount : "+ payCompleted.getAmount());
        payCompleted.setPayStatus("COMPLETE");

        payCompleted.publishAfterCommit();
/*
        PayCancelled payCancelled = new PayCancelled();
        BeanUtils.copyProperties(this, payCancelled);
        payCancelled.publishAfterCommit();
*/
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(Long registerId) {
        this.registerId = registerId;
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
    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
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

    public String getRegisterStatus() {
        return this.registerStatus;
    }

    public void setRegisterStatus(String registerStatus) {
        this.registerStatus = registerStatus;
    }




}