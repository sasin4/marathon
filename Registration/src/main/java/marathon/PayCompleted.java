package marathon;

public class PayCompleted extends AbstractEvent {

    private Long id;
    private Long registerId;
    private String name;
    private String phoneNo;
    private String address;
    private String payStatus;
    private String registerStatus;
    private String topSize;
    private String bottomSize;
    private Integer amount;

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