package com.simon.lib.cacheloader.sample;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.simon.lib.cacheloader.Callback;
import com.simon.lib.cacheloader.DownLoadManager;
import com.simon.lib.cacheloader.util.BitmapUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    private ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        imageView = (ImageView) root.findViewById(R.id.icon);
        return root;
    }


    @Override
    public void onStart() {
        super.onStart();
        loadImage();
    }

    private void loadImage() {
        Callback callback = new Callback() {
            @Override
            public void onResult(byte[] data) {
                imageView.setImageBitmap(BitmapUtils.decodeBitmap(data));
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel(int code) {
                Toast.makeText(getContext(), android.R.string.cancel, Toast.LENGTH_LONG).show();
            }
        };
        DownLoadManager.init(getContext(), DownLoadManager.FLAG_CACHE_AFTER_LOAD |
                DownLoadManager.FLAG_LOAD_FROM_CACHE);
        DownLoadManager.getInstance().load("http://a4.att.hudong" +
                ".com/73/66/20200000013920144740665554724_140.jpg", callback);
    }
}
