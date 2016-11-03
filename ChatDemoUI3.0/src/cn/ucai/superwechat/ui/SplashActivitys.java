package cn.ucai.superwechat.ui;

import android.content.Context;
import android.os.Bundle;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;

import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.db.UserDao;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;

/**
 * 开屏页
 *
 */
public class SplashActivitys extends BaseActivity {
	private static final String TAG=SplashActivitys.class.getSimpleName();
	private static final int sleepTime = 2000;
	Context context;
	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.em_activity_splash);
		super.onCreate(arg0);
		context=SplashActivitys.this;
	}

	@Override
	protected void onStart() {
		super.onStart();

		new Thread(new Runnable() {
			public void run() {
				if (SuperWeChatHelper.getInstance().isLoggedIn()) {
					// auto login mode, make sure all group and conversation is loaed before enter the main screen
					long start = System.currentTimeMillis();
					EMClient.getInstance().groupManager().loadAllGroups();
					EMClient.getInstance().chatManager().loadAllConversations();
					//从数据库中拿到上次登录的用户信息
					UserDao userDao = new UserDao(context);
					User user = userDao.getUser(EMClient.getInstance().getCurrentUser());
					L.e(TAG,"user="+user);
					//保存在内存中
					SuperWeChatHelper.getInstance().setCurrentUser(user);

					long costTime = System.currentTimeMillis() - start;
					//wait
					if (sleepTime - costTime > 0) {
						try {
							Thread.sleep(sleepTime - costTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//enter main screen
					MFGT.gotoMainActivity(SplashActivitys.this);
					MFGT.finish(SplashActivitys.this);
				} else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}
					MFGT.gotoGuideActivity(SplashActivitys.this);
					MFGT.finish(SplashActivitys.this);
				}
			}
		}).start();
	}
	/**
	 * get sdk version
	 */
	private String getVersion() {
	    return EMClient.getInstance().getChatConfig().getVersion();
	}
}
