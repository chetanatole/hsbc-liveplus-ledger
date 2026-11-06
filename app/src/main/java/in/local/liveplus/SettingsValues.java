package in.local.liveplus;

/** Validation before an automatic settings write. Invalid edits never replace saved values. */
public final class SettingsValues {
 public static String last4(String value,String previous,boolean hasTransactions){
  if(value==null||!value.matches("[0-9]{4}"))throw new IllegalArgumentException("Enter all four digits.");
  if(hasTransactions&&previous!=null&&!previous.isEmpty()&&!previous.equals(value))throw new IllegalArgumentException("Export and erase this ledger before changing cards.");
  return value;
 }
 public static int day(String value){
  try{if(value==null||!value.trim().matches("[0-9]{1,2}"))throw new Exception();int day=Integer.parseInt(value.trim());if(day>31)throw new Exception();return day;}catch(Exception ignored){throw new IllegalArgumentException("Enter a day from 0 to 31.");}
 }
 public static long cap(String value){
  try{long amount=Engine.money(value);if(amount<=0||amount>10000000L)throw new Exception();return amount;}catch(Exception ignored){throw new IllegalArgumentException("Enter an amount above ₹0 and up to ₹1,00,000.");}
 }
}
