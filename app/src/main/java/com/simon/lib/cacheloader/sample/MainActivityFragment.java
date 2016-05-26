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
}
