package in.local.liveplus;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
 static final int BRAND_INK=Color.rgb(20,45,53), MINT=Color.rgb(185,237,204);
 int INK,MUTED,BG,SURFACE,NOTICE,CORAL,SUCCESS,SELECTED,ON_SELECTED,REVIEW,ON_REVIEW,TAB_BG,TAB_BORDER;
 boolean darkMode,pendingSmsEnable,pendingNotificationEnable;
 final Handler settingsHandler=new Handler(Looper.getMainLooper());
 final Map<EditText,Runnable> settingWrites=new LinkedHashMap<>();
 SharedPreferences p; Store store; LinearLayout root,body,nav; String tab="Overview"; boolean reviewOnly=false; LocalDate anchor=LocalDate.now(ZoneId.of("Asia/Kolkata"));
 Set<String> selectedPackages=new HashSet<>();
 ScrollView scroll;
 Bundle scrollPositions=new Bundle();
 String renderedPage;
 boolean restoringScroll;
 final BroadcastReceiver changed=new BroadcastReceiver(){public void onReceive(Context c,Intent i){draw();}};
 @Override public void onCreate(Bundle b){p=Store.prefs(this);CaptureStore.prepareEdition(this);if(!CaptureMode.SMS)SmsApps.selected(this);darkMode=p.getString("theme","light").equals("dark");setTheme(darkMode?R.style.AppTheme_Dark:R.style.AppTheme_Light);super.onCreate(b);applyPalette();store=new Store(this);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(darkMode?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);if(b!=null){pendingSmsEnable=b.getBoolean("pendingSmsEnable",false);pendingNotificationEnable=b.getBoolean("pendingNotificationEnable",false);tab=b.getString("tab","Overview");anchor=LocalDate.parse(b.getString("anchor",anchor.toString()));reviewOnly=b.getBoolean("reviewOnly",false);Bundle saved=b.getBundle("scrollPositions");if(saved!=null)scrollPositions=saved;}draw();}
 @Override public void onSaveInstanceState(Bundle b){flushSettings();rememberScroll();b.putBoolean("pendingSmsEnable",pendingSmsEnable);b.putBoolean("pendingNotificationEnable",pendingNotificationEnable);b.putString("tab",tab);b.putString("anchor",anchor.toString());b.putBoolean("reviewOnly",reviewOnly);b.putBundle("scrollPositions",new Bundle(scrollPositions));super.onSaveInstanceState(b);}
 @Override public void onResume(){super.onResume();IntentFilter f=new IntentFilter("in.local.liveplus.CHANGED");if(Build.VERSION.SDK_INT>=33)registerReceiver(changed,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(changed,f);if(pendingNotificationEnable){pendingNotificationEnable=false;if(access()&&savedCardReady())startCapture();}draw();}
 @Override public void onPause(){flushSettings();rememberScroll();unregisterReceiver(changed);super.onPause();}
 @Override public void onDestroy(){flushSettings();settingsHandler.removeCallbacksAndMessages(null);store.close();super.onDestroy();}
 int themeColor(int attribute){android.util.TypedValue value=new android.util.TypedValue();getTheme().resolveAttribute(attribute,value,true);return value.data;}
 void applyPalette(){
  INK=themeColor(android.R.attr.textColorPrimary);MUTED=themeColor(android.R.attr.textColorSecondary);BG=themeColor(android.R.attr.colorBackground);
  SURFACE=themeColor(R.attr.ledgerSurface);NOTICE=themeColor(R.attr.ledgerNotice);CORAL=themeColor(R.attr.ledgerAttention);SUCCESS=themeColor(R.attr.ledgerSuccess);
  SELECTED=themeColor(R.attr.ledgerSelected);ON_SELECTED=themeColor(R.attr.ledgerOnSelected);REVIEW=themeColor(R.attr.ledgerReview);ON_REVIEW=themeColor(R.attr.ledgerOnReview);
  TAB_BG=themeColor(R.attr.ledgerTabBackground);TAB_BORDER=themeColor(R.attr.ledgerTabBorder);
 }
 int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
 TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("tnum");t.setLineSpacing(dp(3),1);if(bold)t.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));return t;}
 LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 void gap(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
 void add(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 LinearLayout card(LinearLayout parent,int color){LinearLayout l=column();l.setPadding(dp(20),dp(20),dp(20),dp(20));l.setBackground(bg(color,22));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);parent.addView(l,lp);return l;}
 Button button(String s,View.OnClickListener click){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(BRAND_INK);b.setTextSize(14);b.setMinHeight(dp(48));b.setBackgroundTintList(ColorStateList.valueOf(MINT));b.setOnClickListener(click);return b;}
 void styleTab(Button button,boolean selected,boolean attention){
  int fill=selected?SELECTED:attention?REVIEW:TAB_BG;
  int foreground=selected?ON_SELECTED:attention?ON_REVIEW:INK;
  int outline=selected?SELECTED:attention?CORAL:TAB_BORDER;
  GradientDrawable normal=bg(fill,12);normal.setStroke(dp(1),outline);
  GradientDrawable focused=bg(fill,12);focused.setStroke(dp(3),CORAL);
  android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();
  states.addState(new int[]{android.R.attr.state_focused},focused);states.addState(new int[]{},normal);
  int ripple=Color.argb(48,Color.red(foreground),Color.green(foreground),Color.blue(foreground));
  button.setBackgroundTintList(null);
  button.setBackground(new android.graphics.drawable.RippleDrawable(ColorStateList.valueOf(ripple),states,bg(Color.WHITE,12)));
  button.setTextColor(foreground);button.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
  button.setPadding(dp(8),dp(4),dp(8),dp(4));button.setSelected(selected);button.setStateListAnimator(null);button.setElevation(dp(1));
 }
 String cash(long n){return java.text.NumberFormat.getCurrencyInstance(new Locale("en","IN")).format(n/100d);}
 int day(){return p.getInt("day",0);} long cap(){return p.getLong("cap",120000);}
 LocalDate start(){return Engine.cycleStart(anchor,day());} LocalDate end(){return Engine.cycleEnd(start(),day());}
 boolean access(){return CaptureMode.access(this);}
 void rememberScroll(){
  if(scroll!=null&&renderedPage!=null&&!restoringScroll)scrollPositions.putInt(renderedPage,scroll.getScrollY());
 }
 String pageKey(){return tab.equals("Settings")?tab:tab+"/"+start()+"/"+end()+(tab.equals("Activity")?"/"+reviewOnly:"");}
 void restoreScroll(){
  final ScrollView target=scroll;
  final int position=scrollPositions.getInt(renderedPage,0);
  restoringScroll=true;
  target.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener(){
   public void onGlobalLayout(){
    target.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    if(scroll!=target)return;
    target.scrollTo(0,position);
    restoringScroll=false;
   }
  });
 }
 String sourceLabel(String source){
  if(source.equals("manual"))return "Manual";
  if(source.startsWith("Alert"))return "Alert";
  if(source.startsWith("Pasted"))return "Pasted";
  return source;
 }
 void draw(){
  flushSettings();rememberScroll();
  root=column();root.setBackgroundColor(BG);root.setPadding(dp(20),dp(12),dp(20),0);setContentView(root);
  root.setOnApplyWindowInsetsListener((v,i)->{root.setPadding(dp(20),Math.max(dp(12),i.getSystemWindowInsetTop()),dp(20),i.getSystemWindowInsetBottom());return i;});
  LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView brand=text("Live+ Ledger",28,INK,true);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));add(root,top);
  TextView subtitle=text("Your everyday cashback, in view.",13,MUTED,false);add(root,subtitle);gap(root,12);
  nav=new LinearLayout(this);
  for(String name:new String[]{"Overview","Activity","Settings"}){
   boolean selected=tab.equals(name);
   Button b=button(name,v->{tab=name;reviewOnly=false;draw();});styleTab(b,selected,false);
   b.setContentDescription(name+" tab"+(selected?", selected":", tap to open"));
   LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(50),1);if(nav.getChildCount()>0)params.setMarginStart(dp(6));nav.addView(b,params);
  }
  add(root,nav);gap(root,8);
  if(tab.equals("Activity"))activityActions();
  scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(tab.equals("Activity"));scroll.setHorizontalScrollBarEnabled(false);scroll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);scroll.setFocusableInTouchMode(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=column();body.setPadding(0,dp(6),0,dp(24));scroll.addView(body);
  renderedPage=pageKey();
  if(tab.equals("Settings"))settings();else{period();if(tab.equals("Overview"))overview();else activity();}
  restoreScroll();
 }
 void period(){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);Button prev=button("‹",v->{anchor=start().minusDays(1);draw();});prev.setContentDescription("Previous reward period");row.addView(prev,new LinearLayout.LayoutParams(dp(48),dp(48)));TextView t=text(start().format(DateTimeFormatter.ofPattern("d MMM"))+" — "+end().format(DateTimeFormatter.ofPattern("d MMM yyyy")),15,INK,true);t.setGravity(Gravity.CENTER);row.addView(t,new LinearLayout.LayoutParams(0,-2,1));Button next=button("›",v->{anchor=end().plusDays(1);draw();});next.setContentDescription("Next reward period");row.addView(next,new LinearLayout.LayoutParams(dp(48),dp(48)));add(body,row);TextView mode=text(day()==0?"Calendar month" : "Statement period · closes on day "+day(),12,MUTED,false);mode.setGravity(Gravity.CENTER);add(body,mode);gap(body,16);}
 void overview(){
  List<Engine.Tx> rows=store.all();Engine.Totals t=Engine.totals(rows,start(),end(),cap());
  if(p.getString("last4","").isEmpty()||!p.getBoolean("periodSet",false)){LinearLayout setup=card(body,NOTICE);add(setup,text("Make it yours",20,INK,true));gap(setup,6);add(setup,text("Set your card’s last four digits and reward period before you start tracking.",14,INK,false));add(setup,button("Set up my card",v->{tab="Settings";draw();}));}
  LinearLayout hero=card(body,BRAND_INK);add(hero,text("ACCELERATED CASHBACK · ESTIMATE",11,MINT,true));gap(hero,8);long bonus=t.acceleratedCashback;add(hero,text(cash(bonus),42,Color.WHITE,true));add(hero,text("of "+cash(cap())+" cap this period",14,Color.rgb(202,220,222),false));gap(hero,18);
  ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(1000);bar.setProgress((int)Math.min(1000,bonus*1000/Math.max(1,cap())));bar.setProgressTintList(ColorStateList.valueOf(MINT));bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(61,82,86)));add(hero,bar);gap(hero,12);add(hero,text(cash(t.remaining)+" eligible spend left at 10%",17,MINT,true));
  LinearLayout info=card(body,SURFACE);add(info,text("THIS PERIOD",11,MUTED,true));gap(info,10);
  cashbackMetric(info,"Eligible spend at 10%",t.accelerated,t.acceleratedCashback);
  cashbackMetric(info,"Other eligible spend · 1.5%",t.other,t.otherCashback);
  metric(info,"Excluded spend · 0%",cash(t.excluded));
  if(t.unknown>0)metric(info,"Unclassified / possible duplicate",cash(t.unknown));
  View divider=new View(this);divider.setBackgroundColor(TAB_BORDER);info.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));gap(info,4);
  cashbackMetric(info,"Total spend",t.totalSpend,t.cashback,"Total estimated cashback");
  if(t.review>0){LinearLayout review=card(body,NOTICE);add(review,text(t.review+" item"+(t.review==1?"":"s")+" to review",19,INK,true));gap(review,5);add(review,text("Suggested categories count toward the estimate. Unknown categories, refunds and possible duplicates need your review.",13,INK,false));add(review,button("Review activity",v->{tab="Activity";reviewOnly=true;draw();}));}
  LinearLayout capture=card(body,SURFACE);boolean live=p.getBoolean("capture",false)&&access();add(capture,text(live?"●  Capture enabled":"○  Capture is off",16,live?SUCCESS:MUTED,true));gap(capture,6);add(capture,text(CaptureMode.SMS?"Capture new HSBC transaction SMS. Existing messages are never read.":"Capture HSBC transaction alerts from your selected SMS app.",13,MUTED,false));if(!CaptureMode.SMS){gap(capture,6);add(capture,text("Android may hide alerts grouped with an OTP. Clear that conversation’s notifications before the next alert. Paste any missed purchase in Activity after checking for an existing entry.",13,CORAL,false));}if(p.getLong("lastCaptured",0)>0)add(capture,text("Last captured purchase: "+Instant.ofEpochMilli(p.getLong("lastCaptured",0)).atZone(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ofPattern("d MMM, h:mm a")),12,MUTED,false));if(p.getBoolean("captureError",false))add(capture,text("A transaction may have been missed. Check Activity and add it manually.",13,CORAL,true));
  add(body,button("Add purchase",v->edit(null)));
  gap(body,12);add(body,text("Estimates use merchant suggestions until you verify the bank’s MCC. No bank login or full card number is needed. Independent app. not affiliated with HSBC.",12,MUTED,false));
 }
 void metric(LinearLayout l,String label,String value){LinearLayout r=new LinearLayout(this);r.setPadding(0,dp(7),0,dp(7));r.addView(text(label,14,MUTED,false),new LinearLayout.LayoutParams(0,-2,1));r.addView(text(value,16,INK,true));add(l,r);}
 void cashbackMetric(LinearLayout parent,String label,long spend,long cashback){
  cashbackMetric(parent,label,spend,cashback,"Estimated cashback");
 }
 void cashbackMetric(LinearLayout parent,String label,long spend,long cashback,String cashbackLabel){
  metric(parent,label,cash(spend));
  LinearLayout row=new LinearLayout(this);row.setPadding(0,0,0,dp(9));
  row.addView(text(cashbackLabel,12,MUTED,false),new LinearLayout.LayoutParams(0,-2,1));
  row.addView(text(cash(cashback),14,INK,true));add(parent,row);
 }
 void activityActions(){
  LinearLayout actions=new LinearLayout(this);
  actions.addView(button("Add purchase",v->edit(null)),new LinearLayout.LayoutParams(0,-2,1));
  actions.addView(button("Paste alert",v->paste()),new LinearLayout.LayoutParams(0,-2,1));
  add(root,actions);
  int count=Engine.reviewCount(store.all(),start(),end());
  Button review=button((reviewOnly?"✓ ":"")+"Needs review"+(count>0?" ("+count+")":""),v->{reviewOnly=!reviewOnly;draw();});
  styleTab(review,reviewOnly,count>0);
  review.setContentDescription("Needs review, "+count+" item"+(count==1?"":"s")+" in this period. "+(reviewOnly?"Filter on. Tap to show all transactions.":"Tap to show only items needing review."));
  add(root,review);gap(root,6);
 }
 void activity(){
  int count=0;for(Engine.Tx x:store.all()){
   LocalDate d=LocalDate.parse(x.date);if(!x.active||d.isBefore(start())||d.isAfter(end()))continue;
   boolean needs=Engine.needsReview(x);if(reviewOnly&&!needs)continue;count++;
   LinearLayout c=card(body,SURFACE);LinearLayout r=new LinearLayout(this);r.addView(text(x.merchant,17,INK,true),new LinearLayout.LayoutParams(0,-2,1));r.addView(text((x.refund?"−":"")+cash(x.amount),17,INK,true));add(c,r);gap(c,6);
   String status=x.refund?(x.linked.isEmpty()?"Refund · link to original purchase":"Refund linked to purchase #"+x.linked):(x.duplicate?"Possible duplicate · excluded from estimate":Engine.CATEGORIES[x.category]+(x.confirmed?" · checked":" · suggested"));add(c,text(status,13,needs?CORAL:MUTED,false));add(c,text(x.date+"  /  "+sourceLabel(x.source),11,MUTED,false));c.setOnClickListener(v->{if(x.refund)refund(x);else edit(x);});c.setContentDescription(x.merchant+", "+cash(x.amount)+", "+status+". Tap to edit.");
  }
  if(count==0){LinearLayout c=card(body,SURFACE);add(c,text(reviewOnly?"All caught up":"A fresh start",24,INK,true));gap(c,8);add(c,text(reviewOnly?"No items need review in this period.":"There are no transactions in this period. You can add a purchase or paste a transaction alert.",14,MUTED,false));}

 }
 EditText input(LinearLayout l,String label,String value,int type){add(l,text(label,13,MUTED,true));EditText e=new EditText(this);e.setText(value);e.setTextSize(16);e.setTextColor(INK);e.setInputType(type);e.setSingleLine(true);add(l,e);gap(l,10);return e;}
 LinearLayout dialogBody(){LinearLayout l=column();l.setPadding(dp(22),dp(10),dp(22),dp(10));return l;}
 ScrollView scrolled(LinearLayout l){ScrollView s=new ScrollView(this);s.addView(l);return s;}
 void dateInput(LinearLayout parent,LocalDate[] selected){
  add(parent,text("Date",13,MUTED,true));
  DateTimeFormatter format=DateTimeFormatter.ofPattern("d MMM yyyy",Locale.ENGLISH);
  Button date=button(selected[0].format(format),v->{});
  date.setContentDescription("Transaction date, "+selected[0].format(format)+". Tap to choose a date.");
  date.setOnClickListener(v->{
   android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
   if(keyboard!=null)keyboard.hideSoftInputFromWindow(v.getWindowToken(),0);
   LocalDate current=selected[0];
   DatePickerDialog picker=new DatePickerDialog(this,darkMode?R.style.AppDialog_Dark:R.style.AppDialog_Light,(view,year,month,day)->{
    selected[0]=LocalDate.of(year,month+1,day);date.setText(selected[0].format(format));
    date.setContentDescription("Transaction date, "+selected[0].format(format)+". Tap to choose a date.");
   },current.getYear(),current.getMonthValue()-1,current.getDayOfMonth());
   picker.show();
  });
  add(parent,date);gap(parent,10);
 }
 void edit(Engine.Tx old){
  boolean fresh=old==null;Engine.Tx x=fresh?new Engine.Tx():old;LinearLayout l=dialogBody();
  EditText merchant=input(l,"Merchant",fresh?"":x.merchant,1);EditText amount=input(l,"Amount in ₹",fresh?"":BigDecimalValue(x.amount),8194);LocalDate[] selectedDate={fresh?LocalDate.now(ZoneId.of("Asia/Kolkata")):LocalDate.parse(x.date)};dateInput(l,selectedDate);
  add(l,text("Reward category",13,MUTED,true));Spinner cat=new Spinner(this);cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Engine.CATEGORIES));cat.setSelection(x.category);add(l,cat);gap(l,8);add(l,text("Choose based on the bank’s merchant category, not the item purchased.",12,MUTED,false));
  CheckBox learn=new CheckBox(this);learn.setText("Remember this merchant’s category");learn.setChecked(x.category!=0&&store.rule(x.merchant)==x.category);add(l,learn);
  // An untouched unchecked box must not remove a different saved category for an older purchase.
  boolean[] forgetRule={false};
  learn.setOnCheckedChangeListener((view,checked)->forgetRule[0]=!checked);
  add(l,text("Applies to future alerts from this merchant. Save to apply changes.",12,MUTED,false));
  if(x.duplicate)add(l,text("Possible duplicate: saving confirms this is a separate purchase. Delete it if another alert already represents it.",13,CORAL,false));
  AlertDialog dlg=new AlertDialog.Builder(this).setTitle(fresh?"Add purchase":"Edit purchase").setView(scrolled(l)).setPositiveButton("Save",null).setNegativeButton("Cancel",null).setNeutralButton(fresh?"":"Delete",null).create();dlg.setOnShowListener(v->{
   dlg.getButton(-1).setOnClickListener(w->{try{long n=Engine.money(amount.getText().toString());LocalDate d=selectedDate[0];String m=merchant.getText().toString().trim();if(n<=0||n>1000000000L||m.isEmpty()||m.length()>90)throw new Exception();if(learn.isChecked()&&cat.getSelectedItemPosition()==0){toast("Choose a category before remembering this merchant.");return;}long linked=0;for(Engine.Tx r:store.all())if(r.active&&r.refund&&r.linked.equals(""+x.id))linked+=r.amount;if(n<linked){toast("Amount is smaller than linked refunds. Unlink the refund first.");return;}x.amount=n;x.merchant=m;x.date=d.toString();x.category=cat.getSelectedItemPosition();x.confirmed=x.category!=0;x.duplicate=false;if(fresh)x.received=System.currentTimeMillis();store.save(x);if(learn.isChecked())store.rule(m,x.category);else if(forgetRule[0])store.forgetRule(m);dlg.dismiss();draw();}catch(Exception e){toast("Enter a merchant and a positive amount, and select a date.");}});
   if(!fresh)dlg.getButton(-3).setOnClickListener(w->new AlertDialog.Builder(this).setMessage("Delete this purchase? Linked refund alerts will return to review.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(a,b)->{x.active=false;store.save(x);for(Engine.Tx r:store.all())if(r.refund&&r.linked.equals(""+x.id)){r.linked="";store.save(r);}dlg.dismiss();draw();}).show());else dlg.getButton(-3).setVisibility(View.GONE);
  });dlg.show();
 }
 String BigDecimalValue(long n){return java.math.BigDecimal.valueOf(n,2).toPlainString();}
 void refund(Engine.Tx refund){
  List<Engine.Tx> options=new ArrayList<>();List<String> labels=new ArrayList<>();List<Engine.Tx> rows=store.all();
  for(Engine.Tx x:rows){if(!x.active||x.refund)continue;long used=0;for(Engine.Tx r:rows)if(r.active&&r.refund&&r.id!=refund.id&&r.linked.equals(""+x.id))used+=r.amount;if(x.amount-used>=refund.amount){options.add(x);labels.add(x.date+" · "+x.merchant+" · "+cash(x.amount-used));}}
  LinearLayout l=dialogBody();add(l,text("Refund "+cash(refund.amount)+" from "+refund.merchant,17,INK,true));gap(l,8);add(l,text("Link to the original purchase to reduce its eligible spend. A later-period refund updates the original period in this tracker. the bank may recover cashback in a later statement. Check that statement separately.",14,MUTED,false));
  Spinner pick=new Spinner(this);pick.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels.isEmpty()?new String[]{"No matching purchase"}:labels.toArray(new String[0])));add(l,pick);
  new AlertDialog.Builder(this).setTitle("Review refund").setView(scrolled(l)).setPositiveButton("Link",(d,w)->{if(options.isEmpty()){toast("Add the original purchase first.");return;}refund.linked=""+options.get(pick.getSelectedItemPosition()).id;refund.confirmed=true;store.save(refund);draw();}).setNegativeButton("Close",null).setNeutralButton("More",(d,w)->new AlertDialog.Builder(this).setItems(new String[]{"Unlink refund","Delete this alert"},(a,i)->{if(i==0){refund.linked="";refund.confirmed=false;}else refund.active=false;store.save(refund);draw();}).show()).show();
 }
 void paste(){LinearLayout l=dialogBody();add(l,text("Paste one HSBC purchase or refund alert. OTPs are rejected. Alert text is parsed on this phone and is not saved.",14,MUTED,false));EditText e=new EditText(this);e.setHint("HSBC transaction alert");e.setMinLines(4);e.setGravity(Gravity.TOP);add(l,e);AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Import one alert").setView(scrolled(l)).setPositiveButton("Preview",null).setNegativeButton("Cancel",null).create();dlg.setOnShowListener(v->dlg.getButton(-1).setOnClickListener(w->{Engine.Parsed a=Engine.parse("HSBC",e.getText().toString(),p.getString("last4",""),System.currentTimeMillis());if(a==null){toast("Could not match a transaction. Check your card’s last four digits, or add it manually.");return;}Engine.Tx x=new Engine.Tx();x.amount=a.amount;x.merchant=a.merchant;x.date=a.date;x.refund=a.refund;x.received=System.currentTimeMillis();x.source="Pasted";store.categorize(x,p.getBoolean("myntra",false));new AlertDialog.Builder(this).setTitle("Confirm import").setMessage(x.merchant+"\n"+cash(x.amount)+"\n"+x.date+"\n"+(x.refund?"Refund · needs linking":Engine.CATEGORIES[x.category])+"\n\nCheck for an existing purchase before importing.").setNegativeButton("Cancel",null).setPositiveButton("Add",(q,z)->{store.save(x);dlg.dismiss();draw();}).show();}));dlg.show();}
 void merchantRules(){
  LinearLayout l=dialogBody();
  add(l,text("Your remembered and added merchant mappings. Tap a mapping to edit or delete it. These apply to new alerts and pasted purchases.",13,MUTED,false));gap(l,10);
  Map<String,Integer> mappings=store.rules();
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Merchant categories").setView(scrolled(l)).setPositiveButton("Add mapping",(d,w)->editMerchantRule(null,0)).setNegativeButton("Close",null).create();
  if(mappings.isEmpty())add(l,text("No saved mappings yet. Add one here or remember a category while editing a purchase.",15,MUTED,false));
  for(Map.Entry<String,Integer> rule:mappings.entrySet()){
   String merchant=rule.getKey();int category=rule.getValue();
   add(l,button(merchant+"\n"+Engine.CATEGORIES[category],v->{dialog.dismiss();editMerchantRule(merchant,category);}));
  }
  dialog.show();
 }
 void editMerchantRule(String previous,int initialCategory){
  LinearLayout l=dialogBody();EditText merchant=input(l,"Exact merchant name",previous==null?"":previous,1);
  add(l,text("Category",13,MUTED,true));Spinner category=new Spinner(this);category.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Engine.CATEGORIES));category.setSelection(initialCategory);add(l,category);gap(l,10);
  add(l,text("Use the merchant name from the alert. Capitalization and repeated spaces do not matter.",12,MUTED,false));
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(previous==null?"Add merchant mapping":"Edit merchant mapping").setView(scrolled(l)).setPositiveButton("Save",null).setNegativeButton("Cancel",(d,w)->merchantRules()).create();
  if(previous!=null)dialog.setButton(AlertDialog.BUTTON_NEUTRAL,"Delete",(d,w)->new AlertDialog.Builder(this).setMessage("Forget this merchant’s category? Existing transactions keep their categories.").setPositiveButton("Delete",(q,z)->{store.forgetRule(previous);draw();merchantRules();}).setNegativeButton("Cancel",(q,z)->merchantRules()).show());
  dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{
   String name=merchant.getText().toString().trim();int chosen=category.getSelectedItemPosition();
   if(name.isEmpty()||name.length()>90||Engine.key(name).equals("unknown merchant")||chosen==0){toast("Enter an exact merchant name and choose a category.");return;}
   if((previous==null||!Engine.key(previous).equals(Engine.key(name)))&&store.rule(name)>=0){toast("A mapping already exists for this merchant. Edit that mapping instead.");return;}
   store.saveRule(previous,name,chosen);dialog.dismiss();draw();merchantRules();
  }));dialog.show();
 }
 void chooseTheme(){
  String[] values={"light","dark"},labels={"Light","Dark"};
  int selected=Arrays.asList(values).indexOf(p.getString("theme","light"));
  new AlertDialog.Builder(this).setTitle("Theme").setSingleChoiceItems(labels,Math.max(0,selected),(dialog,which)->{
   dialog.dismiss();
   if(!values[which].equals(p.getString("theme","light"))){rememberScroll();p.edit().putString("theme",values[which]).apply();recreate();}
  }).setNegativeButton("Cancel",null).show();
 }
 void appearance(){
  LinearLayout appearance=card(body,SURFACE);add(appearance,text("Appearance",23,INK,true));gap(appearance,8);
  add(appearance,button((p.getString("theme","light").equals("dark")?"Dark":"Light"),v->chooseTheme()));

 }
 void settings(){
  LinearLayout c=card(body,SURFACE);add(c,text("Your card",23,INK,true));gap(c,10);EditText last=input(c,"Last four digits only",p.getString("last4",""),2);last.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4)});
  EditText day=input(c,"Statement closing day (1–31), or 0 for calendar month",""+day(),2);add(c,text("Choose the period in your card’s terms. Enter the statement closing day, not the payment due date. Dates near the boundary may post in the following period. you can edit them in Activity.",12,MUTED,false));gap(c,10);EditText limit=input(c,"Accelerated cashback cap in ₹",BigDecimalValue(cap()),8194);
  CheckBox promo=new CheckBox(this);promo.setText("Past Myntra offer · 31 Jul–31 Oct 2026");promo.setChecked(p.getBoolean("myntra",false));add(c,promo);add(c,text("For purchases in that date range only. Off by default. shares the accelerated cap.",12,MUTED,false));
  watchSetting(last,()->saveLastFour(last));watchSetting(day,()->saveDay(day));watchSetting(limit,()->saveCap(limit));
  promo.setOnCheckedChangeListener((view,checked)->p.edit().putBoolean("myntra",checked).apply());
  LinearLayout capture=card(body,SURFACE);add(capture,text("Automatic capture",23,INK,true));gap(capture,8);
  add(capture,text(CaptureMode.SMS?"Capture new HSBC transaction SMS. Existing messages are never read.":"Capture HSBC transaction notifications from one SMS messaging app. Existing messages are never read.",13,MUTED,false));
  if(!CaptureMode.SMS){
   String selected=SmsApps.selected(this);selectedPackages=new HashSet<>();if(!selected.isEmpty())selectedPackages.add(selected);
   TextView sources=text(sourceNames(),12,MUTED,false);gap(capture,8);add(capture,sources);add(capture,button("Choose SMS messaging app",v->chooseSources(sources)));
   add(capture,text("Android may hide alerts grouped with an OTP. Clear that conversation’s notifications before the next alert. Paste any missed purchase in Activity after checking for an existing entry.",13,CORAL,false));
  }
  Switch enabled=new Switch(this);enabled.setText(CaptureMode.SMS?"Capture new transaction SMS":"Capture new transaction notifications");enabled.setChecked(p.getBoolean("capture",false)&&access());enabled.setPadding(0,dp(10),0,dp(10));add(capture,enabled);
  enabled.setOnCheckedChangeListener((view,checked)->{
   if(!checked){p.edit().putBoolean("capture",false).apply();return;}
   flushSettings();boolean ready=saveLastFour(last);ready=saveDay(day)&&ready;ready=saveCap(limit)&&ready;
   if(!ready){enabled.setChecked(false);toast("Check the highlighted card settings.");return;}
   if(!CaptureMode.SMS&&(selectedPackages.size()!=1||!SmsApps.installed(this).containsKey(selectedPackages.iterator().next()))){enabled.setChecked(false);toast("Choose one installed SMS messaging app.");return;}
   if(!access()){
    enabled.setChecked(false);if(CaptureMode.SMS)requestSmsAccess(true);else requestNotificationAccess(true);return;
   }
   startCapture();
  });
  if(CaptureMode.SMS){
   add(capture,button(access()?"SMS permission enabled · manage":"Enable SMS permission",v->{flushSettings();if(access())openAppSettings();else requestSmsAccess(false);}));
  }else{
   add(capture,button(access()?"Notification access enabled · manage":"Enable Android notification access",v->{flushSettings();requestNotificationAccess(false);}));
  }
  LinearLayout merchants=card(body,SURFACE);add(merchants,text("Merchant categories",23,INK,true));gap(merchants,8);
  add(merchants,button("Manage merchant categories",v->merchantRules()));
  appearance();
  LinearLayout data=card(body,SURFACE);add(data,text("Your data",23,INK,true));gap(data,8);add(data,text("Stored in this app’s private storage. Uninstalling removes it. Export a backup before reinstalling or changing phones. Backups contain financial data. store them privately.",13,MUTED,false));add(data,button("Export backup (JSON)",v->export()));add(data,button("Restore backup",v->restore()));add(data,button("Erase local data",v->new AlertDialog.Builder(this).setTitle("Erase this ledger?").setMessage("This removes every transaction, merchant rule and setting on this phone. Export a backup first if you need it.").setNegativeButton("Cancel",null).setPositiveButton("Erase",(a,b)->{store.wipe();p.edit().clear().apply();scrollPositions.clear();toast("Local data erased");recreate();}).show()));
  LinearLayout rules=card(body,SURFACE);add(rules,text("Reward rules & limits",21,INK,true));gap(rules,8);add(rules,text("Rules checked 3 Nov 2026: 10% up to ₹1,200 per month on eligible dining, grocery, shopping and utilities. most other eligible spends earn 1.5%. Amazon, Flipkart and Myntra shopping earn 1.5%. The Myntra 10% offer ended 31 Oct 2026. the option above applies only to historical purchases. Fuel and several other merchant codes are excluded. International transactions should be marked excluded.\n\nUnknown categories are left out of estimates. Suggested categories are provisional. Accelerated spend beyond the cap earns no additional cashback in this estimate. Refunds require linking. The estimate does not predict exact bank rounding, delayed posting, cross-period clawbacks, fee-waiver eligibility or welcome offers.\n\nChanging period or cap applies to all displayed history. Check your statement and amend categories or dates when necessary.",13,MUTED,false));add(rules,button("Open HSBC service guide",v->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.hsbc.bank.in/content/dam/hsbc/in/documents/credit-cards/live-plus-credit-card-services-guide.pdf")))));gap(body,8);add(body,text("Live+ Ledger "+appVersion()+"\nIndependent personal tracker. No affiliation with HSBC.",12,MUTED,false));
 }
 String sourceNames(){
  if(selectedPackages.isEmpty())return "No SMS messaging app selected";
  String pkg=selectedPackages.iterator().next();return "Selected: "+SmsApps.installed(this).getOrDefault(pkg,pkg);
 }
 void watchSetting(EditText field,Runnable save){
  Runnable commit=()->{settingWrites.remove(field);save.run();};
  field.addTextChangedListener(new android.text.TextWatcher(){
   public void beforeTextChanged(CharSequence text,int start,int count,int after){}
   public void onTextChanged(CharSequence text,int start,int before,int count){}
   public void afterTextChanged(android.text.Editable value){field.setError(null);settingsHandler.removeCallbacks(commit);settingWrites.put(field,commit);settingsHandler.postDelayed(commit,650);}
  });
  field.setOnFocusChangeListener((view,focused)->{if(!focused&&settingWrites.containsKey(field)){settingsHandler.removeCallbacks(commit);commit.run();}});
 }
 void flushSettings(){
  List<Runnable> pending=new ArrayList<>(settingWrites.values());settingWrites.clear();
  for(Runnable save:pending){settingsHandler.removeCallbacks(save);save.run();}
 }
 boolean saveLastFour(EditText field){
  try{
   String previous=p.getString("last4","");String four=SettingsValues.last4(field.getText().toString(),previous,!previous.isEmpty()&&!previous.equals(field.getText().toString())&&!store.all().isEmpty());
   if(!four.equals(previous)){p.edit().putString("last4",four).putBoolean("periodSet",true).apply();if(p.getBoolean("capture",false)&&access())startCapture();}
   field.setError(null);return true;
  }catch(IllegalArgumentException error){field.setError(error.getMessage());return false;}
 }
 boolean saveDay(EditText field){
  try{int day=SettingsValues.day(field.getText().toString());p.edit().putInt("day",day).putBoolean("periodSet",true).apply();field.setError(null);return true;}catch(IllegalArgumentException error){field.setError(error.getMessage());return false;}
 }
 boolean saveCap(EditText field){
  try{long amount=SettingsValues.cap(field.getText().toString());p.edit().putLong("cap",amount).apply();field.setError(null);return true;}catch(IllegalArgumentException error){field.setError(error.getMessage());return false;}
 }
 boolean savedCardReady(){return p.getString("last4","").matches("[0-9]{4}")&&p.getInt("day",0)>=0&&p.getInt("day",0)<=31&&p.getLong("cap",120000)>0&&p.getLong("cap",120000)<=10000000L;}
 void startCapture(){p.edit().putBoolean("capture",true).putBoolean("captureError",false).putString("captureEdition",CaptureMode.ID).putLong("started",System.currentTimeMillis()).apply();CaptureMode.enabled();}
 void chooseSources(TextView display){
  Map<String,String> apps=SmsApps.installed(this);
  if(apps.isEmpty()){toast("No SMS messaging app found. Install or enable one in Android Settings.");return;}
  List<String> packages=new ArrayList<>(apps.keySet());packages.sort((a,b)->apps.get(a).compareToIgnoreCase(apps.get(b)));
  String[] labels=new String[packages.size()];int selected=-1;for(int i=0;i<labels.length;i++){labels[i]=apps.get(packages.get(i));if(selectedPackages.contains(packages.get(i)))selected=i;}
  new AlertDialog.Builder(this).setTitle("Choose one SMS messaging app").setSingleChoiceItems(labels,selected,(dialog,index)->{
   Set<String> next=new HashSet<>(Collections.singleton(packages.get(index)));boolean changed=!p.getStringSet("packages",Collections.emptySet()).equals(next);
   selectedPackages=next;p.edit().putStringSet("packages",new HashSet<>(next)).apply();display.setText(sourceNames());if(changed&&p.getBoolean("capture",false)&&access())startCapture();dialog.dismiss();
  }).setNegativeButton("Close",null).show();
 }
 void openAppSettings(){try{startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){toast("Open Android Settings → Apps → Live+ Ledger.");}}
 void requestNotificationAccess(boolean enableAfterGrant){
  pendingNotificationEnable=enableAfterGrant;
  new AlertDialog.Builder(this).setTitle("Allow notification access").setMessage("Live+ Ledger checks transaction alerts from your selected SMS app. Android grants broad notification access. Unrelated alerts and OTPs are ignored locally. You can revoke access at any time.\n\nFor a sideloaded app, Android may require App info → ⋮ → Allow restricted settings first.").setNegativeButton("Cancel",(dialog,which)->pendingNotificationEnable=false).setOnCancelListener(dialog->pendingNotificationEnable=false).setPositiveButton("Open settings",(dialog,which)->{try{startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}catch(Exception ignored){pendingNotificationEnable=false;toast("Open Android Settings → Notification access.");}}).show();
 }
 void requestSmsAccess(boolean enableAfterGrant){
  if(!CaptureMode.SMS)return;
  pendingSmsEnable=enableAfterGrant;
  new AlertDialog.Builder(this).setTitle("Allow new SMS capture?").setMessage("Android grants access to new incoming SMS from all senders. Live+ Ledger checks for HSBC and your card suffix, ignores OTPs and unrelated messages, and stores transaction details locally.\n\nExisting messages are never read. The app cannot send SMS and has no internet permission. You can revoke SMS access in Android Settings.").setNegativeButton("Not now",(dialog,which)->pendingSmsEnable=false).setOnCancelListener(dialog->pendingSmsEnable=false).setPositiveButton("Continue",(dialog,which)->requestPermissions(new String[]{"android.permission.RECEIVE_SMS"},73)).show();
 }
 @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
  super.onRequestPermissionsResult(request,permissions,results);if(request!=73||!CaptureMode.SMS)return;
  boolean enable=pendingSmsEnable;pendingSmsEnable=false;
  if(access()){
   if(enable&&savedCardReady())startCapture();
   toast(enable?"New SMS capture enabled":"SMS permission granted. Turn on capture to begin.");draw();
  }else{
   p.edit().putBoolean("capture",false).apply();draw();
   new AlertDialog.Builder(this).setTitle("SMS access was not granted").setMessage("Capture stays off. Android, the installer or device policy may restrict permission. Review App info → Permissions, or use Paste alert or Add purchase.").setNegativeButton("Close",null).setPositiveButton("App settings",(dialog,which)->openAppSettings()).show();
  }
 }
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 void export(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"liveplus-backup-"+LocalDate.now()+".json");startActivityForResult(i,41);}
 void restore(){new AlertDialog.Builder(this).setTitle("Restore a backup").setMessage("Restore is available into an empty ledger. It restores transactions and settings. Capture stays off until you enable it again.").setNegativeButton("Cancel",null).setPositiveButton("Choose file",(d,w)->{if(!store.all().isEmpty()){toast("Export and erase existing data first.");return;}startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),42);}).show();}
 @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{
  if(req==41){JSONObject o=new JSONObject();o.put("format","liveplus-ledger-v1");o.put("last4",p.getString("last4",""));o.put("day",day());o.put("cap",cap());o.put("periodSet",p.getBoolean("periodSet",false));o.put("myntra",p.getBoolean("myntra",false));o.put("transactions",store.exportRows());try(OutputStream s=getContentResolver().openOutputStream(data.getData())){s.write(o.toString(2).getBytes(StandardCharsets.UTF_8));}toast("Backup exported");}
  else if(req==42){if(!store.all().isEmpty())throw new Exception("Ledger must be empty");ByteArrayOutputStream buffer=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(data.getData())){byte[] chunk=new byte[8192];int n;while((n=in.read(chunk))!=-1){buffer.write(chunk,0,n);if(buffer.size()>10_000_000)throw new Exception("File too large");}}JSONObject o=new JSONObject(buffer.toString("UTF-8"));if(!o.getString("format").equals("liveplus-ledger-v1"))throw new Exception("Wrong format");JSONArray a=o.getJSONArray("transactions");if(a.length()>20000)throw new Exception("Too many rows");String four=o.getString("last4");int close=o.getInt("day");long max=o.getLong("cap");if(!four.matches("[0-9]{4}")||close<0||close>31||max<=0||max>10000000L)throw new Exception("Invalid settings");
   List<Engine.Tx> rows=new ArrayList<>();Set<Long> ids=new HashSet<>();Set<String> events=new HashSet<>();for(int i=0;i<a.length();i++){JSONObject v=a.getJSONObject(i);Engine.Tx x=new Engine.Tx();x.id=v.getLong("id");x.amount=v.getLong("amountPaise");x.merchant=v.getString("merchant");x.date=LocalDate.parse(v.getString("date")).toString();x.category=v.getInt("category");x.confirmed=v.getBoolean("confirmed");x.refund=v.getBoolean("refund");x.duplicate=v.getBoolean("duplicate");x.active=v.getBoolean("active");x.received=v.getLong("received");x.source=v.getString("source");x.event=v.optString("event","");x.linked=v.optString("linked","");if(x.id<=0||!ids.add(x.id)||x.amount<=0||x.amount>1000000000L||x.category<0||x.category>=Engine.CATEGORIES.length||x.merchant.length()>90||x.source.length()>100||(!x.event.isEmpty()&&!events.add(x.event)))throw new Exception("Invalid transaction");rows.add(x);}
   for(Engine.Tx x:rows)if(!x.linked.isEmpty()){Engine.Tx original=null;for(Engine.Tx q:rows)if(x.linked.equals(""+q.id)&&q.active&&!q.refund)original=q;if(!x.refund||original==null)throw new Exception("Invalid refund link");long refunds=0;for(Engine.Tx r:rows)if(r.active&&r.refund&&r.linked.equals(x.linked))refunds+=r.amount;if(refunds>original.amount)throw new Exception("Refund exceeds purchase");}
   android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{for(Engine.Tx x:rows){ContentValues values=new ContentValues();values.put("id",x.id);values.put("amount",x.amount);values.put("merchant",x.merchant);values.put("date",x.date);values.put("category",x.category);values.put("confirmed",x.confirmed?1:0);values.put("refund",x.refund?1:0);values.put("duplicate",x.duplicate?1:0);values.put("active",x.active?1:0);values.put("received",x.received);values.put("source",x.source);values.put("linked",x.linked);if(!x.event.isEmpty())values.put("event",x.event);db.insertOrThrow("tx",null,values);}db.setTransactionSuccessful();}finally{db.endTransaction();}
   p.edit().putString("last4",four).putInt("day",close).putLong("cap",max).putBoolean("periodSet",o.optBoolean("periodSet",true)).putBoolean("myntra",o.optBoolean("myntra",false)).putBoolean("capture",false).apply();toast("Backup restored. Re-enable capture in Settings.");draw();
  }
 }catch(Exception e){toast("Could not complete operation: "+e.getMessage());}}
 String appVersion(){
  try{PackageInfo info=getPackageManager().getPackageInfo(getPackageName(),0);long code=Build.VERSION.SDK_INT>=28?info.getLongVersionCode():info.versionCode;return info.versionName+" · build "+code;}catch(PackageManager.NameNotFoundException ignored){return "unknown";}
 }
}
