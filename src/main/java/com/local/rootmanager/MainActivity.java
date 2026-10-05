package com.local.rootmanager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    static final int BG=Color.rgb(8,15,25), CARD=Color.rgb(20,30,44), CARD2=Color.rgb(28,40,57), BLUE=Color.rgb(25,116,245), GREEN=Color.rgb(54,235,126), WHITE=Color.rgb(242,245,250), MUTED=Color.rgb(174,185,204);
    final HandlerX h=new HandlerX(); final ExecutorService ex=Executors.newSingleThreadExecutor();
    PackageManager pm; EditText search; TextView status; ProgressBar loading; ListView list; AppAdapter adapter; String category="ALL";
    final ArrayList<AppItem> apps=new ArrayList<>(); final HashMap<Integer,Integer> policies=new HashMap<>();

    @Override public void onCreate(Bundle b){super.onCreate(b); pm=getPackageManager(); makeUi(); load();}
    @Override protected void onDestroy(){ex.shutdownNow();super.onDestroy();}
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView text(String s,float z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    GradientDrawableX bg(int c,int r){return new GradientDrawableX(c,dp(r));}
    View gap(int n){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(n)));return v;}

    void makeUi(){
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setBackgroundColor(BG); page.setPadding(dp(16),dp(16),dp(16),0);
        LinearLayout head=new LinearLayout(this); head.setOrientation(LinearLayout.VERTICAL); page.addView(head,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout title=new LinearLayout(this); title.setGravity(Gravity.CENTER_VERTICAL);
        TextView shield=text("#",31,Color.WHITE); shield.setGravity(Gravity.CENTER); shield.setTypeface(null,1); shield.setBackground(bg(BLUE,18)); title.addView(shield,new LinearLayout.LayoutParams(dp(64),dp(64)));
        TextView appName=text("Root Manager by Jhon Simbulas",21,WHITE); appName.setTypeface(null,1); LinearLayout.LayoutParams an=new LinearLayout.LayoutParams(0,dp(64),1);an.leftMargin=dp(14);title.addView(appName,an);
        TextView more=text("⋮",34,MUTED);more.setGravity(Gravity.CENTER);title.addView(more,new LinearLayout.LayoutParams(dp(40),dp(64))); more.setOnClickListener(v->menu(more)); head.addView(title);
        head.addView(gap(12));
        LinearLayout stat=new LinearLayout(this);stat.setGravity(Gravity.CENTER_VERTICAL);stat.setPadding(dp(18),dp(8),dp(18),dp(8));stat.setBackground(bg(CARD,18));
        TextView ok=text("✓",29,GREEN);ok.setGravity(Gravity.CENTER);stat.addView(ok,new LinearLayout.LayoutParams(dp(52),dp(52)));
        status=text("Checking root access…",21,GREEN);status.setTypeface(null,1);LinearLayout.LayoutParams sl=new LinearLayout.LayoutParams(0,dp(56),1);sl.leftMargin=dp(14);stat.addView(status,sl);head.addView(stat);
        head.addView(gap(12));
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(WHITE);search.setHintTextColor(MUTED);search.setTextSize(19);search.setHint("🔍  Search apps…");search.setPadding(dp(18),0,dp(18),0);search.setBackground(bg(CARD2,18));head.addView(search,new LinearLayout.LayoutParams(-1,dp(64)));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){if(adapter!=null)adapter.refresh();}public void afterTextChanged(Editable e){}});
        head.addView(gap(12));
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);String[] cats={"ALL","USER","SYSTEM","ROOT"};
        for(String c:cats){TextView t=text(c,17,WHITE);t.setGravity(Gravity.CENTER);t.setTypeface(null,1);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(56),1);p.setMargins(dp(3),0,dp(3),0);tabs.addView(t,p);t.setOnClickListener(v->{category=c;styleTabs(tabs);adapter.refresh();});}
        head.addView(tabs);styleTabs(tabs);
        loading=new ProgressBar(this);head.addView(loading,new LinearLayout.LayoutParams(-1,dp(42)));
        list=new ListView(this);list.setDivider(null);list.setDividerHeight(dp(10));list.setBackgroundColor(Color.TRANSPARENT);list.setPadding(0,0,0,dp(12));adapter=new AppAdapter();list.setAdapter(adapter);
        page.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(page);
    }
    void styleTabs(LinearLayout t){for(int i=0;i<t.getChildCount();i++){TextView v=(TextView)t.getChildAt(i);v.setBackground(bg(v.getText().toString().equals(category)?BLUE:CARD2,18));}}
    void menu(View anchor){PopupMenu p=new PopupMenu(this,anchor);p.getMenu().add("Refresh");p.getMenu().add("Grant all user apps");p.getMenu().add("Deny all user apps");p.getMenu().add("About");p.setOnMenuItemClickListener(i->{String s=i.getTitle().toString();if(s.equals("Refresh"))load();else if(s.startsWith("Grant"))bulk(2);else if(s.startsWith("Deny"))bulk(0);else Toast.makeText(this,"Root Manager by Jhon Simbulas\nMagisk policy manager",Toast.LENGTH_LONG).show();return true;});p.show();}
    void bulk(int pol){ex.execute(()->{for(AppItem a:apps)if(!a.system)setPolicy(a.uid,pol);load();});}

    void load(){if(loading!=null)loading.setVisibility(View.VISIBLE);ex.execute(()->{
        final HashMap<Integer,Integer> np=readPolicies(); final ArrayList<AppItem> out=new ArrayList<>();
        try{for(ApplicationInfo ai:pm.getInstalledApplications(PackageManager.GET_META_DATA)){
            boolean sys=(ai.flags&ApplicationInfo.FLAG_SYSTEM)!=0||(ai.flags&ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!=0;int uid;
            try{uid=pm.getPackageUid(ai.packageName,0);}catch(Exception e){continue;}String label;try{label=ai.loadLabel(pm).toString();}catch(Exception e){label=ai.packageName;}
            Drawable icon;try{icon=ai.loadIcon(pm);}catch(Exception e){icon=getDrawable(android.R.drawable.sym_def_app_icon);}
            out.add(new AppItem(label,ai.packageName,uid,sys,icon,np.getOrDefault(uid,-1)));
        }}catch(Exception ignored){}
        Collections.sort(out,(a,b)->a.label.compareToIgnoreCase(b.label));
        final boolean root=run("id")==null?false:true;
        h.post(()->{apps.clear();apps.addAll(out);policies.clear();policies.putAll(np);loading.setVisibility(View.GONE);status.setText(root?"Root access available":"Root access unavailable");status.setTextColor(root?GREEN:Color.rgb(255,90,90));adapter.refresh();});
    });}
    HashMap<Integer,Integer> readPolicies(){HashMap<Integer,Integer> m=new HashMap<>();String s=run("magisk --sqlite \"SELECT uid,policy FROM policies;\"");if(s==null)return m;for(String l:s.split("\\n")){String[] q=l.trim().split("\\|");if(q.length>1)try{m.put(Integer.parseInt(q[0].trim()),Integer.parseInt(q[1].trim()));}catch(Exception ignored){}}return m;}
    boolean setPolicy(int uid,int pol){String sql="INSERT OR REPLACE INTO policies (uid,policy,until,logging,notification) VALUES ("+uid+","+pol+",0,1,1);";return run("magisk --sqlite \""+sql+"\"")!=null;}
    String run(String cmd){try{Process p=new ProcessBuilder("su","-c",cmd).redirectErrorStream(true).start();BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream()));StringBuilder s=new StringBuilder();String l;while((l=r.readLine())!=null)s.append(l).append('\n');return p.waitFor()==0?s.toString():null;}catch(Exception e){return null;}}

    class AppAdapter extends BaseAdapter{
        final ArrayList<AppItem> shown=new ArrayList<>();
        void refresh(){shown.clear();String q=search==null?"":search.getText().toString().trim().toLowerCase(Locale.ROOT);for(AppItem a:apps){boolean cat=category.equals("ALL")||(category.equals("USER")&&!a.system)||(category.equals("SYSTEM")&&a.system)||(category.equals("ROOT")&&a.policy==2);boolean find=q.isEmpty()||a.label.toLowerCase(Locale.ROOT).contains(q)||a.pkg.toLowerCase(Locale.ROOT).contains(q)||String.valueOf(a.uid).contains(q);if(cat&&find)shown.add(a);}notifyDataSetChanged();}
        public int getCount(){return shown.size();}public Object getItem(int p){return shown.get(p);}public long getItemId(int p){return p;}
        public View getView(int pos,View cv,ViewGroup parent){Row r=cv instanceof Row?(Row)cv:new Row(MainActivity.this);AppItem a=shown.get(pos);r.icon.setImageDrawable(a.icon);r.name.setText(a.label);r.pkg.setText(a.pkg);setRow(r,a);return r;}
        void setRow(Row r,AppItem a){String st=a.policy==2?"ALLOW":a.policy==0?"DENY":a.policy==1?"ASK":"NONE";r.meta.setText("UID "+a.uid+"  •  "+st);r.meta.setTextColor(a.policy==2?GREEN:MUTED);r.sw.setOnCheckedChangeListener(null);r.sw.setChecked(a.policy==2);r.sw.setOnCheckedChangeListener((b,on)->{b.setEnabled(false);int want=on?2:0;ex.execute(()->{boolean ok=setPolicy(a.uid,want);h.post(()->{b.setEnabled(true);if(ok){a.policy=want;r.meta.setText("UID "+a.uid+"  •  "+(want==2?"ALLOW":"DENY"));r.meta.setTextColor(want==2?GREEN:MUTED);if(category.equals("ROOT"))refresh();Toast.makeText(MainActivity.this,a.label+(want==2?" — root allowed":" — root denied"),Toast.LENGTH_SHORT).show();}else{b.setChecked(!on);Toast.makeText(MainActivity.this,"Magisk policy change failed",Toast.LENGTH_SHORT).show();}});});});}
    }
    class Row extends LinearLayout{
        ImageView icon;TextView name,pkg,meta;Switch sw;
        Row(Context c){super(c);setOrientation(HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);setPadding(dp(14),dp(10),dp(10),dp(10));setBackground(bg(CARD,18));
            icon=new ImageView(c);addView(icon,new LinearLayout.LayoutParams(dp(70),dp(70)));
            LinearLayout tx=new LinearLayout(c);tx.setOrientation(VERTICAL);tx.setPadding(dp(12),0,dp(4),0);name=text("",20,WHITE);name.setTypeface(null,1);pkg=text("",14,MUTED);meta=text("",14,MUTED);tx.addView(name,new LinearLayout.LayoutParams(-1,dp(30)));tx.addView(pkg,new LinearLayout.LayoutParams(-1,dp(24)));tx.addView(meta,new LinearLayout.LayoutParams(-1,dp(24)));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(82),1);addView(tx,tp);
            sw=new Switch(c);sw.setMinWidth(dp(64));addView(sw,new LinearLayout.LayoutParams(dp(70),dp(58)));}
    }
    static class AppItem{String label,pkg;int uid,policy;boolean system;Drawable icon;AppItem(String l,String p,int u,boolean s,Drawable i,int x){label=l;pkg=p;uid=u;system=s;icon=i;policy=x;}}
    static class GradientDrawableX extends android.graphics.drawable.GradientDrawable{GradientDrawableX(int c,int r){setColor(c);setCornerRadius(r);}}
    static class HandlerX{final android.os.Handler x=new android.os.Handler(android.os.Looper.getMainLooper());void post(Runnable r){x.post(r);}}
}
