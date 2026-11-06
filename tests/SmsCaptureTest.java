import in.local.liveplus.SmsCapture;
import java.util.*;

public class SmsCaptureTest {
 static int checks;
 static void check(boolean value,String name){checks++;if(!value)throw new AssertionError(name);}
 static final long NOW=NotificationCaptureTest.NOW,START=NOW-60000;
 static final String PURCHASE=NotificationCaptureTest.PURCHASE,OTP=NotificationCaptureTest.OTP;
 static List<SmsCapture.Part> sms(String body,long sent){return Collections.singletonList(new SmsCapture.Part("TEST-SENDER",body,sent));}
 static SmsCapture.Candidate collect(List<SmsCapture.Part> parts){return SmsCapture.collect(parts,"1234",START,NOW,1);}
 public static int run(){
  checks=0;SmsCapture.Candidate purchase=collect(sms(PURCHASE,NOW-1000));
  check(purchase!=null&&purchase.transaction.amount==10123&&purchase.transaction.merchant.equals("ZOMATO"),"new incoming SMS parses a matching purchase");
  check(collect(sms(OTP,NOW-2000))==null,"OTP SMS is discarded independently");
  check(collect(sms(PURCHASE,NOW-1000))!=null,"purchase after OTP remains parseable without clearing notifications");
  check(collect(sms(PURCHASE+" "+OTP,NOW-1000))==null,"mixed OTP purchase in the same SMS is rejected");
  int split=PURCHASE.indexOf("ZOMATO")+3;
  List<SmsCapture.Part> parts=Arrays.asList(new SmsCapture.Part("TEST-SENDER",PURCHASE.substring(0,split),NOW-1000),new SmsCapture.Part("TEST-SENDER",PURCHASE.substring(split),NOW-1000));
  SmsCapture.Candidate multipart=collect(parts);
  check(multipart!=null&&multipart.event.equals(purchase.event),"multipart SMS is joined before parsing and fingerprinting");
  check(SmsCapture.collect(sms(PURCHASE,NOW-1000),"1234",START,NOW+1000,1).event.equals(purchase.event),"redelivery at a later callback time has the same identity");
  check(!collect(sms(PURCHASE,NOW-999)).event.equals(purchase.event),"separate identical SMS at different send times retain distinct identities");
  check(!SmsCapture.collect(sms(PURCHASE,NOW-1000),"1234",START,NOW,2).event.equals(purchase.event),"SIM subscription distinguishes otherwise identical deliveries");
  check(collect(sms(PURCHASE,START-1))!=null,"newly delivered SMS may have an older network timestamp without querying stored history");
  check(SmsCapture.collect(sms(PURCHASE,NOW-1000),"1234",START,START-1,1)==null,"callback predating capture start is ignored");
  check(collect(sms(PURCHASE,0))==null&&collect(sms(PURCHASE,NOW+300001))==null,"invalid or implausibly future timestamps are rejected");
  check(collect(sms(PURCHASE.replace("1234","5678"),NOW-1000))==null,"other card suffix is rejected");
  check(collect(sms(PURCHASE.replace("used at","declined at"),NOW-1000))==null,"declined SMS remains rejected");
  check(collect(sms("Unrelated personal message",NOW-1000))==null,"unrelated incoming SMS is discarded");
  check(collect(Arrays.asList(new SmsCapture.Part("ONE",PURCHASE.substring(0,split),NOW-1000),new SmsCapture.Part("TWO",PURCHASE.substring(split),NOW-1000)))==null,"different senders are never assembled into one transaction");
  check(collect(Collections.emptyList())==null&&collect(null)==null&&collect(Arrays.asList((SmsCapture.Part)null))==null,"missing or malformed parts safely fail");
  check(collect(sms(String.join("",Collections.nCopies(100,PURCHASE)),NOW-1000))==null,"oversized SMS body is bounded");
  check(collect(Collections.nCopies(65,new SmsCapture.Part("TEST-SENDER",PURCHASE,NOW-1000)))==null,"multipart count is bounded");
  check(purchase.event.matches("[a-f0-9]{64}")&&!purchase.event.contains("TEST-SENDER")&&!purchase.event.contains("1234"),"persisted event identity is a hash without raw SMS or sender");
  check(purchase.received==NOW,"receipt time is retained separately from bank transaction date");
  return checks;
 }
}