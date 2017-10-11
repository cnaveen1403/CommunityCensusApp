package com.zolipe.communitycensus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.fragments.EditSupervisorFragment;
import com.zolipe.communitycensus.fragments.ViewSupervisorFragment;
import com.zolipe.communitycensus.model.SupervisorObj;

public class ViewSupervisor extends AppCompatActivity {

    Toolbar toolbar;
    Bundle bundle;
    MenuItem mi_edit, mi_done;
    SupervisorObj mSupervisorObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_supervisor);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        init();
        Intent i = getIntent();
        if (i.getExtras() != null) {
            mSupervisorObj = i.getExtras().getParcelable("SupervisorObj");
            ViewSupervisorFragment viewSupervisorFragment = new ViewSupervisorFragment();
            bundle = new Bundle();
            bundle.putParcelable("Supervisor", mSupervisorObj);
            viewSupervisorFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_view_member, viewSupervisorFragment).commit();
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.action_edit:
                mi_edit.setVisible(false);
                mi_done.setVisible(true);
                EditSupervisorFragment editFragment = new EditSupervisorFragment();
                bundle = new Bundle();
                bundle.putParcelable("Supervisor", mSupervisorObj);
                editFragment.setArguments(bundle);

                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);

                fragmentTransaction.replace(R.id.fl_view_member, editFragment);
                fragmentTransaction.commit();

                toolbar.setTitle("Edit Supervisor");
                break;
            case R.id.action_done:
                updateSupervisor ();
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
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void updateSupervisor() {
        EditSupervisorFragment.updateProfile();
    }
}
