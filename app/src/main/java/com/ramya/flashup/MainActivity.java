package com.ramya.flashup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.skyfishjy.library.RippleBackground;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {

    ImageView user_image, background;
    BottomSheetBehavior behavior, behavior2;
    LinearLayout bottomSheet, settingSheet;
    MaterialToolbar toolbar;
    TextView day, user;
    FloatingActionButton sound,torch,music;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    ShakeDetector mShakeDetector;
    private CameraManager mCameraManager;
    private String mCameraId;
    boolean light = false, bgv = true, over = false, shakeM = false, ML = false;
    LinearLayout send,share,rate,name,custom;
    Vibrator vibe;
    SwitchMaterial vibrate,lighter,maxVolume, MLoop;
    private Handler handler = new Handler();
    final int REQ_CODE_PICK_SOUNDFILE = 1;
    Uri audioFileUri;
    MediaPlayer player;
    RippleBackground rippleBackground;
    WifiManager wifiManager;
    AudioManager audioManager;

    //Shared Preference keys
    final String USER_NAME = "user_name";
    final String CUSTOM_MUSIC = "custom_music";
    final String SHAKE_MODE = "shakeMode";
    final String TORCH_BUTTON = "torchButton";
    final String MUSIC_BUTTON = "musicButton";
    final String PHONE_BUTTON = "phoneButton";
    final String VIBRATE_SWITCH = "vibrateSwitch";
    final String MUSIC_LOOP = "musicLoop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing
        user_image = findViewById(R.id.button);
        background = findViewById(R.id.shadow);
        bottomSheet = findViewById(R.id.bottom_sheet);
        settingSheet = findViewById(R.id.bottom_setting);
        toolbar = findViewById(R.id.topAppBar);
        day = findViewById(R.id.morning);
        user = findViewById(R.id.user);
        send = findViewById(R.id.feed);
        share = findViewById(R.id.share);
        rate = findViewById(R.id.rate);
        sound = findViewById(R.id.sound);
        torch = findViewById(R.id.torch);
        music = findViewById(R.id.music);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        custom = findViewById(R.id.custom);
        name = findViewById(R.id.name);
        vibrate = findViewById(R.id.vibrate);
        lighter = findViewById(R.id.torchLoop);
        MLoop = findViewById(R.id.MusicLoop);
        maxVolume = findViewById(R.id.max);
        rippleBackground = findViewById(R.id.content);

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        behavior2 = BottomSheetBehavior.from(settingSheet);
        behavior2.setState(BottomSheetBehavior.STATE_HIDDEN);


        audioManager  =   (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        Calendar rightNow = Calendar.getInstance();
        int hours = rightNow.get(Calendar.HOUR_OF_DAY); // return the hour in 24 hrs format (ranging from 0-23)

        //Wish for the time
        if (hours < 10){
            day.setText("Morning,");
        }else if(hours < 15){
            day.setText("Afternoon,");
        }else if(hours < 18){
            day.setText("Evening,");
        }else{
            day.setText("Night,");
        }

        //set StatusBar icon color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //Shared Preference
        SharedPreferences prefs = this.getSharedPreferences(
                "com.ramya.flashup", Context.MODE_PRIVATE);

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shakeMode()){
                    saveShake(false);
                    scaleUp();
                    Snackbar.make(v, R.string.shakeOff, Snackbar.LENGTH_LONG).setTextColor(getResources().getColor(R.color.colorPrimaryDark)).show();
                }else{
                    saveShake(true);
                    scaleDown();
                    Snackbar.make(v, R.string.shake, Snackbar.LENGTH_LONG).setTextColor(getResources().getColor(R.color.colorPrimaryDark)).show();
                }
            }
        });

        //set Button positions
        setButtonPosition();

        //Menu click function
        menuClick();

        //Button click
        buttonClick();

        //Setting click function
        settingClick();

        //Get start with preference
        user.setText(getName());

        //create media player
        player = new MediaPlayer();
        player = MediaPlayer.create(this,R.raw.rahath);

        if (player!=null){
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    rippleBackground.stopRippleAnimation();
                    over = true;
                }
            });
        }


        //create wifi toggle
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    behavior2.setState(BottomSheetBehavior.STATE_HIDDEN);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.set){
                    if (behavior2.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        behavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        behavior2.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                }
                return true;
            }
        });

    } //OnCreate Over here


    public void menuClick(){
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = "masterword330@gmail.com";
                String[] mailArray = mail.split(",");
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, mailArray);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Something went wrong");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello there Jeriya Production team,");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));

                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Jeriya+Production&hl=en"));
                startActivity(browserIntent);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/developer?id=Jeriya+Production&hl=en");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send Url"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There is problem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void settingClick(){
        lighter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
               if (isChecked){
                   mRunnable.run();
               }else{
                   flashOff();
                   handler.removeCallbacks(mRunnable);
               }
            }
        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater linf = LayoutInflater.from(MainActivity.this);
                final View inflator = linf.inflate(R.layout.get_name, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                alert.setTitle("Register");
                alert.setMessage("Enter your correct name");
                alert.setView(inflator);

                final TextInputEditText editText = inflator.findViewById(R.id.textField);

                alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (editText.getText().toString() != "" || editText.getText().toString() != null){
                            String name2 = editText.getText().toString();
                            user.setText(name2);
                            saveName(name2);
                        }
                        //do operations using s1 and s2 here...
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
            }
        });

        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
                rippleBackground.stopRippleAnimation();
                Intent intent;
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/mpeg");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.seclect)), REQ_CODE_PICK_SOUNDFILE);

            }
        });

        maxVolume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    AudioManager am =
                            (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    am.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                            0);
                }else{
                    AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                }
            }
        });
        vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveVibration(isChecked);
            }
        });

        MLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveLoopButton(isChecked);
                player.setLooping(getLoopButton());
                Toast.makeText(MainActivity.this,""+getLoopButton(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (light){
                flashOff();
            }else{
                flashOn();
            }
            handler.postDelayed(this,100);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK){
            if ((data != null) && (data.getData() != null)){
                audioFileUri = data.getData();
                savePath(audioFileUri);
                player = MediaPlayer.create(getApplicationContext(),getPath());
                // Now you can use that Uri to get the file path, or upload it, ...
            }
        }
    }

    public void buttonClick(){
        torch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTorchButton()){
                    saveTorchButton(false);
                    torch.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.white));
                }else{
                    saveTorchButton(true);
                    torch.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gray));
                    Snackbar.make(v, R.string.torch, Snackbar.LENGTH_LONG).setTextColor(getResources().getColor(R.color.colorPrimaryDark)).show();
                }
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMusicButton()){
                    saveMusicButton(false);
                    music.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.white));
                }else{
                    saveMusicButton(true);
                    music.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gray));
                    Snackbar.make(v, R.string.music, Snackbar.LENGTH_LONG).setTextColor(getResources().getColor(R.color.colorPrimaryDark)).show();
                }
            }
        });

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPhoneButton()){
                    savePhoneButton(false);
                    sound.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.white));
                }else{
                    savePhoneButton(true);
                    sound.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gray));
                    Snackbar.make(v, R.string.phone, Snackbar.LENGTH_LONG).setTextColor(getResources().getColor(R.color.colorPrimaryDark)).show();
                }
            }
        });

    }

    public void setButtonPosition(){
        if (getPhoneButton()){
            sound.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gray));
        }if(getTorchButton()){
            torch.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gray));
        }if(getMusicButton()){
            music.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gray));
        }if (shakeMode()){
            scaleDown();
        }
        vibrate.setChecked(getVibration());
        MLoop.setChecked(getLoopButton());
        Toast.makeText(MainActivity.this,""+getLoopButton(),Toast.LENGTH_LONG).show();
    }


    @Override
    public void onResume() {
        super.onResume();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */
//                Toast.makeText(MainActivity.this,"shake you "+count,Toast.LENGTH_LONG).show();
                if (shakeMode()){
                    if (getVibration()){
                        vibe.vibrate(100);
                    }
                    if (getTorchButton()){
                        if (light){
                            flashOff();
                        }else{
                            flashOn();
                        }
                    }if (getMusicButton()){
                        if (player != null){
                            if (player.isPlaying()) {
                                player.pause();
                                shakeM = false;
                                rippleBackground.stopRippleAnimation();
                            } else {
                                player.start();
                                shakeM = true;
                                over = false;
                                rippleBackground.startRippleAnimation();
                            }
                        }else{
                            Toast.makeText(MainActivity.this,"Player null",Toast.LENGTH_LONG).show();
                        }
                    }
                    if (getPhoneButton()){
                        if (wifiManager.isWifiEnabled()){
                            wifiManager.setWifiEnabled(false);
                        }else{
                            wifiManager.setWifiEnabled(true);
                        }
                    }

                }
            }

        });
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);

        if (player!=null){
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    rippleBackground.stopRippleAnimation();
                    over = true;
                }
            });
        }
    }
    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        {
            if (player.isPlaying()){
                player.start();
            }
        }
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */
//                Toast.makeText(MainActivity.this,"shake you "+count,Toast.LENGTH_LONG).show();
                if (shakeMode()){
                    if (getVibration()){
                        vibe.vibrate(100);
                    }
                    if (getTorchButton()){
                        if (light){
                            flashOff();
                        }else{
                            flashOn();
                        }
                    }
                    if (getMusicButton()) {
                        if (player != null){
                            if (player.isPlaying()) {
                                player.pause();
                                shakeM = false;
                                rippleBackground.stopRippleAnimation();
                            } else {
                                if (bgv){
                                    player.start();
                                    over = false;
                                    shakeM = true;
                                    rippleBackground.startRippleAnimation();
                                }
                            }
                        }else{
                            Toast.makeText(MainActivity.this,"Add audio",Toast.LENGTH_LONG).show();
                        }
                    }
                    if (getPhoneButton()){
                        if (wifiManager.isWifiEnabled()){
                            wifiManager.setWifiEnabled(false);
                        }else{
                            wifiManager.setWifiEnabled(true);
                        }
                    }
                }
            }
        });

        if (player != null){
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    rippleBackground.stopRippleAnimation();
                    over = true;
                }
            });
        }
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onDestroy();
    }


    public void scaleDown(){
        float scalingFactor = 0.85f;
        float scalingShadow = 0.82f;// scale down to half the size
        user_image.setScaleX(scalingFactor);
        user_image.setScaleY(scalingFactor);
        background.setScaleX(scalingShadow);
        background.setScaleY(scalingShadow);
        vibe.vibrate(100);
    }
    public void scaleUp(){
        float scalingFactor = 1.00f;
        float scalingShadow = 1.001f;// scale down to half the size
        user_image.setScaleX(scalingFactor);
        user_image.setScaleY(scalingFactor);
        background.setScaleX(scalingShadow);
        background.setScaleY(scalingShadow);
        vibe.vibrate(100);
    }
    public void flashOn(){
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        try {
            mCameraManager.setTorchMode(mCameraId, true);
            light = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    public void flashOff(){
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        try {
            mCameraManager.setTorchMode(mCameraId, false);
            light = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void saveName(String n){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_NAME,n);
        editor.apply();
    }
    public String getName(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString(USER_NAME, "User");
        return name;
    }
    public void savePath(Uri path){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CUSTOM_MUSIC,path.toString());
        editor.apply();
    }
    public Uri getPath(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String path = preferences.getString(CUSTOM_MUSIC, null);
        Uri myUri = Uri.parse(path);
        return myUri;
    }
    private void saveShake(boolean b) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHAKE_MODE,b);
        editor.apply();
    }
    public boolean shakeMode(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mShake = preferences.getBoolean(SHAKE_MODE, false);
        return mShake;
    }
    public void saveTorchButton(boolean t){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(TORCH_BUTTON,t);
        editor.apply();
    }
    public boolean getTorchButton(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean torch = preferences.getBoolean(TORCH_BUTTON, false);
        return torch;
    }
    public void saveMusicButton(boolean m){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MUSIC_BUTTON,m);
        editor.apply();
    }
    private boolean getMusicButton() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean music = preferences.getBoolean(MUSIC_BUTTON, false);
        return music;
    }
    public void savePhoneButton(boolean p){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PHONE_BUTTON,p);
        editor.apply();
    }
    private boolean getPhoneButton(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean phone = preferences.getBoolean(PHONE_BUTTON, false);
        return phone;
    }
    public void saveVibration(boolean v){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(VIBRATE_SWITCH,v);
        editor.apply();
    }
    public boolean getVibration(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean vibration = preferences.getBoolean(VIBRATE_SWITCH, true);
        return vibration;
    }

    private void saveLoopButton(boolean b) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MUSIC_LOOP,b);
        editor.apply();
    }
    public boolean getLoopButton(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean loop = preferences.getBoolean(MUSIC_LOOP, true);
        return loop;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // Set volume level to desired levels
                if (shakeM && !over){
                    player.start();
                }
                bgv = true;
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                // You have audio focus for a short time
                if (shakeM && !over){
                    player.start();
                }
                bgv = true;
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                // Play over existing audio
                if (shakeM && !over){
                    player.start();
                }
                bgv = true;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                player.pause();
                bgv = false;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Temporary loss of audio focus - expect to get it back - you can keep your resources around
                player.pause();
                bgv = false;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lower the volume
                break;
        }
    }
}
