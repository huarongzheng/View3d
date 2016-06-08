package com.huarongzheng.view3d;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.huarongzheng.view3d.othershow.FileListDialog;
import com.huarongzheng.view3d.othershow.PreferencesActivity;
import com.huarongzheng.view3d.stlbean.STLObject;
import com.huarongzheng.view3d.util.IOUtils;
import com.huarongzheng.view3d.util.Log;

/**
 * 灞曠ず鐣岄潰
 * 
 * @author
 * 
 */
public class STLViewActivity extends Activity implements
		FileListDialog.OnFileListDialogListener {
	private STLView stlView = null;
	private ToggleButton toggleButton;
	private ImageButton loadButton;
	private FrameLayout relativeLayout;
	private STLObject stlObject;
	private Context context;
	private ImageButton preferencesButton;
	private byte[] stlBytes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String stl_path = getIntent().getStringExtra("stl_path");
		context = this;
		setContentView(R.layout.stl);
		init();
		setOnListener();
		
		isHaveFile(stl_path);

	}

	public void isHaveFile(String stl_path){
		if(stl_path!=null&&!stl_path.equals("")){
			//褰撴湁鏂囦欢浼犲叆鏃讹紝闅愯棌鍔犺浇鏂囦欢鐨勬寜閽�
			loadButton.setVisibility(View.GONE);
			File file = new File(stl_path);
			openfile(file);
		}else{
			loadButton.setVisibility(View.VISIBLE);
			loadButton.setClickable(true);

		}
	}
	
	
	/**
	 * 鍒濆鍖栨帶浠�
	 */
	public void init() {
		relativeLayout = (FrameLayout) findViewById(R.id.stlFrameLayout);
		toggleButton = (ToggleButton) findViewById(R.id.rotateOrMoveToggleButton);
		loadButton = (ImageButton) findViewById(R.id.loadButton);
		preferencesButton = (ImageButton) findViewById(R.id.preferncesButton);

	}

	/**
	 * 璁剧疆鍝嶅簲鐩戝惉
	 */
	public void setOnListener() {
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (stlView != null) {
					stlView.setRotate(isChecked);
				}
			}
		});
		loadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FileListDialog fileListDialog = new FileListDialog(
						STLViewActivity.this, false, getResources().getString(
								R.string.choose_stl_file), ".stl");
				fileListDialog
						.setOnFileListDialogListener(STLViewActivity.this);

				SharedPreferences config = getSharedPreferences("PathSetting",
						Activity.MODE_PRIVATE);
				fileListDialog.show(config
						.getString("lastPath",Environment.getExternalStorageDirectory().getAbsolutePath()));
				Log.i("loadButton:" + Environment.getExternalStorageDirectory().getAbsolutePath());
			}
		});
		preferencesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(STLViewActivity.this,
						PreferencesActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (stlView != null) {
			Log.i("onResume");
			// stlView.onResume();
			stlView.requestRedraw();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (stlView != null) {
			Log.i("onPause");
			// stlView.onPause();
		}
	}

	/**
	 * 鑾峰彇鐘舵��
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.i("onRestoreInstanceState");
		Parcelable stlFileName = savedInstanceState
				.getParcelable("STLFileName");
		stlBytes=savedInstanceState.getByteArray("stlBytes");
		if(stlBytes!=null){
			stlObject = new STLObject(stlBytes, this, new FinishSTL());
		}
		
	}

	/**
	 * 淇濆瓨鐘舵��
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (stlView != null) {
			Log.i("onSaveInstanceState");
			outState.putParcelable("STLFileName", stlView.getUri());
			outState.putBoolean("isRotate", stlView.isRotate());
			outState.putByteArray("stlBytes", stlBytes);
		}
	}

	/**
	 * 鏂囦欢鐐瑰嚮鍝嶅簲
	 */
	@Override
	public void onClickFileList(File file) {
		if (file == null) {
			return;
		}
		openfile(file);
	}

	/**
	 * 鑾峰彇鏂囦欢瀛楄妭
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	private byte[] getSTLBytes(Context context, Uri uri) {
		byte[] stlBytes = null;
		InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
			stlBytes = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return stlBytes;
	}

	/**
	 * 鍥炶皟瀹炵幇
	 * 
	 * @author
	 * 
	 */
	class FinishSTL implements STLObject.IFinishCallBack {

		@Override
		public void readstlfinish() {
			// TODO Auto-generated method stub
			if (stlObject != null) {
				loadButton.setVisibility(View.GONE);
				if (stlView == null) {
					stlView = new STLView(context, stlObject);
					relativeLayout.addView(stlView);
				} else {
					stlView.setNewSTLObject(stlObject);
				}
			}
		}
	}
	
	/**
	 * 鎵撳紑stl鏂囦欢
	 * @param file
	 */
	public void openfile(File file){
		System.out.println("鏂囦欢澶у皬"+file.length()/1024/1024+"M");
		if(file.length()>10*1024*1024){
			Toast.makeText(this, "鏂囦欢澶т簬10M鍙兘浼氳В鏋愬け璐�", Toast.LENGTH_SHORT).show();
		}
		SharedPreferences config = getSharedPreferences("PathSetting",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor configEditor = config.edit();
		configEditor.putString("lastPath", file.getParent());
		configEditor.commit();
		// 杩欓噷鍒濆鍖栦笅鏁版嵁
		if(stlView!=null){
			
			relativeLayout.removeAllViews();
			stlView.delete();
			stlView=null;
			stlBytes = null;
			System.gc();
		}
		try {
			stlBytes = getSTLBytes(this, Uri.fromFile(file));
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.error_outbig),
					Toast.LENGTH_LONG).show();
		}

		if (stlBytes == null) {
			Toast.makeText(this, getString(R.string.error_fetch_data),
					Toast.LENGTH_LONG).show();
			return;
		}

		stlObject = new STLObject(stlBytes, this, new FinishSTL());
	}
	
}
