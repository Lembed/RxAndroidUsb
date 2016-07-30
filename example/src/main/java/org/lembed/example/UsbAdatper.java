package org.lembed.example;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.em.example.R;

import org.lembed.RxUsbDevice;

import java.util.ArrayList;
import java.util.List;


public class UsbAdatper extends RecyclerView.Adapter<UsbAdatper.ViewHolder> {
    private View.OnClickListener onClickListener;
    private final List<RxUsbDevice> usbDeviceList = new ArrayList<>();


    @Override
    public UsbAdatper.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_device_item, parent, false);
        return new ViewHolder(v, onClickListener);
    }

    @Override
    public void onBindViewHolder(UsbAdatper.ViewHolder holder, int position) {
        final RxUsbDevice usbDevice = usbDeviceList.get(position);
        holder.deviceName.setText(" Name: " + usbDevice.getNativeDevice().getDeviceName()+"");
        holder.deviceId.setText(" ID: " + usbDevice.getNativeDevice().getDeviceId()+"");
    }

    @Override
    public int getItemCount() {
        return usbDeviceList.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView deviceName;
        public TextView deviceId;

        public ViewHolder(View view,View.OnClickListener onClickListener){
            super(view);
            deviceName = (TextView) view.findViewById(R.id.device_name);
            deviceId = (TextView) view.findViewById(R.id.device_id);

            view.setOnClickListener(onClickListener);
        }
    }

    public RxUsbDevice getItem(int position) {
        return usbDeviceList.get(position);
    }

    public void setResult(List<RxUsbDevice> list) {
        this.usbDeviceList.clear();
        this.usbDeviceList.addAll(list);

        notifyDataSetChanged();
    }

    public void addResult(RxUsbDevice usbDevice) {
      if(usbDeviceList.contains(usbDevice)) return;

        usbDeviceList.add(usbDevice);

        notifyDataSetChanged();
    }

}
