package com.angis.fx.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.angis.fx.activity.draft.ListTodayCheckActivity;
import com.angis.fx.activity.draft.TodayAndDraftActivity;
import com.angis.fx.activity.jcdj.CheckTypeActivity;
import com.angis.fx.activity.jcdj.DailyCheckResultActivity;
import com.angis.fx.activity.login.LoginActivity;
import com.angis.fx.activity.map.SearchInMapActivity;
import com.angis.fx.activity.search.FieldSearchActivity;
import com.angis.fx.activity.service.UploadDutyInfoService;
import com.angis.fx.activity.sxry.SxryActivity;
import com.angis.fx.activity.sxry.SxryMActivity;
import com.angis.fx.activity.xtsz.SystemSettingActivity;
import com.angis.fx.activity.zdcf.AttendanceActivity;
import com.angis.fx.adapter.GridAdapter;
import com.angis.fx.data.AreaType;
import com.angis.fx.data.ChangsuoInformation;
import com.angis.fx.data.ChangsuoType;
import com.angis.fx.data.ContextInfo;
import com.angis.fx.data.DraftInfo;
import com.angis.fx.data.DutyInfo;
import com.angis.fx.data.GridInfo;
import com.angis.fx.db.CheckDraftDBHelper;
import com.angis.fx.db.DutyInfoDBHelper;
import com.angis.fx.db.XtszDBHelper;
import com.angis.fx.util.CommonUtil;
import com.angis.fx.util.ContentValuesUtil;

public class Enforcement extends Activity {

    public static int[] image = { R.drawable.homeblack, R.drawable.homesearchblack, R.drawable.registerblack, R.drawable.homenext1, R.drawable.today, R.drawable.homenow, R.drawable.draft, R.drawable.i_options, R.drawable.monials, R.drawable.kcall };

    private GridView gridview;

    private List<GridInfo> list;

    private List<DraftInfo> mDraftList;

    private GridAdapter adapter;

    private static final String FIELD_FIELD = "Field";

    private static final String FIELD_DEPARTMENT = "department";

    private static final String FIELD_NEXTDEPARTMENT = "Nextdepartment";

    public static List<AreaType> mAreaTypes = new ArrayList<AreaType>();

    public static List<ChangsuoType> mCSTypes = new ArrayList<ChangsuoType>();

    public static ChangsuoInformation mCurrentChangsuo = null;

    public static String mDeviceId;

    private Bundle bundle;

    public static boolean isStarted;

    public static int GPSExtent = 500;

    public static int GPSSecond = 3000;

    public static String HOST = "122.224.201.230";

    public static Date mStartdutytime;

    private DutyInfoDBHelper mDBHelper;

    public static String mDutyno;

    public static String mMcTJGuid;

    public static String mAfTJGuid;

    private void readAreaCodes(String filter) throws XmlPullParserException, IOException {
        XmlPullParser lParser = getResources().getXml(R.xml.areacode);
        int eventType = lParser.getEventType();
        AreaType lParentType = null;
        AreaType lChildType;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String lName;
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
                lName = lParser.getName();
                if (lName.equals(FIELD_DEPARTMENT)) {
                    lParentType = new AreaType(lParser.getAttributeValue(0), lParser.getAttributeValue(1));
                    if (lParentType.getId().indexOf(filter) != -1) mAreaTypes.add(lParentType);
                }
                if (lName.equals(FIELD_NEXTDEPARTMENT)) {
                    lChildType = new AreaType(lParser.getAttributeValue(0), lParser.getAttributeValue(2), lParser.getAttributeValue(1), lParentType);
                    if (lChildType.getId().indexOf(filter) != -1) mAreaTypes.add(lChildType);
                }
            }
            eventType = lParser.next();
        }
    }

    private void readChangsuoType(String filter) throws XmlPullParserException, IOException {
        XmlPullParser lParser = getResources().getXml(R.xml.changsuotype);
        int eventType = lParser.getEventType();
        ChangsuoType lType;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String lName;
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
                lName = lParser.getName();
                if (lName.equals(FIELD_FIELD)) {
                    lType = new ChangsuoType(lParser.getAttributeValue(0), lParser.getAttributeValue(1), lParser.getAttributeValue(2));
                    mCSTypes.add(lType);
                }
            }
            eventType = lParser.next();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bundle = getIntent().getExtras();
        try {
            mDeviceId = ((ContextInfo) bundle.get("contextInfo")).getDeviceno();
            readAreaCodes(((ContextInfo) bundle.get("contextInfo")).getDpLevel());
            readChangsuoType(((ContextInfo) bundle.get("contextInfo")).getDpLevel());
        } catch (XmlPullParserException e) {
            Toast.makeText(this, "配置文件读取错误", Toast.LENGTH_LONG);
        } catch (IOException e) {
            Toast.makeText(this, "配置文件读取错误", Toast.LENGTH_LONG);
        }
        try {
            XtszDBHelper lDBHelper = new XtszDBHelper(Enforcement.this);
            Cursor lCursor = lDBHelper.find();
            if (lCursor.getCount() > 0) {
                lCursor.moveToFirst();
                GPSSecond = Integer.valueOf(lCursor.getString(0));
                GPSExtent = Integer.valueOf(lCursor.getString(1));
                HOST = lCursor.getString(2);
            }
            lCursor.close();
            lDBHelper.closeDB();
        } catch (Exception e) {
        }
        gridview = (GridView) findViewById(R.id.gridview);
        list = new ArrayList<GridInfo>();
        list.add(new GridInfo(R.string.home));
        list.add(new GridInfo(R.string.homesearch));
        list.add(new GridInfo(R.string.register));
        list.add(new GridInfo(R.string.homenext));
        list.add(new GridInfo(R.string.today_check));
        list.add(new GridInfo(R.string.homenow));
        list.add(new GridInfo(R.string.draft));
        list.add(new GridInfo(R.string.i_options));
        list.add(new GridInfo(R.string.monials));
        list.add(new GridInfo(R.string.kcall));
        adapter = new GridAdapter(this, image);
        adapter.setList(list);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                doOnClickListener(arg0, view, index, arg3);
            }
        });
        System.out.println(SxryMActivity.SXRP_SELECT_MEMBER);
    }

    void doOnClickListener(AdapterView<?> arg0, View view, int index, long arg3) {
        Intent lIntent = new Intent();
        switch(index) {
            case 0:
                if (isStarted) {
                    lIntent.setClass(Enforcement.this, SearchInMapActivity.class);
                    Enforcement.this.startActivity(lIntent);
                }
                break;
            case 1:
                if (isStarted) {
                    lIntent.setClass(Enforcement.this, FieldSearchActivity.class);
                    Enforcement.this.startActivity(lIntent);
                }
                break;
            case 2:
                if (isStarted) {
                    lIntent.setClass(Enforcement.this, CheckTypeActivity.class);
                    Enforcement.this.startActivity(lIntent);
                }
                break;
            case 3:
                Bundle lBundle = new Bundle();
                if (image[3] == R.drawable.homenext1) {
                    lBundle.putInt("RESULTCODE", 1);
                    isStarted = true;
                } else {
                    lBundle.putInt("RESULTCODE", 2);
                    isStarted = false;
                }
                lIntent.putExtras(lBundle);
                lIntent.setClass(Enforcement.this, AttendanceActivity.class);
                Enforcement.this.startActivityForResult(lIntent, lBundle.getInt("RESULTCODE"));
                break;
            case 4:
                lIntent.setClass(Enforcement.this, ListTodayCheckActivity.class);
                Enforcement.this.startActivity(lIntent);
                break;
            case 5:
                if (Enforcement.mCurrentChangsuo != null) {
                    lIntent.setClass(Enforcement.this, DailyCheckResultActivity.class);
                    lIntent.putExtra("selectedcs", Enforcement.mCurrentChangsuo);
                    Enforcement.this.startActivity(lIntent);
                }
                break;
            case 6:
                lIntent.setClass(Enforcement.this, TodayAndDraftActivity.class);
                Enforcement.this.startActivity(lIntent);
                break;
            case 7:
                lIntent.setClass(Enforcement.this, SystemSettingActivity.class);
                Enforcement.this.startActivity(lIntent);
                break;
            case 8:
                bundle.putBoolean("isClear", false);
                lIntent.setClass(Enforcement.this, SxryMActivity.class);
                lIntent.putExtras(bundle);
                Enforcement.this.startActivity(lIntent);
                break;
            case 9:
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:057185022377"));
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data && null != data.getExtras()) {
            Bundle lBundle = data.getExtras();
            String lDatetime = lBundle.getString("DATATIME");
            SimpleDateFormat lSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            switch(resultCode) {
                case 1:
                    try {
                        if (lSdf.parse(lDatetime).compareTo(lSdf.parse(lSdf.format(new Date()))) > 0) {
                            Toast.makeText(Enforcement.this, "开始时间比系统时间晚,请重新选择!", Toast.LENGTH_SHORT).show();
                            isStarted = false;
                            return;
                        }
                        break;
                    } catch (Exception e) {
                        return;
                    }
                case 2:
                    try {
                        if (lSdf.parse(lDatetime).compareTo(mStartdutytime) < 0) {
                            Toast.makeText(Enforcement.this, "结束时间比开始时间早,请重新选择!", Toast.LENGTH_SHORT).show();
                            isStarted = true;
                            return;
                        }
                        break;
                    } catch (Exception e) {
                        return;
                    }
            }
            if (image[3] == R.drawable.homenext1) {
                image[3] = R.drawable.homeback1;
                list.remove(3);
                list.add(3, new GridInfo(R.string.homeback));
            } else {
                image[3] = R.drawable.homenext1;
                list.remove(3);
                list.add(3, new GridInfo(R.string.homenext));
            }
            this.adapter.setImage(image);
            this.adapter.setList(list);
            this.gridview.invalidateViews();
            int lYear = lBundle.getInt("YEAR");
            int lMonth = lBundle.getInt("MONTH");
            int lDay = lBundle.getInt("DAY");
            int lHour = lBundle.getInt("HOUR");
            int lMinute = lBundle.getInt("MINUTE");
            DutyInfo lDutyInfo = new DutyInfo();
            ContentValues lValues = null;
            Intent lIntent;
            switch(resultCode) {
                case 1:
                    mMcTJGuid = CommonUtil.randomNumber(19);
                    mAfTJGuid = CommonUtil.randomNumber(19);
                    mDutyno = CommonUtil.randomNumber(19);
                    Calendar lCal = Calendar.getInstance();
                    lCal.set(lYear, lMonth, lDay, lHour, lMinute);
                    mStartdutytime = lCal.getTime();
                    lDutyInfo.setDusername(LoginActivity.g_username);
                    lDutyInfo.setDutyno(mDutyno);
                    lDutyInfo.setCdrc((SxryMActivity.SXRP_SELECT_MEMBER.split(",").length + SxryActivity.QTRY_EDIT_MEMBER_COUNT) + "");
                    lDutyInfo.setTogetheruser(SxryMActivity.SXRP_SELECT_MEMBER.replaceAll(",", "|"));
                    lDutyInfo.setOtheruser(SxryActivity.QTRY_EDIT_MEMBER);
                    lDutyInfo.setOthercount(SxryActivity.QTRY_EDIT_MEMBER_COUNT + "");
                    lDutyInfo.setDutystatus("1");
                    lDutyInfo.setMctjuuid(mMcTJGuid);
                    lDutyInfo.setAftjuuid(mAfTJGuid);
                    lDutyInfo.setStartdutytime(lYear + "-" + lMonth + "-" + lDay + " " + lHour + ":" + lMinute + ":00");
                    lDutyInfo.setEnddutytime("");
                    lIntent = new Intent(Enforcement.this, UploadDutyInfoService.class);
                    lIntent.putExtra("dutyinfo", lDutyInfo);
                    startService(lIntent);
                    lValues = ContentValuesUtil.convertDutyInfo(lDutyInfo);
                    mDBHelper = new DutyInfoDBHelper(Enforcement.this);
                    mDBHelper.insertDutyInfo(lValues);
                    mDBHelper.closeDB();
                    break;
                case 2:
                    lDutyInfo.setDutyno(mDutyno);
                    lDutyInfo.setDusername(LoginActivity.g_username);
                    lDutyInfo.setCdrc((SxryMActivity.SXRP_SELECT_MEMBER.split(",").length + SxryActivity.QTRY_EDIT_MEMBER_COUNT) + "");
                    lDutyInfo.setTogetheruser(SxryMActivity.SXRP_SELECT_MEMBER);
                    lDutyInfo.setOtheruser(SxryActivity.QTRY_EDIT_MEMBER);
                    lDutyInfo.setOthercount(SxryActivity.QTRY_EDIT_MEMBER_COUNT + "");
                    lDutyInfo.setDutystatus("2");
                    lDutyInfo.setEnddutytime(lYear + "-" + lMonth + "-" + lDay + " " + lHour + ":" + lMinute + ":00");
                    lDutyInfo.setStartdutytime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mStartdutytime));
                    CheckDraftDBHelper lDBHelper2 = new CheckDraftDBHelper(Enforcement.this);
                    mDraftList = lDBHelper2.findDraftByType();
                    lDBHelper2.closeDB();
                    lDutyInfo.setAfareacode(getAreacode("暗访"));
                    lDutyInfo.setMcareacode(getAreacode("明察"));
                    lIntent = new Intent(Enforcement.this, UploadDutyInfoService.class);
                    lIntent.putExtra("dutyinfo", lDutyInfo);
                    startService(lIntent);
                    lValues = ContentValuesUtil.convertDutyInfo(lDutyInfo);
                    mDBHelper = new DutyInfoDBHelper(Enforcement.this);
                    mDBHelper.updateDutyInfo(lValues);
                    mDBHelper.closeDB();
                    break;
            }
        } else {
            isStarted = isStarted == true ? false : true;
        }
    }

    private String getAreacode(String pCheckxs) {
        List<String> parentList = new ArrayList<String>();
        for (DraftInfo info : mDraftList) {
            if (pCheckxs.equals(info.getCheckxs())) {
                try {
                    getAreaCodeList(parentList, info);
                } catch (Exception e) {
                }
            }
        }
        return retAreaCode(parentList);
    }

    private String retAreaCode(List<String> parentList) {
        for (int i = 0; i < 3; i++) {
            int lCount = 0;
            String lCode = "";
            for (String parent : parentList) {
                for (AreaType type : mAreaTypes) {
                    if (type.getName().equals(parent)) {
                        lCount++;
                        lCode = type.getCode();
                    } else if (null == parent) {
                        return type.getCode();
                    }
                    parent = type.getParent().getName();
                }
            }
            if (lCount == parentList.size()) {
                return lCode;
            }
        }
        return "";
    }

    private void getAreaCodeList(List<String> parentList, DraftInfo info) {
        String lArrivalTime = info.getArrivaltime();
        String[] lRes = lArrivalTime.split(" ");
        String[] lDate = lRes[0].split("-");
        String[] lTime = lRes[1].split(":");
        Calendar lCal = Calendar.getInstance();
        lCal.set(Integer.valueOf(lDate[0]), Integer.valueOf(lDate[1]), Integer.valueOf(lDate[2]), Integer.valueOf(lTime[0]), Integer.valueOf(lTime[1]));
        Date s = lCal.getTime();
        if (s.getTime() > mStartdutytime.getTime()) {
            parentList.add(info.getArea());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_exit:
                this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if (this.isStarted) {
            image[0] = R.drawable.home;
            image[1] = R.drawable.homesearch;
            image[2] = R.drawable.register;
        } else {
            image[0] = R.drawable.homeblack;
            image[1] = R.drawable.homesearchblack;
            image[2] = R.drawable.registerblack;
        }
        if (Enforcement.mCurrentChangsuo != null) {
            image[5] = R.drawable.homenow;
        } else {
            image[5] = R.drawable.homenowblack;
        }
        this.adapter.setImage(image);
        this.gridview.invalidateViews();
        super.onResume();
    }

    @Override
    public void finish() {
        new AlertDialog.Builder(Enforcement.this).setTitle("确认退出？").setPositiveButton("确定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Enforcement.super.finish();
            }
        }).setNegativeButton("取消", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
    }
}
