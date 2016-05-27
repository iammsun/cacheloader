package com.simon.lib.cacheloader.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.simon.lib.cacheloader.Callback;
import com.simon.lib.cacheloader.DownLoadManager;
import com.simon.lib.cacheloader.ImageLoader;

/**
 * Created by sunmeng on 16/5/27.
 */
public class ImageListAdapter extends BaseAdapter {

    String[] images = new String[]{
            "https://img.yzcdn.cn/upload_files/2016/05/12/146302169405468011.jpg!160x160.jpg",
            "https://img.yzcdn.cn/upload_files/2016/05/12/146302169405468011.jpg!160x160.jpg",
            "http://dn-kdt-img.qbox.me/upload_files/shop2.png",
            "http://imgqn.koudaitong.com/upload_files/2015/05/14/FqbbPlOIlIBHliXEYQpBX5QsJU9L.png",
            "http://imgqn.koudaitong.com/upload_files/2015/05/30/143293653939546630.jpg",
            "http://wx.qlogo" +
                    ".cn/mmopen/4v3k2dWtGNkZTtZSGUNx1hMhInTD85PKv8NZkH8ib60BGbKKU1x5ImYpTuQVhwzhfdQxOEx5ibNMMDgjgtOWTiaNthntQfusoB2/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/mOW261WJzibu4Zok2UzLDpON7fQmKvMyzJzJy6icv0t0PFKiapx613yiaaUib8bJ9JLHU9iaDu5AUYEuvL0uqfLMjuribLWOyytdXHF/0",
            "http://dn-kdt-img.qbox.me/upload_files/2016/04/21/14612396746765897.jpg",
            "http://wx.qlogo" +
                    ".cn/mmopen/Q3auHgzwzM64ZNcU7ytPBXkEBo3K3G3yicj4ibjjxJjFnkP9iaemMooKfIjSpB9pU2nHxDw6n95yaWArKauwEGGKA/0",
            "https://img.yzcdn.cn/upload_files/2016/05/09/146280397350999693.jpg",
            "http://wx.qlogo" +
                    ".cn/mmopen/gQpIRfc3cndXyc6Yp6NAUDaGPKibOcricdjKYqe9C9sHSEx62HOSVwnTZ8hZRw6j9BSqVfkrtV5VlMOetfGbycU0nXcz998hoR/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/OV5xMhDJxQsKpibu0tfTOYcnhGPqSoqIvn7XHWwV3Gev0hYkMUk7Qic9icJ01yjB7NOIEnHDx57Pq4KBX0ic9TtO4w/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/mOW261WJzibu4Zok2UzLDpJj9tIG9f12luDibaxnK23KXU91M03hdWXGNx4n2YJo3CwCNEiaAZiczOkazZZuiammbplUAyPRITEJq/0",
            "http://img.yzcdn.cn/upload_files/2016/05/11/146292855502088704.jpg",
            "http://wx.qlogo" +
                    ".cn/mmopen/PiajxSqBRaEJfcT1XbhuSAkF4MJql6V7678sUtXYoIsSTE2SXuUT0ia1on17hxohqZF0aJPTRB7SuIx9Bl9ib28vA/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/gQpIRfc3cndXyc6Yp6NAUDtW8UATFKdc8oOAEglq5l3XhBBFZapzsC8jibtKvJ86BSOAzOq6hnHkp7ibLUUPQkErZ88wVqicqQv/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLBibxmjDIeEBVrSj7yejv10MUT2WTKXVPrNV2Sw6q0TejdTJicKiavbib4a8MicD9ibQqAibsco6nQT5LMEw/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/Q3auHgzwzM6ElXYsw9xctF7obLwFqwa9zv6N86BRtccDSu06oedmnt8fB6Kjyiccqz0ib4BZFuHnJOrVic7ibuKLpE1zLIqV6eD22icr4WWevmiao/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLC5e7cicxibPlUxk55GTUNKTjtF6FXblF00uRsXEIebEPmBoxUcmlo5XmSgzFMj9FsDWLFOyRTXYScg/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/OV5xMhDJxQspFzWThU8OVN5qQIicm4N76rugus00njXuolGB6to1WpKJwicu9EJ1LDbUbWKgJgDov3RSTw5t9GqicEcGs4SPnoN/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/4v3k2dWtGNkZTtZSGUNx1kbtUnHwsh2ibmdKmVvpZEIrRc5VaBclp3almDwV4o600pF48icwuqJrJSUrtibVrtmMpMu2HHZXZDV/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/gQpIRfc3cndXyc6Yp6NAUGrX4K7XYW2JicSekibH68TiaVgqhicoGlnm422sibQowawicxiajTp6Uwa1lFJU7LpMreqbibG52gQAK1oU/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/OV5xMhDJxQspFzWThU8OVN5qQIicm4N76rugus00njXuolGB6to1WpKJwicu9EJ1LDbUbWKgJgDov3RSTw5t9GqicEcGs4SPnoN/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLC5e7cicxibPlUxk55GTUNKTjtF6FXblF00uRsXEIebEPmBoxUcmlo5XmSgzFMj9FsDWLFOyRTXYScg/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/Q3auHgzwzM6ElXYsw9xctF7obLwFqwa9zv6N86BRtccDSu06oedmnt8fB6Kjyiccqz0ib4BZFuHnJOrVic7ibuKLpE1zLIqV6eD22icr4WWevmiao/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLBibxmjDIeEBVrSj7yejv10MUT2WTKXVPrNV2Sw6q0TejdTJicKiavbib4a8MicD9ibQqAibsco6nQT5LMEw/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/gQpIRfc3cndXyc6Yp6NAUDtW8UATFKdc8oOAEglq5l3XhBBFZapzsC8jibtKvJ86BSOAzOq6hnHkp7ibLUUPQkErZ88wVqicqQv/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/PiajxSqBRaEJfcT1XbhuSAkF4MJql6V7678sUtXYoIsSTE2SXuUT0ia1on17hxohqZF0aJPTRB7SuIx9Bl9ib28vA/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLC5e7cicxibPlUxk55GTUNKTjtF6FXblF00uRsXEIebEPmBoxUcmlo5XmSgzFMj9FsDWLFOyRTXYScg/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/Q3auHgzwzM6ElXYsw9xctF7obLwFqwa9zv6N86BRtccDSu06oedmnt8fB6Kjyiccqz0ib4BZFuHnJOrVic7ibuKLpE1zLIqV6eD22icr4WWevmiao/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLBibxmjDIeEBVrSj7yejv10MUT2WTKXVPrNV2Sw6q0TejdTJicKiavbib4a8MicD9ibQqAibsco6nQT5LMEw/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/gQpIRfc3cndXyc6Yp6NAUDtW8UATFKdc8oOAEglq5l3XhBBFZapzsC8jibtKvJ86BSOAzOq6hnHkp7ibLUUPQkErZ88wVqicqQv/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/gQpIRfc3cndXyc6Yp6NAUDtW8UATFKdc8oOAEglq5l3XhBBFZapzsC8jibtKvJ86BSOAzOq6hnHkp7ibLUUPQkErZ88wVqicqQv/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLBibxmjDIeEBVrSj7yejv10MUT2WTKXVPrNV2Sw6q0TejdTJicKiavbib4a8MicD9ibQqAibsco6nQT5LMEw/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/Q3auHgzwzM6ElXYsw9xctF7obLwFqwa9zv6N86BRtccDSu06oedmnt8fB6Kjyiccqz0ib4BZFuHnJOrVic7ibuKLpE1zLIqV6eD22icr4WWevmiao/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLC5e7cicxibPlUxk55GTUNKTjtF6FXblF00uRsXEIebEPmBoxUcmlo5XmSgzFMj9FsDWLFOyRTXYScg/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/OV5xMhDJxQspFzWThU8OVN5qQIicm4N76rugus00njXuolGB6to1WpKJwicu9EJ1LDbUbWKgJgDov3RSTw5t9GqicEcGs4SPnoN/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/4v3k2dWtGNkZTtZSGUNx1kbtUnHwsh2ibmdKmVvpZEIrRc5VaBclp3almDwV4o600pF48icwuqJrJSUrtibVrtmMpMu2HHZXZDV/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/gQpIRfc3cndXyc6Yp6NAUGrX4K7XYW2JicSekibH68TiaVgqhicoGlnm422sibQowawicxiajTp6Uwa1lFJU7LpMreqbibG52gQAK1oU/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/PiajxSqBRaEJN6aMY0icO1UjgbgicmN4wCerfExmfCEj8MY0Nd652OW9NFyL3vQqcMicg61GWQjX6PPfMicdxib2RtYA/0",
            "http://wx.qlogo" +
                    ".cn/mmopen/ajNVdqHZLLCatC2nQgXMRApJff53ChdtujA0t286iagl6bsnBiboibIWjYdGIEO7SsnVgYn2w9zlia0hSMGKVwNUUw/0",
            "https://img.yzcdn.cn/upload_files/2016/05/10/146286433839837799.jpg",
            "http://wx.qlogo" +
                    ".cn/mmopen/mOW261WJzibu4Zok2UzLDpGlpuibxJqJr8cs593b7WHSsAOgFeFl2NRfNibUeveTOrd4uR1WuaG63epRicOmhFj6YSwHscL0viaIm/0",
            "http://dn-kdt-img.qbox.me/upload_files/2016/05/03/146227111394825662.jpg",
            "http://wx.qlogo" +
                    ".cn/mmopen/mOW261WJzibsIBUrhfaeTjIGBic4tp72szwGib6luB6zG1bFCCMGycCEyr7YoGaZjibIB6wqFj6aUm6MVCazNLqa9A/0",
            "http://dn-kdt-img.qbox.me/upload_files/2015/06/30/FvGFaldvNkdNQ4NA76THXSWOWZxk.jpg",
            "https://dn-kdt-img.qbox.me/upload_files/2015/11/25/FrE-Ki5npzmoUC___XrJZ97ZgCHD.jpg",
            "http://img.yzcdn.cn/upload_files/2016/05/23/146399249957148742.jpg",
            "https://dn-kdt-img.qbox.me/upload_files/avatar.png",
            "http://wx.qlogo" +
                    ".cn/mmopen/mOW261WJzibu4Zok2UzLDpGlpuibxJqJr8cs593b7WHSsAOgFeFl2NRfNibUeveTOrd4uR1WuaG63epRicOmhFj6YSwHscL0viaIm/0",
            "https://img.yzcdn.cn/upload_files/2016/05/10/146286433839837799.jpg"};

    private final Context mContext;

    public ImageListAdapter(Context context) {
        DownLoadManager.init(context, new DownLoadManager.Configuration().setFlags
                (DownLoadManager.FLAG_CACHE_AFTER_LOAD | DownLoadManager.FLAG_LOAD_FROM_CACHE));
        mContext = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return images[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
        }
        final ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
        iconView.setImageResource(0);
        iconView.setTag(images[position]);
        DownLoadManager.getInstance().load(images[position], new ImageLoader.ImageLoaderOption()
                .cornerRate(0.05f).width(100).height(100), new Callback<Bitmap>() {
            @Override
            public void onResult(Bitmap data) {
                if (images[position].equals(iconView.getTag()))
                    iconView.setImageBitmap(data);
            }

            @Override
            public void onError(Throwable e) {
                if (images[position].equals(iconView.getTag()))
                    iconView.setImageResource(android.R.drawable.btn_star);
            }

            @Override
            public void onCancel(int code) {

            }
        });
        return convertView;
    }
}
