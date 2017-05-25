package com.example.zxj.bttest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.github.library.BaseRecyclerAdapter;
import com.github.library.BaseViewHolder;
import com.github.library.listener.OnRecyclerItemClickListener;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

/**
 * Created by ZXJ on 2017/5/23.
 * 设备列表
 */

public class DeviceInitializeActivity extends FatherActivity {

    @BindView(R.id.lv_data)
    android.support.v7.widget.RecyclerView lvData;
    private ArrayList<SearchResult> list = new ArrayList<SearchResult>();
    private BaseRecyclerAdapter mAdapter;
    private SearchResult deviceSelect;
    private boolean mConnected;
    @Override
    protected int getLayoutId() {
        return R.layout.act_device_init;
    }

    @Override
    protected void initValues() {
        initDefautHead("选择链接设备", true);
    }

    @Override
    protected void initView() {
        mAdapter = new BaseRecyclerAdapter<SearchResult>(this, list, R.layout.device_list_item) {
            @Override
            protected void convert(BaseViewHolder helper, SearchResult item) {
                helper.setText(R.id.name, item.getName());
                helper.setText(R.id.mac, item.getAddress());
                helper.setText(R.id.rssi, String.format("Rssi: %d", item.rssi));
                Beacon beacon = new Beacon(item.scanRecord);
                helper.setText(R.id.adv, beacon.toString());

            }
        };
        mAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                 deviceSelect = (SearchResult) mAdapter.getItem(position);
                ClientManager.getClient().registerConnectStatusListener(deviceSelect.getAddress(), mConnectStatusListener);
                connectDeviceIfNeeded();
            }
        });
        lvData.setHasFixedSize(true);
        lvData.setLayoutManager(new LinearLayoutManager(this));
        lvData.setItemAnimator(new DefaultItemAnimator());
        mAdapter.openLoadAnimation(false);
        lvData.setAdapter(mAdapter);
        initTextHeadRigth("刷新", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevice();
            }
        });
        searchDevice();
    }

    @Override
    protected void doOperate() {

    }
    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 2).build();
//  .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
        ClientManager.getClient().search(request, mSearchResponse);
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
           showWaitDialog();
            list.clear();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!list.contains(device)) {
                dismissWaitDialog();
                list.add(device);
                mAdapter.setData(list);
            }

        }

        @Override
        public void onSearchStopped() {

        }

        @Override
        public void onSearchCanceled() {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().stopSearch();
    }
    private void connectDevice() {
        showWaitDialog();
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(10000)
                .build();

        ClientManager.getClient().connect(deviceSelect.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
             dismissWaitDialog();
                if (code == REQUEST_SUCCESS) {
                    //链接成功
                    WWToast.showShort("链接成功");
                    Intent intent=new Intent();
                    intent.putExtra("data", deviceSelect);
                    setResult(RESULT_OK,intent);
                    finish();
                }else{
                    //链接失败
                    WWToast.showShort("链接失败");
                }
            }
        });
    }
    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);
            connectDeviceIfNeeded();
        }
    };
    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    @Override
    protected void onDestroy() {
        if(mConnected){
            ClientManager.getClient().disconnect(deviceSelect.getAddress());
        }

        ClientManager.getClient().unregisterConnectStatusListener(deviceSelect.getAddress(), mConnectStatusListener);
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
