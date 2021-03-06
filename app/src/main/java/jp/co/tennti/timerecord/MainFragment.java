package jp.co.tennti.timerecord;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.tennti.timerecord.commonUtils.BitmapUtils;
import jp.co.tennti.timerecord.commonUtils.FontUtils;
import jp.co.tennti.timerecord.commonUtils.GeneralUtils;
import jp.co.tennti.timerecord.commonUtils.RandGeneratUtils;
import jp.co.tennti.timerecord.commonUtils.TimeUtils;
import jp.co.tennti.timerecord.contacts.Constants;
import jp.co.tennti.timerecord.daoUtils.MySQLiteOpenHelper;


public class MainFragment extends Fragment {

    private Bitmap mainImage               = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_4444);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(getActivity().getApplicationContext());
        final SQLiteDatabase db = helper.getWritableDatabase();

        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        final Resources resource = getResources();
        if(mainImage != null){
            mainImage.recycle();
        }
        mainImage = BitmapFactory.decodeResource(resource, R.mipmap.fleet_kongou);
        final ImageView imgView = (ImageView)view.findViewById(R.id.contentImageView);

        imgView.setImageDrawable(null);
        imgView.setImageBitmap(null);
        BitmapUtils bu = new BitmapUtils();
        DisplayMetrics displayMetrics = bu.getDisplayMetrics(getContext());
        imgView.setImageBitmap(bu.resize(mainImage,displayMetrics.widthPixels,displayMetrics.heightPixels));
        imgView.setScaleType(ImageView.ScaleType.FIT_XY);

        /************ 有給関連スイッチ start ************/
        final Typeface meiryoType  = FontUtils.getTypefaceFromAssetsZip(getContext(),"font/meiryo_first_level.zip");
        final SwitchCompat allHolidaySwitch = (SwitchCompat)view.findViewById(R.id.allHolidaySwitch);
        allHolidaySwitch.setTypeface(meiryoType);
        final SwitchCompat amHalfHolidaySwitch = (SwitchCompat)view.findViewById(R.id.amHalfHolidaySwitch);
        amHalfHolidaySwitch.setTypeface(meiryoType);
        final SwitchCompat pmHalfHolidaySwitch = (SwitchCompat)view.findViewById(R.id.pmHalfHolidaySwitch);
        pmHalfHolidaySwitch.setTypeface(meiryoType);
        // リスナーをボタンに登録
        allHolidaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //allHolidaySwitch.setChecked(true);
                amHalfHolidaySwitch.setChecked(false);
                pmHalfHolidaySwitch.setChecked(false);
            }
        });
        amHalfHolidaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allHolidaySwitch.setChecked(false);
                //amHalfHolidaySwitch.setChecked(true);
                pmHalfHolidaySwitch.setChecked(false);
            }
        });
        pmHalfHolidaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allHolidaySwitch.setChecked(false);
                amHalfHolidaySwitch.setChecked(false);
                //pmHalfHolidaySwitch.setChecked(true);
            }
        });
        /************ 有給関連スイッチ end ************/

        /************ 登録ボタン start ************/
        // ボタンを設定
        final ImageButton timeCountButton = (ImageButton)view.findViewById(R.id.timeCountButton);
        timeCountButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_times_day_switch));

        final TimeUtils timeUtil = new TimeUtils();
        /** 初期表示時にボタンを非活性にする判定**/
        if ( helper.isCurrentDate(db,timeUtil.getCurrentTableName(),timeUtil.getCurrentYearMonthDay()) ) {
            timeCountButton.setEnabled(false);
            timeCountButton.setColorFilter(Color.argb(100, 0, 0, 0));
            final Cursor cursor = db.rawQuery("SELECT * FROM "+timeUtil.getCurrentTableName()+" WHERE basic_date = ?", new String[]{timeUtil.getCurrentYearMonthDay()});
            try {
                if(cursor != null && cursor.moveToNext()){
                    cursor.moveToFirst();
                    if( cursor.getString(cursor.getColumnIndex("holiday_flag")).equals("1") ){
                        allHolidaySwitch.setChecked(true);
                    }
                    if( cursor.getString(cursor.getColumnIndex("holiday_flag")).equals("2") ){
                        amHalfHolidaySwitch.setChecked(true);
                    }
                    if( cursor.getString(cursor.getColumnIndex("holiday_flag")).equals("3") ){
                        pmHalfHolidaySwitch.setChecked(true);
                    }
                }
            } catch (SQLException e) {
                GeneralUtils.createErrorDialog(getActivity(), "SQL SELECT エラー", "活性判定時のSELECT 処理に失敗しました:" + e.getLocalizedMessage(),"OK");
                Log.e("SQLException SELECT", e.toString());
            } finally {
                cursor.close();
            }
        }


        // リスナーをボタンに登録
        timeCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.beginTransaction();
                try {
                    final RandGeneratUtils randGenerat = new RandGeneratUtils();
                    final TimeUtils timeUtil = new TimeUtils();
                    String overtime          = timeUtil.getTimeDiff(timeUtil.conTargetDateFullSlash(timeUtil.getCurrentDate()));
                    String holidayFlag       = "0";
                    if (allHolidaySwitch.isChecked()) {
                        holidayFlag = Constants.ALL_DAYS_HOLIDAY_FLAG;
                        overtime    = Constants.TIME_ZERO;
                    }
                    if (amHalfHolidaySwitch.isChecked()) {
                        holidayFlag = Constants.AM_HALF_HOLIDAY_FLAG;
                    }
                    if (pmHalfHolidaySwitch.isChecked()) {
                        holidayFlag = Constants.PM_HALF_HOLIDAY_FLAG;
                        overtime    = Constants.TIME_ZERO;
                    }
                    //アカウント名取得
                    TextView accountCd = (TextView)getActivity().findViewById(R.id.accountMail);
                    final SQLiteStatement statement = db.compileStatement("INSERT INTO " + timeUtil.getCurrentTableName() + Constants.INSERT_SQL_VALUES);
                    try {
//                        statement.bindString(1, randGenerat.get());
                        statement.bindString(1, timeUtil.getCurrentYearMonthDay());
                        statement.bindString(2, timeUtil.getCurrentDate());
                        statement.bindString(3, overtime);
                        statement.bindString(4, timeUtil.getCurrentWeekOmit());
                        statement.bindString(5, holidayFlag);
                        statement.bindString(6, accountCd.getText().toString());
                        statement.executeInsert();
                        timeCountButton.setEnabled(false);
                        timeCountButton.setColorFilter(Color.argb(100, 0, 0, 0));
                        // 第3引数は、表示期間（LENGTH_SHORT、または、LENGTH_LONG）
                        Toast.makeText(getActivity(), "現在時刻を登録しました", Toast.LENGTH_SHORT).show();
                    }  catch (SQLException ex) {
                        GeneralUtils.createErrorDialog(getActivity(),"SQL INSERT エラー","insert処理に失敗しました:" + ex.getLocalizedMessage(),"OK");
                        Log.e("SQLException INSERT", ex.toString());
                    } finally {
                        statement.close();
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        });
        /************ 登録ボタン end ************/
        /************ 制御ボタン start ************/
        // ボタンを設定
        final ImageButton controlButton = (ImageButton)view.findViewById(R.id.controlButton);
        controlButton.setImageBitmap(null);
        controlButton.setImageDrawable(null);
        controlButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_permit_switch));

        // リスナーをボタンに登録
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeCountButton.setEnabled(true);
                timeCountButton.setColorFilter(null);
                allHolidaySwitch.setChecked(false);
                amHalfHolidaySwitch.setChecked(false);
                pmHalfHolidaySwitch.setChecked(false);
            }
        });
        /************ 制御ボタン end ************/
        /************ 削除ボタン start ************/
        // ボタンを設定
        final ImageButton deleteButton = (ImageButton)view.findViewById(R.id.deleteButtonMain);
        deleteButton.setImageBitmap(null);
        deleteButton.setImageDrawable(null);
        deleteButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_delete_switch));

        // リスナーをボタンに登録
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ダイアログの生成
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                // アラートダイアログのタイトルを設定します
                alertDialogBuilder.setTitle("指定日削除ダイアログ");
                // アラートダイアログのメッセージを設定します
                final TimeUtils timeUtil = new TimeUtils();
                alertDialogBuilder.setMessage(timeUtil.getCurrentYearMonthDay()+"のデータ削除を行いますがよろしいですか。");
                // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
                alertDialogBuilder.setNeutralButton("実行",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.beginTransaction();
                                try {
                                    final TimeUtils timeUtil = new TimeUtils();
                                    final SQLiteStatement statement = db.compileStatement("DELETE FROM "+timeUtil.getCurrentTableName()+" WHERE basic_date=?");
                                    try {
                                        /**年月の判定 start**/
                                        /**年月の判定 end**/
                                        statement.bindString(1, timeUtil.getCurrentYearMonthDay());
                                        statement.executeUpdateDelete();
                                        Toast.makeText(getActivity(), "対象日付のデータを削除しました", Toast.LENGTH_SHORT).show();
                                    }  catch (SQLException ex) {
                                        GeneralUtils.createErrorDialog(getActivity(),"SQL DELETE エラー","delete処理に失敗しました:" + ex.getLocalizedMessage(),"OK");
                                        Log.e("SQLException DELETE", ex.toString());
                                    } finally {
                                        statement.close();
                                    }
                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }
                            }
                        });
                alertDialogBuilder.setNegativeButton(
                        "cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                // アラートダイアログのキャンセルが可能かどうかを設定します
                alertDialogBuilder.setCancelable(true);
                AlertDialog alertDialog = alertDialogBuilder.create();
                // アラートダイアログを表示します
                alertDialog.show();
            }
        });
        /************ 削除ボタン end ************/
        return view;
    }
    /***
     * フォアグラウンドでなくなった場合に呼び出される
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /***
     * Fragmentの内部のViewリソースの整理を行う
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mainImage != null){
            mainImage.recycle();
            mainImage = null;
        }
        ImageView imgView = (ImageView)getActivity().findViewById(R.id.contentImageView);
        imgView.setImageBitmap(null);
        imgView.setImageDrawable(null);
        /************ 登録ボタン ************/
        ImageButton timeCountButton = (ImageButton)getActivity().findViewById(R.id.timeCountButton);
        timeCountButton.setImageDrawable(null);
        timeCountButton.setImageBitmap(null);
        timeCountButton.setOnClickListener(null);
        /************ 制御ボタン ************/
        ImageButton controlButton = (ImageButton)getActivity().findViewById(R.id.controlButton);
        controlButton.setImageBitmap(null);
        controlButton.setImageDrawable(null);
        controlButton.setOnClickListener(null);
        /************ 削除ボタン ************/
        ImageButton deleteButton = (ImageButton) getActivity().findViewById(R.id.deleteButtonMain);
        deleteButton.setImageBitmap(null);
        deleteButton.setImageDrawable(null);

        BitmapUtils.cleanupView(getView());
    }

    /***
     * Fragmentが破棄される時、最後に呼び出される
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /***
     * Activityの関連付けから外された時に呼び出される
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }
}
