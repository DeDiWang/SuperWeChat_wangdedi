package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.redpacketui.utils.RedPacketUtil;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;
    @BindView(R.id.tvNick)
    TextView tvNick;
    @BindView(R.id.tvUserName)
    TextView tvUserName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
        setUserInfo();
    }

    private void setUserInfo() {
        EaseUserUtils.setCurrentAppUserAvatar(getActivity(),ivAvatar);
        EaseUserUtils.setCurrentAppUserNick(tvNick);
        EaseUserUtils.setCurrentAppUserNameWithNo(tvUserName);
    }

    @OnClick({R.id.tvPhotos, R.id.tvCollect, R.id.tvMoney, R.id.tvExpression, R.id.tvSetting ,R.id.layout_profile})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_profile:
                MFGT.gotoUserProfileActivity(getActivity());
                break;
            case R.id.tvPhotos:
                break;
            case R.id.tvCollect:
                break;
            //进入零钱界面
            case R.id.tvMoney:
                RedPacketUtil.startChangeActivity(getActivity());
                break;
            case R.id.tvExpression:
                break;
            case R.id.tvSetting:
                MFGT.gotoSettingActivity(getActivity());
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(((MainActivity)getActivity()).isConflict){
            outState.putBoolean("isConflict", true);
        }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
            outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }
}
