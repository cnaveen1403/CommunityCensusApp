package com.zolipe.communitycensus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.fragments.EditMemberFragment;
import com.zolipe.communitycensus.fragments.ViewMemberFragment;
import com.zolipe.communitycensus.model.FamilyHead;

public class ViewMember extends AppCompatActivity {

    private String TAG = "ViewMember";

    Toolbar toolbar;
    Bundle bundle;
    FamilyHead member;

    MenuItem mi_edit, mi_done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_member);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        if (i.getExtras() != null) {
            member = i.getExtras().getParcelable("FamilyMember");
            ViewMemberFragment viewMemberFragment = new ViewMemberFragment();
            bundle = new Bundle();
            bundle.putParcelable("Member", member);
            viewMemberFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_view_member, viewMemberFragment).commit();
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_member, menu);

        mi_edit = menu.findItem(R.id.action_edit);
        mi_done = menu.findItem(R.id.action_done);
        mi_done.setVisible(false);
        mi_edit.setVisible(true);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_edit:
                mi_edit.setVisible(false);
                mi_done.setVisible(true);
                EditMemberFragment editFragment = new EditMemberFragment();
                bundle = new Bundle();
                bundle.putParcelable("Member", member);
                editFragment.setArguments(bundle);

                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);

                fragmentTransaction.replace(R.id.fl_view_member, editFragment);
                fragmentTransaction.commit();

                toolbar.setTitle("Edit Member");
                break;
            case R.id.action_done:
                updateMember ();
                break;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                return true;
            default:
                break;
        }

        return true;
    }

    private void updateMember() {
        EditMemberFragment.updateProfile();
    }
}
