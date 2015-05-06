package ste.gano;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;

/**
 * @author linuxluv
 */
public class Main {
    private static final int MAX_INT_LEN = 4;
    private static final int DATA_SIZE = 32; // number of image bytes required to store one stego byte
    public static void main(String[] args) {        
        Main mn = new Main();
       
        //ubah pesan ke biner, output integer kode ascii dlm array
        byte[] pesanDlmAscii = mn.ubahKeAscii("test");
        
        //gandakan bit, hasil simpan dlm array
        ArrayList<String> binerDigandakan = new ArrayList<String>();
        for(int i=0; i<pesanDlmAscii.length; i++){
            //ubah int kode ascii pesan ke String
            String binerAsciiPesan = mn.genapkanBit(Integer.toBinaryString(pesanDlmAscii[i]), 8);
            //gandakan bit, simpan di arrayList
            binerDigandakan.add(mn.gandakanBit(binerAsciiPesan, 4));
            //binerDigandakan = pesan
        }
        
        //------
        //xor kunci
        int hasilXorKunci = mn.xorKunci("sonny");
        
        //lcg
        ArrayList alHasilLcg = mn.simpleLCG(17, 7, 84, hasilXorKunci);
        
        //ubah hasil lcg ke biner
        Iterator iHasilLcg = alHasilLcg.iterator();
        String pseudoNoiseSignal = "";
        while(iHasilLcg.hasNext()){
            String binerHasilLcg = mn.genapkanBit(Integer.toBinaryString(Integer.parseInt(iHasilLcg.next().toString())), 8);
            pseudoNoiseSignal = pseudoNoiseSignal+binerHasilLcg;
        }
        
        String pesanPostProcess = "";
        //XOR pesan dengan pseudonoise
        for(int i=0; i<binerDigandakan.size(); i++){
            System.out.println(binerDigandakan.get(i)+" xor "+pseudoNoiseSignal+" = "+mn.manualXOR(binerDigandakan.get(i), pseudoNoiseSignal)); //hasil ini jadi var stego kalau di class sebelah
            pesanPostProcess = pesanPostProcess+mn.manualXOR(binerDigandakan.get(i), pseudoNoiseSignal);
        }
        
        String info_panjang_pesan = mn.genapkanBit(Integer.toBinaryString(pesanPostProcess.length()), DATA_SIZE);
        
        System.out.println("pesanPostProcess: "+info_panjang_pesan+" -- "+pesanPostProcess);
        
        pesanPostProcess = info_panjang_pesan+pesanPostProcess;
        
        
        //gambar
        BufferedImage bi = Steganography.loadImage("/home/linuxluv/Pictures/1.jpg");
        if (bi == null)
            System.exit(0);
        byte imBytes[] = Steganography.accessBytes(bi);
        
/*        System.out.println("hasil baca gambar");
        for(int i=0; i<imBytes.length; i++){
            System.out.println("imBytes ["+i+"] "+imBytes[i]);
        }*/
        
        //hide
        mn.hideStego_modified(imBytes, pesanPostProcess, 0);
        
        //get file name gambar
        String fnm = Steganography.getFileName("/home/linuxluv/Pictures/1.jpg");
        
        //test utk lihat apakah last bit sdh ganti
        DataBufferByte buffer = (DataBufferByte) bi.getData().getDataBuffer();
        byte[] testByte = buffer.getData();
        /*for(int i=0; i<testByte.length; i++){
            System.out.println("testByte["+i+"] "+testByte[i]);
        }
        System.out.println("");*/
        
        // store the modified image in <fnm>Msg.png
        Steganography.writeImageToFile( fnm + "Msg.jpg", bi);
        
        //membaca pixel gambar berisi pesan
        //mn.reveal("/home/linuxluv/Pictures/1Msg.jpg");
    }
    
    public boolean reveal(String imFnm){
        // get the image's data as a byte array
	BufferedImage im = Steganography.loadImage(imFnm);
        
        Steganography ste = new Steganography();
        ste.loadImage_NotStatic(imFnm);
        
	if (im == null)
            return false;
	byte[] imBytes = Steganography.accessBytes(im);
	System.out.println("Byte length of image: " + imBytes.length);
        
        //baca pixel ke 0 - 40
        for(int i=0; i<32; i++){
            System.out.println("imBytes["+i+"]: "+imBytes[i]+" _ "+Integer.toBinaryString(imBytes[i]));
        }
        
        // get msg length at the start of the image
	int msgLen = getMsgLength(imBytes, 0);   
	if (msgLen == -1)
            return false;
        System.out.println("Byte length of message: " + msgLen);
        
        return true;
    }
    
    public int getMsgLength(byte[] imBytes, int offset){
        byte[] lenBytes = extractHiddenBytes(imBytes, MAX_INT_LEN, offset);
        return 0;
    }
    
    public byte[] extractHiddenBytes(byte[] imBytes, int size, int offset){
        int finalPosn = 7; // 8 bit (0-7) untuk menyimpan panjang pesan di LSB
        if (finalPosn > imBytes.length) {
            System.out.println("End of image reached");
	    return null;
        }
        byte[] hiddenBytes = new byte[size];
        
        
        
        return null;
    }
    
    //--- hide-------------------------------------------------------------------
            
    public String genapkanBit(String input, int totalBit){
        int selisihPanjang = 0;
        int panjangInput = input.length();

        if(panjangInput < totalBit) {
            selisihPanjang = totalBit - panjangInput;
        }
        for(int i=0; i<selisihPanjang; i++){
            input = "0"+input;
        }
        return input;
    }
    
    public byte[] ubahKeAscii(String inputText){
        byte[] msgBytes = inputText.getBytes();
        return msgBytes;
    }
    
    public String manualXOR(String bits1, String bits2){
        int panjang_bits1 = bits1.length();
        int panjang_bits2 = bits2.length();
        int panjang_bit = panjang_bits1;
        if(panjang_bits1 > panjang_bits2){
            panjang_bit = panjang_bits2;
        } 
        String hasil = "";
        for(int i=0; i<panjang_bit; i++){
            String hasil0 = "";
            if(bits1.charAt(i) == bits2.charAt(i)){
                hasil0 = "0";
            } else {
                hasil0 = "1";
            }
            hasil = hasil + hasil0;
        }
        return hasil;
    }
    
    public String gandakanBit(String input, int skalar){
        String hasil = "";
        for(int i=0; i<input.length(); i++){
            String subInput = "";
            for(int j=0; j<skalar; j++){
                subInput = subInput+input.charAt(i);
            }
            hasil = hasil+subInput;
        }
        return hasil;
    }
    
    public int xorKunci(String kunci){
	byte[] kunciDlmAscii = kunci.getBytes();
		
	int hasilXor = 0;
		
	for(int i=0; i<kunci.length(); i++) {
		hasilXor = kunciDlmAscii[i] ^ hasilXor;
	}
	return hasilXor;
    }
	
    //LGC, hasil integer disimpan di arrayList
    public ArrayList simpleLCG(int a, int c, int m, int hasilXorKunci){
//	int a, c , m, x;
        int x;
//	a = 17; c = 7; m = 84; 
        x = hasilXorKunci;
//	System.out.println("x[0] "+x);
        ArrayList<Integer> hasilLCG = new ArrayList<Integer>();
	for(int i=0; i<5; i++){
		x = (a * x + c) % m ; //ini rumus LCG
//		System.out.println("x["+(i+1)+"] : "+x);
                hasilLCG.add(x);
	}
        return hasilLCG;
    }
    
    public byte[] testGambar(){
        BufferedImage im = Steganography.loadImage("/home/linuxluv/Pictures/fb/10px.png");
        if (im == null)
            System.exit(0);
        byte imBytes[] = Steganography.accessBytes(im);
        
        return imBytes;
    }
    
    public void hideStego_modified(byte[] imBytes, String stego, int offset)
    // store stego in image starting at byte posn offset
    {
        for (int i = 0; i < stego.length(); i++) {       // loop through stego //looping sesuai panjang pesan yg sdh diStego
            int byteVal = Character.getNumericValue(stego.charAt(i));
            //System.out.println("byteVal: "+byteVal);
            int bitVal = (byteVal >>> 0) & 1;//                int bitVal = (byteVal >>> j) & 1;
            //System.out.println("bitVal: "+bitVal+" | imBytes["+offset+"]"+imBytes[offset]);
//            int bb = 0xFE;
//            System.out.println("bb: "+bb);
//            int t = (imBytes[offset] & 0xFE);
//            System.out.println("t: "+t);
//            int s = t | bitVal;
//            System.out.println("s: "+s);
//            byte u = (byte) t;
//            System.out.println("u: "+u);
	    imBytes[offset] = (byte)((imBytes[offset] & 0xFE) | bitVal); 
            /*System.out.println("imBytes["+offset+"]: "+imBytes[offset]);
            System.out.println("");*/
//            byte testB = (byte)((1 & 0xFE) | 0);
//            System.out.println("testB: "+testB);
//            
//            byte nol = 0 & 0xFE;
//            byte testC = (byte) (~(nol | nol)); //~(A | B) kebalikan OR (XOR)
//            System.out.println("testC: "+testC);
            
	    offset++;
        }
    }  // end of hideStego()
}
