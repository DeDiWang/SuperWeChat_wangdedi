package cn.ucai.superwechat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import cn.ucai.superwechat.R;
import com.hyphenate.easeui.adapter.EaseContactAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseSidebar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PickAtUserActivity extends BaseActivity{
    ListView listView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_pick_at_user);
        
        String groupId = getIntent().getStringExtra("groupId");
        EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);

        EaseSidebar sidebar = (EaseSidebar) findViewById(com.hyphenate.easeui.R.id.sidebar);
        listView = (ListView) findViewById(R.id.list);
        sidebar.setListView(listView);
        List<String> members = group.getMembers();
        List<User> userList = new ArrayList<User>();
        for(String username : members){
            User user = EaseUserUtils.getAppUserInfo(username);
            userList.add(user);
        }

        Collections.sort(userList, new Comparator<User>() {

            @Override
            public int compare(User lhs, User rhs) {
                if(lhs.getAppInitialLetter().equals(rhs.getAppInitialLetter())){
                    return lhs.getMUserNick().compareTo(rhs.getMUserNick());
                }else{
                    if("#".equals(lhs.getAppInitialLetter())){
                        return 1;
                    }else if("#".equals(rhs.getAppInitialLetter())){
                        return -1;
                    }
                    return lhs.getAppInitialLetter().compareTo(rhs.getAppInitialLetter());
                }

            }
        });
        final boolean isOwner = EMClient.getInstance().getCurrentUser().equals(group.getOwner());
        if(isOwner) {
            addHeadView();
        }
        listView.setAdapter(new PickUserAdapter(this, 0, userList));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isOwner){
                    if(position != 0) {
                        User user = (User) listView.getItemAtPosition(position);
                        if (EMClient.getInstance().getCurrentUser().equals(user.getMUserName()))
                            return;
                        setResult(RESULT_OK, new Intent().putExtra("username", user.getMUserName()));
                    }else{
                        setResult(RESULT_OK, new Intent().putExtra("username", getString(R.string.all_members)));
                    }
                }else{
                    User user = (User) listView.getItemAtPosition(position);
                    if (EMClient.getInstance().getCurrentUser().equals(user.getMUserName()))
                        return;
                    setResult(RESULT_OK, new Intent().putExtra("username", user.getMUserName()));
                }

                finish();
            }
        });

    }

    private void addHeadView(){
        View view = LayoutInflater.from(this).inflate(R.layout.ease_row_contact, listView, false);
        ImageView avatarView = (ImageView) view.findViewById(R.id.avatar);
        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(getString(R.string.all_members));
        avatarView.setImageResource(R.drawable.ease_groups_icon);
        listView.addHeaderView(view);
    }

    public void back(View view) {
        finish();
    }

    private class PickUserAdapter extends EaseContactAdapter{

        public PickUserAdapter(Context context, int resource, List<User> objects) {
            super(context, resource, objects);
        }
    }
}
