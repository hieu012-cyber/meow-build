package com.lichamduong.app;
// Màn hình chính bản premium: lịch tháng đỏ vàng Tết.
import android.graphics.Color; import android.graphics.Typeface; import android.os.Bundle; import android.view.Gravity; import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
public class MainActivity extends AppCompatActivity {
 GridLayout gridCal, gridWeek;
 TextView tvMonthYear, tvLunarMonth, tvCanChiMonth, tvTodaySolar, tvTodayDow, tvTodayLunar, tvTodayCanChi, tvSelected, tvDetail, tvQuote;
 Calendar current = Calendar.getInstance();
 Calendar selected = Calendar.getInstance();
 String[] quotes = {"Tâm an vạn sự an.","Ngày tốt bắt đầu từ lòng tốt.","Thuận thiên giả tồn.","Mỗi ngày mới là một khởi đầu mới.","Đức năng thắng số.","Bình an là phúc lớn nhất."};
 @Override protected void onCreate(Bundle b){
  super.onCreate(b); setContentView(R.layout.activity_main);
  gridCal=findViewById(R.id.gridCal); gridWeek=findViewById(R.id.gridWeek);
  tvMonthYear=findViewById(R.id.tvMonthYear); tvLunarMonth=findViewById(R.id.tvLunarMonth);
  tvCanChiMonth=findViewById(R.id.tvCanChiMonth); tvTodaySolar=findViewById(R.id.tvTodaySolar);
  tvTodayDow=findViewById(R.id.tvTodayDow); tvTodayLunar=findViewById(R.id.tvTodayLunar);
  tvTodayCanChi=findViewById(R.id.tvTodayCanChi); tvSelected=findViewById(R.id.tvSelected);
  tvDetail=findViewById(R.id.tvDetail); tvQuote=findViewById(R.id.tvQuote);
  findViewById(R.id.btnPrev).setOnClickListener(v->{current.add(Calendar.MONTH,-1);draw();});
  findViewById(R.id.btnNext).setOnClickListener(v->{current.add(Calendar.MONTH,1);draw();});
  findViewById(R.id.btnToday).setOnClickListener(v->{current=Calendar.getInstance();selected=Calendar.getInstance();draw();showDetail();});
  draw(); showDetail();
 }
 void draw(){
  // Thẻ hôm nay trên header
  Calendar now=Calendar.getInstance();
  LunarCalendar.LunarDate lnNow=LunarCalendar.solar2Lunar(now.get(Calendar.DAY_OF_MONTH),now.get(Calendar.MONTH)+1,now.get(Calendar.YEAR));
  tvTodaySolar.setText(String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
  String[] dows={"","Chủ Nhật","Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy"};
  tvTodayDow.setText(dows[now.get(Calendar.DAY_OF_WEEK)]+" • Tháng "+(now.get(Calendar.MONTH)+1)+"/"+now.get(Calendar.YEAR));
  tvTodayLunar.setText(lnNow.day+"/"+lnNow.month+(lnNow.leap?" nhuận":"")+" ÂL");
  tvTodayCanChi.setText(LunarCalendar.canChiDay(now)+" • "+LunarCalendar.canChiYear(lnNow.year));
  int m=current.get(Calendar.MONTH)+1, y=current.get(Calendar.YEAR);
  tvMonthYear.setText("Tháng "+m+" • "+y);
  LunarCalendar.LunarDate first=LunarCalendar.solar2Lunar(1,m,y);
  tvLunarMonth.setText("Tháng âm: "+first.month+(first.leap?" nhuận":"")+" • Năm "+LunarCalendar.canChiYear(first.year));
  Calendar tmp=(Calendar)current.clone(); tmp.set(Calendar.DAY_OF_MONTH,15);
  tvCanChiMonth.setText(LunarCalendar.canChiMonth(tmp)+" • "+LunarCalendar.canChiYear(first.year));
  // Tuần
  gridWeek.removeAllViews();
  String[] w={"CN","T2","T3","T4","T5","T6","T7"};
  for(int i=0;i<7;i++){TextView t=new TextView(this);t.setText(w[i]);t.setGravity(Gravity.CENTER);t.setTextSize(11);t.setTypeface(null,Typeface.BOLD);t.setTextColor(i==0?Color.parseColor("#D11A38"):Color.parseColor("#9B8B8E"));GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.columnSpec=GridLayout.spec(i,1f);t.setLayoutParams(p);gridWeek.addView(t);}
  // Lưới ngày
  gridCal.removeAllViews();
  Calendar c=(Calendar)current.clone(); c.set(Calendar.DAY_OF_MONTH,1);
  int startDow=c.get(Calendar.DAY_OF_WEEK)-1; int max=c.getActualMaximum(Calendar.DAY_OF_MONTH);
  Calendar today=Calendar.getInstance();
  for(int i=0;i<startDow;i++){Space s=new Space(this);GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=300;p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);s.setLayoutParams(p);gridCal.addView(s);}
  for(int d=1;d<=max;d++){
   boolean isToday=(d==today.get(Calendar.DAY_OF_MONTH)&&m==today.get(Calendar.MONTH)+1&&y==today.get(Calendar.YEAR));
   boolean isSel=(d==selected.get(Calendar.DAY_OF_MONTH)&&m==selected.get(Calendar.MONTH)+1&&y==selected.get(Calendar.YEAR));
   LunarCalendar.LunarDate ln=LunarCalendar.solar2Lunar(d,m,y);
   LinearLayout cell=new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(4,14,4,14);
   if(isToday) cell.setBackgroundResource(R.drawable.bg_today);
   else if(isSel) cell.setBackgroundResource(R.drawable.bg_selected);
   else cell.setBackgroundResource(R.drawable.bg_cell);
   TextView tS=new TextView(this); tS.setText(String.valueOf(d)); tS.setGravity(Gravity.CENTER); tS.setTextSize(17); tS.setTypeface(null,Typeface.BOLD);
   int dow=(startDow+d-1)%7;
   if(isToday||isSel) tS.setTextColor(Color.WHITE);
   else if(dow==0) tS.setTextColor(Color.parseColor("#D11A38"));
   else tS.setTextColor(Color.parseColor("#2B0A12"));
   TextView tL=new TextView(this);
   String lTxt=(ln.day==1?ln.month+"/"+ln.year:String.valueOf(ln.day))+(ln.day==1&&ln.leap?"N":"");
   if(ln.day==1||ln.day==15) tL.setText("★ "+lTxt);
   else tL.setText(lTxt);
   tL.setGravity(Gravity.CENTER); tL.setTextSize(10);
   if(isToday) tL.setTextColor(Color.parseColor("#FFE6A3"));
   else if(isSel) tL.setTextColor(Color.parseColor("#F2C14E"));
   else if(ln.day==1||ln.day==15) tL.setTextColor(Color.parseColor("#B3122E"));
   else tL.setTextColor(Color.parseColor("#9B8B8E"));
   cell.addView(tS); cell.addView(tL);
   GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=0; p.height=300; p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); p.setMargins(6,6,6,6); cell.setLayoutParams(p);
   final int day=d; cell.setOnClickListener(v->{selected.set(y,m-1,day);draw();showDetail();});
   gridCal.addView(cell);
  }
 }
 void showDetail(){
  int d=selected.get(Calendar.DAY_OF_MONTH),m=selected.get(Calendar.MONTH)+1,y=selected.get(Calendar.YEAR);
  LunarCalendar.LunarDate ln=LunarCalendar.solar2Lunar(d,m,y);
  String[] dows={"","Chủ Nhật","Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy"};
  tvSelected.setText(dows[selected.get(Calendar.DAY_OF_WEEK)]+", "+d+"/"+m+"/"+y+"  •  "+ln.day+"/"+ln.month+(ln.leap?" nhuận":"")+" ÂL");
  String tietKhi=LunarCalendar.tietKhi(d,m);
  tvDetail.setText("Năm "+LunarCalendar.canChiYear(ln.year)+"  •  Tháng "+LunarCalendar.canChiMonth(selected)+"\nNgày "+LunarCalendar.canChiDay(selected)+"\nTiết khí: "+tietKhi+"\nGiờ hoàng đạo: "+LunarCalendar.gioHoangDao(LunarCalendar.canChiDay(selected).split(" ")[1]));
  tvQuote.setText("\""+quotes[d%quotes.length]+"\"");
 }
}
