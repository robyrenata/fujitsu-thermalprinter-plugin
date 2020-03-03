package id.codify.fujitsuthermalprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Base64;
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
    int nRtn ;

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

        if(hasPermission(ACTION_USB_PERMISSION)){
            JSObject res = new JSObject();
            res.put("message", "User already granted permission");
            call.success(res);
        }else{
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
    }


    @PluginMethod()
    public void Connect(PluginCall call) {
        mRtn = mPrinter.Connect(mUsbManager, mDevice);
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();
                if(mRtn == 0){
                    JSObject res = new JSObject();
                    res.put("message", "Printer Connected");
                    savedCall.success(res);
                }else{
                    savedCall.error(ErrorValue(mRtn));
                }
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

    @PluginMethod()
    public void GetPrinterStatus(PluginCall call){
        mRtn = mPrinter.GetPrinterStatus();
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", StatusValue(mRtn));
                savedCall.success(res);
            }});
    }


    @PluginMethod()
    public void OpenDrawer(PluginCall call){
        Integer signal = call.getInt("signal");
        Integer t1 = call.getInt("t1");
        Integer t2 = call.getInt("t2");
        mRtn = mPrinter.OpenDrawer(signal, t1, t2);
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", DriverStatus(mRtn));
                savedCall.success(res);
            }});
    }

    @PluginMethod()
    public void Beep(PluginCall call){
        Integer t1 = call.getInt("t1");
        Integer t2 = call.getInt("t2");
        mRtn = mPrinter.Beep(t1, t2);
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", DriverStatus(mRtn));
                savedCall.success(res);
            }});
    }


    @PluginMethod()
    public void Buzzer(PluginCall call){
        Integer pattern = call.getInt("pattern");
        Integer count = call.getInt("count");
        mRtn = mPrinter.Buzzer(pattern, count);
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", DriverStatus(mRtn));
                savedCall.success(res);
            }});
    }

    @PluginMethod()
    public void CutPapper(PluginCall call){
        Integer cutType = call.getInt("cutType");
        mRtn = mPrinter.CutPaper(cutType);
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", DriverStatus(mRtn));
                savedCall.success(res);
            }});
    }


    @PluginMethod()
    public void PrintPage(PluginCall call){
        mRtn = mPrinter.PrintPage();
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", DriverStatus(mRtn));
                savedCall.success(res);
            }});
    }

    @PluginMethod()
    public void CancelPage(PluginCall call){
        mRtn = mPrinter.CancelPage();
        saveCall(call);

        handler.post(new Runnable() {
            @Override
            public void run() {
                PluginCall savedCall = getSavedCall();

                JSObject res = new JSObject();
                res.put("message", DriverStatus(mRtn));
                savedCall.success(res);
            }});
    }

    @PluginMethod()
    public void PrintImageBmp(PluginCall call) {
        String base64 = removePrefix(call.getString("base64"),"data:image/png;base64,");
        Integer cutPaper = call.getInt("cutPaper");
        Integer paperFeed = isNullOrEmpty(call.getInt("paperFeed").toString()) ? call.getInt("paperFeed") : 64;

        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        nRtn = mPrinter.PrintImage(decodedByte);

        nRtn = mPrinter.PaperFeed(paperFeed);

        if(!isNullOrEmpty(cutPaper.toString())){
            nRtn = mPrinter.CutPaper(cutPaper);
        }

        JSObject ret = new JSObject();
        ret.put("message", "Success Print Bitmap" + nRtn);
        call.success(ret);
    }

    @PluginMethod()
    public void PrintText(PluginCall call) {
        String code = call.getString("code");
        Integer paperFeed = isNullOrEmpty(call.getInt("paperFeed").toString()) ? call.getInt("paperFeed") : 64;
        Integer cutPaper = call.getInt("cutPaper");
        nRtn = mPrinter.SetLocale(8);

        nRtn = mPrinter.PrintText(code, "SJIS");

        nRtn = mPrinter.PaperFeed(paperFeed);

        if(!isNullOrEmpty(cutPaper.toString())){
            nRtn = mPrinter.CutPaper(cutPaper);
        }

        JSObject ret = new JSObject();
        ret.put("message", "Success Print Text" + nRtn);
        call.success(ret);
    }

    @PluginMethod()
    public void PrintBarcode(PluginCall call) {
        String code = call.getString("code");
        Integer paperFeed = isNullOrEmpty(call.getInt("paperFeed").toString()) ? call.getInt("paperFeed") : 64;
        Integer barWidth = isNullOrEmpty(call.getInt("barWidth").toString()) ? call.getInt("barWidth") : 2;
        Integer barHeight = isNullOrEmpty(call.getInt("barHeight").toString()) ? call.getInt("barHeight") : 100;
        Integer cutPaper = call.getInt("cutPaper");
        nRtn = mPrinter.SetLocale(8);

        nRtn = mPrinter.PrintBarcode(2, code, 2, 0, barWidth, barHeight, 0) ;
        nRtn = mPrinter.PaperFeed(paperFeed);

        if(!isNullOrEmpty(cutPaper.toString())){
            nRtn = mPrinter.CutPaper(cutPaper);
        }

        JSObject ret = new JSObject();
        ret.put("message", "Success Print Barcode" + nRtn);
        call.success(ret);
    }

    @PluginMethod()
    public void PrintQR(PluginCall call) {
        String code = call.getString("code");

        Integer paperFeed = isNullOrEmpty(call.getInt("paperFeed").toString()) ? call.getInt("paperFeed") : 64;
        Integer model = isNullOrEmpty(call.getInt("model").toString()) ? call.getInt("model") : 0;
        Integer cellSize = isNullOrEmpty(call.getInt("cellSize").toString()) ? call.getInt("cellSize") : 6;
        Integer cutPaper = call.getInt("cutPaper");

        nRtn = mPrinter.SetLocale(8);

        nRtn = mPrinter.PrintQrCode(code, model, cellSize, false, 0);
        nRtn = mPrinter.PaperFeed(paperFeed);

        if(!isNullOrEmpty(cutPaper.toString())){
            nRtn = mPrinter.CutPaper(cutPaper);
        }

        JSObject ret = new JSObject();
        ret.put("message", "Success Print QR " + nRtn);
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

    private String DriverStatus(int StatusNum)
    {
        String Result = "";

        if(StatusNum == 1000)
        {
            Result = "Parameter Error(1000) ";
        }
        else if(StatusNum == 1001)
        {
            Result = "Invalid device is designated(1001) ";
        }
        else if(StatusNum == 1002)
        {
            Result = "NULL is designated(1002) ";
        }
        else if(StatusNum == 1003)
        {
            Result = "Length of data is incorrect.(1003) ";
        }
        else if(StatusNum == 1004)
        {
            Result = "Encode which is not defined is designated.(1004) ";
        }
        else if(StatusNum == 1005)
        {
            Result = "Value is out of limit.(1005) ";
        }
        else if(StatusNum == 1100)
        {
            Result = "Incorrect characters in barcode data, or out of specifications.(1100) ";
        }
        else if(StatusNum == 1101)
        {
            Result = "Incorrect length of barcode data.(1101) ";
        }
        else if(StatusNum == 2000)
        {
            Result = "Error in transmission.(2000) ";
        }
        else if(StatusNum == 2001)
        {
            Result = "Failed connection(2001) ";
        }
        else if(StatusNum == 2002)
        {
            Result = "Not connected.(2002) ";
        }
        else if(StatusNum == 2003)
        {
            Result = "Time out(2003) ";
        }
        else if(StatusNum == 3000)
        {
            Result = "Failed access of file(3000) ";
        }
        else if(StatusNum == 3001)
        {
            Result = "Failed read-in file(3001) ";
        }
        else if(StatusNum == 3002)
        {
            Result = "Failed the status receiving.(3002) ";
        }
        else if(StatusNum == 3003)
        {
            Result = "The setting is the status receiving.(3003) ";
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

    public static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }

    public static String removePrefix(String s, String prefix)
    {
        if (s != null && prefix != null && s.startsWith(prefix)){
            return s.substring(prefix.length());
        }
        return s;
    }
}
