![캡처](https://user-images.githubusercontent.com/26429915/134905867-c9120d18-9cca-443b-9502-5b83034e1986.JPG)

# 마라톤 등록 

마라톤 참여 등록 및 등록 시 참여 기념 goods를 보내주는 기능을 구현한 Microservice


# Table of contents

- [마라톤 신청](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출--서킷-브레이킹--장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [Self-Healing](#self-healing)
    - [무정지 재배포](#무정지-재배포)
    - [Persistant Volume Claim](#persistant-volume-claim)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

마라톤 참여 등록하기 서비스

기능적 요구사항
1. 고객이 마라톤에 Register 한다
2. 마라톤 Register 시 참가비를 결제를 한다.
3. 마라톤 주최측 RegisterMaster에 신청 내역이 전달된다
4. 마라톤 참여 Goods가 발송된다
5. 고객이 Register 를 취소할 수 있다
6. Register를 취소하면 결제를 취소한다
7. Register를 취소하면 Goods 발송도 취소한다.

비기능적 요구사항
1. 트랜잭션
  - Register 시 결제 과정을 거치며 결제 실패 시 Register가 불가능하다 : Sync 호출
2. 장애격리
  - 주최측 RegisterMaster 접수 기능이 수행되지 않더라도 Register 신청은 365일, 24시간 받을 수 있어야 한다 : Async (event-driven), Eventual Consistency
  - Registration 시스템 접속이 과중 되면 사용자를 잠시 후에 다시 접속하도록 유도한다 : Circuit breaker, Fallback
3. 성능
  - 고객이 Register 및 Delivery 상태를 확인할 수 있어야 한다 : CQRS
  - Payment, Delivery 상태가 바뀔 때 마다 SMS로 알림을 준다 : Event-driven

    


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른 ?Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과? 도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여? 장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?

- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
  - 무정지 운영 CI/CD
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 



# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)

<img width="1130" alt="2021-09-30 8 55 49" src="https://user-images.githubusercontent.com/26429915/135387123-b9862387-8a5e-482c-baa0-557e1f3e00a7.JPG">


## TO-BE 조직 (Vertically-Aligned)

<img width="1093" alt="2021-09-30 11 13 12" src="https://user-images.githubusercontent.com/26429915/135387125-8b481db9-e0c5-4378-8c84-44d15c52b155.JPG">


## Event Storming 결과

* MSAEz 로 모델링한 이벤트스토밍 결과: https://labs.msaez.io/#/storming/zm7538qsNkhoDMQ3F0AUMpn1wHS2/8e220fa460d7f3692354e798ad599a22


### 이벤트 도출

<img width="1371" alt="2021-09-30 11 42 52" src="https://user-images.githubusercontent.com/26429915/135387126-5877858b-47a6-407c-8717-05d21714ff04.JPG">

### 부적격 이벤트 탈락
- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 결제시 > 결제 승인 및 승인 거부 : 외부 시스템의 이벤트이므로 제외


### 액터, 커맨드 부착

<img width="1426" alt="2021-09-30 9 28 38" src="https://user-images.githubusercontent.com/26429915/135387101-b1afbf14-7da3-4b7a-8a8d-a27ef99e6bf6.JPG">


### Aggreggate 및 Bounded Text로 묶기

<img width="1500" alt="2021-09-30 10 01 42" src="https://user-images.githubusercontent.com/26429915/135387105-5b298d62-1f7e-4a96-b60b-764f11420762.JPG">

- Registration의 예약과 취소, Payment의 결제 요청, 결제 취소, Registermaster의 등록 접수/취소, Goods 발송/취소 등 command와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 Aggregate을 구성


### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

<img width="1498" alt="2021-09-30 10 49 08" src="https://user-images.githubusercontent.com/26429915/135387110-91060713-afb7-4b14-a4ec-18857b67eb94.JPG">


### 완성된 1차 모형

<img width="1469" alt="2021-09-30 11 00 17" src="https://user-images.githubusercontent.com/26429915/135387113-46ecafb2-658a-4752-b8f0-d8cec9ea1486.JPG">

    - View Model 추가


### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

<img width="1448" alt="2021-09-30 11 11 59" src="https://user-images.githubusercontent.com/26429915/135387115-436ac9c6-46c0-4bd7-b8e8-ff5d003cfb9b.JPG">

    - 사용자는 등록을 신청한다(ok)
    - 결제를 완료된다 (ok)
    - 주최측이 등록 정보를 확인하고 Goods를 보낸다(ok) 


<img width="1430" alt="2021-09-30 11 16 05" src="https://user-images.githubusercontent.com/26429915/135387116-e47b8186-d102-40e1-9bb0-94d4e1f906cc.JPG">

    - 사용자는 등록을 취소한다 (ok)
    - 등록을 취소하면 결제가 취소된다 (ok)
    - 등록을 취소하면 Goods 발송이 취소된다 (ok)



### 비기능 요구사항에 대한 검증

<img width="1430" alt="2021-09-30 11 33 01" src="https://user-images.githubusercontent.com/26429915/135387119-a6e5e552-b2f2-4f09-961f-a0150dab9c55.JPG">

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
    - 마라톤 등록 신청 시 결제처리 : 등록 완료 시 결제는 Request-Response 방식 처리
    - 나머지 모든 inter-microservice 트랜잭션: 등록 시 결제로 연결되는 트랜잭션 외에는 모두 Eventual Consistency 를 기본으로 채택함



## 헥사고날 아키텍처 다이어그램 도출
    
<img width="1481" alt="2021-09-30 11 51 17" src="https://user-images.githubusercontent.com/26429915/135387120-1b553ff2-a2bf-4052-ac6a-1301966c507f.JPG">


    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 Business로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8080 ~ 8084 이다)

```
cd gateway
mvn spring-boot:run

cd Registration
mvn spring-boot:run 

cd Registermaster
mvn spring-boot:run  

cd payment
mvn spring-boot:run

cd dashboard
mvn spring-boot:run

```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate 객체를 Entity 로 선언하였다: (예시는 Registration 마이크로 서비스).

```

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
        
        PayRequested payRequested = new PayRequested();
        BeanUtils.copyProperties(this, payRequested);
        payRequested.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate() {
        RegisterCancelled registrationCancelled = new RegisterCancelled();
	BeanUtils.copyProperties(this, registrationCancelled);
	registrationCancelled.publishAfterCommit();
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

```


## Codebuild 후 기능 TEST
 - Registration 서비스의 등록 요청
<img width="1481" alt="2021-09-30 11 51 17" src="https://user-images.githubusercontent.com/26429915/135387215-6f9f562e-fc37-4718-8dc7-c3f676a4c7e9.JPG">

 - Payment 서비스의 결재 요청
<img width="1481" alt="2021-09-30 11 51 17" src="https://user-images.githubusercontent.com/26429915/135387219-add5a3b9-89b6-46b7-83d2-90ff3da0976d.JPG">

 - RegisterMaster 서비스의 접수 요청
<img width="1481" alt="2021-09-30 11 51 17" src="https://user-images.githubusercontent.com/26429915/135387221-ed3c49da-9df2-4177-bd30-eff216eba573.JPG">

 - Dashboard 서비스의 조회 기능
<img width="1481" alt="2021-09-30 11 51 17" src="https://user-images.githubusercontent.com/26429915/135387224-f79904e9-2edf-4edd-8896-578cb5361ba2.JPG">

 - Registration 서비스의 취소 요청
<img width="1481" alt="2021-09-30 11 51 17" src="https://user-images.githubusercontent.com/26429915/135387212-1d606539-3520-4e3e-8436-1cf41dc22725.JPG">

 - Dashboard 서비스의 조회 기능(Cancel)
<img width="1481" alt="2021-09-30 11 51 17" src="https://user-images.githubusercontent.com/26429915/135387213-9a625812-aaf6-4cb8-b715-3aab735a5270.JPG">



## 폴리글랏 퍼시스턴스

전체 서비스의 경우 빠른 속도와 개발 생산성을 극대화하기 위해 Spring Boot에서 기본적으로 제공하는 In-Memory DB인 H2 DB를 사용하였다.

```
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
    

# application.yml

  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password:         

    
```

## 폴리글랏 프로그래밍

Dashboard 서비스의 경우 다른 서비스와 다르게 HSQL DB를 사용하였다.

```

# application.yml

  datasource:
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    url: jdbc:hsqldb:mem:testdb
    username: sa
    password:

```


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 등록(registrations)->결제(payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 FeignClient 를 이용하여 호출하도록 한다. 

- 결제 서비스를 호출하기 위하여 FeignClient를 이용하여 Service 대행 인터페이스를 구현 

```

# (Registration) 

package marathon.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="pays", url="${api.url.pay}", fallback = PayServiceFallback.class)
public interface PayService {
    @RequestMapping(method= RequestMethod.POST, path="/request")
    public boolean payRequest(@RequestBody Pay pay);
    
}

```

-  직후(@PostPersist) 결제를 요청하도록 처리
```

# Registration.java (Entity)

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
        
        boolean result = RegistrationApplication.applicationContext.getBean(marathon.external.PayService.class).payRequest(pay);
        
        if(result) {
        	System.out.println("########## 결제가 완료되었습니다 ############");
        } else {
            System.out.println("########## 결제가 실패하였습니다 ############");
        }  
        
        PayRequested payRequested = new PayRequested();
        BeanUtils.copyProperties(this, payRequested);
        payRequested.publishAfterCommit();

        //PVC
        payRequested.saveJasonToPvc(payRequested.toJson());

    }

```



## 비동기식 호출 / 장애격리 / Eventual Consistency


결제가 이루어진 후에 Registermaster 및 Dashboard 등 타 서비스로 이를 알려주는 행위는 동기식이 아니라 비동기식으로 처리하여 불필요한 커플링을 최소화한다.
 
- 이를 위하여 결제 이력에 기록을 남긴 후에 곧바로 결제 요청이 되었다는 도메인 이벤트를 카프카로 송출한다. (Publish)
  이때 다른 저장 로직에 의해서 해당 이벤트가 발송되는 것을 방지하기 위해 Status 체크하는 로직을 추가했다.


- Payment 서비스의 Status로 구분되는 Publish 구현 
```

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
        System.out.println("############################## Pay PostPersist");
        PayCompleted payCompleted = new PayCompleted();
        BeanUtils.copyProperties(this, payCompleted);
        payCompleted.publishAfterCommit();
        
        //PVC
        payCompleted.saveJasonToPvc(payCompleted.toJson());
    }
    @PostUpdate
    public void onPostUpdate(){
        System.out.println("############################## Pay onPostUpdate1");
        if(this.payStatus.equals("CANCEL")){

```
- Registermaster 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package marathon;

...

@Service
public class PolicyHandler {

  ...
  
  @Service
  public class PolicyHandler{
    @Autowired RegisterMasterRepository registerMasterRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCompleted_SaveRegister(@Payload PayCompleted payCompleted){

        if(!payCompleted.validate()) return;
        System.out.println("\n\n##### RegisterMaster PolicyHandler");
        System.out.println("\n\n##### listener SaveRegister : " + payCompleted.toJson() + "\n\n");

        RegisterMaster registerMaster = new RegisterMaster();
        registerMaster.setRegisterId(payCompleted.getRegisterId());
        registerMaster.setName(payCompleted.getName());
        registerMaster.setAddress(payCompleted.getAddress());     
        registerMaster.setPhoneNo(payCompleted.getPhoneNo());
        registerMaster.setTopSize(payCompleted.getTopSize());
        registerMaster.setBottomSize(payCompleted.getBottomSize());
        
        registerMaster.setDeliveryStatus("DELIVERED");
        registerMasterRepository.save(registerMaster);


    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_CancelRegister(@Payload PayCancelled payCancelled){
  
```
- Registermaster 서비스는 예약/결제와 분리되어, Kafka 이벤트 수신에 따라 처리되기 때문에, Registermaster 서비스가 잠시 내려간 상태라도 등록을 받는데 문제가 없다.

```
# Registermaster 서비스 를 잠시 내려놓음 (ctrl+c)

# 등록처리
POST http://localhost:8080/registrations/register   #Success
{
    "name" :"HJK1",
    "phoneNo" :"010-1234-4256",
    "address" :"경기도성남시",
    "topSize" :"110",
    "bottomSize" :"100",
    "amount" : 20000
}

POST http://localhost:8080/registrations/register   #Success
{
    "name" :"SCKIM",
    "phoneNo" :"010-2223-4256",
    "address" :"서울특별시종로구",
    "topSize" :"105",
    "bottomSize" :"100",
    "amount" : 20000
}

#등록상태 확인
GET http://localhost:8080/registrations     # 등록상태 조회 가능

#Registermaster 서비스 기동
cd Registermaster
mvn spring-boot:run

#등록상태 확인
GET http://localhost:8080/Registermaster/     # 등록상태와 결재 상태가 함께 조회됨

```

## CQRS 패턴 구현

등록과 결제 서비스의 완료 / 취소에 대한 현황을 Dashboard로 구현하여 조회할 수 있게 제공


등록완료, 결제 완료/취소에 대한 Event Listener 구현

```
DashboardViewHandler.java

@StreamListener(KafkaProcessor.INPUT)
    public void whenPayCompleted_then_CREATE_1 (@Payload PayCompleted payCompleted) {
        try {
            System.out.println("################################## DashboardViewHandler whenPayCompleted_then_CREATE_1");

            if (!payCompleted.validate()) return;

            // view 객체 생성
            Dashboard dashboard = new Dashboard();
            // view 객체에 이벤트의 Value 를 set 함
            dashboard.setRegisterId(payCompleted.getRegisterId());
            dashboard.setName(payCompleted.getName());
            dashboard.setPhoneNo(payCompleted.getPhoneNo());
            dashboard.setAddress(payCompleted.getAddress());
            dashboard.setStatus("REGISTERED");
            dashboard.setPayStatus(payCompleted.getPayStatus());
            dashboard.setAmount(payCompleted.getAmount());
            dashboard.setTopSize(payCompleted.getTopSize());
            dashboard.setBottomSize(payCompleted.getBottomSize());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRegisterComplete_then_UPDATE_1(@Payload RegisterComplete registerComplete) {
        try {
            System.out.println("################################## DashboardViewHandler whenRegisterComplete_then_UPDATE_1");
            if (!registerComplete.validate()) return;
                // view 객체 조회
            Dashboard dashboard = dashboardRepository.findByregisterId(registerComplete.getRegisterId());
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            
            System.out.println("################################## deliveryStatus : "+registerComplete.getDeliveryStatus());
            dashboard.setDeliveryStatus(registerComplete.getDeliveryStatus());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCancelled_then_UPDATE_2(@Payload PayCancelled payCancelled) {
        try {
            System.out.println("################################## DashboardViewHandler whenPayCancelled_then_UPDATE_2");
            if (!payCancelled.validate()) return;
                // view 객체 조회
            
            Dashboard dashboard = dashboardRepository.findByregisterId(payCancelled.getRegisterId());

            dashboard.setDeliveryStatus(payCancelled.getPayStatus());
            dashboard.setPayStatus(payCancelled.getPayStatus());
            dashboard.setStatus(payCancelled.getPayStatus());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

```



# 운영


## CI/CD 설정
각 구현체들은 각자의 AWS의 ECR 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS-CodeBuild를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 buildspec-kubectl.yaml 에 포함되었다.

- 레포지터리 생성 확인
![image](https://user-images.githubusercontent.com/26429915/135389656-2da72367-f66b-4cc4-9cf1-1fd44c1510b2.JPG)


- 생성 할 CodeBuild
  -user08-gateway
  -user08-registration
  -user08-payment
  -user08-registermaster
  -user08-dashboard
<br/>


- 연결된 github에 Commit 진행시 5개의 서비스들 build 진행 여부 및 성공 확인 

![image](https://user-images.githubusercontent.com/26429915/135387232-4b500cdd-8913-4c3c-85d0-eeb5db52c660.JPG)


-	배포된 5개의 Service  확인
```
> kubectl get all

NAME                                   READY   STATUS    RESTARTS   AGE
pod/dashboard-6f8df44cbf-z2vp8         1/1     Running   0          23m
pod/efs-provisioner-684b79d857-dvrpt   1/1     Running   0          3h19m
pod/gateway-56f448c7dc-v9njt           1/1     Running   0          23m
pod/payment-5848754879-ghmrs           1/1     Running   0          23m
pod/registermaster-67d96dccc6-5frv2    1/1     Running   0          23m
pod/registration-6d886976b-q95lh       1/1     Running   0          23m
pod/siege-75d5587bf6-9lqdl             1/1     Running   0          123m
pod/siege-pvc                          1/1     Running   0          73m   
```




## 동기식 호출 / 서킷 브레이킹 / 장애격리
- 시나리오
  1. 예약(registrations) --> 결재(payment)로 연결을 RESTful Request/Response 로 연동하여 구현 함.
     결제 요청이 과도할 경우 CB가 발생하고 fallback으로 결재 지연 메새지를 보여줌으로 장애 격리 시킴.
  2. circuit break의 timeout은 610mm 설정. 
  3. Pay 서비스에 임의의 부하 처리.
  4. 부하테스터(seige) 를 통한 circuit break 확인. 
    - 결재 지연 메세지 확인.
    - siege의 Availability 100% 확인.
<br/>
    
- 서킷 브레이킹 구현
```
  - Spring FeignClient + Hystrix 옵션을 사용하여 구현함
```

- Hystrix 를 설정 :  처리시간이 610 밀리가 넘어서기 시작하면 CB가 작동하고,
  결제 로직 대신 fallback으로 결제 지연 메세지 보여줌으로 장애 격리
```
# Registration -> application.yml

feign:
  hystrix:
    enabled: true

hystrix:
  command:
    # 전역설정 timeout이 610ms 가 넘으면 CB 처리.
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```
- Registration -> Payment 서비스에 임의 부하 처리
```
# 400 밀리에서 증감 220 밀리 사이에서 부하가 걸리도록 아래 코드 추가
# PayController.java

try {
    Thread.currentThread().sleep((long) (400 + Math.random() * 220));
} catch (InterruptedException e) {
    e.printStackTrace();
}
```
- Registration/PayService에 FeignClient fallback 코드 추가.
```
# PayService.java

@FeignClient(name="pays", url="${api.url.pay}", fallback = PayServiceFallback.class)
```

```
# PayHistoryServiceImple.java

@Service
public class PayServiceImpl implements PayService {
    /**
     * Pay fallback
     */
    public boolean payRequest(Pay pay) {
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        return false;
    }
}
```

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
  - 동시사용자 100명, 30초 동안 실시
  - Registration 서비스의 log 확인.
```
> siege -c100 -t30S --content-type "application/json" 'http://Registration:8080/registrations POST {"name":"HJK100","phoneNo":"010-1234-4256","address":"경기도 성남시 분당구","topSize":"110","bottomSize":"100","amount":20000}'
```
![image](https://user-images.githubusercontent.com/26429915/135387230-aae3cccb-97b7-47c7-9881-2c0e0774efd9.JPG)

![image](https://user-images.githubusercontent.com/26429915/135387227-cf4a7720-a5a5-41e6-8de3-0d314bd56eb6.JPG)


- 결재 서비스에 지연이 발생하는 경우 결재지연 메세지를 보여주고 장애에 분리되어 Avalablity가 100% 이다. 

- 등록 서비스(registrations)의 log에 아래에서 결재 지연 메세지를 확인한다.

- 시스템은 죽지 않고 지속적으로 과도한 부하시 CB 에 의하여 회로가 닫히고 결재 지연중 메세지를 보여주며 사용자를 장애로 부터 격리시킴.


<br/><br/><br/>

## 오토스케일 아웃
- 등록서비스(Registration)에 대해  CPU Load 50%를 넘어서면 Replica를 늘려준다. 
```
  # buildspec-kubectl.yaml
          cat <<EOF | kubectl apply -f -
          apiVersion: autoscaling/v2beta2
          kind: HorizontalPodAutoscaler
          metadata:
            name: reservation-hpa
          spec:
            scaleTargetRef:
              apiVersion: apps/v1
              kind: Deployment
              name: $_POD_NAME
            minReplicas: 1
            maxReplicas: 10
            metrics:
            - type: Resource
              resource:
                name: cpu
                target:
                  type: Utilization
                  averageUtilization: 50
          EOF
```

- 예약서비스(reservation)에 대한 CPU Resouce를 1000m으로 제한 한다.
```
  #buildspec-kubectl.yaml
                    resources:
                      limits:
                        cpu: 1000m
                        memory: 500Mi
                      requests:
                        cpu: 500m
                        memory: 300Mi
```

- Siege (로더제너레이터)를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
```

- 등록 서비스(reseravation)에 워크로드를 동시 사용자 100명 30초 동안 진행한다.
```
siege -c100 -t30S --content-type "application/json" 'http://Registration:8080/registrations POST {"name":"HJK100","phoneNo":"010-1234-4256","address":"경기도 성남시 분당구","topSize":"110","bottomSize":"100","amount":20000}'
```
<br/>

- 결과확인

![image](https://user-images.githubusercontent.com/26429915/135387235-3933bf1f-189c-42c9-ac34-3361502dc117.JPG)



<br/><br/>

## Self Healing
### ◆ Liveness- HTTP Probe
- 시나리오
  1. Registration 서비스의 Liveness 설정을 확인한다
  2. Registration 서비스의 Liveness Probe는 actuator의 health 상태를 확인한다
  3. pod의 상태를 모니터링 한다
  4. Registration 서비스의 Liveness Probe인 actuator를 down 시켜 서비스가 termination / restart 되는 self healing을 확인한다
  5. Registration 서비스의 describe를 확인하여 Restart가 되는 부분을 확인한다


- Registration 서비스의 Liveness probe 설정 확인
```
kubectl get deploy reservation -o yaml

                  :
        livenessProbe:
          failureThreshold: 5
          httpGet:
            path: /actuator/health
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 120
          periodSeconds: 5
          successThreshold: 1
          timeoutSeconds: 2
                  :
```

- Httpie를 사용하기 위해 Siege를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
```

- Liveness Probe 확인 

![image](https://user-images.githubusercontent.com/26429915/135387196-783a471d-9a33-454c-b49a-b8066381b299.JPG)

- Liveness Probe Down 설정 및 확인 후 Registration Liveness Probe를 Down 상태로 전환한다.
![image](https://user-images.githubusercontent.com/26429915/135387201-6407dc1c-aced-4280-a3c4-07502122e5d8.JPG)


- Probe Fail에 따른 쿠버네티스 동작을 확인 
  - registration 서비스의 Liveness Probe가 /actuator/health의 상태가 DOWN이 된 것을 보고 restart를 진행함. 
  - registration pod의 RESTARTS가 1로 바뀐것을 확인. 
  - 해당 pod가 Liveness가 Unhealthy, failed 된 것을 알 수 있다.
![image](https://user-images.githubusercontent.com/26429915/135387203-59454d50-f7d2-46e9-9a94-3ff87202ca9c.JPG)
![image](https://user-images.githubusercontent.com/26429915/135387202-ba805b78-03bf-4c73-b037-3a97f177a861.JPG)

<br/><br/>
	
## 무정지 재배포
### ◆ Rediness- HTTP Probe
- 시나리오
  1. 현재 구동중인 Registration 서비스에 길게 부하를 준다. 
  2. reservation pod의 상태를 모니터링
  3. AWS CodeBuild에 연결 되어있는 github로 코드를 commit한다.
  4. Codebuild를 통해 새로운 버전의 Registration이 배포 된다. 
  5. pod 상태 모니터링에서 기존 Registration 서비스가 Terminating 되고 새로운 Registration 서비스가 Running하는 것을 확인한다.
  6. Readness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 siege의 Avality가 100%가 된다.

<br/>

- reservstion 서비스의 Readness probe  설정 확인
  - buildspec_kubectl.yaml
```
                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
```

- 현재 구동중인 Registration 서비스에 길게(1~2분) 부하를 준다. 
```
 siege -v -c1 -t120S --content-type "application/json" 'http://Registration:8080/registrations POST {"name":"HJK100","phoneNo":"010-1234-4256","address":"경기도 성남시 
```

- 동시에 AWS CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  배포 될때까지 잠시 기다린다. 

![image](https://user-images.githubusercontent.com/26429915/135387210-db904bf8-5e1e-45e5-8d6c-fc95a71c75ec.JPG)


- pod 상태 모니터링에서 기존 Registration 서비스가 Terminating 되고 새로운 Registration 서비스가 Running하는 것을 확인한다.

![image](https://user-images.githubusercontent.com/26429915/135397301-f63a9f64-2260-441e-ba6c-d0d65e09b722.JPG)


- Readiness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 siege의 Avalabilty가 100%가 된다.

![image](https://user-images.githubusercontent.com/26429915/135387211-40293b13-f127-47a8-abce-e36d1c18e9ec.JPG)



<br/><br/>

## Persistant Volume Claim
- 시나리오
  1. EFS 생성 화면 캡쳐.
  2. 등록된 provisoner / storageclass / pvc 확인.
  3. 각 서비스의 buildspec_kubectl.yaml에 pvc 생성 정보 확인.
  4. bash shell을 사용할 수 있는 pod를 동일한 PVC 사용할 수 있게 설정 후 배포하여 '/mnt/aws'에 각 서비스에서 생성한 파일을 확인. 
  

- EFS 등록 화면 추가..
![image](https://user-images.githubusercontent.com/26429915/135391975-3fa8c9ce-86bb-4c14-b8ab-74bfb248e870.JPG)


- provisioner 확인 및 storageClass 등록, 조회
![image](https://user-images.githubusercontent.com/26429915/135392360-e41c9395-b8f3-4005-9dde-dcaa37218462.JPG)


- pvc 확인

![image](https://user-images.githubusercontent.com/26429915/135392358-90289027-0e32-40ca-812f-b50f58b95466.JPG)

<br/>

- 각 Deployment의 PVC 생성정보는 buildspec-kubeclt.yaml에 적용되어있음
```
                    volumeMounts:
                      - mountPath: "/mnt/aws"
                        name: volume
                        :
                        :
                        :
                volumes:
                  - name: volume
                    persistentVolumeClaim:
                      claimName: aws-efs
```


- 각 서비스의 Event 발생시 JSON 정보를 파일로 저장한다. 마지막 정보만 저정하기 위해 Overwirte하여 저장한다. 
  - 아래와 같은 코드를 통하여 /mnt/aws의 경로에 파일을 저장한다. 
```
# AbstractEvent.java

// PVC Test
    public void saveJasonToPvc(String strJson){
        File file;

        if (strJson.equals("CANCEL")){
		    file = new File("/mnt/aws/registrationCancelled_json.txt");
        }else{
            file = new File("/mnt/aws/payRequested_json.txt");
        }

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(strJson);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}     

```

- 각 서비스에서 저장한 Event 정보파일을 동일한 PVC를 사용하는 Pod를 생성하여 배포 후 /mnt/aws에 저장되어 있는지 확인. 
```
> kubectl apply -f kubectl apply -f https://raw.githubusercontent.com/sasin4/marathon/master/yaml/pod-with-pvc.yaml
> kubectl get pod
> kubectl describe pod registrations
> kubectl exec -it siege-pvc -- /bin/bash
> ls -al /mnt/aws
```

![image](https://user-images.githubusercontent.com/26429915/135387207-bd328393-f6b5-48ec-a083-4babd9630863.JPG)


[END]

<br/>
