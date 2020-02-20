package id.codify.fujitsuthermalprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.fujitsu.fitPrint.Library.FitPrintAndroidUsb_v1011.FitPrintAndroidUsb;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.HashMap;
import java.util.Iterator;


@NativePlugin(
        requestCodes={ 1234}
)
public class FujitsuThermalPrinter extends Plugin {
    static final int REQUEST_CAPACITOR_CODE = 12345;

    FitPrintAndroidUsb mPrinter = new FitPrintAndroidUsb();
    Context context = null;
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent = null;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private UsbDevice mDevice = null;
    UsbManager musbManager = null;


    private final Handler handler = new Handler();
    public int mRtn = 0 ;

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


    public void load() {
        context = getContext();
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, REQUEST_CAPACITOR_CODE, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);
    }

    @PluginMethod()
    public void GetUsbDevice(PluginCall call) {
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Boolean hasNext = deviceIterator.hasNext();
        if(!hasNext){
            call.error("Not Find Printer");
        }

        while(deviceIterator.hasNext()) {
            mDevice = deviceIterator.next();
            int nProduct = mDevice.getProductId();
            int nVendor = mDevice.getVendorId();
            if(nVendor == 0x04C5 &&
                    (nProduct == 0x117A ||
                            nProduct == 0x11CA ||
                            nProduct == 0x126E )){
                mUsbManager.requestPermission(mDevice,mPermissionIntent);
                return;
            } else {
                mDevice = null;
                call.error("Not Find Printer");
            }
        }
    }


    @PluginMethod()
    public void Connect(PluginCall call) {
        mRtn = mPrinter.Connect(mUsbManager, mDevice);
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                savedCall.error(ErrorValue(mRtn));
            }});
    }

    @PluginMethod()
    public void Disconnect(PluginCall call) {
        mPrinter.Disconnect();
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", "success");
                savedCall.success(res);
            }});
    }


    public void PrintImageBmp(Bitmap bmp) {
        mPrinter.PrintImage(bmp);
    }

    @PluginMethod()
    public void PrintText(PluginCall call) {
        String code = call.getString("code");
        mPrinter.PrintText(code, null);
        JSObject ret = new JSObject();
        ret.put("message", "Success Print");
        call.success(ret);
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(getLogTag(), "handling request perms result");

        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                JSObject err = new JSObject();
                err.put("errorType", "UngrantedPermissions");
                err.put("errorMessage", "User denied permission");
                savedCall.error(err.toString());
                return;
            }
        }

        if(requestCode == REQUEST_CAPACITOR_CODE){
            Log.d(getLogTag(), "User granted permission");

            JSObject res = new JSObject();
            res.put("grantedPerm", true);
            res.put("message", "User granted permission");
            savedCall.success(res);
        }
    }

    @PluginMethod()
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }

    private String StatusValue(int StatusNum)
    {
        String Result = "Online(0)";

        // 200
        if(StatusNum == 200)
        {
            Result = "Offline(200)";
        }
        else if(StatusNum == 202)
        {
            Result = "Paper Near End(202) ";
        }
        else if(StatusNum == 301)
        {
            Result = "Cover Open(301) ";
        }
        else if(StatusNum == 302)
        {
            Result = "Paper End(302) ";
        }
        else if(StatusNum == 303)
        {
            Result = "Head Hot(303) ";
        }
        else if(StatusNum == 304)
        {
            Result = "Paper Layout Error(304) ";
        }
        else if(StatusNum == 305)
        {
            Result = "Cutter Jam(305) ";
        }
        else if(StatusNum == 700)
        {
            Result = "Hard Error(700) ";
        }
        else if(StatusNum == 1500)
        {
            Result = "Communication Error(1500) ";
        }
        else if(StatusNum == -3003)
        {
            Result = "Not Ready Status(-3003) ";
        }

        return Result;

    }

    private String ErrorValue(int ErrorStatus)
    {
        String Result = "Success(0)";

        // -1000
        if(ErrorStatus == -1000)
        {
            Result = "Parameter Error(-1000)";
        }
        else if(ErrorStatus == -1001)
        {
            Result = "Invalid Devices(-1001) ";
        }
        else if(ErrorStatus == -1002)
        {
            Result = "Parameter is Null(-1002) ";
        }
        else if(ErrorStatus == -1003)
        {
            Result = "Illegal data length(-1003) ";
        }
        else if(ErrorStatus == -1004)
        {
            Result = "Encoding undefined(-1004) ";
        }
        else if(ErrorStatus == -1005)
        {
            Result = "Value out of range(-1005) ";
        }
        // -1100
        else if(ErrorStatus == -1100)
        {
            Result = "Illegal characters bar code data(-1100) ";
        }
        else if(ErrorStatus == -1101)
        {
            Result = "Illegal length bar code data(-1101) ";
        }
        // -2000
        else if(ErrorStatus == -2000)
        {
            Result = "Communication Error(-2000) ";
        }
        else if(ErrorStatus == -2001)
        {
            Result = "Connect failure(-2001) ";
        }
        else if(ErrorStatus == -2002)
        {
            Result = "Not connected(-2002) ";
        }
        else if(ErrorStatus == -2003)
        {
            Result = "Time out(-2003) ";
        }
        // -3000
        else if(ErrorStatus == -3000)
        {
            Result = "File access failure(-3000) ";
        }
        else if(ErrorStatus == -3001)
        {
            Result = "File failed to read(-3001) ";
        }
        else if(ErrorStatus == -3002)
        {
            Result = "Failures to receive status(-3002) ";
        }
        else if(ErrorStatus == -3003)
        {
            Result = "Not Ready Status(-3003) ";
        }

        return Result;

    }
}
