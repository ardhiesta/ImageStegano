/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author linuxluv
 */
public class Main {
    private static final int MAX_INT_LEN = 4;
    private static final int DATA_SIZE = 8; // number of image bytes required to store one stego byte
    public static void main(String[] args) {
        //ambil image
//        Main main = new Main();
//        main.testGambar();
        
//        Steganography.hide("/home/linuxluv/Desktop/input.txt", "/home/linuxluv/Pictures/fb/10px.png");
        
//        Steganography.reveal("/home/linuxluv/Pictures/fb/10pxMsg.png");
        
        
        Main mn = new Main();
       
        //ubah pesan ke biner, output integer kode ascii dlm array
        byte[] pesanDlmAscii = mn.ubahKeAscii("a");
        
        //gandakan bit, hasil simpan dlm array
        ArrayList<String> binerDigandakan = new ArrayList<String>();
        for(int i=0; i<pesanDlmAscii.length; i++){
            //ubah int kode ascii pesan ke String
//            String binerAsciiPesan = Integer.toBinaryString(pesanDlmAscii[i]);
            String binerAsciiPesan = mn.genapkanBit(Integer.toBinaryString(pesanDlmAscii[i]), 8);
//            System.out.println("binerAsciiPesan: "+binerAsciiPesan);
            
            //gandakan bit, simpan di arrayList
            binerDigandakan.add(mn.gandakanBit(binerAsciiPesan, 4));
            //binerDigandakan = pesan
        }
        
        //------
        //xor kunci
//        System.out.println("xor: "+mn.xorKunci("sonny"));
        int hasilXorKunci = mn.xorKunci("sonny");
        
        //lcg
        ArrayList alHasilLcg = mn.simpleLCG(17, 7, 84, hasilXorKunci);
        
        //ubah hasil lcg ke biner
        Iterator iHasilLcg = alHasilLcg.iterator();
        String pseudoNoiseSignal = "";
        while(iHasilLcg.hasNext()){
            String binerHasilLcg = mn.genapkanBit(Integer.toBinaryString((int) iHasilLcg.next()), 8);
//            System.out.println("binerHasilLcg: "+binerHasilLcg);
            pseudoNoiseSignal = pseudoNoiseSignal+binerHasilLcg; //TODO: jumlah bit blm cocok, blm ditambah 0 di depan
        }
//        System.out.println("pseudoNoiseSignal: "+pseudoNoiseSignal+", length: "+pseudoNoiseSignal.length());
        
        String pesanPostProcess = "";
        //XOR pesan dengan pseudonoise
        for(int i=0; i<binerDigandakan.size(); i++){
            System.out.println(binerDigandakan.get(i)+" xor "+pseudoNoiseSignal+" = "+mn.manualXOR(binerDigandakan.get(i), pseudoNoiseSignal)); //hasil ini jadi var stego kalau di class sebelah
            pesanPostProcess = pesanPostProcess+mn.manualXOR(binerDigandakan.get(i), pseudoNoiseSignal);
        }
        
        System.out.println("pesanPostProcess: "+pesanPostProcess);
        
        
        //gambar
        BufferedImage bi = Steganography.loadImage("/home/linuxluv/Pictures/fb/10px.png");
        if (bi == null)
            System.exit(0);
        byte imBytes[] = Steganography.accessBytes(bi);
        
        System.out.println("hasil baca gambar");
        for(int i=0; i<imBytes.length; i++){
            System.out.println("imBytes ["+i+"] "+imBytes[i]);
        }
        
        //hide
        mn.hideStego_modified(imBytes, pesanPostProcess, 0);
        
        //get file name gambar
        String fnm = Steganography.getFileName("/home/linuxluv/Pictures/fb/10px.png");
        
        //test utk lihat apakah last bit sdh ganti
        DataBufferByte buffer = (DataBufferByte) bi.getData().getDataBuffer();
        byte[] testByte = buffer.getData();
        for(int i=0; i<testByte.length; i++){
            System.out.println("testByte["+i+"] "+testByte[i]);
        }
        System.out.println("");
        
        // store the modified image in <fnm>Msg.png
        Steganography.writeImageToFile( fnm + "Msg.png", bi);
        
    }
    
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
        
        //cek output
//	System.out.println(">> msgBytes: "+msgBytes);
//	for(int i=0; i<msgBytes.length; i++){
//            System.out.println(">> msgBytes["+i+"]: "+msgBytes[i]);
//	    System.out.println(Integer.toBinaryString(msgBytes[i]));
//        }
        
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
//        System.out.println(">> im: "+im);
        if (im == null)
            System.exit(0);
        byte imBytes[] = Steganography.accessBytes(im);
//        for(int i=0; i<imBytes.length; i++){
//            System.out.println(">> imBytes["+i+"] : "+imBytes[i]);
//        }
        
        return imBytes;
    }
    
    /*
    private static boolean singleHide(byte[] imBytes, byte[] stego)
    // store stego in image bytes
    {
        int imLen = imBytes.length;
	System.out.println("Byte length of image: " + imLen);

	int totalLen = stego.length;
	System.out.println("Total byte length of message: " + totalLen);

	// check that the stego will fit into the image
	// multiply stego length by number of image bytes required to store one stego byte
	if ((totalLen*DATA_SIZE) > imLen) {
            System.out.println("Image not big enough for message");
	    return false;
        }

	hideStego(imBytes, stego, 0);  // hide at start of image
	return true;
    }  // end of singleHide()
    
    private static void hideStego(byte[] imBytes, byte[] stego, int offset)
    // store stego in image starting at byte posn offset
    {
        for (int i = 0; i < stego.length; i++) {       // loop through stego
            int byteVal = stego[i];
	    for(int j=7; j >= 0; j--) {    // loop through the 8 bits of each stego byte
                int bitVal = (byteVal >>> j) & 1;

                // change last bit of image byte to be the stego bit
	        imBytes[offset] = (byte)((imBytes[offset] & 0xFE) | bitVal);
	        offset++;
            }
        }
    }  // end of hideStego()
    
    private static byte[] accessBytes(BufferedImage image)
    // access the data bytes in the image
    {
        WritableRaster raster = image.getRaster();
//	System.out.println(">> raster: "+raster);
	DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
//	System.out.println(">> buffer: "+buffer);
//	System.out.println(">> buffer.getData(): "+buffer.getData());
//	System.out.println(">> buffer.getSize(): "+buffer.getSize());
//	System.out.println("");
	return buffer.getData();
    }  // end of accessBytes() 
    
    private static BufferedImage loadImage(String imFnm)
    // read the image from the imFnm file
    {
        BufferedImage im = null;
	try {
            im = ImageIO.read( new File(imFnm) );
	    System.out.println("Read " + imFnm);
        } 
	catch (IOException e) 
	{ System.out.println("Could not read image from " + imFnm);  }

	return im;
    }   // end of loadImage() */
    
    public void hideStego_modified(byte[] imBytes, String stego, int offset)
    // store stego in image starting at byte posn offset
    {
//        System.out.println("stego.length: "+stego.length);
//        System.out.println("");
        for (int i = 0; i < stego.length(); i++) {       // loop through stego //looping sesuai panjang pesan yg sdh diStego
//            System.out.println("stego.charAt(i): "+stego.charAt(i));
            int byteVal = Character.getNumericValue(stego.charAt(i));
//            System.out.println("byteVal0: "+byteVal);
//            System.out.println("");
//            System.out.println("imBytes["+offset+"]A: "+imBytes[offset]);
//	    for(int j=7; j >= 0; j--) {    // loop through the 8 bits of each stego byte //GA PERLU DILOOP 8x
                int bitVal = (byteVal >>> 0) & 1;//                int bitVal = (byteVal >>> j) & 1;
//                System.out.println("byteVal1: "+byteVal);
                
//                System.out.println("imBytes["+offset+"]B: "+imBytes[offset]);
                // change last bit of image byte to be the stego bit
	        imBytes[offset] = (byte)((imBytes[offset] & 0xFE) | bitVal); 
//                System.out.println("imBytes["+offset+"]C: "+imBytes[offset]); 
//                System.out.println("");
	        offset++;
//            }
        }
    }  // end of hideStego()
}
