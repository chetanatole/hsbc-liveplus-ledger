package in.local.liveplus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

/** Notification extraction and replay tracking without Android dependencies. Raw text is transient. */
public final class NotificationCapture {
 private static final Pattern OTP_CONTEXT=Pattern.compile("(?i)\\b(?:otp|one.time.password|verification.code)\\b");
 private static final Pattern COMPLETED=Pattern.compile("(?i)\\b(spent|used|debited|charged|refund(?:ed)?|reversal|reversed)\\b|\\b(?:transaction|txn|purchase)\\b.{0,30}\\b(?:successful|completed)\\b");
 private static final Pattern AUTHORIZATION=Pattern.compile("(?i)\\b(will|would|authori[sz](?:e|ation)|password|validity|valid)\\b");
 private static final Pattern MESSAGE_BREAK=Pattern.compile("\\r?\\n(?=\\s*(?:(?:[A-Za-z0-9 +()._-]{1,48}:\\s*)?(?:your\\s+)?HSBC\\b|(?:OTP|one.time.password|verification.code)\\b|(?:INR|Rs\\.?|₹)\\s*[0-9][^\\r\\n]*\\b(?:spent|used|debited|charged)\\b))",Pattern.CASE_INSENSITIVE);
 public static final class Message {
  public final String text,sender; public final long time;
  public Message(String text,String sender,long time){this.text=text==null?"":text;this.sender=sender==null?"":sender;this.time=time;}
 }
 public static final class Envelope {
  public String source="",key="",title="",bigText="",text="";
  public long posted; public boolean summary,redacted;
  public final List<Message> messages=new ArrayList<>();
  public final List<String> lines=new ArrayList<>();
 }
 private static final class Seen {
  final long time; boolean processed;
  Seen(long time,boolean processed){this.time=time;this.processed=processed;}
 }
 public static final class State {
  public long started,posted;
  private final Map<String,Seen> entries=new LinkedHashMap<>();
  public boolean empty(){return entries.isEmpty();}
  public String encode(){
   StringBuilder out=new StringBuilder().append(started).append('|').append(posted);
   for(Map.Entry<String,Seen> entry:entries.entrySet())out.append('\n').append(entry.getKey()).append('|').append(entry.getValue().time).append('|').append(entry.getValue().processed?'1':'0');
   return out.toString();
  }
  public static State decode(String value){
   State state=new State();if(value==null||value.length()>30000)return state;
   try{
    String[] rows=value.split("\\n");String[] header=rows[0].split("\\|");state.started=Long.parseLong(header[0]);state.posted=Long.parseLong(header[1]);
    for(int i=1;i<rows.length&&i<=100;i++){
     String[] row=rows[i].split("\\|");if(row.length!=3||!row[0].matches("[a-f0-9]{64}:[0-9]+"))return new State();
     state.entries.put(row[0],new Seen(Long.parseLong(row[1]),row[2].equals("1")));
    }
   }catch(RuntimeException ignored){return new State();}return state;
  }
 }
 public static final class Candidate {
  public final Engine.Parsed transaction; public final String event;
  public final long received; public final boolean possibleDuplicate; private final Seen seen;
  Candidate(Engine.Parsed transaction,String event,Seen seen,boolean possibleDuplicate){this.transaction=transaction;this.event=event;this.received=seen.time;this.seen=seen;this.possibleDuplicate=possibleDuplicate;}
  public void processed(){seen.processed=true;}
 }
 public static final class Batch {
  public final State state=new State();public final List<Candidate> candidates=new ArrayList<>();
  public void baseline(){for(Seen seen:state.entries.values())seen.processed=true;}
 }
 private static final class Entry {
  final Engine.Parsed transaction;final String body;final long time;final boolean timed;
  Entry(Engine.Parsed transaction,String body,long time,boolean timed){this.transaction=transaction;this.body=body;this.time=time;this.timed=timed;}
 }
 private static String normalize(String value){return value.replace('\u00a0',' ').replaceAll("\\s+"," ").trim();}
 public static String digest(String value){
  try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}catch(Exception e){throw new IllegalStateException(e);}
 }
 public static String cacheKey(Envelope envelope){return "capture_seen_"+digest(envelope.source+"|"+envelope.key);}
 private static void add(List<Entry> out,String context,String body,long time,boolean timed,String last4,boolean split){
  if(body==null||body.isEmpty()||body.length()>24000)return;
  // OTP words in a conversation title must not poison an independent purchase body.
  // The body is never scrubbed: OTPs, failed purchases and repayments still use the strict parser.
  String metadata=OTP_CONTEXT.matcher(context==null?"":context).replaceAll(" ");
  body=body.replace('\u00a0',' ').replace("\u200b","");
  String[] entries=split?MESSAGE_BREAK.split(body):new String[]{body};
  for(String entry:entries){
   if(out.size()>=100)break;
   if(split&&entries.length>1&&OTP_CONTEXT.matcher(body).find()&&(!COMPLETED.matcher(entry).find()||AUTHORIZATION.matcher(entry).find()))continue;
   Engine.Parsed parsed=Engine.parse(metadata,entry,last4,time);
   if(parsed!=null)out.add(new Entry(parsed,normalize(entry),time,timed&&entries.length==1));
  }
 }
 public static Batch collect(Envelope envelope,String last4,long started,State previous){
  Batch batch=new Batch();batch.state.started=started;batch.state.posted=envelope.posted;
  if(envelope.posted<started)return batch;
  if(envelope.redacted){retainPrevious(batch.state,previous);return batch;}
  List<Entry> entries=new ArrayList<>();
  if(!envelope.messages.isEmpty()){
   for(Message message:envelope.messages){
    long time=message.time>0?message.time:envelope.posted;if(time<started)continue;
    add(entries,envelope.title+" "+message.sender,message.text,time,message.time>0,last4,true);
   }
  }else if(!envelope.lines.isEmpty()){
   for(String line:envelope.lines)add(entries,envelope.title,line,envelope.posted,false,last4,true);
  }else{
   add(entries,envelope.title,envelope.bigText,envelope.posted,false,last4,true);
   if(entries.isEmpty())add(entries,envelope.title,envelope.text,envelope.posted,false,last4,true);
  }
  Map<String,Integer> occurrences=new HashMap<>();
  for(Entry entry:entries){
   String fingerprint=digest(entry.body+(entry.timed?"|"+entry.time:""));
   int occurrence=occurrences.containsKey(fingerprint)?occurrences.get(fingerprint)+1:1;occurrences.put(fingerprint,occurrence);
   String identity=fingerprint+":"+occurrence;
   Seen old=previous!=null&&previous.started==started?previous.entries.get(identity):null;
   // Flat notifications expose no per-message identity. Preserve a new identical post for review.
   boolean ambiguous=!entry.timed&&old!=null&&old.processed&&previous.posted!=envelope.posted&&!envelope.summary&&envelope.messages.isEmpty()&&envelope.lines.isEmpty()&&entries.size()==1&&!OTP_CONTEXT.matcher(envelope.bigText+" "+envelope.text).find();
   Seen seen=old==null||ambiguous?new Seen(entry.time,false):new Seen(old.time,old.processed);
   batch.state.entries.put(identity,seen);
   if(seen.processed)continue;
   // Timestamped SMS entries deduplicate across group/child notification keys.
   String event=entry.timed?digest("alert-v2|"+envelope.source+"|"+fingerprint+"|"+occurrence):digest("alert-v2|"+envelope.source+"|"+envelope.key+"|"+identity+"|"+seen.time);
   batch.candidates.add(new Candidate(entry.transaction,event,seen,ambiguous));
  }
  retainPrevious(batch.state,previous);
  return batch;
 }
 // Empty/redacted updates must not erase replay protection for an existing conversation.
 // Keep at most 100 identities, preferring entries still visible in the current update.
 private static void retainPrevious(State current,State previous){
  if(previous==null||previous.started!=current.started)return;
  for(Map.Entry<String,Seen> old:previous.entries.entrySet()){
   if(current.entries.size()>=100)break;
   if(!current.entries.containsKey(old.getKey()))current.entries.put(old.getKey(),new Seen(old.getValue().time,old.getValue().processed));
  }
 }
}
