import in.local.liveplus.Engine;
import java.time.*;
import java.util.*;

public class EngineTest {
 static int checks=0;
 static void check(boolean b,String name){checks++;if(!b)throw new AssertionError(name);}
 static Engine.Tx purchase(long id,long amount,int category){Engine.Tx t=new Engine.Tx();t.id=id;t.amount=amount;t.category=category;t.confirmed=true;t.merchant="Example";t.date="2026-10-31";return t;}
 public static void main(String[] args){
  long now=LocalDate.of(2026,10,31).atTime(15,0).atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli();
  Engine.Parsed p=Engine.parse("AX-HSBCBK","INR 1,234.50 spent on your HSBC Credit Card ending 1234 at SWIGGY on 30/10/2026. Avl limit INR 40,000.","1234",now);
  check(p!=null&&p.amount==123450&&p.merchant.equals("SWIGGY")&&p.date.equals("2026-10-30"),"purchase parse chooses spend, merchant and date");
  check(Engine.parse("HSBC","Your HSBC Credit Card XXXX1234 has been used for Rs.500.00 at CAFE on 31-Oct-26.","1234",now)!=null,"masked card and named month");
  check(Engine.parse("HSBC","INR 500 spent on HSBC Credit Card ending 5678 at SWIGGY", "1234",now)==null,"other card rejected");
  check(Engine.parse("HSBC","OTP 123456 for transaction of INR 500 on HSBC card ending 1234", "1234",now)==null,"OTP rejected");
  check(Engine.parse("HSBC","Transaction INR 500 declined on HSBC card XXXX1234", "1234",now)==null,"decline rejected");
  check(Engine.parse("HSBC","Payment of INR 1000 received for HSBC card ending 1234", "1234",now)==null,"repayment rejected");
  check(Engine.parse("HSBC","INR 1000 credited to HSBC card ending 1234. Transaction successful.", "1234",now)==null,"generic credit rejected");
  check(Engine.parse("Other bank","INR 500 spent on card XXXX1234 at SWIGGY", "1234",now)==null,"non-HSBC rejected");
  p=Engine.parse("HSBC","Refund of INR 500 credited to HSBC card ending 1234 at SWIGGY on 31/10/26", "1234",now);
  check(p!=null&&p.refund&&p.amount==50000,"refund captured separately");
  check(Engine.money("1,234.50")==123450,"decimal precision");
  check(Engine.suggest("Amazon",LocalDate.of(2026,10,31),false)==5,"amazon base rate");
  check(Engine.suggest("Myntra",LocalDate.of(2026,10,31),false)==5,"promo opt-in only");
  check(Engine.suggest("Myntra",LocalDate.of(2026,10,31),true)==3,"promo last day included");
  check(Engine.suggest("Myntra",LocalDate.of(2026,11,1),true)==5,"promo expiry");
  check(Engine.suggest("New merchant",LocalDate.of(2026,10,31),true)==0,"unknown stays unknown");
  check(Engine.cycleStart(LocalDate.of(2026,10,15),15).equals(LocalDate.of(2026,9,16)),"closing date belongs to old period");
  check(Engine.cycleStart(LocalDate.of(2026,10,16),15).equals(LocalDate.of(2026,10,16)),"new period next day");
  check(Engine.cycleEnd(LocalDate.of(2026,10,16),15).equals(LocalDate.of(2026,11,15)),"statement end");
  check(Engine.cycleStart(LocalDate.of(2026,3,1),31).equals(LocalDate.of(2026,3,1)),"short month boundary");
  check(Engine.cycleEnd(LocalDate.of(2026,1,31),30).equals(LocalDate.of(2026,2,28)),"short month closing day clamps");
  check(Engine.cycleEnd(LocalDate.of(2028,2,1),0).equals(LocalDate.of(2028,2,29)),"leap year");
  List<Engine.Tx> rows=new ArrayList<>();rows.add(purchase(1,1300000,1));rows.add(purchase(2,100000,5));rows.add(purchase(3,99999,6));Engine.Tx unknown=purchase(4,20000,0);rows.add(unknown);
  Engine.Totals t=Engine.totals(rows,LocalDate.of(2026,10,1),LocalDate.of(2026,10,31),120000);
  check(t.accelerated==1300000&&t.cashback==121500&&t.remaining==0&&t.unknown==20000,"shared cap no base fallback, unknown excluded");
  Engine.Tx duplicate=purchase(5,100000,1);duplicate.duplicate=true;rows.add(duplicate);
  t=Engine.totals(rows,LocalDate.of(2026,10,1),LocalDate.of(2026,10,31),120000);check(t.accelerated==1300000&&t.unknown==120000,"possible duplicate excluded");
  Engine.Tx r=purchase(6,300000,0);r.refund=true;rows.add(r);t=Engine.totals(rows,LocalDate.of(2026,10,1),LocalDate.of(2026,10,31),120000);check(t.accelerated==1300000&&t.refunds==300000,"unlinked refund excluded");
  r.linked="1";r.date="2026-11-02";t=Engine.totals(rows,LocalDate.of(2026,10,1),LocalDate.of(2026,10,31),120000);check(t.accelerated==1000000&&t.cashback==101500&&t.remaining==200000,"linked later refund reduces original period");
  rows.get(0).active=false;t=Engine.totals(rows,LocalDate.of(2026,10,1),LocalDate.of(2026,10,31),120000);check(t.accelerated==0,"deleted purchase excluded");
  List<Engine.Tx> reviewRows=new ArrayList<>();
  Engine.Tx suggested=purchase(10,10000,1);suggested.confirmed=false;reviewRows.add(suggested);
  Engine.Tx unclassified=purchase(11,10000,0);reviewRows.add(unclassified);
  Engine.Tx possibleDuplicate=purchase(12,10000,1);possibleDuplicate.duplicate=true;reviewRows.add(possibleDuplicate);
  Engine.Tx unlinkedRefund=purchase(13,10000,0);unlinkedRefund.refund=true;reviewRows.add(unlinkedRefund);
  Engine.Tx linkedRefund=purchase(14,10000,0);linkedRefund.refund=true;linkedRefund.linked="10";reviewRows.add(linkedRefund);
  reviewRows.add(purchase(15,10000,1));
  Engine.Tx deleted=purchase(16,10000,0);deleted.active=false;reviewRows.add(deleted);
  Engine.Tx earlier=purchase(17,10000,0);earlier.date="2026-09-30";reviewRows.add(earlier);
  Engine.Tx later=purchase(18,10000,0);later.date="2026-11-01";reviewRows.add(later);
  LocalDate octoberStart=LocalDate.of(2026,10,1),octoberEnd=LocalDate.of(2026,10,31);
  check(Engine.reviewCount(reviewRows,octoberStart,octoberEnd)==4,"review badge includes suggestions, unknowns, duplicates and unlinked refunds only in the selected period");
  check(Engine.totals(reviewRows,octoberStart,octoberEnd,120000).review==4,"overview review count agrees with activity badge");
  suggested.confirmed=true;unclassified.category=2;possibleDuplicate.duplicate=false;unlinkedRefund.linked="15";
  check(Engine.reviewCount(reviewRows,octoberStart,octoberEnd)==0,"review badge clears after items are resolved");
  System.out.println("PASS: "+checks+" parser, reward, refund, period, and review checks");
 }
}
