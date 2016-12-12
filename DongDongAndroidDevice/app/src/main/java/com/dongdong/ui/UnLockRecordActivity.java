package com.dongdong.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.dongdong.adapter.SpinnerAdapter;
import com.dongdong.adapter.UnLockRecordAdapter;
import com.dongdong.base.BaseApplication;
import com.dongdong.db.UnlockLogOpe;
import com.dongdong.db.entry.UnlockLogBean;
import com.dongdong.ui.dialog.CommonDialog;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.ProcessDataUtils;
import com.dongdong.utils.TimeZoneUtil;
import com.dongdong.wheel.NumericWheelAdapter;
import com.dongdong.wheel.WheelView;
import com.jr.door.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 显示开门记录界面
 * author leer （http://www.dd121.com）
 * created at 2016/12/1 16:24
 */

public class UnLockRecordActivity extends Activity implements View.OnClickListener,
        AdapterView.OnItemLongClickListener {

    private Unbinder mUnBinder;
    private UnLockRecordAdapter mUnLockRecordAdapter;
    private CommonDialog mDialog;
    private PopupWindow mPopupWindow;
    private TextView mTvBeginTime;
    private TextView mTvEndTime;

    private WheelView mYearWheel, mMonthWheel, mDayWheel, mHourWheel, mMinuteWheel, mSecondWheel;
    private StringBuffer mStringBuffer;
    public static String[] mYearContent;
    public static String[] mMonthContent;
    public static String[] mDayContent;
    public static String[] mHourContent;
    public static String[] mMinuteContent;
    public static String[] mSecondContent;

    private List<UnlockLogBean> mUnlockLogBeanList;
    private UnlockLogBean mUnlockLogBean;
    private String mBeginTime, mEndTime, mUnLockNumber, mUnLockRoomNum;
    private int mUnLockType;
    private int mUnLockUpLoad;


    private SpinnerAdapter<String> mTypeAdapter, mUpLoadAdapter;

    @BindView(R.id.lv_unlock_record)
    ListView mLvUnLockRecord;
    @BindView(R.id.iv_back)
    ImageView mIvBack;
    @BindView(R.id.tv_record_count)
    TextView mTvRecordCount;

    @BindView(R.id.bt_search)
    Button mBtSearch;

    private final static ThreadLocal<SimpleDateFormat> mDateFormat =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_unlock_record);
        mUnBinder = ButterKnife.bind(this);

        mUnlockLogBeanList = UnlockLogOpe.queryAll(BaseApplication.context());
        mUnLockRecordAdapter = new UnLockRecordAdapter(this);
        mLvUnLockRecord.setAdapter(mUnLockRecordAdapter);
        mUnLockRecordAdapter.setData(mUnlockLogBeanList);
        mLvUnLockRecord.setOnItemLongClickListener(this);

        mTvRecordCount.setText(String.format(this.getString(R.string.unlock_record_count), mUnlockLogBeanList.size()));
        initContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }


    public void initContent() {
        mYearContent = new String[10];
        for (int i = 0; i < 10; i++)
            mYearContent[i] = String.valueOf(i + Calendar.getInstance().get(Calendar.YEAR) - 3);

        mMonthContent = new String[12];
        for (int i = 0; i < 12; i++) {
            mMonthContent[i] = String.valueOf(i + 1);
            if (mMonthContent[i].length() < 2) {
                mMonthContent[i] = "0" + mMonthContent[i];
            }
        }

        mDayContent = new String[31];
        for (int i = 0; i < 31; i++) {
            mDayContent[i] = String.valueOf(i + 1);
            if (mDayContent[i].length() < 2) {
                mDayContent[i] = "0" + mDayContent[i];
            }
        }
        mHourContent = new String[24];
        for (int i = 0; i < 24; i++) {
            mHourContent[i] = String.valueOf(i);
            if (mHourContent[i].length() < 2) {
                mHourContent[i] = "0" + mHourContent[i];
            }
        }

        mMinuteContent = new String[60];
        for (int i = 0; i < 60; i++) {
            mMinuteContent[i] = String.valueOf(i);
            if (mMinuteContent[i].length() < 2) {
                mMinuteContent[i] = "0" + mMinuteContent[i];
            }
        }
        mSecondContent = new String[60];
        for (int i = 0; i < 60; i++) {
            mSecondContent[i] = String.valueOf(i);
            if (mSecondContent[i].length() < 2) {
                mSecondContent[i] = "0" + mSecondContent[i];
            }
        }
    }

    public void loadWheelView(final View view) {
        View loadView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.time_picker, new LinearLayout(this), false);
        Calendar calendar = Calendar.getInstance();
        int curMonth = calendar.get(Calendar.MONTH) + 1;
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMinute = calendar.get(Calendar.MINUTE);
        int curSecond = calendar.get(Calendar.SECOND);

        mYearWheel = (WheelView) loadView.findViewById(R.id.year_wheel);
        mMonthWheel = (WheelView) loadView.findViewById(R.id.month_wheel);
        mDayWheel = (WheelView) loadView.findViewById(R.id.day_wheel);
        mHourWheel = (WheelView) loadView.findViewById(R.id.hour_wheel);
        mMinuteWheel = (WheelView) loadView.findViewById(R.id.minute_wheel);
        mSecondWheel = (WheelView) loadView.findViewById(R.id.second_wheel);

        Button btSure = (Button) loadView.findViewById(R.id.bt_sure);

        mYearWheel.setAdapter(new NumericWheelAdapter(mYearContent));
        mYearWheel.setCurrentItem(3);
        mYearWheel.setCyclic(true);
        mYearWheel.setInterpolator(new AnticipateOvershootInterpolator());


        mMonthWheel.setAdapter(new NumericWheelAdapter(mMonthContent));
        mMonthWheel.setCurrentItem(curMonth - 1);
        mMonthWheel.setCyclic(true);
        mMonthWheel.setInterpolator(new AnticipateOvershootInterpolator());

        mDayWheel.setAdapter(new NumericWheelAdapter(mDayContent));
        mDayWheel.setCurrentItem(curDay - 1);
        mDayWheel.setCyclic(true);
        mDayWheel.setInterpolator(new AnticipateOvershootInterpolator());

        mHourWheel.setAdapter(new NumericWheelAdapter(mHourContent));
        mHourWheel.setCurrentItem(curHour);
        mHourWheel.setCyclic(true);
        mHourWheel.setInterpolator(new AnticipateOvershootInterpolator());

        mMinuteWheel.setAdapter(new NumericWheelAdapter(mMinuteContent));
        mMinuteWheel.setCurrentItem(curMinute);
        mMinuteWheel.setCyclic(true);
        mMinuteWheel.setInterpolator(new AnticipateOvershootInterpolator());

        mSecondWheel.setAdapter(new NumericWheelAdapter(mSecondContent));
        mSecondWheel.setCurrentItem(curSecond);
        mSecondWheel.setCyclic(true);
        mSecondWheel.setInterpolator(new AnticipateOvershootInterpolator());

        mDialog = new CommonDialog(this);
        mDialog.setContent(loadView);
        mDialog.setTitle(getString(R.string.select_time));
        mDialog.show();
        btSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStringBuffer = new StringBuffer();
                mStringBuffer.append(mYearWheel.getCurrentItemValue()).append("-")
                        .append(mMonthWheel.getCurrentItemValue()).append("-")
                        .append(mDayWheel.getCurrentItemValue());

                mStringBuffer.append(" ");
                mStringBuffer.append(mHourWheel.getCurrentItemValue())
                        .append(":").append(mMinuteWheel.getCurrentItemValue())
                        .append(":").append(mSecondWheel.getCurrentItemValue());
                if (view.getId() == R.id.tv_begin_time) {
                    mBeginTime = mStringBuffer.toString();
                    mTvBeginTime.setText(mBeginTime);
                } else {
                    mEndTime = mStringBuffer.toString();
                    mTvEndTime.setText(mEndTime);
                }
                mDialog.cancel();

            }
        });
    }

    public void loadPopupWindow() {
        final View popupView = getLayoutInflater().inflate(R.layout.search_unlock,
                new LinearLayout(this), false);
        mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        mTvBeginTime = (TextView) popupView.findViewById(R.id.tv_begin_time);
        mTvEndTime = (TextView) popupView.findViewById(R.id.tv_end_time);
        final EditText etCardOrPhoneNumber = (EditText) popupView.findViewById(R.id.et_num);
        final EditText etUnLockRoomNum = (EditText) popupView.findViewById(R.id.et_room_number);
        final Spinner spUnlockType = (Spinner) popupView.findViewById(R.id.sp_unlock_type);
        final Spinner spUpLoad = (Spinner) popupView.findViewById(R.id.sp_upload);
        Button btPopupSure = (Button) popupView.findViewById(R.id.bt_popup_sure);


        String[] unLockType = new String[]{getString(R.string.all), getString(R.string.App),
                getString(R.string.card_local),
                getString(R.string.WIFI), getString(R.string.Temporary_Password),
                getString(R.string.Household_Password), getString(R.string.card_cloud),
                getString(R.string.bluetooth), getString(R.string.phone)};
        String[] unLockIsUpLoad = new String[]{getString(R.string.all),
                getString(R.string.is_upload), getString(R.string.is_not_upload)};

        AdapterView.OnItemSelectedListener spTypeItemClick = new
                AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        if (parent.getId() == R.id.sp_unlock_type) {
                            String type = mTypeAdapter.getItem(position);
                            mUnLockType = ProcessDataUtils.getUnlockTypeByName(type, UnLockRecordActivity.this);
                            spUnlockType.setSelection(position, true);
                        } else {
                            String upLoad = mUpLoadAdapter.getItem(position);
                            mUnLockUpLoad = ProcessDataUtils.getUnlockState(upLoad, UnLockRecordActivity.this);
                            spUpLoad.setSelection(position, true);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                };
        mTypeAdapter = new SpinnerAdapter<>(BaseApplication.context(), unLockType);
        mTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnlockType.setAdapter(mTypeAdapter);
        spUnlockType.setOnItemSelectedListener(spTypeItemClick);

        mUpLoadAdapter = new SpinnerAdapter<>(BaseApplication.context(), unLockIsUpLoad);
        mUpLoadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUpLoad.setAdapter(mUpLoadAdapter);
        spUpLoad.setOnItemSelectedListener(spTypeItemClick);

        if (mUnlockLogBean != null) {
            Date date = TimeZoneUtil.transformTime(new Date(mUnlockLogBean.getUnlockTime() * 1000L),
                    TimeZone.getTimeZone("GMT"), TimeZone.getTimeZone("GMT+08"));
            mTvBeginTime.setText(new StringBuffer(String.format("%tF", date)).append(" 00:00:00"));
            mTvEndTime.setText(new StringBuffer(String.format("%tF", date)).append(" 23:59:59"));
            etCardOrPhoneNumber.setText(mUnlockLogBean.getCardOrPhoneNum());
            etUnLockRoomNum.setText(mUnlockLogBean.getRoomNum());
            for (int i = 0; i < unLockType.length; i++) {
                if (ProcessDataUtils.getUnlockNameByType(mUnlockLogBean.getUnlockType(), UnLockRecordActivity.
                        this).equals(unLockType[i])) {
                    spUnlockType.setSelection(i, true);
                }
            }
            spUpLoad.setSelection(mUnlockLogBean.getUpload() + 1, true);
            mBeginTime = mTvBeginTime.getText().toString();
            mEndTime = mTvEndTime.getText().toString();
        }


        View.OnClickListener tvTimeClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.bt_popup_sure) {
                    try {
                        long beginTime = 0;
                        long endTime = 0;
                        if (!TextUtils.isEmpty(mBeginTime) && !TextUtils.isEmpty(mEndTime)) {
                            beginTime = TimeZoneUtil.transformTime(new Date(mDateFormat.get().parse
                                            (mBeginTime).getTime()), TimeZone.getTimeZone("GMT"),
                                    TimeZone.getTimeZone("GMT-08")).getTime() / 1000L;
                            endTime = TimeZoneUtil.transformTime(new Date(mDateFormat.get().parse
                                            (mEndTime).getTime()), TimeZone.getTimeZone("GMT"),
                                    TimeZone.getTimeZone("GMT-08")).getTime() / 1000L;
                        }
                        mUnLockNumber = etCardOrPhoneNumber.getText().toString();
                        mUnLockRoomNum = etUnLockRoomNum.getText().toString();
                        DDLog.i("UnLockRecordActivity.clazz-->> beginTime:" + beginTime
                                + " ,endTime:" + endTime + " ,mUnLockNumber:"
                                + mUnLockNumber + " ,mUnLockType:" + mUnLockType +
                                " ,mUnLockUpLoad:" + mUnLockUpLoad);
                        mUnlockLogBeanList = UnlockLogOpe.queryDataByConditions
                                (BaseApplication.context(), (int) beginTime, (int) endTime,
                                        mUnLockNumber, mUnLockRoomNum, mUnLockType, mUnLockUpLoad);
                        mUnLockRecordAdapter.setData(mUnlockLogBeanList);
                        mUnLockRecordAdapter.notifyDataSetChanged();
                        mTvRecordCount.setText(String.format(getString(R.string.unlock_record_count),
                                mUnlockLogBeanList.size()));
                        mPopupWindow.dismiss();
                        mUnlockLogBean = null;
                        mBeginTime = null;
                        mEndTime = null;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    loadWheelView(v);
                }
            }
        };
        mTvBeginTime.setOnClickListener(tvTimeClick);
        mTvEndTime.setOnClickListener(tvTimeClick);
        btPopupSure.setOnClickListener(tvTimeClick);

    }

    @OnClick({R.id.iv_back, R.id.bt_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bt_search:
                loadPopupWindow();
                mPopupWindow.showAsDropDown(v);
            default:
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        DDLog.i("UnLockRecordActivity.clazz-->>onItemLongClick()  position:" +
                position + " ,id:" + id);
        mUnlockLogBean = mUnlockLogBeanList.get(position);
        mBtSearch.performClick();
        return false;
    }
}
