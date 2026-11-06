package in.local.liveplus;

import java.util.*;

/** Parses one newly delivered SMS, including multipart PDUs. Never queries an inbox. */
public final class SmsCapture {
 public static final class Part {
  public final String sender,body;public final long sent;
  public Part(String sender,String body,long sent){this.sender=sender==null?"":sender;this.body=body==null?"":body;this.sent=sent;}
 }
 public static final class Candidate {
  public final Engine.Parsed transaction;public final String event;public final long received;
  Candidate(Engine.Parsed transaction,String event,long received){this.transaction=transaction;this.event=event;this.received=received;}
 }
 public static Candidate collect(List<Part> parts,String last4,long started,long received,int subscription){
  if(parts==null||parts.isEmpty()||parts.size()>64||received<started)return null;
  Part first=parts.get(0);if(first==null||first.sent<=0||first.sent>received+300000)return null;
  StringBuilder body=new StringBuilder();
  for(Part part:parts){
   if(part==null||!part.sender.equals(first.sender)||part.sent<=0||part.sent>received+300000)return null;
   body.append(part.body);if(body.length()>6000)return null;
  }
  String text=body.toString().replace('\u00a0',' ').replace("\u200b","");
  Engine.Parsed parsed=Engine.parse(first.sender,text,last4,first.sent);if(parsed==null)return null;
  String fingerprint=NotificationCapture.digest("sms-v1|"+subscription+"|"+first.sender+"|"+first.sent+"|"+text.replaceAll("\\s+"," ").trim());
  return new Candidate(parsed,fingerprint,received);
 }
}