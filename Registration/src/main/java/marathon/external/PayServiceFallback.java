package marathon.external;

public class PayServiceFallback implements PayService{
    @Override
    public boolean payRequest(Pay pay) {
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        return false;
    }
}