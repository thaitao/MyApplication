package com.example.employeecheckin;

import java.util.Arrays;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.example.employeecheckin.USBOp;

public class RFIDPnPacket {

    private static final String TAG = "ZZYLG";

    public static int[] PACKET = new int[9 * 1024];

    //protected SerialPort mSerialPort;
    //protected InputStream mInputStream;
    //protected OutputStream mOutputStream;
    ////´íÎóÁÐ±í
    public static final int SUCCESS				= 0;				//²Ù×÷³É¹¦
    public static final int PARAMETER_ERR		= -1001;			//Êý¾Ý°ü¸ñÊ½´íÎó
    public static final int READ_ERR			= -1002;			//¶ÁÊý¾Ý´íÎó
    public static final int WRITE_ERR			= -1003;			//Ð´Êý¾Ý´íÎó
    public static final int COMOPEN_ERR			= -1004;			//´ò¿ª´®¿ÚÊ§°Ü
    public static final int FORMART_ERR			= -1005;			//Êý¾Ý°ü¸ñÊ½´íÎó
    public static final int AUTHENTICATION_ERR	= -1006;			//ÑéÖ¤ÃÜÔ¿Ê§°Ü
    public static final int READ_FORMAT_ERR		= -1007;			//¶ÁÈ¡·µ»ØµÄÊý¾Ý¸ñÊ½´íÎó
    public static final int READ_CARD_ERR		= -1008;			//¶Á¿¨·µ»Ø×´Ì¬´íÎó
    public static final int PURSE_FORMAT_ERR	= -1009;			//Ç®°ü¸ñÊ½´íÎó
    public static final int TRANSFER_ERR		= -1010;			//Ç®°ü¿Û¿î³äÖµÍê³ÉÖ®ºóµÄtransferÖ¸ÁîÊ§°Ü


    private static void clear() {
        Arrays.fill(PACKET, 0);
    }
    public USBOp mUSBOp;
    ///20190822 Ìí¼Ó¹¹Ôìº¯Êý
    public RFIDPnPacket(UsbManager manager,Context context, String AppName)
    {

        mUSBOp= new USBOp(manager,context,AppName);
        if(!mUSBOp.UsbFeatureSupported()){
            Log.e("jack", "Éè±¸²»Ö§³ÖUSBHOST");
        }
        //mUSBOp.SetUsbConfig( 0x7580,0x10c4, 10, 0x00, 0x00);
        mUSBOp.SetUsbConfig( 0x8238,0x10c4, 0x03, 0x01, 0x02);
    }
    //ÉèÖÃ´®¿Ú²¨ÌØÂÊ
/*	public void SetBaud()
	{
		mUSBOp.setUsbBaud();
	}*/

    /////////////////Êý¾Ý´ò°ü/////////////////////////////////////////////////////////////////////////
    public static int buildPackage(int[] inTFI,int inTFILen,int[] inData, int inDataLen)
    {

        int[] tmpPACKET = new int[1024];
        Arrays.fill(tmpPACKET, 0);
        clear();
        int i = 0, j = 0;
        int ch;
        int tmpChk;
        //ÐòÁÐÍ·
        tmpPACKET[i++] = 0x00;
        //ÆðÊ¼ºÅ
        tmpPACKET[i++] = 0x00;
        tmpPACKET[i++] = 0xFF;
        //ÅÐ¶ÏTFI+Êý¾Ý ÊÇ·ñ´óÓÚ256
        //³¤¶ÈºÍ³¤¶ÈÐ£ÑéºÍ
        if((inTFILen+inDataLen)<256)//TFI ºÍ dataÊý¾ÝÐ¡ÓÚ256¸ö
        {
            ch=(inTFILen+inDataLen)%256;
            tmpPACKET[i++] = ch;
            tmpPACKET[i++] = (0x100-ch)%256;

        }
        else
        {
            tmpPACKET[i++] = 0xFF;
            tmpPACKET[i++] = 0xFF;
            ch=((inTFILen+inDataLen)%(256*256))/256;//³¤¶È¸ßÎ»
            tmpPACKET[i++] = ch;
            tmpChk=ch;
            ch=(inTFILen+inDataLen)%256;
            tmpPACKET[i++]	= ch;
            tmpChk=tmpChk+ch;
            tmpPACKET[i++]=(0x100-(tmpChk%256))%256;
        }
        //TFI
        System.arraycopy(inTFI, 0, tmpPACKET, i, inTFILen);
        i=i+inTFILen;
        //Êý¾Ý
        System.arraycopy(inData, 0, tmpPACKET, i, inDataLen);
        i=i+inDataLen;
        //Êý¾ÝÐ£ÑéºÍ
        tmpChk=0;
        for(j=0;j<inTFILen;j++)
            tmpChk+=inTFI[j];
        for(j=0;j<inDataLen;j++)
            tmpChk+=inData[j];
        tmpPACKET[i++] = (0x100-(tmpChk%256))%256;
        //ÐòÁÐ½áÎ²
        tmpPACKET[i++]=0x00;
        j=0;
        PACKET[j++]=0x08;
        PACKET[j++]=0x41;
        PACKET[j++]=i/256;
        PACKET[j++]=i%256;
        System.arraycopy(tmpPACKET, 0, PACKET, j, i);
        j=j+i;
        return j;
    }

    public static int resolveConfigData(int[] configData) {
        int[] GOALBUF=new int[9];
        int i,configDataLen;
        int tmpDataLen=0;
        int tmpChk;
        int ch;
        GOALBUF[0]=0x00;
        GOALBUF[1]=0x00;
        GOALBUF[2]=0xFF;
        GOALBUF[3]=0x00;
        GOALBUF[4]=0xFF;
        GOALBUF[5]=0x00;
        GOALBUF[6]=0x00;
        GOALBUF[7]=0x00;
        GOALBUF[8]=0xFF;
        for(i=0;i<9;i++)
        {
            if(GOALBUF[i]!=PACKET[i])
                break;
        }
        if(i<9)
            return FORMART_ERR;
        //»ñÈ¡µ½Êµ¼Ê¶ÁÈ¡Êý¾Ý³¤¶È
        for(i=1024;i>=0;i--)
        {
            if(PACKET[i]!=0)
                break;
        }
        configDataLen=i+1+1;//ÕâÀïÒª¼ÓÉÏ×îºóµÄÒ»¸ö0
        //´Ó³¤¶ÈÅÐ¶Ï¸ñÊ½ÊÇ·ñÕýÈ·
        //´óÓÚ255³¤¶ÈµÄÇé¿öÅÐ¶Ï
        if(configDataLen>(255+9+3+1))
        {
            tmpDataLen=PACKET[9+2]*256;
            tmpDataLen=tmpDataLen+PACKET[9+3];
            if(configDataLen!=(tmpDataLen+9+3+2+1))//±ÈÆðÐ¡ÓÚµÈÓÚ255µÄÇé¿ö¶àÁËÁ½¸ö0xFF 0xFF
                return FORMART_ERR;
            //¼ÓÈëÅÐ¶Ï³¤¶ÈÐ£Ñé
            tmpChk=PACKET[11]+PACKET[12];
            ch=(0x100-(tmpChk)%256)%256;
            if(ch!=PACKET[13])
                return FORMART_ERR;
            //¼ÓÈëÅÐ¶ÏÊý¾ÝÐ£Ñé
            tmpChk=0;
            for(i=0;i<tmpDataLen;i++)
            {
                tmpChk+=PACKET[14+i];

            }
            ch=(0x100-(tmpChk%256))%256;
            if(PACKET[configDataLen-2]!=ch)//µ¹ÊýµÚ¶þ¸öÊýÎªÐ£Ñé
                return FORMART_ERR;
            System.arraycopy(PACKET, 14, configData, 0, tmpDataLen);
            return tmpDataLen;
        }
        else
        {
            tmpDataLen=PACKET[9];
            if(configDataLen!=(tmpDataLen+9+3+1))
                return FORMART_ERR;
            //¼ÓÈëÅÐ¶Ï³¤¶ÈÐ£Ñé
            ch=(0x100-PACKET[9])%256;
            if(ch!=PACKET[10])
                return FORMART_ERR;
            //¼ÓÈëÅÐ¶ÏÊý¾ÝÐ£Ñé
            tmpChk=0;
            for(i=0;i<tmpDataLen;i++)
                tmpChk+=PACKET[11+i];
            ch=(0x100-(tmpChk%256))%256;
            if(PACKET[configDataLen-2]!=ch)//µ¹ÊýµÚ¶þ¸öÊýÎªÐ£Ñé
                return FORMART_ERR;
            ///¼ÓÈëÅÐ¶ÏÓ¦ÓÃ³ÌÐò¼¶´íÎóThe syntax error frame is used to inform the host controller that the PN532 has detected
            //an error at the application level
            //00 00 FF 01 FF 7F 81 00
            if((tmpDataLen==1)&&(PACKET[11]==0x7F))
                return FORMART_ERR;
            System.arraycopy(PACKET, 11, configData, 0, tmpDataLen);
            return tmpDataLen;

        }



    }

    public static String convert2String(int data[], int length) {
        if (length < 1)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < length; n++) {
            String hex = Integer.toHexString(data[n] & 0xFF);
            if (hex.length() == 1)
                hex = '0' + hex;

            sb.append(hex.toUpperCase() + " ");
            if (n > 0 && (n + 1) % 16 == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * ¹¦ÄÜ£º´ò¿ª»òÕß¹Ø±ÕÌìÏß
     * @param inMode 0 = ´ò¿ªÌìÏß      1 = ¹Ø±ÕÌìÏß
     * @return
     * 		  0  =   SUCCESS
     * 		      ÆäËûÖµ´íÎó
     */
    public int PN_RF_Set(int inMode)
    //public String PS_RF_Set(int inMode)
    {

        int buildLen;
        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x32,0x01,0x00};
        int iRet;
        if(inMode==0)//»½ÐÑÄ£¿é
        {
            clear();
            buildLen=0;
            PACKET[buildLen++]=0x08;
            PACKET[buildLen++]=0x41;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x1C;
            PACKET[buildLen++]=0x55;
            PACKET[buildLen++]=0x55;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=0x00;
            PACKET[buildLen++]=(byte)0xFF;
            PACKET[buildLen++]=0x03;
            PACKET[buildLen++]=(byte)0xFD;
            PACKET[buildLen++]=(byte)0xD4;
            PACKET[buildLen++]=0x14;
            PACKET[buildLen++]=0x01;
            PACKET[buildLen++]=0x17;
            PACKET[buildLen++]=0x00;
            //buildLen=28;
            iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
            if (iRet < 0)
                return READ_ERR;
            iRet=resolveConfigData(tmpBuf);
            if(iRet<0)
                return READ_ERR;
            //recBuf=convert2String(PACKET,18);

        }
        else//¹Ø±ÕÌìÏß
        {
            buildLen=buildPackage(tmpTFI,1,tmpData,3);
            iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
            if (iRet < 0)
                return READ_ERR;
            iRet=resolveConfigData(tmpBuf);
            if(iRet<0)
                return READ_ERR;
            //return SUCCESS;
            //recBuf=convert2String(PACKET,18);

        }
        return SUCCESS;
        //return recBuf;

    }
    //int32_t WINAPI RFID_M1_Search(int8_t * outCardNo,int32_t *outCardNoLen,int32_t inTimeOut)
    /**
     * º¯Êý¹¦ÄÜ£º Ñ°¿¨
     * @param outCardNoData
     * @return
     */
    public int PN_RF_M1_Search(int []outCardNoData,int inTimeOut)
    {
        int buildLen;
        int cardNoDataLen;
        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x4A,0x01,0x00};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int iRet;
        buildLen=buildPackage(tmpTFI,1,tmpData,3);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet==-1005)
            iRet=-1005;
        if(iRet<0)
            return iRet;
        cardNoDataLen=iRet;
        System.arraycopy(tmpBuf, cardNoDataLen-4, outCardNoData, 0, 4);//³ýµôÐÍºÅºÍÃÜÔ¿ÀàÐÍ
        return 4;

    }
    //int32_t WINAPI RFID_M1_Authentication(int32_t inBlock,int8_t* inCardNo,int32_t inCardNoLen,int8_t* inCardKey,int32_t inCardKeyLen,int32_t inTimeOut)
    /**
     * º¯Êý¹¦ÄÜ£º	ÑéÖ¤ÃØÔ¿
     * @param inBlock 		¿éºÅ			--input
     * @param inCardNo		¿¨ºÅ			--input
     * @param inCardNoLen	¿¨ºÅ³¤¶È		--input
     * @param inCardKey		ÃØÔ¿			--input
     * @param inCardKeyLen	ÃØÔ¿³¤¶È		--input
     * @param inTimeOut		³¬Ê±			--input
     * @return
     * 			³É¹¦			0
     *			Ê§°Ü			¸ºÊý
     */
    public int PN_RF_M1_Authentication(int inBlock,int []inCardNo,int inCardNoLen,int []inCardKey,int inCardKeyLen,int inTimeOut)
    {
        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x40,0x01,0x60};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpBufLen=0;
        int buildLen=0;
        int iRet;
        //ÑéÖ¤ÃØÔ¿
        //¼ÓÈë¿éºÅ
        Arrays.fill(tmpBuf, 0);
        System.arraycopy(tmpData, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;
        tmpBuf[tmpBufLen++]=inBlock%256;

        //¼ÓÈëÃØÔ¿
        System.arraycopy(inCardKey, 0, tmpBuf, tmpBufLen, inCardKeyLen);
        tmpBufLen=tmpBufLen+inCardKeyLen;
        //¼ÓÈë¿¨ºÅ
        System.arraycopy(inCardNo, 0, tmpBuf, tmpBufLen, inCardNoLen);
        tmpBufLen=tmpBufLen+inCardNoLen;
        //´ò°ü
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        //if(tmpBuf[iRet-1]!=0x00)
        if(tmpBuf[2]!=0x00)
            return AUTHENTICATION_ERR;

        return SUCCESS;

    }
    /**
     * º¯Êý¹¦ÄÜ£º	¶ÁS50¿¨Æ¬
     * @param inBlock		¿éºÅ
     * @param outCardNoData	¶Áµ½µÄ¿¨Æ¬Êý¾Ý
     * @param inTimeOut		¶Á¿¨³¬Ê±
     * @return
     * 		  Ê§°Ü				¸ºÊý
     * 		  ³É¹¦				·µ»Ø¶Áµ½µÄ¿¨Æ¬Êý¾Ý³¤¶È
     */
    public int PN_RF_M1_Read(int inBlock,int []outCardNoData,int inTimeOut)
    {
        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x40,0x01,0x30};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpBufLen=0;
        int buildLen=0;
        int iRet;
        //int cardNoDataLen;

        //¶Á¿¨
        //¼ÓÈë¿éºÅ
        Arrays.fill(tmpBuf, 0);
        System.arraycopy(tmpData, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;
        tmpBuf[tmpBufLen++]=inBlock%256;
        //´ò°ü
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        //cardNoDataLen=iRet;
        if(tmpBuf[2]!=0x00)
            return READ_CARD_ERR;
        System.arraycopy(tmpBuf, 3, outCardNoData, 0, 16);//³ýµôÐÍºÅºÍÃÜÔ¿ÀàÐÍ
        return 16;
    }
    //int32_t WINAPI RFID_M1_Write(int32_t inBlock,int8_t * inCardData,int32_t inCardDataLen,int32_t inTimeOut)
    /**
     * º¯Êý¹¦ÄÜ£º Ð´¿¨
     * @param inBlock			Ð´¿¨¿éºÅ
     * @param inCardNoData		Ð´ÈëµÄÊý¾Ý
     * @param inCardNoDataLen	Ð´ÈëµÄÊý¾Ý³¤¶È
     * @param inTimeOut			³¬Ê±
     * @return
     * 		 	³É¹¦ 				0
     * 			Ê§°Ü				¸ºÊý
     */
    public int PN_RF_M1_Write(int inBlock,int []inCardNoData,int inCardNoDataLen,int inTimeOut)
    {
        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x40,0x01,0xA0};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpBufLen=0;
        int buildLen=0;
        int iRet;

        //Ð´¿¨
        //¼ÓÈë¿éºÅ
        Arrays.fill(tmpBuf, 0);
        System.arraycopy(tmpData, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;
        tmpBuf[tmpBufLen++]=inBlock%256;
        //¼ÓÈë¿éÊý¾Ý
        System.arraycopy(inCardNoData, 0, tmpBuf, tmpBufLen, inCardNoDataLen);
        tmpBufLen+=inCardNoDataLen;

        //´ò°ü
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        if(tmpBuf[2]!=0x00)
            return READ_CARD_ERR;
        return SUCCESS;

    }
    //int32_t WINAPI RFID_M1_PurseInit(int32_t inBlock,int32_t inAmount,int32_t inTimeOut)
    /**
     * º¯Êý¹¦ÄÜ£º	³õÊ¼»¯µç×ÓÇ®°ü
     * @param inBlock		ÓÃ×÷Ç®°üµÄ¿éºÅ
     * @param inAmount		³õÊ¼»¯Ç®°ü½ð¶î
     * @param inTimeOut		³¬Ê±
     * @return
     * 		 	³É¹¦ 				0
     * 			Ê§°Ü				¸ºÊý
     */
    public int PN_RF_M1_PurseInit(int inBlock,long inAmount,int inTimeOut)
    {
        int []tmpBuf=new int [128];
        int tmpBufLen;
        int iRet;
        //ÅÐ¶Ï³õÊ¼»¯Ç®°üµÄÖµÊÇ·ñ³¬±ê
        if(inAmount<0||inAmount>0xFFFFFFFFL)
            return FORMART_ERR;
        if(inBlock<=0||(inBlock%4==3))//µÚ0¿éºÍ Ã¿¸öÉÈÇøµÄµÚ3¿é²»ÄÜµ±Ç®°üÊ¹ÓÃ
            return FORMART_ERR;

        //³õÊ¼»¯
        Arrays.fill(tmpBuf, 0);
        tmpBufLen=0;
        tmpBuf[tmpBufLen++]=(int)inAmount%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/256)%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/(256*256))%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/(256*256*256))%256;
        tmpBuf[tmpBufLen++]=~tmpBuf[0];
        tmpBuf[tmpBufLen++]=~tmpBuf[1];
        tmpBuf[tmpBufLen++]=~tmpBuf[2];
        tmpBuf[tmpBufLen++]=~tmpBuf[3];
        tmpBuf[tmpBufLen++]=tmpBuf[0];
        tmpBuf[tmpBufLen++]=tmpBuf[1];
        tmpBuf[tmpBufLen++]=tmpBuf[2];
        tmpBuf[tmpBufLen++]=tmpBuf[3];
        tmpBuf[tmpBufLen++]=(inBlock%256);
        tmpBuf[tmpBufLen++]=~(inBlock%256);
        tmpBuf[tmpBufLen++]=(inBlock%256);
        tmpBuf[tmpBufLen++]=~(inBlock%256);

        iRet=PN_RF_M1_Write(inBlock,tmpBuf,tmpBufLen,inTimeOut);
        return iRet;
    }
    //int32_t WINAPI RFID_M1_PurseBalance(int32_t inBlock,int32_t *inAmount,int32_t inTimeOut)
    /**
     * º¯Êý¹¦ÄÜ£º	¶ÁÈ¡Ç®°üÓà¶î
     * @param inBlock		Ç®°üµÄ¿éºÅ
     * @param inTimeOut		¶ÁÇ®°üÓà¶î³¬Ê±
     * @return
     * 		  >=0		    Ç®°ü½ð¶î
     * 		  <0		    ¶ÁÈ¡Ç®°üÓà¶îÊ§°Ü
     */
    public long PN_RF_M1_PurseBalance(int inBlock,int inTimeOut)
    {
        int []tmpBuf=new int [128];
        int tmpBufLen;
        int tmpCh;
        int iRet,i;
        if(inBlock<=0||(inBlock%4==3))//µÚ0¿éºÍ Ã¿¸öÉÈÇøµÄµÚ3¿é²»ÄÜµ±Ç®°üÊ¹ÓÃ
            return FORMART_ERR;

        //³õÊ¼»¯
        Arrays.fill(tmpBuf, 0);
        tmpBufLen=0;

        iRet=PN_RF_M1_Read(inBlock,tmpBuf,inTimeOut);
        if(iRet<0)
            return iRet;
        //ÑéÖ¤¶Áµ½µÄÇ®°ü¸ñÊ½
        //Ñé½ð¶î·´Âë
        for(i=0;i<4;i++)
        {
            tmpCh=~tmpBuf[i];
            tmpCh&=0xFF;
            if(tmpCh!=tmpBuf[i+4])
                return PURSE_FORMAT_ERR;

        }
        //ÑéÖ¤½ð¶îÕýÂë
        for(i=0;i<4;i++)
        {
            if(tmpBuf[i]!=tmpBuf[i+8])
                return PURSE_FORMAT_ERR;
        }
        //ÑéÖ¤µØÖ·ÕýÂë
        tmpCh=inBlock%256;
        if((tmpCh!=tmpBuf[12])||(tmpCh!=tmpBuf[14]))
            return PURSE_FORMAT_ERR;
        //ÑéÖ¤µØÖ··´Âë
        tmpCh=~tmpCh;
        tmpCh&=0xFF;
        if((tmpCh!=tmpBuf[13])||(tmpCh!=tmpBuf[15]))
            return PURSE_FORMAT_ERR;
        //·µ»Ø½ð¶î
        return tmpBuf[0]+tmpBuf[1]*256+tmpBuf[2]*256*256+tmpBuf[3]*256*256*256;

    }
    //int32_t WINAPI RFID_M1_PurseIncrease(int32_t inBlock,int32_t inAmount,int32_t inTimeOut)
    /**
     * º¯Êý¹¦ÄÜ£º	µç×ÓÇ®°ü³äÖµ
     * @param inBlock		Ç®°üµÄ¿éºÅ
     * @param inAmount		³äÖµ½ð¶î
     * @param inTimeOut		Ç®°ü³äÖµ³¬Ê±
     * @return
     * 		 	³É¹¦ 				0
     * 			Ê§°Ü				¸ºÊý
     */
    public int PN_RF_M1_PurseIncrease(int inBlock,long inAmount,int inTimeOut)
    {
        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x40,0x01,0xC1};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpData1[]={0x40,0x01,0xB0};//
        int tmpBufLen=0;
        int buildLen=0;
        int iRet;

        //¼ÓÈë¿éºÅ
        Arrays.fill(tmpBuf, 0);
        tmpBufLen=0;
        System.arraycopy(tmpData, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;
        //¼ÓÈë¿éºÅ
        tmpBuf[tmpBufLen++]=inBlock%256;
        //¼ÓÈë½ð¶î
        tmpBuf[tmpBufLen++]=(int)inAmount%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/256)%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/(256*256))%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/(256*256*256))%256;

        //´ò°ü
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;

        if(tmpBuf[2]!=0x00)
            return READ_FORMAT_ERR;

        //½Ó×Å·¢ËÍ´«ÊäÖ¸ÁîB0
        //¼ÓÈë¿éºÅ
        Arrays.fill(tmpBuf, 0);
        tmpBufLen=0;
        System.arraycopy(tmpData1, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;
        tmpBuf[tmpBufLen++]=inBlock%256;
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        if(tmpBuf[2]!=0x00)
            return READ_FORMAT_ERR;
        return SUCCESS;
    }

    /**
     * º¯Êý¹¦ÄÜ£º	µç×ÓÇ®°ü¿Û¿î
     * @param inBlock		Ç®°üµÄ¿éºÅ
     * @param inAmount		¿Û¿î½ð¶î
     * @param inTimeOut		Ç®°ü¿Û¿î³¬Ê±
     * @return
     * 		 	³É¹¦ 				0
     * 			Ê§°Ü				¸ºÊý
     */
    public int PN_RF_M1_PurseDecrease(int inBlock,long inAmount,int inTimeOut)
    {
        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x40,0x01,0xC0};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpData1[]={0x40,0x01,0xB0};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpBufLen=0;
        int buildLen=0;
        int iRet;

        Arrays.fill(tmpBuf, 0);
        tmpBufLen=0;
        System.arraycopy(tmpData, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;
        //¼ÓÈë¿éºÅ
        tmpBuf[tmpBufLen++]=inBlock%256;
        //¼ÓÈë½ð¶î
        tmpBuf[tmpBufLen++]=(int)inAmount%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/256)%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/(256*256))%256;
        tmpBuf[tmpBufLen++]=(int)(inAmount/(256*256*256))%256;

        //´ò°ü
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        if(tmpBuf[2]!=0x00)
            return READ_FORMAT_ERR;

        //½Ó×Å·¢ËÍ´«ÊäÖ¸ÁîB0
        //¼ÓÈë¿éºÅ
        Arrays.fill(tmpBuf, 0);
        tmpBufLen=0;
        System.arraycopy(tmpData1, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;
        tmpBuf[tmpBufLen++]=inBlock%256;
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        if(tmpBuf[2]!=0x00)
            return READ_FORMAT_ERR;


        return SUCCESS;
    }

    //int32_t WINAPI RFID_CPU_Search(int32_t inTimeOut)
    /**
     * º¯Êý¹¦ÄÜ£º Ñ°·Ç½ÓCPU¿¨
     * @param inTimeOut 		Ñ°¿¨³¬Ê±Ê±¼ä
     * @return
     * 		 	³É¹¦ 				0
     * 			Ê§°Ü				¸ºÊý
     */
    public int PN_RF_CPU_Search(int inTimeOut)
    {

        int []tmpBuf=new int [128];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x4A,0x01,0x00};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpBufLen=0;
        int buildLen=0;
        int iRet;

        //Ñ°ºÅ
        Arrays.fill(tmpBuf, 0);
        System.arraycopy(tmpData, 0, tmpBuf, tmpBufLen, 3);
        tmpBufLen=tmpBufLen+3;

        //´ò°ü
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        return SUCCESS;

    }

    public int PN_RF_CPU_APDU(int []inApduData,int inApduDataLen,int []outApduData,int inTimeOut)
    {
        int []tmpBuf=new int [1024];
        int tmpTFI[]={0xD4};
        int tmpData[]={0x40,0x01};// 01 ±íÊ¾¶ÁÒ»ÕÅ¿¨ 00±íÊ¾106kbps
        int tmpBufLen=0;
        int buildLen=0;
        int iRet;

        //¼ÓÈëAPDUÊý¾Ý
        Arrays.fill(tmpBuf, 0);
        System.arraycopy(tmpData, 0, tmpBuf, tmpBufLen, 2);
        tmpBufLen=tmpBufLen+2;
        System.arraycopy(inApduData, 0, tmpBuf, tmpBufLen, inApduDataLen);
        tmpBufLen=tmpBufLen+inApduDataLen;

        //´ò°ü
        buildLen=buildPackage(tmpTFI,1,tmpBuf,tmpBufLen);
        iRet = mUSBOp.sendDatarRfid(PACKET, buildLen);
        if (iRet < 0)
            return READ_ERR;
        iRet=resolveConfigData(tmpBuf);
        if(iRet<0)
            return iRet;
        System.arraycopy(tmpBuf, 0, outApduData, 0, iRet);
        return iRet;


    }
    //int32_t WINAPI RFID_CPU_APDU(int8_t * inApduData,int32_t inApduDataLen,int8_t* outApduData,int32_t *outApduDataLen,int32_t inTimeOut)
	/*public int PN_RF_Open(String mDevice)
	{
		try{
			mSerialPort = new SerialPort(new File(mDevice), 115200, 0);
		}catch (IOException e) {
			Log.d(TAG, "Open Error!");
			e.printStackTrace();
		}

		return SUCCESS;
	}*/

}
