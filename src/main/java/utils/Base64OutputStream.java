package utils;

import java.io.*;

/**
   Dieser Stream-Filter konvertiert einen Strom von Bytes
   in deren Base64-Kodierung.

   Base64-Kodierung verschlüsselt 3 Bytes in 4 Zeichen:
   |11111122|22223333|33444444|
   Jede Gruppe von 6 Bit wird gemäß der toBase64-Tabelle
   verschlusselt. Wenn die Zahl der Eingabebytes kein Vielfaches
   von 3 ist, dann wird die letzte Gruppe von 4 Zeichen mit einem
   oder zwei Gleichheitszeichen aufgefüllt. Jede Ausgabezeile
   besteht aus maximal 76 Zeichen.
   
   Adapted from "core Java 2" (author Cay Horstmann)
*/
public class Base64OutputStream extends FilterOutputStream
{
   /**
      Konstruiert den Stream-Filter.
      @param out Der zu filternde Strom
   */
   public Base64OutputStream(OutputStream out)
   {
      super(out);
   }

   public static String encode (String s)
   {
       ByteArrayOutputStream bOut = new ByteArrayOutputStream ();
       Base64OutputStream out = new Base64OutputStream(bOut);
       try
       {
           out.write(s.getBytes());
           out.flush();
       }
       catch (IOException exception)
       {}
       return bOut.toString();
   }
   
   public void write(int c) throws IOException
   {
      inbuf[i] = c;
      i++;
      if (i == 3)
      {
         super.write(toBase64[(inbuf[0] & 0xFC) >> 2]);
         super.write(toBase64[((inbuf[0] & 0x03) << 4) |
            ((inbuf[1] & 0xF0) >> 4)]);
         super.write(toBase64[((inbuf[1] & 0x0F) << 2) |
            ((inbuf[2] & 0xC0) >> 6)]);
         super.write(toBase64[inbuf[2] & 0x3F]);
         col += 4;
         i = 0;
         /*
         if (col >= 76)
         {
            super.write('\n');
            col = 0;
         }
         */
      }
   }

   public void flush() throws IOException
   {
      if (i == 1)
      {
         super.write(toBase64[(inbuf[0] & 0xFC) >> 2]);
         super.write(toBase64[(inbuf[0] & 0x03) << 4]);
         super.write('=');
         super.write('=');
      }
      else if (i == 2)
      {
         super.write(toBase64[(inbuf[0] & 0xFC) >> 2]);
         super.write(toBase64[((inbuf[0] & 0x03) << 4) |
            ((inbuf[1] & 0xF0) >> 4)]);
         super.write(toBase64[(inbuf[1] & 0x0F) << 2]);
         super.write('=');
      }
   }

   private static char[] toBase64 =
   {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
      'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
      'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
      'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
      'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
      'w', 'x', 'y', 'z', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '+', '/'
   };

   private int col = 0;
   private int i = 0;
   private int[] inbuf = new int[3];
}
