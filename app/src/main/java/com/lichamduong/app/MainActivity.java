package com.lichamduong.app;
// Màn hình chính: lịch tháng + chi tiết âm dương.
import android.graphics.Color; import android.graphics.Typeface; import android.os.Bundle; import android.view.Gravity; import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
public class MainActivity extends AppCompatActivity {
 Calendar viewCal = Calendar.getInstance(); Calendar selected = Calendar.getInstance();
 GridLayout gridCal, gridWeek; TextView tvSolarBig, tvMonthYear, tvLunarBig, tvCalTitle, tvDetail, tvQuote, chipTietKhi, chipCanChi, chipHoangDao;
 String[] week = {"T2","T3","T4","T5","T6","T7","CN"};
 String[] quotes = {"Mồng một sớm mai, mồng hai đầu tháng — chúc bình an.","Ngày đẹp gieo việc lành, ắt gặt quả ngọt.","Thuận thiên giả tồn — xem ngày tốt để vững tâm.","Tâm an thì ngày nào cũng là ngày tốt."};
 @Override protected void onCreate(Bundle s){ super.onCreate(s); setContentView(R.layout.activity_main);
  gridCal=findViewById(R.id.gridCal); gridWeek=findViewById(R.id.gridWeek);
  tvSolarBig=findViewById(R.id.tvSolarBig); tvMonthYear=findViewById(R.id.tvMonthYear); tvLunarBig=findViewById(R.id.tvLunarBig);
  tvCalTitle=findViewById(R.id.tvCalTitle); tvDetail=findViewById(R.id.tvDetail); tvQuote=findViewById(R.id.tvQuote);
  chipTietKhi=findViewById(R.id.chipTietKhi); chipCanChi=findViewById(R.id.chipCanChi); chipHoangDao=findViewById(R.id.chipHoangDao);
  findViewById(R.id.btnPrev).setOnClickListener(v->{viewCal.add(Calendar.MONTH,-1); render();});
  findViewById(R.id.btnNext).setOnClickListener(v->{viewCal.add(Calendar.MONTH,1); render();});
  findViewById(R.id.btnToday).setOnClickListener(v->{viewCal=Calendar.getInstance(); selected=Calendar.getInstance(); render();});
  render();
 }
 void render(){
  gridWeek.removeAllViews(); gridCal.removeAllViews();
  for(String d: week){ TextView t=new TextView(this); t.setText(d); t.setGravity(Gravity.CENTER); t.setTypeface(null,Typeface.BOLD); t.setTextColor(d.equals("CN")?Color.parseColor("#E63946"):Color.parseColor("#7A86A8")); GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=0; p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); t.setLayoutParams(p); gridWeek.addView(t); }
  Calendar first=(Calendar)viewCal.clone(); first.set(Calendar.DAY_OF_MONTH,1);
  int offset=(first.get(Calendar.DAY_OF_WEEK)+5)%7; int max=first.getActualMaximum(Calendar.DAY_OF_MONTH);
  Calendar today=Calendar.getInstance();
  for(int i=0;i<offset;i++) gridCal.addView(emptyCell());
  for(int d=1;d<=max;d++){
   Calendar c=(Calendar)viewCal.clone(); c.set(Calendar.DAY_OF_MONTH,d);
   LunarCalendar.LunarDate ln=LunarCalendar.solarToLunar(c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR));
   boolean isToday=same(c,today); boolean isSel=same(c,selected);
   LinearLayout cell=new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(4,10,4,10);
   GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=0; p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); p.setMargins(3,3,3,3); cell.setLayoutParams(p);
   if(isSel||isToday) cell.setBackground(getDrawable(R.drawable.bg_today)); else { cell.setBackground(getDrawable(R.drawable.bg_card)); cell.setElevation(2f); }
   TextView tS=new TextView(this); tS.setText(String.valueOf(d)); tS.setGravity(Gravity.CENTER); tS.setTypeface(null,Typeface.BOLD); tS.setTextSize(16);
   boolean sunday=c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY;
   tS.setTextColor((isSel||isToday)?Color.WHITE:(sunday?Color.parseColor("#E63946"):Color.parseColor("#0F1B3D")));
   TextView tL=new TextView(this); tL.setText((ln.leap?"N":"")+ln.day+(ln.day==1?"/"+ln.month:"")); tL.setGravity(Gravity.CENTER); tL.setTextSize(11);
   tL.setTextColor((isSel||isToday)?Color.WHITE:Color.parseColor("#7A86A8"));
   cell.addView(tS); cell.addView(tL);
   final Calendar cc=(Calendar)c.clone();
   cell.setOnClickListener(v->{selected=cc; render();});
   gridCal.addView(cell);
  }
  updateHeader();
 }
 LinearLayout emptyCell(){ LinearLayout l=new LinearLayout(this); GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=0; p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); l.setLayoutParams(p); return l; }
 boolean same(Calendar a,Calendar b){ return a.get(Calendar.YEAR)==b.get(Calendar.YEAR)&&a.get(Calendar.MONTH)==b.get(Calendar.MONTH)&&a.get(Calendar.DAY_OF_MONTH)==b.get(Calendar.DAY_OF_MONTH); }
 void updateHeader(){
  tvSolarBig.setText(String.valueOf(selected.get(Calendar.DAY_OF_MONTH)));
  String[] thu={"","Chủ Nhật","Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy"};
  tvMonthYear.setText(thu[selected.get(Calendar.DAY_OF_WEEK)]+" • Tháng "+(selected.get(Calendar.MONTH)+1)+"/"+selected.get(Calendar.YEAR));
  LunarCalendar.LunarDate ln=LunarCalendar.solarToLunar(selected.get(Calendar.DAY_OF_MONTH),selected.get(Calendar.MONTH)+1,selected.get(Calendar.YEAR));
  String canChi=LunarCalendar.canChiYear(ln.year)+" • "+LunarCalendar.canChiDay(selected);
  tvLunarBig.setText(ln.day+"/"+(ln.leap?"nhuận ":"")+ln.month+" • "+LunarCalendar.canChiYear(ln.year));
  tvCalTitle.setText("Tháng "+(viewCal.get(Calendar.MONTH)+1)+" • "+viewCal.get(Calendar.YEAR));
  String tietKhi=LunarCalendar.tietKhi(selected.get(Calendar.DAY_OF_MONTH),selected.get(Calendar.MONTH)+1);
  chipTietKhi.setText(tietKhi); chipCanChi.setText(canChi.split(" • ")[0]); chipHoangDao.setText(LunarCalendar.gioHoangDao(LunarCalendar.canChiDay(selected).split(" ")[1]).split(",")[0]+" h");
  String detail="Dương lịch: "+selected.get(Calendar.DAY_OF_MONTH)+"/"+(selected.get(Calendar.MONTH)+1)+"/"+selected.get(Calendar.YEAR)+"\nÂm lịch: "+ln.day+"/"+ln.month+(ln.leap?" (nhuận)":"")+"/"+ln.year+"\nNăm "+LunarCalendar.canChiYear(ln.year)+" • Ngày "+LunarCalendar.canChiDay(selected)+"\nTiết khí: "+tietKhi+"\nGiờ hoàng đạo: "+LunarCalendar.gioHoangDao(LunarCalendar.canChiDay(selected).split(" ")[1]);
  tvDetail.setText(detail);
  tvQuote.setText("\""+quotes[selected.get(Calendar.DAY_OF_MONTH)%quotes.length]+"\"");
 }
}
