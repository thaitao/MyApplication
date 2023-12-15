package com.example.employeecheckin;

//import java.io.IOException;
//import java.io.OutputStream;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;

public class USBOp
{
	  public static final String LOGNAME = "ZZYLG";
	  public static final int MAX_GLOBAL_BUFF_LEN= 30*1024;
	  //public static final int USB_TRANS_TIMEOUT=500;
	  public static final int USB_TRANS_TIMEOUT=2000;
	  public static final int USB_TRANS_MAX_LEN=64;
	  public static final int USB_TRANS_MAX_DATA_LEN=USB_TRANS_MAX_LEN-2;
	  private int availableLen;
	  private byte []globalBuf=new byte[MAX_GLOBAL_BUFF_LEN];
	  private byte []sndBuffer=new byte[USB_TRANS_MAX_LEN];
	  private byte []rcvBuffer=new byte[USB_TRANS_MAX_LEN];
	  
	  private Context mContext;
	  private String mString;
	  //private UsbManager mUsbmanager;
	  private PendingIntent mPendingIntent;
	  
	  private UsbEndpoint epOut;
	  private UsbEndpoint epIn;
	  private UsbManager myUsbManager;
	  private UsbDevice myUsbDevice;
	  private UsbInterface myInterface;
	  private UsbDeviceConnection myDeviceConnection;
	  
	  //20201010����
	  private UsbRequest myRequest;
	  
	  private boolean BroadcastFlag = false;
	  private boolean UsbOpenflag=false;//�Ƿ�ɹ����豸
	  
	  private int VendorID = 0x096e;    //����Ҫ�ĳ��Լ���Ӳ��ID
	  private int ProductID = 0x0603;
	  private int mInterfaceClass= 0x03;
	  private int mInterfaceSub= 0x01;
	  private int mInterfaceProtocol= 0x02;
	
	public USBOp(UsbManager manager, Context context, String AppName)
	{
		this.myUsbManager = manager;
	    this.mContext = context;
	    this.mString = AppName;
		
	}
	public void SetUsbConfig(int npid,int nvid,int nInterfaceClass,int nInterfaceSub,int nInterfaceProtocol)
	{
		this.ProductID=npid;
		this.VendorID=nvid;
		this.mInterfaceClass=nInterfaceClass;
		this.mInterfaceSub=nInterfaceSub;
		this.mInterfaceProtocol=nInterfaceProtocol;
		USBOpInit();	
	}
	public boolean USBOpInit()
	{
		UsbOpenflag=false;
		enumerateDevice();
		 
        findInterface();
 
        openDevice();
 
        assignEndpoint();
        return UsbOpenflag;
        
	}
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
	{
	    public void onReceive(Context context, Intent intent)
	    {
	      String action = intent.getAction();
	      if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
	        return;
	      }
	      if (USBOp.this.mString.equals(action)) {
	        synchronized (this)
	        {
	          UsbDevice localUsbDevice = (UsbDevice)intent.getParcelableExtra("device");
	          if (intent.getBooleanExtra("permission", false))
	          {
	        	  USBOp.this.openDevice();
	          }
	          else
	          {
	            Toast.makeText(USBOp.this.mContext, "Deny USB Permission", 0).show();
	            Log.d("123456789", "permission denied");
	          }
	        }
	      }
	      if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action))
	      {
	        Toast.makeText(USBOp.this.mContext, "Disconnect", 0).show();
	        USBOp.this.CloseDevice();
	      }
	      else
	      {
	        Log.d("123456789", "......");
	      }
	    }
	  };
	  public synchronized void CloseDevice()
	  {
	    try
	    {
	      Thread.sleep(10L);
	    }
	    catch (Exception localException) {}
	    if (this.myDeviceConnection != null)
	    {
	      if (this.myInterface != null)
	      {
	        this.myDeviceConnection.releaseInterface(this.myInterface);
	        this.myInterface = null;
	      }
	      this.myDeviceConnection.close();
	    }
	    if (this.myUsbDevice != null) {
	      this.myUsbDevice = null;
	    }
//	    if (this.myUsbManager != null) {
//	      this.myUsbManager = null;
//	    }
	    if (this.BroadcastFlag)
	    {
	      this.mContext.unregisterReceiver(this.mUsbReceiver);
	      this.BroadcastFlag = false;
	    }
	    UsbOpenflag=false;
	  } 
	
	  private void enumerateDevice(){
		
		if (this.myUsbManager == null)
            return;
 
        HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();
        /*new AlertDialog.Builder(mContext).setTitle("����" )  

        .setMessage("kaisjinrushebei" )  

        .setPositiveButton("ȷ��" ,  null )  

        .show(); */
        if (!deviceList.isEmpty()) { // deviceList��Ϊ��
            //StringBuffer sb = new StringBuffer();
        	//Toast.makeText(MainActivity.this, "Please input card data!", Toast.LENGTH_SHORT).show();
        	
        	
        	for (UsbDevice device : deviceList.values()) {
        		/*new AlertDialog.Builder(mContext).setTitle("����" )  

                //.setMessage(device.getDeviceName() )
        		.setMessage(String.valueOf(device.getProductId()) )

                .setPositiveButton( "ȷ��" ,  null )  

                .show(); */
                
        
                // ö�ٵ��豸
                if (device.getVendorId() == VendorID
                        && device.getProductId() == ProductID) {
                    this.myUsbDevice = device;
                    Log.e(LOGNAME, "ö���豸�ɹ�");
                }
            }
        }
	}
	private void findInterface() {
        if (myUsbDevice != null) {
            for (int i = 0; i < myUsbDevice.getInterfaceCount(); i++) {
                UsbInterface intf = myUsbDevice.getInterface(i);
                // �������ϵ��豸��һЩ�жϣ���ʵ��Щ��Ϣ��������ö�ٵ��豸ʱ��ӡ����
                if (intf.getInterfaceClass() == mInterfaceClass
                        && intf.getInterfaceSubclass() == mInterfaceSub
                        && intf.getInterfaceProtocol() == mInterfaceProtocol) {
                    myInterface = intf;
                    Log.d(LOGNAME, "�ҵ��ҵ��豸�ӿ�");
                }
                break;
            }
        }
    }
	
	/**
     * ���豸
     */
    private void openDevice() {
    	////ע��㲥
    	this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(this.mString), PendingIntent.FLAG_IMMUTABLE);
    	IntentFilter filter = new IntentFilter(this.mString);
    	filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
    	this.mContext.registerReceiver(this.mUsbReceiver, filter);
    	this.BroadcastFlag = true;
    	////////////////////////////////////////////////////////////////////////////////
    	
        if (myInterface != null) {
            UsbDeviceConnection conn = null;
            // ��openǰ�ж��Ƿ�������Ȩ�ޣ���������Ȩ�޿��Ծ�̬���䣬Ҳ���Զ�̬����Ȩ�ޣ����Բ����������
            if (myUsbManager.hasPermission(myUsbDevice)) {
                conn = myUsbManager.openDevice(myUsbDevice);
            }
            else
            {
            	//PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            	myUsbManager.requestPermission(myUsbDevice, mPendingIntent);
            	
            }
 
            if (conn == null) {
                return;
            }
 
            if (conn.claimInterface(myInterface, true)) {
                myDeviceConnection = conn; // �������android�豸�Ѿ�����HID�豸
                
                UsbOpenflag=true;
                Log.d(LOGNAME, "���豸�ɹ�");
            } else {
                conn.close();   
            }
        }
    }
    
	////////////////////////////////////////////////////////////////////////
	/**
	* ����˵㣬IN | OUT��������������˴���ֱ����1ΪOUT�˵㣬0ΪIN����Ȼ��Ҳ����ͨ���ж�
	*/
	//USB_ENDPOINT_XFER_BULK 
	/* 
	#define USB_ENDPOINT_XFER_CONTROL 0 --���ƴ���
	#define USB_ENDPOINT_XFER_ISOC 1 --��ʱ����
	#define USB_ENDPOINT_XFER_BULK 2 --�鴫��
	#define USB_ENDPOINT_XFER_INT 3 --�жϴ��� 
	* */

	private void assignEndpoint() {
	if (myInterface != null) { //��һ�䲻�ӵĻ� �����ױ���  ���ºܶ����ڸ�����̳��:Ϊʲô����ѽ 
	
	//����Ĵ����滻��һ�� ���Լ�Ӳ�������жϰ�
	
	for (int i = 0; i < myInterface.getEndpointCount(); i++) { 
	
		UsbEndpoint ep = myInterface.getEndpoint(i);
		
		if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) { 
			if (ep.getDirection() == UsbConstants.USB_DIR_OUT) { 
			epOut = ep;
			
			} else { 
			
			epIn = ep;
			
			} 
			}
		}
	}
	
	Log.d(LOGNAME, "assignEndpoint suc");
	}
	
	public boolean UsbFeatureSupported()
	{
		
		//�ж��ֻ��Ƿ�֧��HOST
		boolean bool = this.mContext.getPackageManager().hasSystemFeature("android.hardware.usb.host");
		return bool;
	}
	private void initGlobalBuf(int []intArr, int arrLen) {
		availableLen = 0;
		int n;
		for(n=0;n<MAX_GLOBAL_BUFF_LEN;n++)
			globalBuf[n]=0x00;
		for (n = 0; n < arrLen; n++)
			globalBuf[n] = (byte) (intArr[n]);
		availableLen = arrLen;
	}
	
	void ucharp2jintp(int []intArr, byte []ucharp, int arrLen) {
		int n;
		for (n = 0; n < arrLen; n++)
			intArr[n] = (int)(ucharp[n]&0xFF);
	}
	
	/**
	 * �������ܣ���дMSR����
	 * @param data
	 * @param len
	 * @return
	 */
	public int sendDataMsr(int[] data,int len)
	{
		int iRet = -1;
		int i;
		if(!UsbOpenflag)//���U��δ�򿪣���������ȥ��
		{
			if(!USBOpInit())
				return -1;
		}
		
		initGlobalBuf(data, len);
		byte header = globalBuf[0];
		byte cmd 	= globalBuf[1];
		int leftLen = availableLen - 2;
		int dataValidLen;
		int startIndex=2;
		while(leftLen>=0)
		{
			Log.d("gary", "leftLen= " + leftLen);
			for(i=0;i<USB_TRANS_MAX_LEN;i++)
				sndBuffer[i]=0x00;
			sndBuffer[0]=header;
			if(leftLen == (availableLen - 2))
				sndBuffer[1]=cmd;
			else
				sndBuffer[1]=(byte)(cmd|(1<<7));
				
			System.arraycopy(globalBuf, startIndex, sndBuffer, 2, USB_TRANS_MAX_DATA_LEN);
			Log.d("gary", "send: " + converB2String(sndBuffer, USB_TRANS_MAX_DATA_LEN));
			iRet=myDeviceConnection.bulkTransfer(epOut, sndBuffer, sndBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet<0)
				return iRet;
			leftLen -= USB_TRANS_MAX_DATA_LEN;
			startIndex+=USB_TRANS_MAX_DATA_LEN;	
		}
		Log.d("gary", "jies leftLen= " + leftLen);
		///��������
		for(i=0;i<USB_TRANS_MAX_LEN;i++)
			rcvBuffer[i]=0x00;
		//rcvBuffer
		Arrays.fill(rcvBuffer, (byte)0x00);
		Arrays.fill(globalBuf, (byte)0x00);
		iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
		if(iRet<0)
			return iRet;
		Log.d("gary", "rec: " + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
		//leftLen = rcvBuffer[2] << 8 | rcvBuffer[3];
		leftLen=(rcvBuffer[2]&0xFF)*256;
		leftLen+=(rcvBuffer[3]&0xFF);
		dataValidLen = leftLen + 4;
		leftLen -= USB_TRANS_MAX_DATA_LEN;
		startIndex=0;
		System.arraycopy(rcvBuffer,0,globalBuf, startIndex, USB_TRANS_MAX_LEN);
		startIndex+=USB_TRANS_MAX_LEN;
		while(leftLen > 0)
		{
			
			Arrays.fill(rcvBuffer,(byte)0x00);
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet>=0)
			{
				
				System.arraycopy(rcvBuffer, 2, globalBuf, startIndex, USB_TRANS_MAX_DATA_LEN);
				Log.d("gary", "rec: " + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
				leftLen=leftLen-USB_TRANS_MAX_DATA_LEN;
				startIndex+=USB_TRANS_MAX_DATA_LEN;
			}
			else	//�������ֱ�ӷ���ʧ��
			{
					return iRet;
				
			}
				
			
		}
		//�����ӷ���
		int max = dataValidLen > len ? dataValidLen : len;
		for(i=0;i<max;i++)
			data[i]=0;
		ucharp2jintp(data, globalBuf, dataValidLen);
		return dataValidLen;
	}
	//��RFID����Ч����
	public int readDataSubRfid(byte[]data)
	{
		
		long myStartTime;
		int iRet = -1;
		int i;
		if(!UsbOpenflag)//���U��δ�򿪣���������ȥ��
		{
			if(!USBOpInit())
				return -1;
		}
		byte []tmpGlobalBuf=new byte[1024];//�����ϲ��ᳬ��������
		int leftLen = 0;	///leftLen��Ч����
		int dataValidLen;
		int startIndex=2;
		///��������
		//rcvBuffer
		
		Arrays.fill(tmpGlobalBuf, (byte)0x00);
		myStartTime=System.currentTimeMillis();
		while(true)
		{
			
			if((System.currentTimeMillis()-myStartTime)/1000>2)
				return -1002;//�����ݳ�ʱ
			Arrays.fill(rcvBuffer, (byte)0x00);
		
		//iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, 64, USB_TRANS_TIMEOUT);
			if(iRet>=0)
				break;
			Log.d("gary", "rec:" + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
		}
		leftLen=(rcvBuffer[2]&0xFF)*256;
		leftLen+=(rcvBuffer[3]&0xFF);
		//dataValidLen = leftLen + 4;	///���ص����������ݳ���Ϊ ��������+ 1BYTE head +1BYTE  0x50+2BYTE���ȱ���
		dataValidLen = leftLen;
		leftLen -= (USB_TRANS_MAX_DATA_LEN-2);//��һ������ʵ�����ݳ���Ӧ�ü�ȥ1BYTE head +1BYTE  0x50+2BYTE���ȱ��� Ҳ����64-4  20190831
		startIndex=0;
		//System.arraycopy(rcvBuffer,0,tmpGlobalBuf, startIndex, USB_TRANS_MAX_LEN);
		if(dataValidLen>(USB_TRANS_MAX_DATA_LEN-2))
		{
			System.arraycopy(rcvBuffer,4,tmpGlobalBuf, startIndex, USB_TRANS_MAX_DATA_LEN-2);
			startIndex+=USB_TRANS_MAX_DATA_LEN-2;
		}
		else
		{
			System.arraycopy(rcvBuffer,4,tmpGlobalBuf, startIndex, dataValidLen);
			startIndex+=dataValidLen;
		}
		while(leftLen > 0)
		{
			
			Arrays.fill(rcvBuffer,(byte)0x00);
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet>=0)
			{
				Log.d("gary", "rec:" + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
				System.arraycopy(rcvBuffer, 2, tmpGlobalBuf, startIndex, USB_TRANS_MAX_DATA_LEN);
				leftLen=leftLen-USB_TRANS_MAX_DATA_LEN;
				startIndex+=USB_TRANS_MAX_DATA_LEN;
			}
			else	//�������ֱ�ӷ���ʧ��
			{
				String s = String.valueOf(iRet);
				Log.d("gary", "iRet:" + s);	
				return iRet;	
			}
				
			
		}
		//�����ӷ���
		//int max = dataValidLen > len ? dataValidLen : len;
		int max=dataValidLen;
		for(i=0;i<max;i++)
			data[i]=0;
		//ucharp2jintp(data, tmpGlobalBuf, dataValidLen);
		System.arraycopy(tmpGlobalBuf, 0, data, 0, dataValidLen);
		return dataValidLen;
	}
	
	/**
	 * �������ܣ���дRFID����
	 * @param data
	 * @param len
	 * @return
	 */
	public int sendDatarRfid(int[] data,int len)
	{
		int iRet = -1;
		int i;
		long tmpStartTime;
		byte []tmpRecBuf=new byte[1024];
		byte[] GOALBUF=new byte[9];
		int tmpRecBufLen=0;
		GOALBUF[0]=0x00;
		GOALBUF[1]=0x00;
		GOALBUF[2]=(byte)0xFF;
		GOALBUF[3]=0x00;
		GOALBUF[4]=(byte)0xFF;
		GOALBUF[5]=0x00;
		GOALBUF[6]=0x00;
		GOALBUF[7]=0x00;
		GOALBUF[8]=(byte)0xFF;
		if(!UsbOpenflag)//���U��δ�򿪣���������ȥ��
		{
			if(!USBOpInit())
				return -1;
		}
		//myDeviceConnection.claimInterface(myInterface, BroadcastFlag);
		//myDeviceConnection.releaseInterface(myInterface);
		//myInterface.
		//myDeviceConnection.
		initGlobalBuf(data, len);
		byte header = globalBuf[0];
		byte cmd 	= globalBuf[1];
		int leftLen = availableLen - 2;	///leftLen��Ч����
		int dataValidLen;
		int startIndex=2;
		while(leftLen>=0)
		{
			
			for(i=0;i<USB_TRANS_MAX_LEN;i++)
				sndBuffer[i]=0x00;
			sndBuffer[0]=header;
			if(leftLen == (availableLen - 2))///����ǵ�һ����0x50����
				sndBuffer[1]=cmd;
			else
				sndBuffer[1]=(byte)(cmd|(1<<7));
				
			System.arraycopy(globalBuf, startIndex, sndBuffer, 2, USB_TRANS_MAX_DATA_LEN);
			Log.d("gary", "send: " + converB2String(sndBuffer, USB_TRANS_MAX_DATA_LEN));
			iRet=myDeviceConnection.bulkTransfer(epOut, sndBuffer, sndBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet<0)
				return iRet;
			leftLen -= USB_TRANS_MAX_DATA_LEN;
			startIndex+=USB_TRANS_MAX_DATA_LEN;	
		}
		///��������
		///��ACK
		tmpStartTime=System.currentTimeMillis();
		while(true)
		{
			if((System.currentTimeMillis()-tmpStartTime)/1000>2)
				return -1002;//�����ݳ�ʱ
			
			for(i=0;i<USB_TRANS_MAX_LEN;i++)
				rcvBuffer[i]=0x00;
			//rcvBuffer
			Arrays.fill(tmpRecBuf, (byte)0x00);
			Arrays.fill(rcvBuffer, (byte)0x00);
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet<0)
				return iRet;
			Log.d("gary", "rec:" + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
			
			leftLen=(rcvBuffer[2]&0xFF)*256;
			leftLen+=(rcvBuffer[3]&0xFF);
			if(leftLen!=6)//ACK���ȹ̶�Ϊ6
				continue;
			for(i=0;i<6;i++)
			{
				if(rcvBuffer[4+i]!=GOALBUF[i])
					break;
				
			}
			if(i<6)
				continue;
			
			dataValidLen = leftLen;
			
			if(dataValidLen<=(USB_TRANS_MAX_LEN-4))
			{
				System.arraycopy(rcvBuffer,4,tmpRecBuf, 0, dataValidLen);///�������60ʱ����������
				tmpRecBufLen+=dataValidLen;
				break;
			}
			else
			{
				//���ACK�ĳ��ȴ�����60ֱ�Ӿ��Ǵ���İ�
				return -1;
				
			}
		}
		///��ACK���
		///��ʵ������
		//rcvBuffer
		//����һ����
		startIndex=0;
		Arrays.fill(globalBuf, (byte)0x00);
		Arrays.fill(rcvBuffer, (byte)0x00);
		iRet=readDataSubRfid(rcvBuffer);
		//if(iRet<0)
		if(iRet<5)//�����ĵ�һ�������Ȳ���С��5�������Ƕ�ʧ��֮�󷵻صĸ���
			return iRet;
		for(i=0;i<3;i++)
		{
			if(rcvBuffer[i]!=GOALBUF[6+i])
				break;
			
		}
		if(i<3)
			return -1;
		System.arraycopy(rcvBuffer,0,globalBuf, startIndex, iRet);
		startIndex+=iRet;
		//Ϊ�˷�ֱֹ��ȡֵ0xFF���ָ���,���Խ���GOALBUF[2]
		if((rcvBuffer[3]==GOALBUF[2])&&(rcvBuffer[4]==GOALBUF[2]))//��PNģ�鷵�صĳ��ȴ���256�����
		{
			leftLen=rcvBuffer[5]*256;
			leftLen+=rcvBuffer[6];
			leftLen+=7+3;
			
		}
		else
			leftLen=rcvBuffer[3]+7;
		leftLen-=iRet;
		while(leftLen > 0)
		{
			Arrays.fill(rcvBuffer, (byte)0x00);
			iRet=readDataSubRfid(rcvBuffer);
			if(iRet<0)
				return iRet;
			System.arraycopy(rcvBuffer,0,globalBuf, startIndex, iRet);
			startIndex+=iRet;
			leftLen-=iRet;	
		}
		//�����ӷ���
		int max = startIndex > len ? startIndex : len;
		System.arraycopy(globalBuf, 0, tmpRecBuf, tmpRecBufLen, startIndex);
		tmpRecBufLen+=startIndex;
		for(i=0;i<max;i++)
			data[i]=0;
		ucharp2jintp(data, tmpRecBuf, tmpRecBufLen);
		return tmpRecBufLen;
		
		
		
		/*
		Arrays.fill(rcvBuffer, (byte)0x00);
		Arrays.fill(globalBuf, (byte)0x00);
		//iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
		iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, 500);
		if(iRet<0)
			return iRet;
		Log.d("gary", "rec:" + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
		
		//leftLen=(rcvBuffer[1]&0xFF)*256;
		//leftLen+=(rcvBuffer[2]&0xFF);
		leftLen=(rcvBuffer[2]&0xFF)*256;
		leftLen+=(rcvBuffer[3]&0xFF);
		//dataValidLen = leftLen + 4;	///���ص����������ݳ���Ϊ ��������+ 1BYTE head +1BYTE  0x50+2BYTE���ȱ���
		dataValidLen = leftLen;
		leftLen -= (USB_TRANS_MAX_DATA_LEN-2);//��һ������ʵ�����ݳ���Ӧ�ü�ȥ1BYTE head +1BYTE  0x50+2BYTE���ȱ��� Ҳ����64-4  20190831
		startIndex=0;
		//System.arraycopy(rcvBuffer,0,globalBuf, startIndex, USB_TRANS_MAX_LEN);
		if(dataValidLen<=(USB_TRANS_MAX_LEN-4))
		{
			System.arraycopy(rcvBuffer,4,globalBuf, startIndex, dataValidLen);///����������С��60
			startIndex+=dataValidLen;
		}
		else
		{
			System.arraycopy(rcvBuffer,4,globalBuf, startIndex, USB_TRANS_MAX_LEN-4);/////���������ȴ���60
			startIndex+=(USB_TRANS_MAX_LEN-4);		
		}
		//////////////////////////////////////////////////////////////////////////////////////////
		while(leftLen > 0)
		{
			
			Arrays.fill(rcvBuffer,(byte)0x00);
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet>=0)
			{
				Log.d("gary", "rec:" + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
				System.arraycopy(rcvBuffer, 2, globalBuf, startIndex, USB_TRANS_MAX_DATA_LEN);
				leftLen=leftLen-USB_TRANS_MAX_DATA_LEN;
				startIndex+=USB_TRANS_MAX_DATA_LEN;
			}
			else	//�������ֱ�ӷ���ʧ��
			{
				String s = String.valueOf(iRet);
				Log.d("gary", "iRet:" + s);	
				return iRet;	
			}
				
			
		}
		//�����ӷ���
		int max = dataValidLen > len ? dataValidLen : len;
		System.arraycopy(globalBuf, 0, tmpRecBuf, tmpRecBufLen, dataValidLen);
		tmpRecBufLen+=dataValidLen;
		for(i=0;i<max;i++)
			data[i]=0;
		ucharp2jintp(data, tmpRecBuf, tmpRecBufLen);
		return tmpRecBufLen;	*/
	}
	
	/**
	 * �������ܣ���дFinger����
	 * @param data
	 * @param len
	 * @return
	 */
	public int sendDatarFinger(int[] data,int len)
	{
		int iRet = -1;
		int i;
		long tmpStartTime;
		if(!UsbOpenflag)//���U��δ�򿪣���������ȥ��
		{
			if(!USBOpInit())
				return -1;
		}
		initGlobalBuf(data, len); //finalize

		//myDeviceConnection.releaseInterface(myInterface);//20201009 �建����
		/*int inMax = epIn.getMaxPacketSize();
		ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
		myRequest.initialize(myDeviceConnection, epIn);
		myRequest.queue(byteBuffer, inMax);
		myRequest.cancel();*/
//		myInterface.
		byte header = globalBuf[0];
		byte cmd 	= globalBuf[1];
		int leftLen = availableLen - 2;
		int dataValidLen;
		int startIndex=2;
		while(leftLen>=0)
		{
			for(i=0;i<USB_TRANS_MAX_LEN;i++)
				sndBuffer[i]=0x00;
			sndBuffer[0]=header;
			if(leftLen == (availableLen - 2))
				sndBuffer[1]=cmd;
			else
				sndBuffer[1]=(byte)(cmd|(1<<7));
				
			System.arraycopy(globalBuf, startIndex, sndBuffer, 2, USB_TRANS_MAX_DATA_LEN);
			Log.d("gary", "send: " + converB2String(sndBuffer, USB_TRANS_MAX_DATA_LEN));
			iRet=myDeviceConnection.bulkTransfer(epOut, sndBuffer, sndBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet<0)
				return iRet;
			leftLen -= USB_TRANS_MAX_DATA_LEN;
			startIndex+=USB_TRANS_MAX_DATA_LEN;	
		}
		///��������
		for(i=0;i<USB_TRANS_MAX_LEN;i++)
			rcvBuffer[i]=0x00;
		//rcvBuffer
		/*
		Arrays.fill(rcvBuffer, (byte)0x00);
		Arrays.fill(globalBuf, (byte)0x00);
		
		iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
		if(iRet<0)
			return iRet;
		Log.d("gary", "rec:" + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));*/
		//Ϊ�˼��ݵ�һ�η�ָ��ʱ���еε�һ�����������ݴ��������ڶ���һ����������ѭ��
		tmpStartTime=System.currentTimeMillis();
		while(true)
		{
			if((System.currentTimeMillis()-tmpStartTime)/1000>2)
				return -1002;//�����ݳ�ʱ
			Arrays.fill(rcvBuffer, (byte)0x00);
			Arrays.fill(globalBuf, (byte)0x00);
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			if((iRet>=0)&&(rcvBuffer[1]==0x20))//ֻ�е����ɹ�������08�������������0x20ʱ�������� 20Ŀǰ����ֻ����ָ�ƺ�RFID
				break;
		
		
		}
		//leftLen = rcvBuffer[1] << 8 | rcvBuffer[2];
		leftLen=(rcvBuffer[2]&0xFF)*256;
		leftLen+=(rcvBuffer[3]&0xFF);
		dataValidLen = leftLen + 4;
		//dataValidLen = leftLen;//�ĳɲ�������08 20 ��2���ֽڳ���
		leftLen -= USB_TRANS_MAX_DATA_LEN;
		startIndex=0;
		System.arraycopy(rcvBuffer,0,globalBuf, startIndex, USB_TRANS_MAX_LEN);
		startIndex+=USB_TRANS_MAX_LEN;
		while(leftLen > 0)
		{
			
			Arrays.fill(rcvBuffer,(byte)0x00);
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet>=0)
			{
				Log.d("gary", "rec:" + converB2String(rcvBuffer, USB_TRANS_MAX_DATA_LEN));
				System.arraycopy(rcvBuffer, 2, globalBuf, startIndex, USB_TRANS_MAX_DATA_LEN);
				leftLen=leftLen-USB_TRANS_MAX_DATA_LEN;
				startIndex+=USB_TRANS_MAX_DATA_LEN;
			}
			else	//�������ֱ�ӷ���ʧ��
			{
				String s = String.valueOf(iRet);
				Log.d("gary", "iRet:" + s);	
				return iRet;	
			}
				
			
		}
		//�����ӷ���
		int max = dataValidLen > len ? dataValidLen : len;
		for(i=0;i<max;i++)
			data[i]=0;
		ucharp2jintp(data, globalBuf, dataValidLen);
		return dataValidLen;	
	}
	/**
	 * �������ܣ���дFingerͼƬ����
	 * @param data
	 * @param len
	 * @return
	 */
	public int sendDatarFingerImage(int[] data,int len)
	{
		int iRet = -1;
		int i;
		if(!UsbOpenflag)//���U��δ�򿪣���������ȥ��
		{
			if(!USBOpInit())
				return -1;
		}
		initGlobalBuf(data, len);
		byte header = globalBuf[0];
		byte cmd 	= globalBuf[1];
		int leftLen = availableLen - 2;
		//int dataValidLen;
		int startIndex=2;
		int tmpbufLen=0;
		int tmpbufLen1=0;
		byte[] tmpbuf=new byte [(64*280)*2+1];//���ݰ���С*2
		byte[] tmpbuf1=new byte [(64*280)*2*2];//���ݰ���С*2
		int AReadLen=0;
		int myFlag=0;
		while(leftLen>=0)
		{
			for(i=0;i<USB_TRANS_MAX_LEN;i++)
				sndBuffer[i]=0x00;
			sndBuffer[0]=header;
			if(leftLen == (availableLen - 2))
				sndBuffer[1]=cmd;
			else
				sndBuffer[1]=(byte)(cmd|(1<<7));
				
			System.arraycopy(globalBuf, startIndex, sndBuffer, 2, USB_TRANS_MAX_DATA_LEN);
			Log.d("gary", "send: " + converB2String(sndBuffer, USB_TRANS_MAX_DATA_LEN));
			iRet=myDeviceConnection.bulkTransfer(epOut, sndBuffer, sndBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet<0)
				return iRet;
			leftLen -= USB_TRANS_MAX_DATA_LEN;
			startIndex+=USB_TRANS_MAX_DATA_LEN;	
		}
		///��������
		for(i=0;i<USB_TRANS_MAX_LEN;i++)
			rcvBuffer[i]=0x00;
		//rcvBuffer
		Arrays.fill(rcvBuffer, (byte)0x00);
		Arrays.fill(globalBuf, (byte)0x00);
		///ͼƬ�̶�ȥȡ251����
		//for(i=0;i<251;i++)
		for(i=0;i<500;i++)//��ԭ���Ĺ̶���251��������Ϊ�ж��Ƿ�Ϊ���һ��08���
		{
			iRet=myDeviceConnection.bulkTransfer(epIn, rcvBuffer, rcvBuffer.length, USB_TRANS_TIMEOUT);
			if(iRet<0)
				return iRet;
			else
			{
				
				if(i>=1)//������һ�����ݰ�,�ӵڶ�������ʼΪͼ������
        		{
					AReadLen=(rcvBuffer[2]&0xFF)*256;
					AReadLen=AReadLen+(rcvBuffer[3]&0xFF);
					//if((i-1)%5==0)
					if((AReadLen==60)&&(rcvBuffer[4]==-17)&&(rcvBuffer[5]==0x01)&&(rcvBuffer[6]==-1)&&(rcvBuffer[7]==-1)&&(rcvBuffer[10]==0x02))
	        		{
	        			System.arraycopy(rcvBuffer, 13, tmpbuf, tmpbufLen, 51);
	        			tmpbufLen+=51;
	        			myFlag=0;//zzy
	        		}
					//else if((AReadLen==60)&&(rcvBuffer[4]==(byte)0xef)&&(rcvBuffer[1]==0x01)&&(rcvBuffer[2]==(byte)0xff)&&(rcvBuffer[3]==(byte)0xff)&&(rcvBuffer[6]==0x08))
					//else if((AReadLen==60)&&(rcvBuffer[4]==-17)&&(rcvBuffer[1]==0x01)&&(rcvBuffer[2]==-1)&&(rcvBuffer[3]==-1)&&(rcvBuffer[6]==0x08))
					else if((AReadLen==60)&&(rcvBuffer[4]==-17)&&(rcvBuffer[5]==0x01)&&(rcvBuffer[6]==-1)&&(rcvBuffer[7]==-1)&&(rcvBuffer[10]==0x08))
	        		{
	        			System.arraycopy(rcvBuffer, 13, tmpbuf, tmpbufLen, 51);
	        			tmpbufLen+=51;
	        			myFlag=1;//zzy
	        		}
					else if(AReadLen==60)
					{
						System.arraycopy(rcvBuffer, 4, tmpbuf, tmpbufLen, 60);
	        			tmpbufLen+=60;
	        			
					}
	        		/*else if((i-1)%5==4)
	        		{
	        			System.arraycopy(rcvBuffer, 4, tmpbuf, tmpbufLen, 25);
	        			tmpbufLen+=25;
	        			
	        		}*/
	        		else
	        		{
	        			AReadLen=AReadLen-2;//ȡ��������֤����
	        			System.arraycopy(rcvBuffer, 4, tmpbuf, tmpbufLen, AReadLen);
	        			tmpbufLen+=AReadLen;
	        			if(myFlag!=0)//�����һ����������һ��С�������������
	                        break;
	        			
	        		}
        		}
			}
			
		}
		//if(tmpbufLen==12800)
		if((tmpbufLen==18432)||(tmpbufLen==12800))//��ָ��ģ���޸� //��������ģ������ͼƬ�ߴ�
		{
			
			tmpbufLen1=img_One2Two(tmpbuf,tmpbuf1,tmpbufLen);	
			ucharp2jintp(data, tmpbuf1, tmpbufLen1);
			return tmpbufLen1;
			
		}
		else
			return -1;	
	}

	public static String converB2String(byte data[], int length) {
		if (length < 1)
			return "";
		int tmpi;
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < length; n++) {
			tmpi=data[n];
			String hex = Integer.toHexString(tmpi & 0xFF);
			if (hex.length() == 1)
				hex = '0' + hex;

			// sb.append(hex.toUpperCase() + " ");
			sb.append(hex.toUpperCase() + "");
			if (n > 0 && (n + 1) % 16 == 0) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static String convert2String(int data[], int length) {
		if (length < 1)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < length; n++) {
			String hex = Integer.toHexString(data[n] & 0xFF);
			if (hex.length() == 1)
				hex = '0' + hex;

			// sb.append(hex.toUpperCase() + " ");
			sb.append(hex.toUpperCase() + "");
			if (n > 0 && (n + 1) % 16 == 0) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public static int[] convertStringToIntArr(String hexString) {
		int len = 0, strLen = 0;
		strLen = hexString.length();
		len = (strLen % 2 == 0) ? (strLen / 2) : (strLen / 2 + 1);
		int data[] = new int[len];
		String tmpStr = hexString;
		if (strLen % 2 == 1)
			tmpStr += "0";
		for (int i = 0; i < len; i++) {
			String s = tmpStr.substring(2 * i, 2 * i + 2);
			try {
				data[i] = Integer.parseInt(s, 16);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return data;

	}
	
	public static int img_One2Two(byte[] psFrom,byte[] psTo, int psFromLen)
	  {
		  int i=0;
		  byte tmpCh=0x0F;
		  for(i=0;i<psFromLen;i++)
		  {
			  
			  psTo[i*2]=(byte)((psFrom[i]&tmpCh)<<4);
			  psTo[i*2+1]=(byte)((psFrom[i])&0xF0);
			    
		  }
		  return i*2;
		  
	  }

}
