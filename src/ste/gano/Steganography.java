/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.gano;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 *
 * @author linuxluv
 */
public class Steganography {
    private static final int MAX_INT_LEN = 4;
    private static final int DATA_SIZE = 8; // number of image bytes required to store one stego byte
    
    public static boolean hide(String textFnm, String imFnm){
        /* hide message read from textFnm inside image read from imFnm;
	     the resulting image is stored in <inFnm>Msg.png */
        // read in the message
	String inputText = readTextFile(textFnm);
//	System.out.println(">> inputText: "+inputText);
	
	if ((inputText == null) || (inputText.length() == 0))
            return false;

        byte[] stego = buildStego(inputText);

	// access the image's data as a byte array
	BufferedImage im = loadImage(imFnm);
//	System.out.println(">> im: "+im);
	
	if (im == null)
            return false;
//        System.out.println("im1: "+im.toString());
        byte imBytes[] = accessBytes(im);
	    
//        for(int i=0; i<imBytes.length; i++){
//            System.out.println(">> imBytes["+i+"] : "+Integer.toBinaryString(imBytes[i]));
//            System.out.println("");
//        }
//        System.out.println("");

	if (!singleHide(imBytes, stego))   // im is modified with the stego
            return false;
        
        // store the modified image in <fnm>Msg.png
	String fnm = getFileName(imFnm);
//        System.out.println("im2: "+im.toString());
        DataBufferByte buffer = (DataBufferByte) im.getData().getDataBuffer();
        byte[] testByte = buffer.getData();
        
        /*for(int i=0; i<testByte.length; i++){
            System.out.println("testByte["+i+"] "+testByte[i]);
        }
        System.out.println("");*/
	return writeImageToFile( fnm + "Msg.png", im);
    }  // end of hide()
    
    private static String readTextFile(String fnm)
    // read in fnm, returning it as a single string
    {
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
	 
	try {
            br = new BufferedReader(new FileReader( new File(fnm) ));      
	    String text = null;
	    while ((text = br.readLine()) != null)
                sb.append(text + "\n");
        } 
	catch (Exception e) {
            System.out.println("Could not completely read " + fnm);
	    return null;
        } 
        finally {
            try {
                if (br != null)
                    br.close();
            } 
	    catch (IOException e) {
                System.out.println("Problem closing " + fnm);
	        return null;
            } 
        }
	System.out.println("Read in " + fnm);
	return sb.toString();
    }  // end of readTextFile()
    
    private static byte[] buildStego(String inputText)
    /* Build a stego (a byte array), made up of 2 fields:
	       <size of binary message> 
	       <binary message>   */
    { 
        // convert data to byte arrays
	byte[] msgBytes = inputText.getBytes();
	/*System.out.println(">> msgBytes: "+msgBytes);
	for(int i=0; i<msgBytes.length; i++){
            System.out.println(">> msgBytes["+i+"]: "+msgBytes[i]);
	    System.out.println(Integer.toBinaryString(msgBytes[i]));
        }
	System.out.println("");*/
	byte[] lenBs = intToBytes(msgBytes.length);
	System.out.println(">> msgBytes.length : "+msgBytes.length);
	for(int i=0; i<lenBs.length; i++){
            System.out.println(">> lnBs["+i+"] : "+lenBs[i]);
        }
	System.out.println("");

        int totalLen = lenBs.length + msgBytes.length;
	System.out.println("lenBs.length: "+lenBs.length +", msgBytes.length: "+ msgBytes.length+", totalLen: "+totalLen);
	System.out.println("");
	byte[] stego = new byte[totalLen];    // for holding the resulting stego
	    
	/*for(int i=0; i<stego.length; i++){
            System.out.println(">> stego["+i+"]: "+stego[i]);
        }
	System.out.println("");*/

	// combine the 2 fields into one byte array
	// public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);
	System.arraycopy(lenBs, 0, stego, 0, lenBs.length);          // length of binary message
	System.arraycopy(msgBytes, 0, stego, lenBs.length, msgBytes.length);   // binary message
	    
	/*for(int i=0; i<stego.length; i++){
            System.out.println(">> stego["+i+"]: "+stego[i]);
        }
	System.out.println("");*/

	// System.out.println("Num. pixels to store fragment " + i + ": " + totalLen*DATA_SIZE);
	return stego;
    }  // end of buildStego()
    
    private static byte[] intToBytes(int i)
    // split integer i into a MAX_INT_LEN-element byte array
    {
        // map the parts of the integer to a byte array
	byte[] integerBs = new byte[MAX_INT_LEN];
	integerBs[0] = (byte) ((i >>> 24) & 0xFF);
	integerBs[1] = (byte) ((i >>> 16) & 0xFF);
	integerBs[2] = (byte) ((i >>> 8) & 0xFF);
	integerBs[3] = (byte) (i & 0xFF);

	// for (int j=0; j < integerBs.length; j++)
	//  System.out.println(" integerBs[ " + j + "]: " + integerBs[j]);

	return integerBs;
    }  // end of intToBytes()
    
    public static BufferedImage loadImage(String imFnm)
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
    }   // end of loadImage()
    
    public BufferedImage loadImage_NotStatic(String imFnm)
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
    }   // end of loadImage()
    
    public static byte[] accessBytes(BufferedImage image)
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
    
    private static boolean singleHide(byte[] imBytes, byte[] stego)
    // store stego in image bytes
    {
        int imLen = imBytes.length;
	System.out.println("Byte length of image: " + imLen);

	int totalLen = stego.length;
	System.out.println("Total byte length of message: " + totalLen);
        
        System.out.println("");

	// check that the stego will fit into the image
	// multiply stego length by number of image bytes required to store one stego byte
	if ((totalLen*DATA_SIZE) > imLen) {
            System.out.println("Image not big enough for message");
	    return false;
        }
        
//        for(int i=0; i<imBytes.length; i++){
//            System.out.println(""+imBytes[i]);
//            System.out.println("");
//        }

	hideStego(imBytes, stego, 0);  // hide at start of image
	return true;
    }  // end of singleHide()
    
    private static void hideStego(byte[] imBytes, byte[] stego, int offset)
    // store stego in image starting at byte posn offset
    {
//        System.out.println("stego.length: "+stego.length);
//        System.out.println("");
        for (int i = 0; i < stego.length; i++) {       // loop through stego //looping sesuai panjang pesan yg sdh diStego
            int byteVal = stego[i];
            //System.out.println("byteVal0: "+byteVal);
//            System.out.println("");
            //System.out.println("imBytes["+offset+"]A: "+imBytes[offset]);
	    for(int j=7; j >= 0; j--) {    // loop through the 8 bits of each stego byte
                int bitVal = (byteVal >>> j) & 1;
                //System.out.println("byteVal1: "+byteVal);
                
                //System.out.println("imBytes["+offset+"]B: "+imBytes[offset]);
                // change last bit of image byte to be the stego bit
	        imBytes[offset] = (byte)((imBytes[offset] & 0xFE) | bitVal); //HERE HERE
                //System.out.println("imBytes["+offset+"]C: "+imBytes[offset]); //imBytes[offset] yang ditampilkan di sini adalah bentuk desimal, belum biner
                //System.out.println("");
	        offset++;
            }
        }
    }  // end of hideStego()
    
    public static String getFileName(String fnm)
    // extract the name from the filename without its suffix
    {
        int extPosn = fnm.lastIndexOf('.');
	if (extPosn == -1) {
            System.out.println("No extension found for " + fnm);
	    return fnm;   // use the original file name
        }

	return fnm.substring(0, extPosn);
    }  // end of getFileName()
    
    public static boolean writeImageToFile(String outFnm, BufferedImage im)
    // save the im image in a PNG file called outFnm
    {
        if (!canOverWrite(outFnm))
            return false;

	try {
            ImageIO.write(im, "png", new File(outFnm));
	    System.out.println("Image written to PNG file: " + outFnm);
	    return true;
        } 
	catch(IOException e)
	{ System.out.println("Could not write image to " + outFnm); 
            return false;
	}
    } // end of writeImageToFile();
    
    private static boolean canOverWrite(String fnm)
    /* If fnm already exists, get a response from the
	     user about whether it should be overwritten or not. */
    {
        File f = new File(fnm);
	if (!f.exists())
            return true;     // can overewrite since the file is new

        // prompt the user about whether the file can be overwritten 
	Scanner in = new Scanner(System.in);
	String response;
	System.out.print("File " + fnm + " already exists. ");
	while (true) {
            System.out.print("Overwrite (y|n)? ");
	    response = in.nextLine().trim().toLowerCase();
	    if (response.startsWith("n"))  // no
                return false;
	    else if (response.startsWith("y"))  // yes
	        return true;
        }
    }  // end of canOverWrite()
    
    // --------------------------- reveal a message -----------------------------------
	
    public static boolean reveal(String imFnm)
    /* Retrieve the hidden message from imFnm from the beginning
	     of the image after first extractibg its length information.  
	     The extracted message is stored in <imFnm>.txt
	  */
    {
        // get the image's data as a byte array
	BufferedImage im = loadImage(imFnm);
	if (im == null)
            return false;
	byte[] imBytes = accessBytes(im);
	System.out.println("Byte length of image: " + imBytes.length);

	// get msg length at the start of the image
	int msgLen = getMsgLength(imBytes, 0);   
	if (msgLen == -1)
            return false;
        System.out.println("Byte length of message: " + msgLen);

	// get message located after the length info in the image
	String msg = getMessage(imBytes, msgLen, MAX_INT_LEN*DATA_SIZE);          
	if (msg != null) {
            String fnm = getFileName(imFnm);
            return writeStringToFile(fnm + ".txt", msg);  // save message in a text file
        }
	else {
            System.out.println("No message found");
            return false;
        }
    }  // end of reveal()
	
    private static int getMsgLength(byte[] imBytes, int offset)
    // retrieve binary message length from the image
    {
        byte[] lenBytes = extractHiddenBytes(imBytes, MAX_INT_LEN, offset);
	// get the binary message length as a byte array
	if (lenBytes == null)
            return -1;

	 for (int j=0; j < lenBytes.length; j++)
	  System.out.println(" lenBytes[ " + j + "]: " + lenBytes[j]);

	// convert the byte array into an integer
	int msgLen = ((lenBytes[0] & 0xff) << 24) | 
            ((lenBytes[1] & 0xff) << 16) | 
	    ((lenBytes[2] & 0xff) << 8) | 
	    (lenBytes[3] & 0xff);
	 System.out.println("Message length: " + msgLen);

	if ((msgLen <= 0) || (msgLen > imBytes.length))  {
            System.out.println("Incorrect message length");
	    return -1;
        }
	// else
	//  System.out.println("Revealed message length: " + msgLen);

	return msgLen;
    }  // end of getMsgLength()
	
    private static String getMessage(byte[] imBytes, int msgLen, int offset)
    /* Extract a binary message of size msgLen from the image, and 
    convert it to a string 
    */
    {
        byte[] msgBytes = extractHiddenBytes(imBytes, msgLen, offset); 
	// the message is msgLen bytes long
	if (msgBytes == null)
            return null;

        String msg = new String(msgBytes);

        // check the message is all characters
	if (isPrintable(msg)) {
             System.out.println("Found message: \"" + msg + "\"");
	    return msg;
        }
	else
            return null;
    }  // end of getMessage()
	
    private static byte[] extractHiddenBytes(byte[] imBytes, int size, int offset)
    // extract 'size' hidden data bytes, starting from 'offset' in the image bytes
    {
        int finalPosn = offset + (size*DATA_SIZE);
	if (finalPosn > imBytes.length) {
            System.out.println("End of image reached");
	    return null;
        }

	byte[] hiddenBytes = new byte[size];
        
        //test
//        for(int i=0; i<hiddenBytes.length; i++){
//            System.out.println("> hiddenBytes["+i+"] "+hiddenBytes[i]);
//        }

	for (int j = 0; j < size; j++) {    // loop through each hidden byte
            for (int i=0; i < DATA_SIZE; i++) {   // make one hidden byte from DATA_SIZE image bytes
                //test
                int b1 = hiddenBytes[j] << 1;
                int b2 = imBytes[offset] & 1;
                //System.out.println("hiddenBytes["+j+"] : "+hiddenBytes[j]+" , imBytes["+offset+"] : "+imBytes[offset]+" , b1: "+b1+" , b2: "+b2);
                
                hiddenBytes[j] = (byte) ((hiddenBytes[j] << 1) | (imBytes[offset] & 1));   // | : operator OR
	                             // shift existing 1 left; store right most bit of image byte // << Binary Left Shift Operator
                
                /*System.out.println("-> hiddenBytes["+j+"] : "+hiddenBytes[j]);
                System.out.println("");*/
	        offset++;
            }
        }
        System.out.println("");
	return hiddenBytes;
    }  // end of extractHiddenBytes()
	
    private static boolean isPrintable(String str)
    // is the string printable?
    {
        for (int i=0; i < str.length(); i++)
            if (!isPrintable(str.charAt(i))) {
                System.out.println("Unprintable character found");
	        return false;
            }
        return true;
    }  // end of isPrintable()
	
    private static boolean isPrintable(int ch)
    // is ch a 7-bit ASCII character that could (sensibly) be printed?
    {
        if (Character.isWhitespace(ch) && (ch < 127))  // whitespace, 7-bit
            return true;
        else if ((ch > 32) && (ch < 127))
            return true;

        return false;
    }  // end of isPrintable()
	
    private static boolean writeStringToFile(String outFnm, String msgStr)
    // write the message string into the outFnm text file
    {
        if (!canOverWrite(outFnm))
            return false;

        try {
            FileWriter out = new FileWriter( new File(outFnm) );
	    out.write(msgStr);
	    out.close();
	    System.out.println("Message written to " + outFnm);
	    return true;
        }
	catch(IOException e)
	{  System.out.println("Could not write message to " + outFnm);  
            return false;
	}
    }  // end of writeStringToFile()
}