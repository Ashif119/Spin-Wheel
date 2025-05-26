package com.solution.easypay.xyz.SpinWheel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.solution.easypay.xyz.Activities.GetSpinWheelResponse;
import com.solution.easypay.xyz.Activities.GetWinngspinDataResponse;
import com.solution.easypay.xyz.Activities.WalletSpinBalanceAdapter;
import com.solution.easypay.xyz.Api.Object.BalanceData;
import com.solution.easypay.xyz.Api.Object.BalanceType;
import com.solution.easypay.xyz.Api.Response.BalanceResponse;
import com.solution.easypay.xyz.Api.Response.LoginResponse;
import com.solution.easypay.xyz.ApiHits.ApiClient;
import com.solution.easypay.xyz.ApiHits.ApplicationConstant;
import com.solution.easypay.xyz.ApiHits.EndPointInterface;
import com.solution.easypay.xyz.ApiHits.UtilMethods;
import com.solution.easypay.xyz.BuildConfig;
import com.solution.easypay.xyz.R;
import com.solution.easypay.xyz.Util.AppPreferences;
import com.solution.easypay.xyz.Util.CustomAlertDialog;
import com.solution.easypay.xyz.Util.CustomLoader;
import com.solution.easypay.xyz.Util.SpinWheel.LuckyItem;
import com.solution.easypay.xyz.Util.SpinWheel.LuckyWheelView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpinWheelActivity extends AppCompatActivity {
    List<LuckyItem> data = new ArrayList<>();
    private CustomAlertDialog mCustomAlertDialog;
    private CustomLoader loader;
    private AppPreferences mAppPreferences;
    TextView coinsTv, spinCount, spinCountNo, timer, spinConditionTittle;
    View mainView, topView;
    ImageView closeBtn, spinTitle;
    private boolean isActivityOpen = false;
    private AppPreferences preferencesService;
    int indexStop = 0;
    private LoginResponse mLoginDataResponse;
    private View playBtn;
    int stopCoin;
    private String deviceId, deviceSerialNum;
    private CountDownTimer countDownTimer;
    int[] spinImage = new int[]{R.drawable.spin_coin1, R.drawable.spin_coin2, R.drawable.spin_coin3, R.drawable.spin_coin4, R.drawable.spin_coin5, R.drawable.spin_coin6, R.drawable.spin_coin7, R.drawable.spin_coin8, R.drawable.spin_coin9,
            R.drawable.spin_coin10};
    LuckyWheelView luckyWheelView;
    private GetWinngspinDataResponse mGetSpinDataResponse;
    private View bgView, spinView;
    private BalanceResponse balanceCheckResponse;
    private ArrayList<BalanceType> mBalanceTypes = new ArrayList<>();
    private ArrayList<BalanceType> mBalanceTypesForMoveFund = new ArrayList<>();
    TextToSpeech tts;
    private RecyclerView walletRecyclerView;
    private WalletSpinBalanceAdapter mWalletBalanceAdapter;
    private View spinBonusView, spinCountView;
    private TextView name, amount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin_wheel);
        mCustomAlertDialog = new CustomAlertDialog(this);
        loader = new CustomLoader(this, R.style.TransparentTheme);
        mAppPreferences = new AppPreferences(this);
        if (UtilMethods.INSTANCE.mLoginResponse == null) {
            UtilMethods.INSTANCE.mLoginResponse = new Gson().fromJson(mAppPreferences.getString(ApplicationConstant.INSTANCE.LoginPref), LoginResponse.class);
        }
        mLoginDataResponse = UtilMethods.INSTANCE.mLoginResponse;
        deviceId = mAppPreferences.getNonRemovalString(ApplicationConstant.INSTANCE.DeviceIdPref);
        deviceSerialNum = mAppPreferences.getNonRemovalString(ApplicationConstant.INSTANCE.DeviceSerialNumberPref);
        if (!mAppPreferences.getBoolean(ApplicationConstant.INSTANCE.voiceEnablePref)) {
            tts = new TextToSpeech(this, null);
        }
        coinsTv = findViewById(R.id.coins);
        mainView = findViewById(R.id.mainView);
        topView = findViewById(R.id.topView);
        spinConditionTittle = findViewById(R.id.spinConditionTittle);
//        mainView.setVisibility(View.GONE);
        spinCount = findViewById(R.id.spinCount);
        spinCountNo = findViewById(R.id.spinCountNo);

        bgView = findViewById(R.id.bgView);
        spinView = findViewById(R.id.spinView);
        closeBtn = findViewById(R.id.closebtn);
        spinTitle = findViewById(R.id.spinTitle);
        walletRecyclerView = findViewById(R.id.walletRecyclerView);
        spinBonusView = findViewById(R.id.spinBonusView);
        spinCountView = findViewById(R.id.spinCountView);
        name = findViewById(R.id.name);
        amount = findViewById(R.id.amount);
        walletRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWalletBalanceAdapter = new WalletSpinBalanceAdapter(this, mBalanceTypes, R.layout.adapter_spin_wallet_balance ,mLoginDataResponse, loader);
        walletRecyclerView.setAdapter(mWalletBalanceAdapter);
        isActivityOpen = true;
        preferencesService = new AppPreferences(this);
        luckyWheelView = (LuckyWheelView) findViewById(R.id.luckyWheel);
//        setSpinnerData();
        playBtn = findViewById(R.id.play);
//        playBtn.setVisibility(View.GONE);
        closeBtn.setOnClickListener(v -> onBackPressed());
        getBalanceData(this);

        playBtn.setOnClickListener(v -> {
            getWinngspinData(this);

        });
        getSpinWheelData(this);
    }

    private void getBalanceData(SpinWheelActivity mActivity) {
        UtilMethods.INSTANCE.BalancecheckNew(this, mCustomAlertDialog, 0, mAppPreferences, mLoginDataResponse, tts, object -> {
            UtilMethods.INSTANCE.balanceCheckResponse = (BalanceResponse) object;
            balanceCheckResponse = UtilMethods.INSTANCE.balanceCheckResponse;
            if (balanceCheckResponse != null && balanceCheckResponse.getBalanceData() != null) {

                showBalanceData();
            }
        });
    }

    private void showBalanceData() {
        mBalanceTypes.clear();
        if (balanceCheckResponse != null && balanceCheckResponse.getBalanceData() != null) {
            BalanceData mBalanceData = balanceCheckResponse.getBalanceData();
            if (mBalanceData.getIsUBalance() || mBalanceData.isUBalanceFund()) {
                String walletName = "SuperCoins Wallet";
                if (mBalanceData.getUtilityWalletName() != null &&
                        !mBalanceData.getUtilityWalletName().isEmpty()) {
                    walletName = mBalanceData.getUtilityWalletName() + " Wallet";
                }
                mBalanceTypes.add(new BalanceType(walletName, mBalanceData.getuBalance()));
            }
            spinBonusView.setVisibility(View.GONE);

            spinCount.setText(mBalanceData.getRegIDWalletName() + " Count");
            Double D =mBalanceData.getIdBalnace();
            int i = Integer.valueOf(D.intValue());
            spinCountNo.setText(i+"");

            walletRecyclerView.setAdapter(mWalletBalanceAdapter);
            mWalletBalanceAdapter.notifyDataSetChanged();
        } else {

            if (UtilMethods.INSTANCE.balanceCheckResponse != null && UtilMethods.INSTANCE.balanceCheckResponse.getBalanceData() != null) {
                balanceCheckResponse = UtilMethods.INSTANCE.balanceCheckResponse;
                if (balanceCheckResponse != null && balanceCheckResponse.getBalanceData() != null) {
                    showBalanceData();
                }
            } else {
                balanceCheckResponse = new Gson().fromJson(mAppPreferences.getString(ApplicationConstant.INSTANCE.balancePref), BalanceResponse.class);
                if (balanceCheckResponse != null && balanceCheckResponse.getBalanceData() != null) {
                    UtilMethods.INSTANCE.balanceCheckResponse = balanceCheckResponse;
                    showBalanceData();
                } else {
                    UtilMethods.INSTANCE.Balancecheck(this, loader, mLoginDataResponse, mAppPreferences, object -> {
                        balanceCheckResponse = (BalanceResponse) object;
                        if (balanceCheckResponse != null && balanceCheckResponse.getBalanceData() != null) {
                            showBalanceData();
                        }
                    });
                }
            }


        }
    }

    private void showTimer(long time) {

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (countDownTimer == null) {

            countDownTimer = new CountDownTimer(time, 1000) {

                public void onTick(long millisUntilFinished) {

                    String text = String.format(Locale.getDefault(), "%02d : %02d",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);

                    if (timer != null) {
                        timer.setText(text);
                    }


                }

                public void onFinish() {
                    playBtn.setVisibility(View.GONE);
//                    getSpinData(SpinWheelActivity.this);
                }
            };

            countDownTimer.start();
        }
    }

    private void setSpinnerData() {


        LuckyItem luckyItem1 = new LuckyItem();
        luckyItem1.text = "1";
        luckyItem1.icon = R.drawable.spin_coin1;
        luckyItem1.color = getResources().getColor(R.color.reddishBrown);
        data.add(luckyItem1);

        LuckyItem luckyItem2 = new LuckyItem();
        luckyItem2.text = "5";
        luckyItem2.icon = R.drawable.spin_coin2;
        luckyItem2.color = getResources().getColor(R.color.blue);
        data.add(luckyItem2);

        LuckyItem luckyItem3 = new LuckyItem();
        luckyItem3.text = "10";
        luckyItem3.icon = R.drawable.spin_coin3;
        luckyItem3.color = getResources().getColor(R.color.green);
        data.add(luckyItem3);

        LuckyItem luckyItem4 = new LuckyItem();
        luckyItem4.text = "25";
        luckyItem4.icon = R.drawable.spin_coin4;
        luckyItem4.color = getResources().getColor(R.color.color_purple);
        data.add(luckyItem4);

        LuckyItem luckyItem5 = new LuckyItem();
        luckyItem5.text = "35";
        luckyItem5.icon = R.drawable.spin_coin5;
        luckyItem5.color = getResources().getColor(R.color.color_orange);
        data.add(luckyItem5);

        LuckyItem luckyItem6 = new LuckyItem();
        luckyItem6.text = "40";
        luckyItem6.icon = R.drawable.spin_coin6;
        luckyItem6.color = getResources().getColor(R.color.darkGreen);
        data.add(luckyItem6);

        LuckyItem luckyItem7 = new LuckyItem();
        luckyItem7.text = "50";
        luckyItem7.icon = R.drawable.spin_coin7;
        luckyItem7.color = getResources().getColor(R.color.color_pink);
        data.add(luckyItem7);

        LuckyItem luckyItem8 = new LuckyItem();
        luckyItem8.text = "70";
        luckyItem8.icon = R.drawable.spin_coin8;
        luckyItem8.color = getResources().getColor(R.color.dark_blue);
        data.add(luckyItem8);


        LuckyItem luckyItem9 = new LuckyItem();
        luckyItem9.text = "85";
        luckyItem9.icon = R.drawable.spin_coin9;
        luckyItem9.color = getResources().getColor(R.color.color_deep_purple);
        data.add(luckyItem9);

        LuckyItem luckyItem10 = new LuckyItem();
        luckyItem10.text = "100";
        luckyItem10.icon = R.drawable.spin_coin10;
        luckyItem10.color = getResources().getColor(R.color.color_blue_grey);
        data.add(luckyItem10);

        luckyWheelView.setData(data);
        luckyWheelView.setRound(getRandomRound());


    }

    private void getSpinWheelData(SpinWheelActivity mActivity) {
        try {
            if (UtilMethods.INSTANCE.isNetworkAvialable(this)) {
                try {
                    loader.show();
                    loader.setCancelable(false);
                    EndPointInterface git = ApiClient.getClient().create(EndPointInterface.class);
//                    {"userID":0,"sessionID":0,"session":null,"appid":null,"imei":null,"regKey":null,"version":null,"serialNo":null,"loginTypeID":0}
                    HashMap<String, String> map = new HashMap<>();
                    map.put("userID", mLoginDataResponse.getData().getUserID());
                    map.put("sessionID", mLoginDataResponse.getData().getSessionID());
                    map.put("session", mLoginDataResponse.getData().getSession());
                    map.put("appid", ApplicationConstant.INSTANCE.APP_ID);
                    map.put("imei", deviceId);
                    map.put("regKey", "");
                    map.put("version", BuildConfig.VERSION_NAME);
                    map.put("serialNo", deviceSerialNum);
                    map.put("loginTypeID", String.valueOf(mLoginDataResponse.getData().getLoginTypeID()));
                    Call<GetSpinWheelResponse> call = git.GetSpinWheel(map);
                    call.enqueue(new Callback<GetSpinWheelResponse>() {
                        @Override
                        public void onResponse(Call<GetSpinWheelResponse> call, Response<GetSpinWheelResponse> response) {
                            loader.dismiss();
                            if (response.isSuccessful()) {
                                GetSpinWheelResponse dataRes = response.body();
                                if (dataRes != null && dataRes.getSpinWheel() != null) {
                                    setSpinnerData(dataRes.getSpinWheel());

                                }
                            } else {
                                UtilMethods.INSTANCE.Error(mActivity, response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<GetSpinWheelResponse> call, Throwable t) {
                            try {
                                loader.dismiss();
                                if (t.getMessage() != null && !t.getMessage().isEmpty()) {

                                    if (t.getMessage().contains("No address associated with hostname")) {
                                        UtilMethods.INSTANCE.NetworkError(mActivity);

                                    } else {
                                        UtilMethods.INSTANCE.Error(mActivity, t.getMessage());

                                    }

                                } else {
                                    UtilMethods.INSTANCE.Error(mActivity, getString(R.string.something_error));

                                }
                            } catch (IllegalStateException ise) {
                                UtilMethods.INSTANCE.Error(mActivity, ise.getMessage());

                            }
                        }
                    });
                } catch (Exception ex) {
                    loader.dismiss();
                    UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());

                }
//                setSpinnerData(dataRes.getPoint());
            }

        } catch (Exception ex) {
            loader.dismiss();
            UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());

        }

    }

    private void getWinngspinData(SpinWheelActivity mActivity) {
        try {
            if (UtilMethods.INSTANCE.isNetworkAvialable(this)) {
                try {
                    loader.show();
                    loader.setCancelable(false);
                    EndPointInterface git = ApiClient.getClient().create(EndPointInterface.class);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("userID", mLoginDataResponse.getData().getUserID());
                    map.put("sessionID", mLoginDataResponse.getData().getSessionID());
                    map.put("session", mLoginDataResponse.getData().getSession());
                    map.put("appid", ApplicationConstant.INSTANCE.APP_ID);
                    map.put("imei", deviceId);
                    map.put("regKey", "");
                    map.put("version", BuildConfig.VERSION_NAME);
                    map.put("serialNo", deviceSerialNum);
                    map.put("loginTypeID", String.valueOf(mLoginDataResponse.getData().getLoginTypeID()));
                    Call<GetWinngspinDataResponse> call = git.GetWinngspin(map);
                    call.enqueue(new Callback<GetWinngspinDataResponse>() {
                        @Override
                        public void onResponse(Call<GetWinngspinDataResponse> call, Response<GetWinngspinDataResponse> response) {
                            loader.dismiss();
                            if (response.isSuccessful()) {
                                if (response != null) {
                                    try {
                                        if (response.body() != null) {
                                            mGetSpinDataResponse = response.body();
                                            if (mGetSpinDataResponse.getStatuscode() == 1) {
                                                if (mGetSpinDataResponse.getSpinNo() == 0) {
                                                    UtilMethods.INSTANCE.Processing(mActivity, "You don't have spin now, please try after some time");
                                                } else {
                                                    spinWheel(mGetSpinDataResponse.getSpinNo());
//                                                    getBalanceData(SpinWheelActivity.this);

                                                }

                                            } else if (mGetSpinDataResponse.getStatuscode() == -1) {
                                                UtilMethods.INSTANCE.Processing(mActivity, mGetSpinDataResponse.getMsg());

                                            }

                                        }
                                    } catch (Exception ex) {

                                        Log.e("exception", ex.getMessage());

                                        UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());

                                    }
                                }
                            } else {
                                UtilMethods.INSTANCE.Error(mActivity, response.message());
                            }

                        }

                        @Override
                        public void onFailure(Call<GetWinngspinDataResponse> call, Throwable t) {
                            try {
                                loader.dismiss();
                                if (t.getMessage() != null && !t.getMessage().isEmpty()) {

                                    if (t.getMessage().contains("No address associated with hostname")) {
                                        UtilMethods.INSTANCE.NetworkError(mActivity);

                                    } else {
                                        UtilMethods.INSTANCE.Error(mActivity, t.getMessage());

                                    }

                                } else {
                                    UtilMethods.INSTANCE.Error(mActivity, getString(R.string.something_error));

                                }
                            } catch (IllegalStateException ise) {
                                UtilMethods.INSTANCE.Error(mActivity, ise.getMessage());

                            }
                        }
                    });
                } catch (Exception ex) {
                    loader.dismiss();
                    UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());

                }
            }
        } catch (Exception ex) {
            loader.dismiss();
            UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());

        }
    }

    private void setSpinnerData(ArrayList<SpinWheel> pointArray) {

        int[] spinColor = getResources().getIntArray(R.array.spin_color);
        for (int i = 0; i < pointArray.size(); i++) {
            LuckyItem luckyItem1 = new LuckyItem();
            luckyItem1.number = pointArray.get(i).getNumber();
            luckyItem1.text = String.valueOf(pointArray.get(i).getSuperCoin());
            luckyItem1.icon = spinImage[i];
            luckyItem1.color = spinColor[i];
            data.add(luckyItem1);
        }
        bgView.setVisibility(View.VISIBLE);
        spinView.setVisibility(View.VISIBLE);
        spinConditionTittle.setVisibility(View.VISIBLE);
        spinTitle.setVisibility(View.VISIBLE);
        playBtn.setVisibility(View.VISIBLE);
        spinCountView.setVisibility(View.VISIBLE);
        walletRecyclerView.setVisibility(View.VISIBLE);
        luckyWheelView.setData(data);
        luckyWheelView.setRound(getRandomRound());
        luckyWheelView.setLuckyRoundItemSelectedListener(index -> {
            UtilMethods.INSTANCE.Successful(this, "Congratulations, you have won " + data.get(indexStop).text + " Super Coins");
            getBalanceData(this);
        });

    }

    private int getRandomRound() {
        Random rand = new Random();
        return rand.nextInt(10) + 15;
    }

    private void spinWheel(int spinNum) {


        if (spinNum != 0) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).number == spinNum) {
                    indexStop = i;
                    break;
                }
            }
        } else {
            indexStop = getRandomIndex();

        }
        luckyWheelView.startLuckyWheelWithTargetIndex(indexStop);
    }

    private int getRandomIndex() {
        Random rand = new Random();
        return rand.nextInt(data.size() - 1) + 0;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}