package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.net.NetDao;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.MFGT;

/**
 * register screen
 */
public class RegisterActivity extends BaseActivity {
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.etUserName)
    EditText etUserName;
    @BindView(R.id.etNick)
    EditText etNick;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.etConfirmPassword)
    EditText etPasswordConfirm;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_register);
        ButterKnife.bind(this);
        initView();
        mContext=this;
    }

    private void initView() {
        ivBack.setVisibility(View.VISIBLE);
    }

    String username,nick,pwd,confirm_pwd;
    ProgressDialog pd;

    public void register() {
        username = etUserName.getText().toString().trim();
        nick = etNick.getText().toString().trim();
        pwd = etPassword.getText().toString().trim();
        confirm_pwd = etPasswordConfirm.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            etUserName.setError(getResources().getString(R.string.User_name_cannot_be_empty));
            etUserName.requestFocus();
            return;
        }
        if(!username.matches("[a-zA-Z]\\w{5,15}")){
            etUserName.setError(getResources().getString(R.string.illegal_user_name));
            etUserName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            etPassword.setError(getResources().getString(R.string.Password_cannot_be_empty));
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirm_pwd)) {
            etPasswordConfirm.setError(getResources().getString(R.string.Confirm_password_cannot_be_empty));
            etPasswordConfirm.requestFocus();
            return;
        }
        if (!pwd.equals(confirm_pwd)) {
            etPasswordConfirm.setError(getResources().getString(R.string.Two_input_password));
            return;
        }
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
            pd = new ProgressDialog(this);
            pd.setMessage(getResources().getString(R.string.Is_the_registered));
            pd.show();

            registerAppServer();
        }
    }

    private void registerAppServer() {
        NetDao.register(mContext, username, nick, pwd, new OkHttpUtils.OnCompleteListener<Result>() {
            @Override
            public void onSuccess(Result result) {
                if(result!=null && result.isRetMsg()){
                    registerEMServer();
                }else{
                    if(result.getRetCode()== I.MSG_REGISTER_USERNAME_EXISTS){
                        CommonUtils.showMsgShortToast(result.getRetCode());
                        pd.dismiss();
                    }else{
                        unregisterAppServer();
                    }
                }
            }

            @Override
            public void onError(String error) {
                pd.dismiss();
            }
        });
    }

    private void unregisterAppServer() {
        NetDao.unregister(mContext, username, new OkHttpUtils.OnCompleteListener<Result>() {
            @Override
            public void onSuccess(Result result) {
                pd.dismiss();
            }

            @Override
            public void onError(String error) {
                pd.dismiss();
            }
        });
    }

    private void registerEMServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // call method in SDK
                    EMClient.getInstance().createAccount(username, MD5.getMessageDigest(pwd));
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            // save current user
                            SuperWeChatHelper.getInstance().setCurrentUserName(username);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } catch (final HyphenateException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NETWORK_ERROR) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                            }
                            unregisterAppServer();
                        }
                    });
                }
            }
        }).start();
    }

    @OnClick({R.id.iv_back, R.id.btnRegister})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                MFGT.finish(this);
                break;
            case R.id.btnRegister:
                register();
                break;
        }
    }
}
