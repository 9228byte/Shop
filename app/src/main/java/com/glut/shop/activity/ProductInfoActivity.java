package com.glut.shop.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.glut.shop.R;
import com.glut.shop.adapter.VPagerFragmentAdapter;
import com.glut.shop.application.MainApplication;
import com.glut.shop.bean.CartInfo;
import com.glut.shop.bean.GoodsInfo;
import com.glut.shop.bean.HistoryInfo;
import com.glut.shop.bean.Specification;
import com.glut.shop.bean.ViewBundle;
import com.glut.shop.database.CartDBHelper;
import com.glut.shop.database.GoodDBHelper;
import com.glut.shop.database.HistoryDBHelper;
import com.glut.shop.fragment.GraphicDetailsFragment;
import com.glut.shop.fragment.ProductEvalInfoFragment;
import com.glut.shop.fragment.ProductWillFragment;
import com.glut.shop.util.ClickUtil;
import com.glut.shop.util.DateUtil;
import com.glut.shop.util.ToastUtils;
import com.glut.shop.widget.ChildAutoHeightViewPager;
import com.glut.shop.widget.FlowLayout;
import com.glut.shop.widget.GeneralVpLayout;
import com.glut.shop.widget.Holder;
import com.glut.shop.widget.HolderCreator;
import com.glut.shop.widget.MyScrollView;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;
import java.util.ArrayList;

public class ProductInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProductInfoActivity";
    private CartDBHelper mCartDBHelper;
    private HistoryDBHelper mHistoryDBHelper;
    private String user_id  = null;
    private String goods_id = null;
    /**
     * 顶部tool，工具栏
     */
    private RelativeLayout toolbarLayout;

    /**
     * 顶部ViewPager
     * 商品详情轮播图
     */
    private GeneralVpLayout<String> generalVpLayout;
    private LinearLayout mtopViewGroup;
    private ImageView[] mImageViews;

    private TextView tvPrice;   //现价
    private TextView tvTitle;   //商品标题
    private TextView tvProductDesc; //商品描述

    /**
     * 中间浮动栏（图文详情，商品实拍，评价）
     */
    private LinearLayout classifyLayout;
    private TextView imgtextInfoTv, photoInfoTv, evalInfoTv;
    private ImageView cursor;

    /**
     * 浮动栏高度
     */
    private int classifyHeight;

    /**
     * 滑动ScrollView
     */
    private MyScrollView myScrollView;

    /**
     * 底部ViewPager设置
     */
    private ChildAutoHeightViewPager bottomViewPager;
    private VPagerFragmentAdapter bottomAdapter;
    private ArrayList<Fragment> mDatas;
    private GraphicDetailsFragment graphicDetailsFragment;
    private ProductWillFragment productWillFragment;
    private ProductEvalInfoFragment productEvalInfoFragment;

    //bmpw游标宽度，mCurrentIndex表示当前所在页面，fixLeftMargin固定左侧边距
    private int bmpw = 0;
    private int mCurrentIndex = 0;
    private int fixLeftMargin;
    private LinearLayout.LayoutParams params;

    /**
     * 其他控件（返回顶部、特别提供、商品特征、商品规格选项）
     */
    private ImageView backTopIv;
    private int viewPagerTopDistance;   //记录底部ViewPager距离顶部的高度
    private FlowLayout specialOfferFlowLayout, productFeaturesFlowLayout, specificationsChoiceFlowLayout;

    //底部固定栏
    private ImageView collectIv;
    private ImageView shoppingCartIv;
    private TextView addShoppingCartTv;

    /**
     * 相关数据
     */
    private ArrayList<String> bannerList = null;
    private ArrayList<String> priceList = null;
    private ArrayList<String> titleList = null;
    private ArrayList<String> specialOfferList = null;
    private ArrayList<String> productFeaturesList = null;
    private ArrayList<Specification> specificationList = null;
    private ArrayList<String> detailList = null;
    private ArrayList<String> willList = null;
    private GoodsInfo info = null;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);
        goods_id = getIntent().getStringExtra("goods_id");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    info = GoodDBHelper.getDbService().getGoodsById(goods_id);
                    Log.d(TAG, "run: " + info.getTitle() + " " + info.getFeature2());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        mHandler.postDelayed(connectDB, 200);
    }

    private Runnable connectDB = new Runnable() {
        @Override
        public void run() {
            if (info != null) {
                initData();
                initView();
                initImg();
                reflashData();
            } else {
                Log.d(TAG, "run: 延迟加载");
                mHandler.postDelayed(this, 200);
            }
        }
    };

    private void initData() {
        //初始化顶部轮播图
        if (bannerList == null) {
            bannerList = new ArrayList<>();
        }
        for (int i = 0; i < info.getImg().size(); i++) {
            bannerList.add(info.getImg().get(i));
            Log.d(TAG, info.getImg().get(i));
        }
        //初始化现价
        if (priceList == null) {
            priceList = new ArrayList<>();
        }
        priceList.add(info.getPrice());
        //初始化商品标题
        if (titleList == null) {
            titleList = new ArrayList<>();
        }
        titleList.add(info.getTitle());
        //初始化优惠
        if (specialOfferList == null) {
            specialOfferList = new ArrayList<>();
        }
        specialOfferList.add("包邮");
        //初始化规格
        if (specificationList == null) {
            specificationList = new ArrayList<>();
        }
        for (int i = 0; i < info.getSpec().size(); i++) {
//            Specification specification = new Specification(i, "规格" + i, null);
            Specification specification = new Specification(i, info.getSpec().get(i), null);
            specificationList.add(specification);
        }
        //初始化商品特征
        if (productFeaturesList == null) {
            productFeaturesList = new ArrayList<>();
        }
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                productFeaturesList.add(info.getFeature1());
            } else if (i == 1) {
                productFeaturesList.add(info.getFeature2());
            }

        }
        //初始化商品图文详情
        if (detailList == null) {
            detailList = new ArrayList<>();
        }
        for (int i = 0; i < info.getImg().size(); i++) {
            detailList.add(info.getImg().get(i));
        }
        //初始化商品实拍
        if (willList == null) {
            willList = new ArrayList<>();
        }
        for (int i = 0; i < info.getImg().size(); i++) {
            willList.add(info.getImg().get(i));
        }
    }

    private void initView() {
        //顶部返回
        ImageView backImg = (ImageView)findViewById(R.id.iv_back);
        backImg.setOnClickListener(this);
        //顶部分享
        ImageView shareImg = (ImageView)findViewById(R.id.iv_share);
        shareImg.setOnClickListener(this);
        //顶部tool工具栏
        toolbarLayout = (RelativeLayout)findViewById(R.id.layout_toolbar);
        final TextView pdescTv = (TextView)findViewById(R.id.tv_product_title);
        //顶部的ViewPager轮播图
        RelativeLayout headerVpLayout = (RelativeLayout)findViewById(R.id.layout_header_vp);
        if (headerVpLayout != null) {
            generalVpLayout = (GeneralVpLayout<String>)findViewById(R.id.generalVpLayout);
            mtopViewGroup = headerVpLayout.findViewById(R.id.viewGroup);
        }
        //现价、商品标题、商品描述
        tvPrice = (TextView)findViewById(R.id.tv_price);
        tvTitle = (TextView)findViewById(R.id.tv_title);
        //中间浮动栏（图文详情，商品实拍，评价）
        classifyLayout = (LinearLayout)findViewById(R.id.layout_classify);
        classifyLayout.setVisibility(View.INVISIBLE);   //浮动栏初始化时隐藏
        //获取控件大小（高度）
        classifyLayout.post(new Runnable() {
            @Override
            public void run() {
                classifyHeight = classifyLayout.getHeight();
            }
        });
        imgtextInfoTv = (TextView)findViewById(R.id.tv_info_imgtext);
        imgtextInfoTv.setOnClickListener(this);
        photoInfoTv = (TextView)findViewById(R.id.tv_info_photo);
        photoInfoTv.setOnClickListener(this);
        evalInfoTv = (TextView)findViewById(R.id.tv_info_eval);
        evalInfoTv.setOnClickListener(this);
        cursor = (ImageView)findViewById(R.id.cursor);
        //底部ViewPager
        bottomViewPager = (ChildAutoHeightViewPager)findViewById(R.id.bottomvpager);
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
        graphicDetailsFragment = GraphicDetailsFragment.newInstance(new ViewBundle(bottomViewPager));
        productWillFragment = ProductWillFragment.newInstance(new ViewBundle(bottomViewPager));
        productEvalInfoFragment = ProductEvalInfoFragment.newInstance(new ViewBundle(bottomViewPager));
        mDatas.add(graphicDetailsFragment);
        mDatas.add(productWillFragment);
        mDatas.add(productEvalInfoFragment);

        bottomAdapter = new VPagerFragmentAdapter(getSupportFragmentManager(), mDatas);
        bottomViewPager.setAdapter(bottomAdapter);
        bottomViewPager.setOffscreenPageLimit(mDatas.size());
        bottomViewPager.addOnPageChangeListener(new BottomPageChangeListener());

        //滚动ScrollView
        myScrollView = (MyScrollView)findViewById(R.id.myScrollView);
        myScrollView.setOnScrollListener(new MyScrollView.OnScrollListener() {
            @Override
            public void onScrollchanged(int l, int scrollY, int oldl, int oldt) {
                viewPagerTopDistance = bottomViewPager.getTop() - classifyHeight - toolbarLayout.getHeight();
                // 设置浮动栏
                int translation = Math.max(scrollY, viewPagerTopDistance);
                classifyLayout.setTranslationY(translation);
                classifyLayout.setVisibility(View.VISIBLE);

                // 设置返回顶部
                if (scrollY >= viewPagerTopDistance) {
                    backTopIv.setVisibility(View.VISIBLE);
                } else {
                    backTopIv.setVisibility(View.GONE);
                }

                // 顶部栏透明度控制
//                if (scrollY >= 0 && scrollY < 190) {
//                    toolbarLayout.getBackground().mutate().setAlpha(scrollY / 3);
//                } else if (scrollY >= 190) {
//                    toolbarLayout.getBackground().mutate().setAlpha(229);
//                }
                // 顶部栏透明度控制
                if (scrollY >= 190) {
                    toolbarLayout.getBackground().mutate().setAlpha(220);
                } else {
                    toolbarLayout.getBackground().mutate().setAlpha(255);
                }
            }

            @Override
            public void onTouchUp() {

            }

            @Override
            public void onTouchDown() {

            }
        });
        myScrollView.smoothScrollTo(0, 0);

        //返回顶部
        backTopIv = (ImageView)findViewById(R.id.iv_back_top);
        backTopIv.setVisibility(View.GONE);
        backTopIv.setOnClickListener(this);
        specialOfferFlowLayout = (FlowLayout)findViewById(R.id.flowlayout_special_offer);
        specificationsChoiceFlowLayout = (FlowLayout)findViewById(R.id.flowlayout_specifications_choice);
        productFeaturesFlowLayout = (FlowLayout)findViewById(R.id.flowlayout_product_features);

        //底部固定栏
        collectIv = (ImageView)findViewById(R.id.img_collect);
        collectIv.setOnClickListener(this);
        shoppingCartIv = (ImageView)findViewById(R.id.img_shopping_cart);
        shoppingCartIv.setOnClickListener(this);
        addShoppingCartTv = (TextView)findViewById(R.id.btn_add_shopping_cart);
        addShoppingCartTv.setOnClickListener(this);
    }

    private void reflashData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setProductTopViewPager(bannerList);
                setNowPriceData(priceList);
                setTitleData(titleList);

                setSpecialOfferFlowLayoutData(specialOfferList);
                setProductFeaturesFlowLayoutData(productFeaturesList);
                setSpecificationsChoiceFlowLayoutData(specificationList);
                graphicDetailsFragment.setLinearLayoutData(detailList);
                productWillFragment.setLinearLayoutData(willList);
                /**
                 * 刷新底部ViewPager，每一次刷新数据都要重置高度，默认显示第一页
                 */
                bottomAdapter.reflashData(mDatas);
                bottomViewPager.resetHeight(0);
            }
        }, 1000);
    }

    /**
     * 设置商品轮播图
     * @param data
     */
    private void setProductTopViewPager(ArrayList<String> data) {
        if (data != null && data.size() > 0) {
            generalVpLayout.setVisibility(View.VISIBLE);
            //初始化指示器
            //对ImageViews进行填充
            if (mImageViews == null) {
                mImageViews = new ImageView[data.size()];
            }
            mtopViewGroup.removeAllViews();
            //小图标
            for (int k = 0; k < data.size(); k++) {
                LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                dotParams.setMargins(0, 0, 20, 0);
                dotParams.gravity = Gravity.CENTER_VERTICAL;
                ImageView mImageView = new ImageView(ProductInfoActivity.this);
                mImageView.setLayoutParams(dotParams);
                mImageViews[k] = mImageView;
                if (k == 0) {
                    mImageViews[k].setBackgroundResource(R.mipmap.icon_focusdot);
                } else {
                    mImageViews[k].setBackgroundResource(R.mipmap.icon_defaultdot);
                }
                mtopViewGroup.addView(mImageViews[k]);
            }

            //初始化generalVpLayout
            generalVpLayout.init(new HolderCreator<ImageViewHolder>() {
                @Override
                public ImageViewHolder createHolder() {
                    return new ImageViewHolder();
                }
            }, data)
                    //设置轮播停顿时间
                    .setDuration(3500)
                    //设置指示器是否可见
                    .setPointViewVisible(false)
                    //开始轮播
                    .start();
            //ViewPager状态改变监听（轮播图）
            generalVpLayout.setOnViewPagerChangeListener(new GeneralVpLayout.OnViewPagerChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    //修改指示器
                    for (int i = 0; i < mImageViews.length; i++) {
                        mImageViews[position].setBackgroundResource(R.mipmap.icon_focusdot);
                        if (position != i) {
                            mImageViews[i].setBackgroundResource(R.mipmap.icon_defaultdot);
                        }
                    }
                }
            });
        } else {
            generalVpLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 设置现价
     * @param data
     */
    private void setNowPriceData(ArrayList<String> data) {
        if (data != null && data.size() > 0) {
            for (String value : data) {
                tvPrice.setText(value);
            }
        }
    }

    private void setTitleData(ArrayList<String> data) {
        if (data != null && data.size() > 0) {
            for (String value : data) {
                tvTitle.setText(value);
            }
        }
    }

    /**
     * 设置优惠活动
     * @param datas
     */
    private void setSpecialOfferFlowLayoutData(ArrayList<String> datas) {
        if (datas != null && datas.size() > 0) {
            LayoutInflater mInflater = LayoutInflater.from(this);
            specialOfferFlowLayout.removeAllViews();
            for (String value : datas) {
                TextView tv = (TextView)mInflater.inflate(R.layout.tag_red_circular_textview,
                        specialOfferFlowLayout, false);
                tv.setText(value);
                specialOfferFlowLayout.addView(tv);
            }
        }
    }

    /**
     * 设置商品特征
     * @param datas
     */
    private void setProductFeaturesFlowLayoutData(ArrayList<String> datas) {
        if (datas != null && datas.size() > 0) {
            LayoutInflater mInflater = LayoutInflater.from(this);
            productFeaturesFlowLayout.removeAllViews();
            for (String value : datas) {
                TextView tv = (TextView)mInflater.inflate(R.layout.tag_gray_circular_textview,
                        productFeaturesFlowLayout, false);
                tv.setText(value);
                productFeaturesFlowLayout.addView(tv);
            }
            productFeaturesFlowLayout.setVisibility(View.VISIBLE);
        } else {
            productFeaturesFlowLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 设置选择规格
     * @param datas
     */
    private void setSpecificationsChoiceFlowLayoutData(final ArrayList<Specification> datas) {
        if (datas != null && datas.size() > 0) {
            LayoutInflater mInflater = LayoutInflater.from(this);
            specificationsChoiceFlowLayout.removeAllViews();
            for (Specification value : datas) {
                final TextView tv = (TextView)mInflater.inflate(R.layout.tag_gray_circular_textview,
                        specificationsChoiceFlowLayout, false);
                tv.setTag(value.getId());
                tv.setText(value.getText());
                //规格点击事件监听
                tv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        //防止连续点击
                        if (ClickUtil.isFastClick()) {
                            return;
                        }
                        int tvTag = (int)view.getTag();
                        for (int i = 0; i < datas.size(); i++) {
                            Specification specification = datas.get(i);
                            if (specification.getId() == tvTag) {
                                tv.setBackgroundResource(R.drawable.bg_red_circular);
                                tv.setTextColor(ProductInfoActivity.this.getColor(R.color.colorTitle));
                            } else {
                                TextView tagView = specificationsChoiceFlowLayout.findViewWithTag(specification.getId());
                                tagView.setBackgroundResource(R.drawable.bg_gray_circular);
                                tagView.setTextColor(ProductInfoActivity.this.getColor(R.color.colorSomber));
                            }
                        }
                    }
                });
                specificationsChoiceFlowLayout.addView(tv);
            }
            specificationsChoiceFlowLayout.setVisibility(View.VISIBLE);
        } else {
            specificationsChoiceFlowLayout.setVisibility(View.GONE);
        }
    }

    //自定义Holder
    private class ImageViewHolder implements Holder<String> {
        private ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(ProductInfoActivity.this);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, String imgPath) {
            //加载数据，一般为加载url
            if (TextUtils.isEmpty(imgPath)) {
                imageView.setVisibility(View.GONE);
            } else {

                imageView.setVisibility(View.VISIBLE);
            }
            imageView.setVisibility(View.VISIBLE);
            Picasso.with(ProductInfoActivity.this)
                    .load(imgPath)
                    .into(imageView);
        }
    }

    //点击事件监听
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back://返回
                finish();
                break;
            case R.id.iv_share://分享
                ToastUtils.showToast(this,"分享功能");
                break;
            case R.id.tv_info_imgtext://图文详情
                bottomViewPager.setCurrentItem(0);
                break;
            case R.id.tv_info_photo://商品实拍
                bottomViewPager.setCurrentItem(1);
                break;
            case R.id.tv_info_eval://评价详情
                bottomViewPager.setCurrentItem(2);
                break;
            case R.id.iv_back_top://返回顶部
                myScrollView.smoothScrollTo(0, viewPagerTopDistance);
                break;
            case R.id.img_collect://收藏
                ToastUtils.showToast(this,"收藏功能");
                break;
            case R.id.img_shopping_cart:
                if (!TextUtils.isEmpty(user_id)) {
                    startActivity(new Intent(ProductInfoActivity.this, CartActivity.class));
                }  else {
                    ToastUtils.showToast(getApplicationContext(), "请登录账户");
                }
                break;
            case R.id.btn_add_shopping_cart:
                //防止连续点击
                if (ClickUtil.isFastClick()) {
                    return;
                }
                if (!TextUtils.isEmpty(user_id)) {
                    CartInfo cartInfo = new CartInfo();
                    cartInfo.setGoods_id(info.getId());
                    cartInfo.setPrice(Float.parseFloat(info.getPrice()));
                    cartInfo.setShop(info.getShop());
                    cartInfo.setTitle(info.getTitle());
                    cartInfo.setPrice(Float.parseFloat(info.getPrice()));
                    cartInfo.setCount(1);
                    cartInfo.setImage(info.getImg().get(0));
                    cartInfo.setUpdate_time(DateUtil.getNowDateTime("yyyyMMdd"));
                    cartInfo.setIsSelect(1);
                    cartInfo.setUser_id(user_id);
//                mHepler.insert(cartInfo);
                    ToastUtils.showToast(getApplicationContext(), "添加商品成功");
                    mCartDBHelper.insertByUser(cartInfo);

                } else {
                    ToastUtils.showToast(getApplicationContext(), "请登录账户");
                }

                break;
        }
    }

    /**
     * 内部类实现底部ViewPager设置变化监听
     */
    private class BottomPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPx) {
            //滑动过程
            if (mCurrentIndex == 0 && position == 0) {
                //0-->1
                params.leftMargin = (int)(mCurrentIndex * bmpw + positionOffset * bmpw)
                        + fixLeftMargin;
            } else if (mCurrentIndex == 1 && position == 0) {
                //1-->1
                params.leftMargin = (int)(mCurrentIndex * bmpw + (positionOffset - 1) * bmpw)
                        + fixLeftMargin;
            } else if (mCurrentIndex == 1 && position == 1) {
                //1-->2
                params.leftMargin = (int)(mCurrentIndex * bmpw + positionOffset * bmpw)
                        + fixLeftMargin;
            } else if (mCurrentIndex == 2 && position == 1) {
                //2-->1
                params.leftMargin = (int)(mCurrentIndex * bmpw + (positionOffset - 1) * bmpw)
                        + fixLeftMargin;
            }
            cursor.setLayoutParams(params);
            //重置当前高度
            bottomViewPager.resetHeight(position);
        }

        @Override
        public void onPageSelected(int arg0) {
            //滑动结束
            changeTextView(arg0);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    /**
     * 初始化底部指示器ImageView
     */
    private void initImg() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        int mScreen_4 = outMetrics.widthPixels / 4;
        bmpw = outMetrics.widthPixels / 3;
        fixLeftMargin = (bmpw - mScreen_4) / 2;
        ViewGroup.LayoutParams layoutParams = cursor.getLayoutParams();
        layoutParams.width = mScreen_4;
        cursor.setLayoutParams(layoutParams);
        //设置左侧固定距离
        params = (LinearLayout.LayoutParams)cursor.getLayoutParams();
        params.leftMargin = fixLeftMargin;
        cursor.setLayoutParams(params);
    }

    //改变底部游动条（图文详情、商品实拍、评价）
    private void changeTextView(int position) {
        imgtextInfoTv.setTextColor(Color.parseColor("#666666"));
        photoInfoTv.setTextColor(Color.parseColor("#666666"));
        evalInfoTv.setTextColor(Color.parseColor("#666666"));
        switch (position) {
            case 0:
                imgtextInfoTv.setTextColor(Color.parseColor("#FF7198"));
                break;
            case 1:
                photoInfoTv.setTextColor(Color.parseColor("#FF7198"));
                break;
            case 2:
                evalInfoTv.setTextColor(Color.parseColor("#FF7198"));
                break;
        }
        mCurrentIndex = position;
    }

    @Override
    protected void onPause() {
        mCartDBHelper.closeLink();
        mHistoryDBHelper.closeLink();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        user_id = MainApplication.getInstance().getUser_id();
        mCartDBHelper = CartDBHelper.getInstance(this, 1);
        mCartDBHelper.openWriteLink();
        mHistoryDBHelper = HistoryDBHelper.getInstance(this, 1);
        mHistoryDBHelper.openWriteLink();
       /* if (user_id != null) {
            HistoryInfo historyInfo = new HistoryInfo();
            historyInfo.setUser_id(user_id);
            historyInfo.setGoods_id(goods_id);
            historyInfo.setTitle(info.getTitle());
            historyInfo.setPrice(Float.parseFloat(info.getPrice()));
            historyInfo.setImage(info.getImg().get(0));
            historyInfo.setUpdate_time(DateUtil.getNowDateTime("yyyyMMdd"));
            mHistoryDBHelper.insert(historyInfo);
        }*/
       mHandler.postDelayed(mInsert, 300);
    }

    private Runnable mInsert = new Runnable() {
        @Override
        public void run() {
            if (user_id != null && info != null) {
                HistoryInfo historyInfo = new HistoryInfo();
                historyInfo.setUser_id(user_id);
                historyInfo.setGoods_id(goods_id);
                historyInfo.setTitle(info.getTitle());
                historyInfo.setPrice(Float.parseFloat(info.getPrice()));
                historyInfo.setImage(info.getImg().get(0));
                historyInfo.setUpdate_time(DateUtil.getNowDateTime("yyyyMMdd"));
                mHistoryDBHelper.insert(historyInfo);
            } else {
                mHandler.postDelayed(this, 100);
            }
        }
    };
}

