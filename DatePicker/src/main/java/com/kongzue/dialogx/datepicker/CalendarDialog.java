package com.kongzue.dialogx.datepicker;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.datepicker.interfaces.CalendarLabelTextViewCustomization;
import com.kongzue.dialogx.datepicker.interfaces.OnDateSelected;
import com.kongzue.dialogx.datepicker.interfaces.OnMultiDateSelected;
import com.kongzue.dialogx.datepicker.util.Lunar;
import com.kongzue.dialogx.datepicker.view.ArrayWheelAdapter;
import com.kongzue.dialogx.datepicker.view.CalendarLabelTextView;
import com.kongzue.dialogx.datepicker.view.OnWheelChangedListener;
import com.kongzue.dialogx.datepicker.view.TableLayout;
import com.kongzue.dialogx.datepicker.view.WheelView;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.style.MaterialStyle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2021/10/5 10:24
 */
public class CalendarDialog {

    public int errorNotAllowDateTipRes = R.string.error_dialogx_calendardialog_max_multi_select;
    protected BottomDialog bottomDialog;
    protected int maxYear = 2100, minYear = 1900;
    protected int maxYearMonth = 12, minYearMonth = 1;
    protected int maxYearDay = 30, minYearDay = 1;
    protected String yearLabel = "年", monthLabel = "月", dayLabel = "日";
    protected String title;
    protected int selectedYearIndex, selectedMonthIndex, selectedDayIndex;
    protected int selectYearIndex = -1, selectMonthIndex = -1, selectDayIndex = -1;
    protected int selectYearStart = -1, selectMonthStart = -1, selectDayStart = -1;
    protected int selectYearEnd = -1, selectMonthEnd = -1, selectDayEnd = -1;
    protected boolean multiSelect = false;
    protected int maxMultiDay = 1;
    protected boolean showLunarCalendar = false;
    protected CalendarLabelTextViewCustomization calendarLabelTextViewCustomization;

    public static CalendarDialog build() {
        return new CalendarDialog();
    }

    public CalendarDialog setDefaultSelect(int year, int month, int day) {
        selectYearIndex = year - minYear;
        selectMonthIndex = month - 1;
        selectDayIndex = day - 1;

        selectedYearIndex = year - minYear;
        selectedMonthIndex = month - 1;
        selectedDayIndex = day - 1;
        return this;
    }

    public CalendarDialog setDefaultSelect(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        int delta = dayDelta(startYear + "-" + startMonth + "-" + startDay, endYear + "-" + endMonth + "-" + endDay);
        if (delta < 0) {
            selectYearIndex = endYear - minYear;
            selectMonthIndex = endMonth - 1;
            selectDayIndex = endDay - 1;

            selectYearStart = endYear - minYear;
            selectMonthStart = endMonth - 1;
            selectDayStart = endDay;

            selectedYearIndex = endYear - minYear;
            selectedMonthIndex = endMonth - 1;
            selectedDayIndex = endDay - 1;

            selectYearEnd = startYear - minYear;
            selectMonthEnd = startMonth - 1;
            selectDayEnd = startDay;
        } else {
            selectYearIndex = startYear - minYear;
            selectMonthIndex = startMonth - 1;
            selectDayIndex = startDay - 1;

            selectYearStart = startYear - minYear;
            selectMonthStart = startMonth - 1;
            selectDayStart = startDay;

            selectedYearIndex = startYear - minYear;
            selectedMonthIndex = startMonth - 1;
            selectedDayIndex = startDay - 1;

            selectYearEnd = endYear - minYear;
            selectMonthEnd = endMonth - 1;
            selectDayEnd = endDay;
        }
        return this;
    }

    public CalendarDialog cleanDefaultSelect() {
        selectYearIndex = 0;
        selectMonthIndex = 0;
        selectDayIndex = 0;
        return this;
    }

    CalendarLabelTextView selectDayViewCache;
    OnDateSelected onDateSelected;
    OnMultiDateSelected onMultiDateSelected;

    public CalendarDialog show(OnDateSelected onDateSelected) {
        this.onDateSelected = onDateSelected;
        realShow();
        return this;
    }

    public CalendarDialog show(OnMultiDateSelected onMultiDateSelected) {
        this.onMultiDateSelected = onMultiDateSelected;
        setMultiSelect(true);
        realShow();
        return this;
    }

    boolean isShowYearAndMonthSelect;

    private void realShow() {
        bottomDialog = BottomDialog.show(new OnBindView<BottomDialog>(R.layout.dialogx_calendar) {

                    private TextView txtDialogYearAndMonth;
                    private ImageView imgDialogSelectYearAndMonth;
                    private ImageView btnLastMonth;
                    private ImageView btnNextMonth;
                    private LinearLayout boxYearMonthSelector;
                    private WheelView idYear;
                    private WheelView idMonth;
                    private ImageView imgCalendarScreenshot;
                    private LinearLayout boxCalendar;
                    private LinearLayout boxCalendarTitle;
                    private TableLayout tabCalendar;

                    @Override
                    public void onBind(BottomDialog dialog, View v) {
                        bottomDialog = dialog;

                        txtDialogYearAndMonth = v.findViewById(R.id.txt_dialog_year_and_month);
                        imgDialogSelectYearAndMonth = v.findViewById(R.id.img_dialog_select_year_and_month);
                        btnLastMonth = v.findViewById(R.id.btn_last_month);
                        btnNextMonth = v.findViewById(R.id.btn_next_month);
                        boxYearMonthSelector = v.findViewById(R.id.box_year_month_selector);
                        idYear = v.findViewById(R.id.id_year);
                        idMonth = v.findViewById(R.id.id_month);
                        imgCalendarScreenshot = v.findViewById(R.id.img_calendar_screenshot);
                        boxCalendar = v.findViewById(R.id.box_calendar);
                        boxCalendarTitle = v.findViewById(R.id.box_calendar_title);
                        tabCalendar = v.findViewById(R.id.tab_calendar);

                        initDefaultDate();
                        initWheelYearAndMonthPicker();
                        initCalendar();

                        txtDialogYearAndMonth.setTextColor(v.getContext().getResources().getColor((bottomDialog.isLightTheme() ? R.color.black : R.color.white)));
                        imgDialogSelectYearAndMonth.setImageTintList(ColorStateList.valueOf(v.getContext().getResources().getColor((bottomDialog.isLightTheme() ? R.color.black : R.color.white))));
                        btnLastMonth.setImageTintList(ColorStateList.valueOf(v.getContext().getResources().getColor((bottomDialog.isLightTheme() ? R.color.black : R.color.white))));
                        btnNextMonth.setImageTintList(ColorStateList.valueOf(v.getContext().getResources().getColor((bottomDialog.isLightTheme() ? R.color.black : R.color.white))));
                        for (int i = 0; i < boxCalendarTitle.getChildCount(); i++) {
                            TextView weekTitle = (TextView) boxCalendarTitle.getChildAt(i);
                            weekTitle.setTextColor(v.getContext().getResources().getColor(bottomDialog.isLightTheme() ? R.color.black60 : R.color.white70));
                        }

                        View.OnClickListener switchSelectYearMonthOrCalendarClick;
                        txtDialogYearAndMonth.setOnClickListener(switchSelectYearMonthOrCalendarClick = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (boxCalendar.getVisibility() == View.VISIBLE) {
                                    idYear.setCurrentItem(selectYearIndex < yearList.size() ? selectYearIndex : 0);
                                    idMonth.setCurrentItem(selectMonthIndex < monthList.size() ? selectMonthIndex - (getMinMonthDefaultValue() - 1) : 0);

                                    boxCalendar.animate().alpha(0f).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            boxCalendar.setVisibility(View.GONE);
                                        }
                                    });
                                    boxYearMonthSelector.setAlpha(0f);
                                    boxYearMonthSelector.setVisibility(View.VISIBLE);
                                    boxYearMonthSelector.animate().alpha(1f);
                                    imgDialogSelectYearAndMonth.animate().rotation(180).setDuration(100);

                                    btnLastMonth.animate().alpha(0f).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            btnLastMonth.setVisibility(View.GONE);
                                        }
                                    });
                                    btnNextMonth.animate().alpha(0f).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            btnNextMonth.setVisibility(View.GONE);
                                        }
                                    });
                                    isShowYearAndMonthSelect = true;
                                } else {
                                    boxYearMonthSelector.animate().alpha(0f).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            boxYearMonthSelector.setVisibility(View.GONE);
                                        }
                                    });
                                    boxCalendar.setAlpha(0f);
                                    boxCalendar.setVisibility(View.VISIBLE);
                                    boxCalendar.animate().alpha(1f);
                                    imgDialogSelectYearAndMonth.animate().rotation(0).setDuration(100);

                                    btnLastMonth.setVisibility(View.VISIBLE);
                                    btnNextMonth.setVisibility(View.VISIBLE);
                                    btnLastMonth.animate().alpha(1f);
                                    btnNextMonth.animate().alpha(1f);

                                    initCalendar();
                                    isShowYearAndMonthSelect = false;
                                }
                            }
                        });
                        imgDialogSelectYearAndMonth.setOnClickListener(switchSelectYearMonthOrCalendarClick);

                        btnLastMonth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boxCalendar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap screenshot = screenshotView(boxCalendar);
                                        imgCalendarScreenshot.setImageBitmap(screenshot);

                                        imgCalendarScreenshot.setVisibility(View.VISIBLE);
                                        imgCalendarScreenshot.setX(0);

                                        boxCalendar.setX(-boxCalendar.getWidth());

                                        if (selectMonthIndex == 0) {
                                            selectMonthIndex = 11;
                                            selectYearIndex--;
                                        } else {
                                            selectMonthIndex--;
                                        }
                                        initCalendar();

                                        imgCalendarScreenshot.animate().setInterpolator(new DecelerateInterpolator(2f)).x(boxCalendar.getWidth());
                                        boxCalendar.animate().setInterpolator(new DecelerateInterpolator(2f)).x(0);
                                    }
                                });
                            }
                        });

                        btnNextMonth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boxCalendar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap screenshot = screenshotView(boxCalendar);
                                        imgCalendarScreenshot.setImageBitmap(screenshot);

                                        imgCalendarScreenshot.setVisibility(View.VISIBLE);
                                        imgCalendarScreenshot.setX(0);

                                        boxCalendar.setX(boxCalendar.getWidth());

                                        if (selectMonthIndex == 11) {
                                            selectMonthIndex = 0;
                                            selectYearIndex++;
                                        } else {
                                            selectMonthIndex++;
                                        }
                                        initCalendar();

                                        imgCalendarScreenshot.animate().setInterpolator(new DecelerateInterpolator(2f)).x(-boxCalendar.getWidth());
                                        boxCalendar.animate().setInterpolator(new DecelerateInterpolator(2f)).x(0);
                                    }
                                });
                            }
                        });
                    }

                    List<String> yearList;
                    List<String> monthList;

                    private void initWheelYearAndMonthPicker() {
                        yearList = new ArrayList<>();
                        for (int i = minYear; i <= maxYear; i++) {
                            yearList.add(i + yearLabel);
                        }
                        ArrayWheelAdapter<String> yearAdapter = new ArrayWheelAdapter<String>(BottomDialog.getTopActivity(), yearList);
                        yearAdapter.setItemResource(R.layout.default_item_date);
                        yearAdapter.setItemTextResource(R.id.default_item_date_name_tv);
                        yearAdapter.setTextColor(bottomDialog.getResources().getColor(bottomDialog.isLightTheme() ? R.color.black60 : R.color.white70));
                        idYear.setViewAdapter(yearAdapter);
                        idYear.setCurrentItem(selectYearIndex < yearList.size() ? selectYearIndex : 0);
                        idYear.addChangingListener(new OnWheelChangedListener() {
                            @Override
                            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                                selectYearIndex = newValue;
                                initMonthPicker();
                                txtDialogYearAndMonth.setText((selectYearIndex + minYear) + yearLabel + (selectMonthIndex + 1) + monthLabel);
                            }
                        });

                        initMonthPicker();
                    }

                    // 获取当前年的最小月份
                    private int getMinMonthDefaultValue() {
                        if (selectYearIndex == 0) {
                            //min
                            return Math.max(minYearMonth, 1);
                        } else {
                            return 1;
                        }
                    }

                    private void initMonthPicker() {
                        monthList = new ArrayList<>();
                        if (selectYearIndex == 0) {
                            //min
                            for (int i = getMinMonthDefaultValue(); i <= 12; i++) {
                                monthList.add(i + monthLabel);
                            }
                        } else if (selectYearIndex == yearList.size() - 1) {
                            //max
                            for (int i = 1; i <= (maxYearMonth == 0 ? 12 : Math.min(12, maxYearMonth)); i++) {
                                monthList.add(i + monthLabel);
                            }
                        } else {
                            for (int i = 1; i <= 12; i++) {
                                monthList.add(i + monthLabel);
                            }
                        }
                        ArrayWheelAdapter<String> monthAdapter = new ArrayWheelAdapter<String>(BottomDialog.getTopActivity(), monthList);
                        monthAdapter.setItemResource(R.layout.default_item_date);
                        monthAdapter.setItemTextResource(R.id.default_item_date_name_tv);
                        monthAdapter.setTextColor(bottomDialog.getResources().getColor(bottomDialog.isLightTheme() ? R.color.black60 : R.color.white70));
                        idMonth.setViewAdapter(monthAdapter);
                        idMonth.setCurrentItem(selectMonthIndex < monthList.size() ? selectMonthIndex : 0);
                        idMonth.addChangingListener(new OnWheelChangedListener() {
                            @Override
                            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                                selectMonthIndex = newValue + (getMinMonthDefaultValue() - 1);
                                txtDialogYearAndMonth.setText((selectYearIndex + minYear) + yearLabel + (selectMonthIndex + 1) + monthLabel);
                            }
                        });
                    }

                    private void initCalendar() {
                        //默认选中的日期超限（低于最低允许日期或高于最高允许日期）的情况
                        int minDelta = dayDelta(minYear + "-" + minYearMonth + "-" + minYearDay,
                                (minYear + selectYearIndex) + "-" +
                                        (selectMonthIndex + 1) + "-" +
                                        (selectDayIndex + 1));
                        int maxDelta = dayDelta((minYear + selectYearIndex) + "-" +
                                        (selectMonthIndex + 1) + "-" +
                                        (selectDayIndex + 1),
                                maxYear + "-" + maxYearMonth + "-" + maxYearDay);
                        if (minDelta < 0) {
                            setDefaultSelect(minYear, minYearMonth, minYearDay);
                        }
                        if (maxDelta < 0) {
                            setDefaultSelect(maxYear, maxYearMonth, maxYearDay);
                        }
                        txtDialogYearAndMonth.setText((selectYearIndex + minYear) + yearLabel + (selectMonthIndex + 1) + monthLabel);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, (selectYearIndex + minYear));
                        calendar.set(Calendar.MONTH, selectMonthIndex);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);

                        boolean isFirstSunday = (calendar.getFirstDayOfWeek() == Calendar.SUNDAY);
                        int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                        tabCalendar.removeAllViews();
                        for (int i = 0; i < weekDay; i++) {
                            tabCalendar.addView(new TextView(tabCalendar.getContext()));
                        }

                        int lastDay = getLastDayOfMonth((selectYearIndex + minYear), selectMonthIndex + 1);
                        for (int i = 1; i <= lastDay; i++) {
                            CalendarLabelTextView dayView = new CalendarLabelTextView(tabCalendar.getContext());
                            if (calendarLabelTextViewCustomization != null) {
                                dayView = calendarLabelTextViewCustomization.getView(dayView);
                            }

                            dayView.setLight(bottomDialog.isLightTheme());
                            dayView.setGravity(Gravity.CENTER);
                            dayView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                            dayView.setText(String.valueOf(i));
                            dayView.setTag(i);

                            if (showLunarCalendar) {
                                Calendar dayCalendar = Calendar.getInstance();
                                dayCalendar.set(Calendar.YEAR, (selectYearIndex + minYear));
                                dayCalendar.set(Calendar.MONTH, selectMonthIndex);
                                dayCalendar.set(Calendar.DAY_OF_MONTH, i);
                                dayView.setLabel(new Lunar(dayCalendar).getLunar());
                            } else {
                                dayView.setLabel(null);
                            }

                            if (i == nowDay && selectMonthIndex == nowMonth && selectYearIndex == nowYear - minYear) {
                                dayView.setToday(true);
                                selectDayViewCache = dayView;
                            } else {
                                dayView.setToday(false);
                            }

                            dayView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (isMultiSelect()) {
                                        int i = v.getTag() != null ? (int) v.getTag() : -1;
                                        selectDayIndex = i;
                                        if (selectYearStart == -1 && selectMonthStart == -1 && selectDayStart == -1) {
                                            selectYearStart = selectYearIndex;
                                            selectMonthStart = selectMonthIndex;
                                            selectDayStart = i;
                                            refreshCalendarViews();
                                        } else {
                                            if (v.getAlpha() != 1f) {
                                                PopTip.show(errorNotAllowDateTipRes);
                                                return;
                                            }
                                            int delta = dayDelta((minYear + selectYearStart) + "-" + (selectMonthStart + 1) + "-" + selectDayStart,
                                                    (minYear + selectYearIndex) + "-" + (selectMonthIndex + 1) + "-" + i);

                                            if (delta == 0) {
                                                if (selectYearEnd == -1 && selectMonthEnd == -1 && selectDayEnd == -1) {
                                                    //取消选择
                                                    ((CalendarLabelTextView) v).setSelect(false);
                                                }
                                                selectYearEnd = -1;
                                                selectMonthEnd = -1;
                                                selectDayEnd = -1;
                                                selectYearStart = -1;
                                                selectMonthStart = -1;
                                                selectDayStart = -1;
                                                refreshCalendarViews();
                                            } else {
                                                if (selectYearEnd == -1 && selectMonthEnd == -1 && selectDayEnd == -1) {
                                                    if (delta < 0) {
                                                        selectYearEnd = selectYearStart;
                                                        selectMonthEnd = selectMonthStart;
                                                        selectDayEnd = selectDayStart;

                                                        selectYearStart = selectYearIndex;
                                                        selectMonthStart = selectMonthIndex;
                                                        selectDayStart = i;
                                                    } else {
                                                        selectYearEnd = selectYearIndex;
                                                        selectMonthEnd = selectMonthIndex;
                                                        selectDayEnd = i;
                                                    }
                                                } else {
                                                    selectYearEnd = -1;
                                                    selectMonthEnd = -1;
                                                    selectDayEnd = -1;
                                                    selectYearStart = selectYearIndex;
                                                    selectMonthStart = selectMonthIndex;
                                                    selectDayStart = i;
                                                }
                                                refreshCalendarViews();
                                            }
                                        }
                                    } else {
                                        if (v.getAlpha() != 1f) {
                                            PopTip.show(errorNotAllowDateTipRes);
                                            return;
                                        }
                                        if (selectDayViewCache != null) selectDayViewCache.setSelect(false);
                                        selectDayIndex = (int) v.getTag() - 1;
                                        selectDayViewCache = (CalendarLabelTextView) v;
                                        selectDayViewCache.setSelect(true);

                                        selectedMonthIndex = selectMonthIndex;
                                        selectedYearIndex = selectYearIndex;
                                        selectedDayIndex = selectDayIndex;
                                    }
                                }
                            });
                            dayView.setOnTouchListener(new View.OnTouchListener() {

                                boolean isTouch;
                                float x;

                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    switch (event.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            x = event.getX();
                                            isTouch = true;
                                            break;
                                        case MotionEvent.ACTION_CANCEL:
                                        case MotionEvent.ACTION_UP:
                                            if (isTouch) {
                                                if (event.getX() - x > bottomDialog.dip2px(10)) {
                                                    btnLastMonth.callOnClick();
                                                }
                                                if (event.getX() - x < -bottomDialog.dip2px(10)) {
                                                    btnNextMonth.callOnClick();
                                                }
                                            }
                                            isTouch = false;
                                            break;
                                    }
                                    return false;
                                }
                            });
                            tabCalendar.addView(dayView);
                        }

                        refreshCalendarViews();

                        //处理默认选中逻辑
                        for (int c = 0; c < tabCalendar.getChildCount(); c++) {
                            View childLabelView = tabCalendar.getChildAt(c);
                            if (childLabelView instanceof CalendarLabelTextView) {
                                CalendarLabelTextView labelView = (CalendarLabelTextView) childLabelView;
                                int day = labelView.getTag() != null ? (int) labelView.getTag() : -1;

                                //如果是之前选择的日期，放大+变红
                                if (isMultiSelect()) {
                                    //ToDo

                                } else {
                                    if ((day == selectDayIndex + 1) && selectMonthIndex == selectedMonthIndex && selectYearIndex == selectedYearIndex) {
                                        selectDayIndex = (int) labelView.getTag() - 1;
                                        selectDayViewCache = (CalendarLabelTextView) labelView;
                                        labelView.setSelect(true);
                                    }
                                }
                            }
                        }
                    }

                    private void refreshCalendarViews() {
                        if (tabCalendar == null) return;
                        for (int c = 0; c < tabCalendar.getChildCount(); c++) {
                            View childLabelView = tabCalendar.getChildAt(c);
                            if (childLabelView instanceof CalendarLabelTextView) {
                                CalendarLabelTextView labelView = (CalendarLabelTextView) childLabelView;
                                int day = childLabelView.getTag() != null ? (int) childLabelView.getTag() : -1;

                                int minDelta = dayDelta(minYear + "-" + minYearMonth + "-" + minYearDay,
                                        (minYear + selectYearIndex) + "-" + (selectMonthIndex + 1) + "-" + day);
                                int maxDelta = dayDelta((minYear + selectYearIndex) + "-" + (selectMonthIndex + 1) + "-" + day,
                                        maxYear + "-" + maxYearMonth + "-" + maxYearDay);
                                if (minDelta < 0) {
                                    labelView.setAlpha(0.2f);
                                    continue;
                                }
                                if (maxDelta < 0) {
                                    labelView.setAlpha(0.2f);
                                    continue;
                                }

                                if (selectYearStart == -1 && selectMonthStart == -1 && selectDayStart == -1) {
                                    labelView.setAlpha(1f);
                                } else {
                                    int startDelta = dayDelta((minYear + selectYearStart) + "-" + (selectMonthStart + 1) + "-" + selectDayStart,
                                            (minYear + selectYearIndex) + "-" + (selectMonthIndex + 1) + "-" + day);
                                    int endDelta = dayDelta((minYear + selectYearIndex) + "-" + (selectMonthIndex + 1) + "-" + day,
                                            (minYear + selectYearEnd) + "-" + (selectMonthEnd + 1) + "-" + selectDayEnd);

                                    if (selectYearEnd == -1 && selectMonthEnd == -1 && selectDayEnd == -1) {
                                        if (selectYearStart == selectYearIndex && selectMonthStart == selectMonthIndex && selectDayStart == day) {
                                            labelView.setSelect(true);
                                            labelView.setSection(2);
                                            selectDayIndex = (int) labelView.getTag() - 1;
                                            selectDayViewCache = labelView;
                                        } else {
                                            labelView.setSelect(false);
                                        }
                                        if (getMaxMultiDay() > 1 && Math.abs(startDelta) > getMaxMultiDay() - 1) {
                                            labelView.setAlpha(0.2f);
                                        } else {
                                            labelView.setAlpha(1f);
                                        }
                                    } else {
                                        if (startDelta == 0) {
                                            labelView.setSelect(true);
                                            labelView.setSection(-1);

                                            selectDayIndex = (int) labelView.getTag() - 1;
                                            selectDayViewCache = labelView;

                                            labelView.setAlpha(1f);
                                        } else if (startDelta > 0) {
                                            if (endDelta > 0) {
                                                labelView.setSelect(true);
                                                labelView.setSection(0);

                                                selectDayIndex = (int) labelView.getTag() - 1;
                                                selectDayViewCache = labelView;
                                            } else if (endDelta == 0) {
                                                labelView.setSelect(true);
                                                labelView.setSection(1);

                                                selectDayIndex = (int) labelView.getTag() - 1;
                                                selectDayViewCache = labelView;
                                            } else {
                                                labelView.setSelect(false);
                                            }
                                        }
                                        labelView.setAlpha(1f);
                                    }
                                }
                            }
                        }
                    }
                })
                .setCancelable(true)
                .setAllowInterceptTouch(false);
        if (DialogX.globalStyle instanceof MaterialStyle) {
            bottomDialog.setOkButton(R.string.dialogx_date_picker_ok_button, new OnDialogButtonClickListener<BottomDialog>() {
                @Override
                public boolean onClick(BottomDialog baseDialog, View v) {
                    if (isShowYearAndMonthSelect) {
                        View txtDialogYearAndMonth = baseDialog.getDialogImpl().boxContent.findViewById(R.id.txt_dialog_year_and_month);
                        if (txtDialogYearAndMonth != null) txtDialogYearAndMonth.callOnClick();
                        return true;
                    }

                    if (onDateSelected != null) {
                        int year = minYear + selectedYearIndex;
                        int month = 1 + selectedMonthIndex;
                        int day = 1 + selectedDayIndex;

                        onDateSelected.onSelect(year + "-" + format(month) + "-" + format(day),
                                year,
                                month,
                                day
                        );
                    }
                    if (onMultiDateSelected != null) {
                        int startYear = minYear + selectYearStart;
                        int startMonth = 1 + selectMonthStart;
                        int startDay = selectDayStart;

                        int endYear = minYear + selectYearEnd;
                        int endMonth = 1 + selectMonthEnd;
                        int endDay = selectDayEnd;

                        onMultiDateSelected.onSelect((selectYearStart == -1 && selectMonthStart == -1 && startDay == -1) ? "" : startYear + "-" + format(startMonth) + "-" + format(startDay),
                                (selectYearEnd == -1 && selectMonthEnd == -1 && endDay == -1) ? "" : endYear + "-" + format(endMonth) + "-" + format(endDay),
                                startYear, startMonth, startDay,
                                endYear, endMonth, endDay
                        );
                    }
                    return false;
                }
            }).setCancelButton(R.string.dialogx_date_picker_dialog_cancel, new OnDialogButtonClickListener<BottomDialog>() {
                @Override
                public boolean onClick(BottomDialog baseDialog, View v) {
                    if (isShowYearAndMonthSelect) {
                        View txtDialogYearAndMonth = baseDialog.getDialogImpl().boxContent.findViewById(R.id.txt_dialog_year_and_month);
                        if (txtDialogYearAndMonth != null) txtDialogYearAndMonth.callOnClick();
                        return true;
                    }
                    if (onDateSelected != null) {
                        onDateSelected.onCancel();
                    }
                    return false;
                }
            });
        } else {
            bottomDialog.setCancelButton(R.string.dialogx_date_picker_ok_button, new OnDialogButtonClickListener<BottomDialog>() {
                @Override
                public boolean onClick(BottomDialog baseDialog, View v) {
                    if (isShowYearAndMonthSelect) {
                        View txtDialogYearAndMonth = baseDialog.getDialogImpl().boxContent.findViewById(R.id.txt_dialog_year_and_month);
                        if (txtDialogYearAndMonth != null) txtDialogYearAndMonth.callOnClick();
                        return true;
                    }

                    if (onDateSelected != null) {
                        int year = minYear + selectedYearIndex;
                        int month = 1 + selectedMonthIndex;
                        int day = 1 + selectedDayIndex;

                        onDateSelected.onSelect(year + "-" + format(month) + "-" + format(day),
                                year,
                                month,
                                day
                        );
                    }
                    if (onMultiDateSelected != null) {
                        int startYear = minYear + selectYearStart;
                        int startMonth = 1 + selectMonthStart;
                        int startDay = selectDayStart;

                        int endYear = minYear + selectYearEnd;
                        int endMonth = 1 + selectMonthEnd;
                        int endDay = selectDayEnd;

                        onMultiDateSelected.onSelect((selectYearStart == -1 && selectMonthStart == -1 && startDay == -1) ? "" : startYear + "-" + format(startMonth) + "-" + format(startDay),
                                (selectYearEnd == -1 && selectMonthEnd == -1 && endDay == -1) ? "" : endYear + "-" + format(endMonth) + "-" + format(endDay),
                                startYear, startMonth, startDay,
                                endYear, endMonth, endDay
                        );
                    }
                    return false;
                }
            });
        }
    }

    //API-23+ 支持水波纹效果
    @Deprecated
    private Drawable getDefaultCalendarItemBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int[] attrs = new int[]{android.R.attr.selectableItemBackgroundBorderless};
            TypedArray ta = BottomDialog.getTopActivity().obtainStyledAttributes(attrs);
            Drawable mDefaultFocusHighlightCache = ta.getDrawable(0);
            ta.recycle();
            return mDefaultFocusHighlightCache;
        }
        return null;
    }

    //记录今天日期
    private int nowYear, nowMonth, nowDay;

    private void initDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        nowYear = calendar.get(Calendar.YEAR);
        if (selectYearIndex == -1) {
            selectYearIndex = calendar.get(Calendar.YEAR) - minYear;
        }
        nowMonth = calendar.get(Calendar.MONTH);
        if (selectMonthIndex == -1) {
            selectMonthIndex = calendar.get(Calendar.MONTH);
        }
        nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (selectDayIndex == -1) {
            selectDayIndex = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        }
    }

    private int getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    private Bitmap screenshotView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public int getMaxYear() {
        return maxYear;
    }

    public int getMinYear() {
        return minYear;
    }

    public CalendarDialog setMaxYear(int maxYear) {
        this.maxYear = maxYear;
        return this;
    }

    public CalendarDialog setMinYear(int minYear) {
        this.minYear = minYear;
        return this;
    }

    public int getMaxYearMonth() {
        return maxYearMonth;
    }

    public CalendarDialog setMaxYearMonth(int maxYearMonth) {
        this.maxYearMonth = maxYearMonth;
        return this;
    }

    public int getMinYearMonth() {
        return minYearMonth;
    }

    public CalendarDialog setMinYearMonth(int minYearMonth) {
        this.minYearMonth = minYearMonth;
        return this;
    }

    public int getMaxYearDay() {
        return maxYearDay;
    }

    public CalendarDialog setMaxYearDay(int maxYearDay) {
        this.maxYearDay = maxYearDay;
        return this;
    }

    public int getMinYearDay() {
        return minYearDay;
    }

    public CalendarDialog setMinYearDay(int minYearDay) {
        this.minYearDay = minYearDay;
        return this;
    }

    public CalendarDialog setMinTime(int minYear, int minMonth, int minDay) {
        this.minYear = minYear;
        this.minYearMonth = minMonth;
        this.minYearDay = minDay;
        return this;
    }

    public CalendarDialog setMaxTime(int maxYear, int maxMonth, int maxDay) {
        this.maxYear = maxYear;
        this.maxYearMonth = maxMonth;
        this.maxYearDay = maxDay;
        return this;
    }


    public String getYearLabel() {
        return yearLabel;
    }

    public CalendarDialog setYearLabel(String yearLabel) {
        this.yearLabel = yearLabel;
        return this;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public CalendarDialog setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
        return this;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public CalendarDialog setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
        return this;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public CalendarDialog setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
        return this;
    }

    public int getMaxMultiDay() {
        return maxMultiDay;
    }

    public CalendarDialog setMaxMultiDay(int maxMultiDay) {
        this.maxMultiDay = maxMultiDay;
        return this;
    }

    private static int dayDelta(String origin, String newDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = df.parse(origin);
            Date date2 = df.parse(newDate);
            return (int) ((date2.getTime() - date1.getTime()) / 86400000);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    boolean doubleDateFormat;

    public boolean isDoubleDateFormat() {
        return doubleDateFormat;
    }

    public CalendarDialog setDoubleDateFormat(boolean doubleDateFormat) {
        this.doubleDateFormat = doubleDateFormat;
        return this;
    }

    private String format(int day) {
        if (isDoubleDateFormat()) {
            if (day < 10) {
                return "0" + day;
            }
        }
        return String.valueOf(day);
    }

    public BottomDialog getDialog() {
        return bottomDialog;
    }

    public int getErrorNotAllowDateTipRes() {
        return errorNotAllowDateTipRes;
    }

    public CalendarDialog setErrorNotAllowDateTipRes(int errorNotAllowDateTipRes) {
        this.errorNotAllowDateTipRes = errorNotAllowDateTipRes;
        return this;
    }

    public boolean isShowLunarCalendar() {
        return showLunarCalendar;
    }

    public CalendarDialog setShowLunarCalendar(boolean showLunarCalendar) {
        this.showLunarCalendar = showLunarCalendar;
        return this;
    }

    public CalendarLabelTextViewCustomization getCalendarLabelTextViewCustomization() {
        return calendarLabelTextViewCustomization;
    }

    public CalendarDialog setCalendarLabelTextViewCustomization(CalendarLabelTextViewCustomization calendarLabelTextViewCustomization) {
        this.calendarLabelTextViewCustomization = calendarLabelTextViewCustomization;
        return this;
    }
}
