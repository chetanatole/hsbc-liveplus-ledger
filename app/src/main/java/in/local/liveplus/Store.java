package in.local.liveplus;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;
import org.json.*;

public final class Store extends SQLiteOpenHelper {
 public Store(Context c){super(c,"ledger.db",null,1);}
 public static SharedPreferences prefs(Context c){return c.getSharedPreferences("settings",Context.MODE_PRIVATE);}
 @Override public void onCreate(SQLiteDatabase db){
  db.execSQL("CREATE TABLE tx (id INTEGER PRIMARY KEY AUTOINCREMENT, amount INTEGER NOT NULL, merchant TEXT NOT NULL, date TEXT NOT NULL, category INTEGER NOT NULL, confirmed INTEGER NOT NULL, refund INTEGER NOT NULL, duplicate INTEGER NOT NULL, active INTEGER NOT NULL, received INTEGER NOT NULL, source TEXT NOT NULL, event TEXT UNIQUE, linked TEXT NOT NULL)");
  db.execSQL("CREATE TABLE rules (merchant TEXT PRIMARY KEY, category INTEGER NOT NULL)");
 }
 @Override public void onUpgrade(SQLiteDatabase d,int a,int b){}
 public synchronized List<Engine.Tx> all(){
  List<Engine.Tx> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM tx ORDER BY date DESC, id DESC",null)){
   while(c.moveToNext()){Engine.Tx x=new Engine.Tx();x.id=c.getLong(0);x.amount=c.getLong(1);x.merchant=c.getString(2);x.date=c.getString(3);x.category=c.getInt(4);x.confirmed=c.getInt(5)==1;x.refund=c.getInt(6)==1;x.duplicate=c.getInt(7)==1;x.active=c.getInt(8)==1;x.received=c.getLong(9);x.source=c.getString(10);x.event=c.isNull(11)?"":c.getString(11);x.linked=c.getString(12);out.add(x);}
  }return out;
 }
 public synchronized long save(Engine.Tx x){
  ContentValues v=new ContentValues();v.put("amount",x.amount);v.put("merchant",x.merchant);v.put("date",x.date);v.put("category",x.category);v.put("confirmed",x.confirmed?1:0);v.put("refund",x.refund?1:0);v.put("duplicate",x.duplicate?1:0);v.put("active",x.active?1:0);v.put("received",x.received);v.put("source",x.source);v.put("linked",x.linked);if(x.event.isEmpty())v.putNull("event");else v.put("event",x.event);
  if(x.id>0){getWritableDatabase().update("tx",v,"id=?",new String[]{""+x.id});return x.id;}
  long id=getWritableDatabase().insertWithOnConflict("tx",null,v,SQLiteDatabase.CONFLICT_IGNORE);if(id>0)x.id=id;return id;
 }
 public void rule(String merchant,int category){ContentValues v=new ContentValues();v.put("merchant",Engine.key(merchant));v.put("category",category);getWritableDatabase().insertWithOnConflict("rules",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
 public void forgetRule(String merchant){getWritableDatabase().delete("rules","merchant=?",new String[]{Engine.key(merchant)});}
 public int rule(String merchant){try(Cursor c=getReadableDatabase().rawQuery("SELECT category FROM rules WHERE merchant=?",new String[]{Engine.key(merchant)})){return c.moveToFirst()?c.getInt(0):-1;}}
 public JSONArray exportRows() throws JSONException {
  JSONArray a=new JSONArray();for(Engine.Tx x:all()){JSONObject o=new JSONObject();o.put("id",x.id);o.put("amountPaise",x.amount);o.put("merchant",x.merchant);o.put("date",x.date);o.put("category",x.category);o.put("confirmed",x.confirmed);o.put("refund",x.refund);o.put("duplicate",x.duplicate);o.put("active",x.active);o.put("received",x.received);o.put("source",x.source);o.put("event",x.event);o.put("linked",x.linked);a.put(o);}return a;
 }
 public void wipe(){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{db.delete("tx",null,null);db.delete("rules",null,null);db.setTransactionSuccessful();}finally{db.endTransaction();}}
}
