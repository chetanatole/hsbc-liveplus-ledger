import in.local.liveplus.SettingsValues;

public class SettingsValuesTest {
 static int checks;
 static void check(boolean value,String name){checks++;if(!value)throw new AssertionError(name);}
 static void rejects(Runnable edit,String name){checks++;try{edit.run();}catch(IllegalArgumentException expected){return;}throw new AssertionError(name);}
 public static int run(){
  checks=0;
  check(SettingsValues.last4("0123","",false).equals("0123"),"complete suffix including a leading zero is accepted");
  rejects(()->SettingsValues.last4("123","",false),"partial suffix is not eligible for automatic persistence");
  rejects(()->SettingsValues.last4("12ab","",false),"non-digit suffix is rejected");
  check(SettingsValues.last4("1234","1234",true).equals("1234"),"existing ledger permits retaining its current card");
  rejects(()->SettingsValues.last4("5678","1234",true),"automatic edits cannot switch a populated ledger to another card");
  check(SettingsValues.last4("5678","1234",false).equals("5678"),"an empty ledger permits a card change");
  check(SettingsValues.day("0")==0&&SettingsValues.day("31")==31,"calendar mode and closing-day upper boundary are valid");
  rejects(()->SettingsValues.day(""),"clearing day does not persist an empty setting");
  rejects(()->SettingsValues.day("32"),"out-of-range day is rejected");
  rejects(()->SettingsValues.day("-1"),"negative day is rejected");
  rejects(()->SettingsValues.day("1.5"),"fractional day is rejected");
  check(SettingsValues.cap("1,200.50")==120050,"cashback cap preserves exact paise and accepts grouped input");
  check(SettingsValues.cap("0.01")==1&&SettingsValues.cap("100000")==10000000,"cap accepts positive minimum and maximum");
  rejects(()->SettingsValues.cap("0"),"zero cap is rejected");
  rejects(()->SettingsValues.cap("100000.01"),"cap above maximum is rejected");
  rejects(()->SettingsValues.cap("1.001"),"fractional paise are rejected without rounding");
  rejects(()->SettingsValues.cap("9999999999999999999999"),"overflow cannot replace a saved cap");
  rejects(()->SettingsValues.cap(""),"clearing cap does not persist an empty setting");
  return checks;
 }
}
