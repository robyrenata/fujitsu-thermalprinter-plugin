package id.codify.fujitsuthermalprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.fujitsu.fitPrint.Library.FitPrintAndroidUsb_v1011.FitPrintAndroidUsb;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.HashMap;
import java.util.Iterator;


@NativePlugin()
public class FujitsuThermalPrinter extends Plugin {

    FitPrintAndroidUsb mPrinter = new FitPrintAndroidUsb();
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent = null;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private UsbDevice mDevice = null;
    UsbManager musbManager = null;



    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(mDevice != null){
                            //call method to set up device communication
                        }
                        else
                        {

                        }
                    }
                    else {
                        //Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }

        }
    };



    private void GetUsbDevice() {
        mUsbManager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while(deviceIterator.hasNext()) {
            mDevice = deviceIterator.next();
            int nProduct = mDevice.getProductId();
            int nVendor = mDevice.getVendorId();

            if(nVendor == 0x04C5 &&
                    (nProduct == 0x117A ||
                            nProduct == 0x11CA ||
                            nProduct == 0x126E )){
                mUsbManager.requestPermission(mDevice,mPermissionIntent);
                return
            } else {
                mDevice = null;
//                HANDLE ERROR HERE!
            }
        }
    }


    public void Connect(UsbManager usbManager, UsbDevice usbDevice) {

        mPrinter.Connect(usbManager, usbDevice);
    }

    public void PrintImageBmp(Bitmap bmp) {
        mPrinter.PrintImage(bmp);
    }

    public void PrintText(String str) {
        mPrinter.PrintText(str, null);
    }

    @PluginMethod()
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }
}
