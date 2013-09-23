package org.mariotaku.aria2.android;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



import org.mariotaku.aria2.Aria2API;
import org.mariotaku.aria2.DownloadUris;
import org.mariotaku.aria2.GlobalStat;
import org.mariotaku.aria2.Options;
import org.mariotaku.aria2.Status;
import org.mariotaku.aria2.Version;
import org.mariotaku.aria2.android.NewDownloadDialogFragment.NewDownloadDialogListener;
import org.mariotaku.aria2.android.utils.CommonUtils;


import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Aria2Activity extends ActionBarActivity implements OnClickListener,Aria2UIMessage,Aria2APIMessage,NewDownloadDialogFragment.NewDownloadDialogListener{

	
	private Aria2Manager _aria2Manager = null;
	
	private ListView downloadListView = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		_aria2Manager = new Aria2Manager(this,mRefreshHandler);
		downloadListView = (ListView)findViewById(R.id.download_list_view);
		downloadListView.setOnItemClickListener(mMessageClickedHandler);
		
		
		findViewById(R.id.version).setOnClickListener(this);
		findViewById(R.id.session_info).setOnClickListener(this);
		findViewById(R.id.status).setOnClickListener(this);
		findViewById(R.id.run).setOnClickListener(this);
		
	}
	
	// Create a message handling object as an anonymous class.
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id)
		{
			// TODO Auto-generated method stub
			
		}
	};

	
	@Override
	public void onStart() {
		super.onStart();
		try
		{
			_aria2Manager.InitHost();
			_aria2Manager.StartUpdateGlobalStat();
		}
		catch(Exception e)
		{
			Toast.makeText(Aria2Activity.this,e.getMessage(),Toast.LENGTH_LONG).show();
		}	 
	}

	@Override
	public void onStop() {
		_aria2Manager.StopUpdateGlobalStat();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_download:
				showNoticeDialog();
				break;
			case R.id.action_exit:
				finish();
				break;
			case R.id.action_settings:
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		
		try 
		{
			switch (v.getId()) {
				case R.id.version:
					_aria2Manager.sendToAria2APIHandlerMsg(GET_VERSION_INFO);
					break;
				case R.id.session_info:
					_aria2Manager.sendToAria2APIHandlerMsg(GET_SESSION_INFO);
					break;
				case R.id.status:
					_aria2Manager.sendToAria2APIHandlerMsg(GET_ALL_STATUS);
					break;
				case R.id.run:
					_aria2Manager.sendToAria2APIHandlerMsg(ADD_URI);
					break;
			}
		}catch (Exception e) {
			Toast.makeText(Aria2Activity.this,e.getMessage(), Toast.LENGTH_LONG).show();
		}

		

	}

	private Handler mRefreshHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
				case GLOBAL_STAT_REFRESHED:
					if (msg.obj == null) return;
					GlobalStat stat = (GlobalStat) msg.obj;
					String subtitle = getString(R.string.global_speed_format,
							CommonUtils.formatSpeedString(stat.downloadSpeed),
							CommonUtils.formatSpeedString(stat.uploadSpeed));
					getSupportActionBar().setSubtitle(subtitle);
					break;
				case VERSION_INFO_REFRESHED:
					if (msg.obj == null) return;
					String versionInfo = (String)msg.obj;
					((TextView)findViewById(R.id.version)).setText(versionInfo);
					break;
				case SESSION_INFO_REFRESHED:
					if (msg.obj == null) return;
					String sessionInfo = (String)msg.obj;
					((TextView)findViewById(R.id.session_info)).setText(sessionInfo);
					break;
				case DOWNLOAD_INFO_REFRESHED:
					if (msg.obj == null) return;
					String downloadInfo = (String)msg.obj;
					((TextView)findViewById(R.id.run)).setText(downloadInfo);
					break;
				case ALL_STATUS_REFRESHED:
					if (msg.obj == null) return;
					
					List<DownloadItem> downloadItems = new ArrayList<DownloadItem>();
					List<Status> status = (ArrayList<Status>) msg.obj;
					Iterator<Status> it = status.iterator(); 
					while (it.hasNext())
					{
						Status statusTemp = it.next();
						DownloadItem downloadItem = new DownloadItem(statusTemp.getFiles().get(0).path, statusTemp.status,statusTemp.totalLength);
						downloadItems.add(downloadItem);
					}
					
					
					DownloadItemAdapter adapter = new DownloadItemAdapter(Aria2Activity.this,R.layout.download_item,downloadItems);
		 			
		 			downloadListView.setAdapter(adapter);
					
					break;
			}
		}
	};
	
	public void showNoticeDialog() {
        // Create an instance of the dialog fragmnt and show it
        DialogFragment dialog = new NewDownloadDialogFragment();
        dialog.show(getSupportFragmentManager(), "NewDownloadFragment");
    }

	@Override
	public void onDialogPositiveClick(DialogFragment dialog)
	{
		try 
		{
			String uri = ((NewDownloadDialogFragment)dialog).getDownloadUri(); 
			_aria2Manager.sendToAria2APIHandlerMsg(ADD_URI,uri);
			
		}catch (Exception e) {
			Toast.makeText(Aria2Activity.this,e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}


}