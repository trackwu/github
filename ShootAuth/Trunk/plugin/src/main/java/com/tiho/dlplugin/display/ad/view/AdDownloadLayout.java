package com.tiho.dlplugin.display.ad.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.display.ad.processer.UrlProcessorFactory;
import com.tiho.dlplugin.display.ad.view.AdWebBase.BeginDownload;
import com.tiho.dlplugin.util.BitmapUtil;

public class AdDownloadLayout extends RelativeLayout implements OnClickListener {

	private ImageView closeBtn;
	private AdWebBase adview;
	private long id;
	private int from;

	public AdDownloadLayout(Context context,String url,long id,int from) {
		super(context);
		this.id =id;
		this.from =from;
		addCloseButton();
		addAdWebView(url);
	}

	private void addCloseButton() {
		closeBtn = new ImageView(getContext());
		closeBtn.setId((int) (System.nanoTime() % Integer.MAX_VALUE));
		Bitmap bm = BitmapUtil.getBitmap("close");
		closeBtn.setImageBitmap(bm);
		int imageWidth = (int) (getResolution().first / 32);
		int imageHeight = imageWidth;
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dip2px(getContext(), imageWidth), dip2px(getContext(), imageHeight));

		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		closeBtn.setOnClickListener(this);

		addView(closeBtn , lp);
	}

	private void addAdWebView(final String url) {

		ScrollView scrollView = new ScrollView(getContext());
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		if (from == UrlProcessorFactory.FROM_OLD_LINK) {
			adview = new AdWebOld(getContext(), id, from);
		} else if (from == UrlProcessorFactory.FROM_NEW_LINK) {
			adview = new AdWebLink(getContext(), id, from);
		}
		if (adview == null) {
			LogManager.LogShow("adview初始化错误");
			return;
		}
		adview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		lp.addRule(RelativeLayout.BELOW, closeBtn.getId());

		scrollView.addView(adview);
		addView(scrollView, lp);
		adview.setBeginDownload(new BeginDownload() {

			@Override
			public void callbackBegin() {
				AdDownloadLayout.this.setVisibility(View.GONE);
				LogManager.LogShow("隐藏AdDownloadLayout");
			}
		});
		adview.loadUrl(url);

	}

	@Override
	public void onClick(View v) {
		AdLoaderManager.closeWindow(getContext());
	}

	private Pair<Integer, Integer> getResolution() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		return new Pair<Integer, Integer>(width, height);
	}
	private static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

}
